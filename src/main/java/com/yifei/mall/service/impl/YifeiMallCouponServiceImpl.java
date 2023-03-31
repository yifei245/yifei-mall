package com.yifei.mall.service.impl;

import com.yifei.mall.controller.vo.YifeiMallCouponVO;
import com.yifei.mall.controller.vo.YifeiMallMyCouponVO;
import com.yifei.mall.controller.vo.YifeiMallShoppingCartItemVO;
import com.yifei.mall.common.YifeiMallException;
import com.yifei.mall.dao.YifeiMallCouponMapper;
import com.yifei.mall.dao.YifeiMallGoodsMapper;
import com.yifei.mall.dao.YifeiMallUserCouponRecordMapper;
import com.yifei.mall.entity.YifeiMallCoupon;
import com.yifei.mall.entity.YifeiMallGoods;
import com.yifei.mall.entity.YifeiMallUserCouponRecord;
import com.yifei.mall.service.YifeiMallCouponService;
import com.yifei.mall.util.BeanUtil;
import com.yifei.mall.util.PageQueryUtil;
import com.yifei.mall.util.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class YifeiMallCouponServiceImpl implements YifeiMallCouponService {

    @Autowired
    private YifeiMallCouponMapper yifeiMallCouponMapper;

    @Autowired
    private YifeiMallUserCouponRecordMapper yifeiMallUserCouponRecordMapper;

    @Autowired
    private YifeiMallGoodsMapper yifeiMallGoodsMapper;

    @Override
    public PageResult getCouponPage(PageQueryUtil pageUtil) {
        List<YifeiMallCoupon> carousels = yifeiMallCouponMapper.findCouponlList(pageUtil);
        int total = yifeiMallCouponMapper.getTotalCoupons(pageUtil);
        return new PageResult(carousels, total, pageUtil.getLimit(), pageUtil.getPage());
    }

    @Override
    public boolean saveCoupon(YifeiMallCoupon yifeiMallCoupon) {
        return yifeiMallCouponMapper.insertSelective(yifeiMallCoupon) > 0;
    }

    @Override
    public boolean updateCoupon(YifeiMallCoupon yifeiMallCoupon) {
        return yifeiMallCouponMapper.updateByPrimaryKeySelective(yifeiMallCoupon) > 0;
    }

    @Override
    public YifeiMallCoupon getCouponById(Long id) {
        return yifeiMallCouponMapper.selectByPrimaryKey(id);
    }

    @Override
    public boolean deleteCouponById(Long id) {
        return yifeiMallCouponMapper.deleteByPrimaryKey(id) > 0;
    }

    @Override
    public List<YifeiMallCouponVO> selectAvailableCoupon(Long userId) {
        List<YifeiMallCoupon> coupons = yifeiMallCouponMapper.selectAvailableCoupon();
        List<YifeiMallCouponVO> couponVOS = BeanUtil.copyList(coupons, YifeiMallCouponVO.class);
        for (YifeiMallCouponVO couponVO : couponVOS) {
            if (userId != null) {
                int num = yifeiMallUserCouponRecordMapper.getUserCouponCount(userId, couponVO.getCouponId());
                if (num > 0) {
                    couponVO.setHasReceived(true);
                }
            }
            if (couponVO.getCouponTotal() != 0) {
                int count = yifeiMallUserCouponRecordMapper.getCouponCount(couponVO.getCouponId());
                if (count >= couponVO.getCouponTotal()) {
                    couponVO.setSaleOut(true);
                }
            }
        }
        return couponVOS;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean saveCouponUser(Long couponId, Long userId) {
        YifeiMallCoupon yifeiMallCoupon = yifeiMallCouponMapper.selectByPrimaryKey(couponId);
        if (yifeiMallCoupon.getCouponLimit() != 0) {
            int num = yifeiMallUserCouponRecordMapper.getUserCouponCount(userId, couponId);
            if (num != 0) {
                throw new YifeiMallException("优惠券已经领过了,无法再次领取！");
            }
        }
        if (yifeiMallCoupon.getCouponTotal() != 0) {
            int count = yifeiMallUserCouponRecordMapper.getCouponCount(couponId);
            if (count >= yifeiMallCoupon.getCouponTotal()) {
                throw new YifeiMallException("优惠券已经领完了！");
            }
            if (yifeiMallCouponMapper.reduceCouponTotal(couponId) <= 0) {
                throw new YifeiMallException("优惠券领取失败！");
            }
        }
        YifeiMallUserCouponRecord couponUser = new YifeiMallUserCouponRecord();
        couponUser.setUserId(userId);
        couponUser.setCouponId(couponId);
        return yifeiMallUserCouponRecordMapper.insertSelective(couponUser) > 0;
    }

    @Override
    public List<YifeiMallCouponVO> selectMyCoupons(Long userId) {
        List<YifeiMallUserCouponRecord> coupons = yifeiMallUserCouponRecordMapper.selectMyCoupons(userId);
        List<YifeiMallCouponVO> couponVOS = new ArrayList<>();
        for (YifeiMallUserCouponRecord couponUser : coupons) {
            YifeiMallCoupon yifeiMallCoupon = yifeiMallCouponMapper.selectByPrimaryKey(couponUser.getCouponId());
            if (yifeiMallCoupon == null) {
                continue;
            }
            YifeiMallCouponVO yifeiMallCouponVO = new YifeiMallCouponVO();
            BeanUtil.copyProperties(yifeiMallCoupon, yifeiMallCouponVO);
            yifeiMallCouponVO.setCouponUserId(couponUser.getCouponUserId());
            yifeiMallCouponVO.setUsed(couponUser.getUsedTime() != null);
            couponVOS.add(yifeiMallCouponVO);
        }
        return couponVOS;
    }

    @Override
    public List<YifeiMallMyCouponVO> selectOrderCanUseCoupons(List<YifeiMallShoppingCartItemVO> myShoppingCartItems, int priceTotal, Long userId) {
        List<YifeiMallUserCouponRecord> couponUsers = yifeiMallUserCouponRecordMapper.selectMyAvailableCoupons(userId);
        List<YifeiMallMyCouponVO> myCouponVOS = BeanUtil.copyList(couponUsers, YifeiMallMyCouponVO.class);
        List<Long> couponIds = couponUsers.stream().map(YifeiMallUserCouponRecord::getCouponId).collect(Collectors.toList());
        if (!couponIds.isEmpty()) {
            ZoneId zone = ZoneId.systemDefault();
            List<YifeiMallCoupon> coupons = yifeiMallCouponMapper.selectByIds(couponIds);
            for (YifeiMallCoupon coupon : coupons) {
                for (YifeiMallMyCouponVO myCouponVO : myCouponVOS) {
                    if (coupon.getCouponId().equals(myCouponVO.getCouponId())) {
                        myCouponVO.setName(coupon.getCouponName());
                        myCouponVO.setCouponDesc(coupon.getCouponDesc());
                        myCouponVO.setDiscount(coupon.getDiscount());
                        myCouponVO.setMin(coupon.getMin());
                        myCouponVO.setGoodsType(coupon.getGoodsType());
                        myCouponVO.setGoodsValue(coupon.getGoodsValue());
                        ZonedDateTime startZonedDateTime = coupon.getCouponStartTime().atStartOfDay(zone);
                        ZonedDateTime endZonedDateTime = coupon.getCouponEndTime().atStartOfDay(zone);
                        myCouponVO.setStartTime(Date.from(startZonedDateTime.toInstant()));
                        myCouponVO.setEndTime(Date.from(endZonedDateTime.toInstant()));
                    }
                }
            }
        }
        long nowTime = System.currentTimeMillis();
        return myCouponVOS.stream().filter(item -> {
            // 判断有效期
            Date startTime = item.getStartTime();
            Date endTime = item.getEndTime();
            if (startTime == null || endTime == null || nowTime < startTime.getTime() || nowTime > endTime.getTime()) {
                return false;
            }
            // 判断使用条件
            boolean b = false;
            if (item.getMin() <= priceTotal) {
                if (item.getGoodsType() == 1) { // 指定分类可用
                    String[] split = item.getGoodsValue().split(",");
                    List<Long> goodsValue = Arrays.stream(split).map(Long::valueOf).collect(Collectors.toList());
                    List<Long> goodsIds = myShoppingCartItems.stream().map(YifeiMallShoppingCartItemVO::getGoodsId).collect(Collectors.toList());
                    List<YifeiMallGoods> goods = yifeiMallGoodsMapper.selectByPrimaryKeys(goodsIds);
                    List<Long> categoryIds = goods.stream().map(YifeiMallGoods::getGoodsCategoryId).collect(Collectors.toList());
                    for (Long categoryId : categoryIds) {
                        if (goodsValue.contains(categoryId)) {
                            b = true;
                            break;
                        }
                    }
                } else if (item.getGoodsType() == 2) { // 指定商品可用
                    String[] split = item.getGoodsValue().split(",");
                    List<Long> goodsValue = Arrays.stream(split).map(Long::valueOf).collect(Collectors.toList());
                    List<Long> goodsIds = myShoppingCartItems.stream().map(YifeiMallShoppingCartItemVO::getGoodsId).collect(Collectors.toList());
                    for (Long goodsId : goodsIds) {
                        if (goodsValue.contains(goodsId)) {
                            b = true;
                            break;
                        }
                    }
                } else { // 全场通用
                    b = true;
                }
            }
            return b;
        }).sorted(Comparator.comparingInt(YifeiMallMyCouponVO::getDiscount)).collect(Collectors.toList());
    }

    @Override
    public boolean deleteCouponUser(Long couponUserId) {
        return yifeiMallUserCouponRecordMapper.deleteByPrimaryKey(couponUserId) > 0;
    }

    @Override
    public void releaseCoupon(Long orderId) {
        YifeiMallUserCouponRecord yifeiMallUserCouponRecord = yifeiMallUserCouponRecordMapper.getUserCouponByOrderId(orderId);
        if (yifeiMallUserCouponRecord == null) {
            return;
        }
        yifeiMallUserCouponRecord.setUseStatus((byte) 0);
        yifeiMallUserCouponRecord.setUpdateTime(new Date());
        yifeiMallUserCouponRecordMapper.updateByPrimaryKey(yifeiMallUserCouponRecord);
    }
}
