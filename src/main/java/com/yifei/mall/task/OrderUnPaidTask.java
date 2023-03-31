package com.yifei.mall.task;

import com.yifei.mall.common.Constants;
import com.yifei.mall.common.YifeiMallOrderStatusEnum;
import com.yifei.mall.dao.YifeiMallSeckillMapper;
import com.yifei.mall.entity.YifeiMallOrder;
import com.yifei.mall.entity.YifeiMallOrderItem;
import com.yifei.mall.redis.RedisCache;
import com.yifei.mall.service.YifeiMallCouponService;
import com.yifei.mall.util.SpringContextUtil;
import com.yifei.mall.dao.YifeiMallGoodsMapper;
import com.yifei.mall.dao.YifeiMallOrderItemMapper;
import com.yifei.mall.dao.YifeiMallOrderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

/**
 * 未支付订单超时自动取消任务
 */
public class OrderUnPaidTask extends Task {
    /**
     * 默认延迟时间30分钟，单位毫秒
     */
    private static final long DELAY_TIME = 30 * 60 * 1000;

    private final Logger log = LoggerFactory.getLogger(OrderUnPaidTask.class);
    /**
     * 订单id
     */
    private final Long orderId;

    public OrderUnPaidTask(Long orderId, long delayInMilliseconds) {
        super("OrderUnPaidTask-" + orderId, delayInMilliseconds);
        this.orderId = orderId;
    }

    public OrderUnPaidTask(Long orderId) {
        super("OrderUnPaidTask-" + orderId, DELAY_TIME);
        this.orderId = orderId;
    }

    @Override
    public void run() {
        log.info("系统开始处理延时任务---订单超时未付款--- {}", this.orderId);

        YifeiMallOrderMapper yifeiMallOrderMapper = SpringContextUtil.getBean(YifeiMallOrderMapper.class);
        YifeiMallOrderItemMapper yifeiMallOrderItemMapper = SpringContextUtil.getBean(YifeiMallOrderItemMapper.class);
        YifeiMallGoodsMapper yifeiMallGoodsMapper = SpringContextUtil.getBean(YifeiMallGoodsMapper.class);
        YifeiMallCouponService yifeiMallCouponService = SpringContextUtil.getBean(YifeiMallCouponService.class);

        YifeiMallOrder order = yifeiMallOrderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            log.info("系统结束处理延时任务---订单超时未付款--- {}", this.orderId);
            return;
        }
        if (order.getOrderStatus() != YifeiMallOrderStatusEnum.ORDER_PRE_PAY.getOrderStatus()) {
            log.info("系统结束处理延时任务---订单超时未付款--- {}", this.orderId);
            return;
        }

        // 设置订单为已取消状态
        order.setOrderStatus((byte) YifeiMallOrderStatusEnum.ORDER_CLOSED_BY_EXPIRED.getOrderStatus());
        order.setUpdateTime(new Date());
        if (yifeiMallOrderMapper.updateByPrimaryKey(order) <= 0) {
            throw new RuntimeException("更新数据已失效");
        }

        // 商品货品数量增加
        List<YifeiMallOrderItem> yifeiMallOrderItems = yifeiMallOrderItemMapper.selectByOrderId(orderId);
        for (YifeiMallOrderItem orderItem : yifeiMallOrderItems) {
            if (orderItem.getSeckillId() != null) {
                Long seckillId = orderItem.getSeckillId();
                YifeiMallSeckillMapper yifeiMallSeckillMapper = SpringContextUtil.getBean(YifeiMallSeckillMapper.class);
                RedisCache redisCache = SpringContextUtil.getBean(RedisCache.class);
                if (!yifeiMallSeckillMapper.addStock(seckillId)) {
                    throw new RuntimeException("秒杀商品货品库存增加失败");
                }
                redisCache.increment(Constants.SECKILL_GOODS_STOCK_KEY + seckillId);
            } else {
                Long goodsId = orderItem.getGoodsId();
                Integer goodsCount = orderItem.getGoodsCount();
                if (!yifeiMallGoodsMapper.addStock(goodsId, goodsCount)) {
                    throw new RuntimeException("商品货品库存增加失败");
                }
            }
        }

        // 返还优惠券
        yifeiMallCouponService.releaseCoupon(orderId);
        log.info("系统结束处理延时任务---订单超时未付款--- {}", this.orderId);
    }
}
