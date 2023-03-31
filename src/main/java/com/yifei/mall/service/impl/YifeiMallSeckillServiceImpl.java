package com.yifei.mall.service.impl;

import com.google.common.util.concurrent.RateLimiter;
import com.yifei.mall.controller.vo.ExposerVO;
import com.yifei.mall.controller.vo.YifeiMallSeckillGoodsVO;
import com.yifei.mall.controller.vo.SeckillSuccessVO;
import com.yifei.mall.common.Constants;
import com.yifei.mall.common.YifeiMallException;
import com.yifei.mall.common.SeckillStatusEnum;
import com.yifei.mall.common.ServiceResultEnum;
import com.yifei.mall.dao.YifeiMallGoodsMapper;
import com.yifei.mall.dao.YifeiMallSeckillMapper;
import com.yifei.mall.dao.YifeiMallSeckillSuccessMapper;
import com.yifei.mall.entity.YifeiMallSeckill;
import com.yifei.mall.entity.YifeiMallSeckillSuccess;
import com.yifei.mall.redis.RedisCache;
import com.yifei.mall.service.YifeiMallSeckillService;
import com.yifei.mall.util.MD5Util;
import com.yifei.mall.util.PageQueryUtil;
import com.yifei.mall.util.PageResult;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class YifeiMallSeckillServiceImpl implements YifeiMallSeckillService {

    // 使用令牌桶RateLimiter 限流
    private static final RateLimiter rateLimiter = RateLimiter.create(100);

    @Autowired
    private YifeiMallSeckillMapper yifeiMallSeckillMapper;

    @Autowired
    private YifeiMallSeckillSuccessMapper yifeiMallSeckillSuccessMapper;

    @Autowired
    private YifeiMallGoodsMapper yifeiMallGoodsMapper;

    @Autowired
    private RedisCache redisCache;

    @Override
    public PageResult getSeckillPage(PageQueryUtil pageUtil) {
        List<YifeiMallSeckill> carousels = yifeiMallSeckillMapper.findSeckillList(pageUtil);
        int total = yifeiMallSeckillMapper.getTotalSeckills(pageUtil);
        return new PageResult(carousels, total, pageUtil.getLimit(), pageUtil.getPage());
    }

    @Override
    public boolean saveSeckill(YifeiMallSeckill yifeiMallSeckill) {
        if (yifeiMallGoodsMapper.selectByPrimaryKey(yifeiMallSeckill.getGoodsId()) == null) {
            YifeiMallException.fail(ServiceResultEnum.GOODS_NOT_EXIST.getResult());
        }
        return yifeiMallSeckillMapper.insertSelective(yifeiMallSeckill) > 0;
    }

    @Override
    public boolean updateSeckill(YifeiMallSeckill yifeiMallSeckill) {
        if (yifeiMallGoodsMapper.selectByPrimaryKey(yifeiMallSeckill.getGoodsId()) == null) {
            YifeiMallException.fail(ServiceResultEnum.GOODS_NOT_EXIST.getResult());
        }
        YifeiMallSeckill temp = yifeiMallSeckillMapper.selectByPrimaryKey(yifeiMallSeckill.getSeckillId());
        if (temp == null) {
            YifeiMallException.fail(ServiceResultEnum.DATA_NOT_EXIST.getResult());
        }
        yifeiMallSeckill.setUpdateTime(new Date());
        return yifeiMallSeckillMapper.updateByPrimaryKeySelective(yifeiMallSeckill) > 0;
    }

    @Override
    public YifeiMallSeckill getSeckillById(Long id) {
        return yifeiMallSeckillMapper.selectByPrimaryKey(id);
    }

    @Override
    public boolean deleteSeckillById(Long id) {
        return yifeiMallSeckillMapper.deleteByPrimaryKey(id) > 0;
    }

    @Override
    public List<YifeiMallSeckill> getHomeSeckillPage() {
        return yifeiMallSeckillMapper.findHomeSeckillList();
    }

    @Override
    public ExposerVO exposerUrl(Long seckillId) {
        YifeiMallSeckillGoodsVO yifeiMallSeckillGoodsVO = redisCache.getCacheObject(Constants.SECKILL_GOODS_DETAIL + seckillId);
        Date startTime = yifeiMallSeckillGoodsVO.getSeckillBegin();
        Date endTime = yifeiMallSeckillGoodsVO.getSeckillEnd();
        // 系统当前时间
        Date nowTime = new Date();
        if (nowTime.getTime() < startTime.getTime() || nowTime.getTime() > endTime.getTime()) {
            return new ExposerVO(SeckillStatusEnum.NOT_START, seckillId, nowTime.getTime(), startTime.getTime(), endTime.getTime());
        }
        // 检查虚拟库存
        Integer stock = redisCache.getCacheObject(Constants.SECKILL_GOODS_STOCK_KEY + seckillId);
        if (stock == null || stock < 0) {
            return new ExposerVO(SeckillStatusEnum.STARTED_SHORTAGE_STOCK, seckillId);
        }
        // 加密
        String md5 = MD5Util.MD5Encode(seckillId.toString(), Constants.UTF_ENCODING);
        return new ExposerVO(SeckillStatusEnum.START, md5, seckillId);
    }

    @Override
    public SeckillSuccessVO executeSeckill(Long seckillId, Long userId) {
        // 判断能否在500毫秒内得到令牌，如果不能则立即返回false，不会阻塞程序
        if (!rateLimiter.tryAcquire(500, TimeUnit.MILLISECONDS)) {
            throw new YifeiMallException("秒杀失败");
        }
        // 判断用户是否购买过秒杀商品
        if (redisCache.containsCacheSet(Constants.SECKILL_SUCCESS_USER_ID + seckillId, userId)) {
            throw new YifeiMallException("您已经购买过秒杀商品，请勿重复购买");
        }
        // 更新秒杀商品虚拟库存
        Long stock = redisCache.luaDecrement(Constants.SECKILL_GOODS_STOCK_KEY + seckillId);
        if (stock < 0) {
            throw new YifeiMallException("秒杀商品已售空");
        }
        YifeiMallSeckill yifeiMallSeckill = redisCache.getCacheObject(Constants.SECKILL_KEY + seckillId);
        if (yifeiMallSeckill == null) {
            yifeiMallSeckill = yifeiMallSeckillMapper.selectByPrimaryKey(seckillId);
            redisCache.setCacheObject(Constants.SECKILL_KEY + seckillId, yifeiMallSeckill, 24, TimeUnit.HOURS);
        }
        // 判断秒杀商品是否再有效期内
        long beginTime = yifeiMallSeckill.getSeckillBegin().getTime();
        long endTime = yifeiMallSeckill.getSeckillEnd().getTime();
        Date now = new Date();
        long nowTime = now.getTime();
        if (nowTime < beginTime) {
            throw new YifeiMallException("秒杀未开启");
        } else if (nowTime > endTime) {
            throw new YifeiMallException("秒杀已结束");
        }

        Date killTime = new Date();
        Map<String, Object> map = new HashMap<>(8);
        map.put("seckillId", seckillId);
        map.put("userId", userId);
        map.put("killTime", killTime);
        map.put("result", null);
        // 执行存储过程，result被赋值
        try {
            yifeiMallSeckillMapper.killByProcedure(map);
        } catch (Exception e) {
            throw new YifeiMallException(e.getMessage());
        }
        // 获取result -2sql执行失败 -1未插入数据 0未更新数据 1sql执行成功
        map.get("result");
        int result = MapUtils.getInteger(map, "result", -2);
        if (result != 1) {
            throw new YifeiMallException("很遗憾！未抢购到秒杀商品");
        }
        // 记录购买过的用户
        redisCache.setCacheSet(Constants.SECKILL_SUCCESS_USER_ID + seckillId, userId);
        long endExpireTime = endTime / 1000;
        long nowExpireTime = nowTime / 1000;
        redisCache.expire(Constants.SECKILL_SUCCESS_USER_ID + seckillId, endExpireTime - nowExpireTime, TimeUnit.SECONDS);
        YifeiMallSeckillSuccess seckillSuccess = yifeiMallSeckillSuccessMapper.getSeckillSuccessByUserIdAndSeckillId(userId, seckillId);
        SeckillSuccessVO seckillSuccessVO = new SeckillSuccessVO();
        Long seckillSuccessId = seckillSuccess.getSecId();
        seckillSuccessVO.setSeckillSuccessId(seckillSuccessId);
        seckillSuccessVO.setMd5(MD5Util.MD5Encode(seckillSuccessId + Constants.SECKILL_ORDER_SALT, Constants.UTF_ENCODING));
        return seckillSuccessVO;
    }

}
