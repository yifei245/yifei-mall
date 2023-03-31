
package com.yifei.mall.service.impl;

import com.yifei.mall.common.*;
import com.yifei.mall.config.ProjectConfig;
import com.yifei.mall.controller.vo.*;
import com.yifei.mall.dao.*;
import com.yifei.mall.entity.*;
import com.yifei.mall.service.YifeiMallOrderService;
import com.yifei.mall.task.OrderUnPaidTask;
import com.yifei.mall.task.TaskService;
import com.yifei.mall.util.BeanUtil;
import com.yifei.mall.util.NumberUtil;
import com.yifei.mall.util.PageQueryUtil;
import com.yifei.mall.util.PageResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
public class YifeiMallOrderServiceImpl implements YifeiMallOrderService {

    @Autowired
    private YifeiMallOrderMapper yifeiMallOrderMapper;
    @Autowired
    private YifeiMallOrderItemMapper yifeiMallOrderItemMapper;
    @Autowired
    private YifeiMallShoppingCartItemMapper yifeiMallShoppingCartItemMapper;
    @Autowired
    private YifeiMallGoodsMapper yifeiMallGoodsMapper;
    @Autowired
    private YifeiMallUserCouponRecordMapper yifeiMallUserCouponRecordMapper;
    @Autowired
    private YifeiMallCouponMapper yifeiMallCouponMapper;
    @Autowired
    private YifeiMallSeckillMapper yifeiMallSeckillMapper;
    @Autowired
    private YifeiMallSeckillSuccessMapper yifeiMallSeckillSuccessMapper;
    @Autowired
    private TaskService taskService;

    @Override
    public PageResult getYifeiMallOrdersPage(PageQueryUtil pageUtil) {
        List<YifeiMallOrder> yifeiMallOrders = yifeiMallOrderMapper.findYifeiMallOrderList(pageUtil);
        int total = yifeiMallOrderMapper.getTotalYifeiMallOrders(pageUtil);
        return new PageResult(yifeiMallOrders, total, pageUtil.getLimit(), pageUtil.getPage());
    }

    @Override
    @Transactional
    public String updateOrderInfo(YifeiMallOrder yifeiMallOrder) {
        YifeiMallOrder temp = yifeiMallOrderMapper.selectByPrimaryKey(yifeiMallOrder.getOrderId());
        // 不为空且orderStatus>=0且状态为出库之前可以修改部分信息
        if (temp != null && temp.getOrderStatus() >= 0 && temp.getOrderStatus() < 3) {
            temp.setTotalPrice(yifeiMallOrder.getTotalPrice());
            temp.setUserAddress(yifeiMallOrder.getUserAddress());
            temp.setUpdateTime(new Date());
            if (yifeiMallOrderMapper.updateByPrimaryKeySelective(temp) > 0) {
                return ServiceResultEnum.SUCCESS.getResult();
            }
            return ServiceResultEnum.DB_ERROR.getResult();
        }
        return ServiceResultEnum.DATA_NOT_EXIST.getResult();
    }

    @Override
    public boolean updateByPrimaryKeySelective(YifeiMallOrder yifeiMallOrder) {
        return yifeiMallOrderMapper.updateByPrimaryKeySelective(yifeiMallOrder) > 0;
    }

    @Override
    @Transactional
    public String checkDone(Long[] ids) {
        // 查询所有的订单 判断状态 修改状态和更新时间
        List<YifeiMallOrder> orders = yifeiMallOrderMapper.selectByPrimaryKeys(Arrays.asList(ids));
        StringBuilder errorOrderNos = new StringBuilder();
        if (!CollectionUtils.isEmpty(orders)) {
            for (YifeiMallOrder yifeiMallOrder : orders) {
                if (yifeiMallOrder.getIsDeleted() == 1) {
                    errorOrderNos.append(yifeiMallOrder.getOrderNo()).append(" ");
                    continue;
                }
                if (yifeiMallOrder.getOrderStatus() != 1) {
                    errorOrderNos.append(yifeiMallOrder.getOrderNo()).append(" ");
                }
            }
            if (StringUtils.isEmpty(errorOrderNos.toString())) {
                // 订单状态正常 可以执行配货完成操作 修改订单状态和更新时间
                if (yifeiMallOrderMapper.checkDone(Arrays.asList(ids)) > 0) {
                    return ServiceResultEnum.SUCCESS.getResult();
                } else {
                    return ServiceResultEnum.DB_ERROR.getResult();
                }
            } else {
                // 订单此时不可执行出库操作
                if (errorOrderNos.length() > 0 && errorOrderNos.length() < 100) {
                    return errorOrderNos + "订单的状态不是支付成功无法执行出库操作";
                } else {
                    return "你选择了太多状态不是支付成功的订单，无法执行配货完成操作";
                }
            }
        }
        // 未查询到数据 返回错误提示
        return ServiceResultEnum.DATA_NOT_EXIST.getResult();
    }

    @Override
    @Transactional
    public String checkOut(Long[] ids) {
        // 查询所有的订单 判断状态 修改状态和更新时间
        List<YifeiMallOrder> orders = yifeiMallOrderMapper.selectByPrimaryKeys(Arrays.asList(ids));
        StringBuilder errorOrderNos = new StringBuilder();
        if (!CollectionUtils.isEmpty(orders)) {
            for (YifeiMallOrder yifeiMallOrder : orders) {
                if (yifeiMallOrder.getIsDeleted() == 1) {
                    errorOrderNos.append(yifeiMallOrder.getOrderNo()).append(" ");
                    continue;
                }
                if (yifeiMallOrder.getOrderStatus() != 1 && yifeiMallOrder.getOrderStatus() != 2) {
                    errorOrderNos.append(yifeiMallOrder.getOrderNo()).append(" ");
                }
            }
            if (StringUtils.isEmpty(errorOrderNos.toString())) {
                // 订单状态正常 可以执行出库操作 修改订单状态和更新时间
                if (yifeiMallOrderMapper.checkOut(Arrays.asList(ids)) > 0) {
                    return ServiceResultEnum.SUCCESS.getResult();
                } else {
                    return ServiceResultEnum.DB_ERROR.getResult();
                }
            } else {
                // 订单此时不可执行出库操作
                if (errorOrderNos.length() > 0 && errorOrderNos.length() < 100) {
                    return errorOrderNos + "订单的状态不是支付成功或配货完成无法执行出库操作";
                } else {
                    return "你选择了太多状态不是支付成功或配货完成的订单，无法执行出库操作";
                }
            }
        }
        // 未查询到数据 返回错误提示
        return ServiceResultEnum.DATA_NOT_EXIST.getResult();
    }

    @Override
    @Transactional
    public String closeOrder(Long[] ids) {
        // 查询所有的订单 判断状态 修改状态和更新时间
        List<YifeiMallOrder> orders = yifeiMallOrderMapper.selectByPrimaryKeys(Arrays.asList(ids));
        StringBuilder errorOrderNos = new StringBuilder();
        if (!CollectionUtils.isEmpty(orders)) {
            for (YifeiMallOrder yifeiMallOrder : orders) {
                // isDeleted=1 一定为已关闭订单
                if (yifeiMallOrder.getIsDeleted() == 1) {
                    errorOrderNos.append(yifeiMallOrder.getOrderNo()).append(" ");
                    continue;
                }
                // 已关闭或者已完成无法关闭订单
                if (yifeiMallOrder.getOrderStatus() == 4 || yifeiMallOrder.getOrderStatus() < 0) {
                    errorOrderNos.append(yifeiMallOrder.getOrderNo()).append(" ");
                }
            }
            if (StringUtils.isEmpty(errorOrderNos.toString())) {
                // 订单状态正常 可以执行关闭操作 修改订单状态和更新时间
                if (yifeiMallOrderMapper.closeOrder(Arrays.asList(ids), YifeiMallOrderStatusEnum.ORDER_CLOSED_BY_JUDGE.getOrderStatus()) > 0) {
                    return ServiceResultEnum.SUCCESS.getResult();
                } else {
                    return ServiceResultEnum.DB_ERROR.getResult();
                }
            } else {
                // 订单此时不可执行关闭操作
                if (errorOrderNos.length() > 0 && errorOrderNos.length() < 100) {
                    return errorOrderNos + "订单不能执行关闭操作";
                } else {
                    return "你选择的订单不能执行关闭操作";
                }
            }
        }
        // 未查询到数据 返回错误提示
        return ServiceResultEnum.DATA_NOT_EXIST.getResult();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveOrder(YifeiMallUserVO user, Long couponUserId, List<YifeiMallShoppingCartItemVO> myShoppingCartItems) {
        List<Long> itemIdList = myShoppingCartItems.stream().map(YifeiMallShoppingCartItemVO::getCartItemId).collect(Collectors.toList());
        List<Long> goodsIds = myShoppingCartItems.stream().map(YifeiMallShoppingCartItemVO::getGoodsId).collect(Collectors.toList());
        List<YifeiMallGoods> yifeiMallGoods = yifeiMallGoodsMapper.selectByPrimaryKeys(goodsIds);
        // 检查是否包含已下架商品
        List<YifeiMallGoods> goodsListNotSelling = yifeiMallGoods.stream()
                .filter(yifeiMallGoodsTemp -> yifeiMallGoodsTemp.getGoodsSellStatus() != Constants.SELL_STATUS_UP)
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(goodsListNotSelling)) {
            // goodsListNotSelling 对象非空则表示有下架商品
            YifeiMallException.fail(goodsListNotSelling.get(0).getGoodsName() + "已下架，无法生成订单");
        }
        Map<Long, YifeiMallGoods> yifeiMallGoodsMap = yifeiMallGoods.stream().collect(Collectors.toMap(YifeiMallGoods::getGoodsId, Function.identity(), (entity1, entity2) -> entity1));
        // 判断商品库存
        for (YifeiMallShoppingCartItemVO shoppingCartItemVO : myShoppingCartItems) {
            // 查出的商品中不存在购物车中的这条关联商品数据，直接返回错误提醒
            if (!yifeiMallGoodsMap.containsKey(shoppingCartItemVO.getGoodsId())) {
                YifeiMallException.fail(ServiceResultEnum.SHOPPING_ITEM_ERROR.getResult());
            }
            // 存在数量大于库存的情况，直接返回错误提醒
            if (shoppingCartItemVO.getGoodsCount() > yifeiMallGoodsMap.get(shoppingCartItemVO.getGoodsId()).getStockNum()) {
                YifeiMallException.fail(ServiceResultEnum.SHOPPING_ITEM_COUNT_ERROR.getResult());
            }
        }
        if (CollectionUtils.isEmpty(itemIdList) || CollectionUtils.isEmpty(goodsIds) || CollectionUtils.isEmpty(yifeiMallGoods)) {
            YifeiMallException.fail(ServiceResultEnum.ORDER_GENERATE_ERROR.getResult());
        }
        if (yifeiMallShoppingCartItemMapper.deleteBatch(itemIdList) <= 0) {
            YifeiMallException.fail(ServiceResultEnum.DB_ERROR.getResult());
        }
        List<StockNumDTO> stockNumDTOS = BeanUtil.copyList(myShoppingCartItems, StockNumDTO.class);
        int updateStockNumResult = yifeiMallGoodsMapper.updateStockNum(stockNumDTOS);
        if (updateStockNumResult < 1) {
            YifeiMallException.fail(ServiceResultEnum.SHOPPING_ITEM_COUNT_ERROR.getResult());
        }
        // 生成订单号
        String orderNo = NumberUtil.genOrderNo();
        int priceTotal = 0;
        // 保存订单
        YifeiMallOrder yifeiMallOrder = new YifeiMallOrder();
        yifeiMallOrder.setOrderNo(orderNo);
        yifeiMallOrder.setUserId(user.getUserId());
        yifeiMallOrder.setUserAddress(user.getAddress());
        // 总价
        for (YifeiMallShoppingCartItemVO yifeiMallShoppingCartItemVO : myShoppingCartItems) {
            priceTotal += yifeiMallShoppingCartItemVO.getGoodsCount() * yifeiMallShoppingCartItemVO.getSellingPrice();
        }
        // 如果使用了优惠券
        if (couponUserId != null) {
            YifeiMallUserCouponRecord yifeiMallUserCouponRecord = yifeiMallUserCouponRecordMapper.selectByPrimaryKey(couponUserId);
            YifeiMallCoupon yifeiMallCoupon = yifeiMallCouponMapper.selectByPrimaryKey(yifeiMallUserCouponRecord.getCouponId());
            priceTotal -= yifeiMallCoupon.getDiscount();
        }
        if (priceTotal < 1) {
            YifeiMallException.fail(ServiceResultEnum.ORDER_PRICE_ERROR.getResult());
        }
        yifeiMallOrder.setTotalPrice(priceTotal);
        String extraInfo = "mall支付宝沙箱支付";
        yifeiMallOrder.setExtraInfo(extraInfo);
        // 生成订单项并保存订单项纪录
        if (yifeiMallOrderMapper.insertSelective(yifeiMallOrder) <= 0) {
            YifeiMallException.fail(ServiceResultEnum.DB_ERROR.getResult());
        }
        // 如果使用了优惠券，则更新优惠券状态
        if (couponUserId != null) {
            YifeiMallUserCouponRecord couponUser = new YifeiMallUserCouponRecord();
            couponUser.setCouponUserId(couponUserId);
            couponUser.setOrderId(yifeiMallOrder.getOrderId());
            couponUser.setUseStatus((byte) 1);
            couponUser.setUsedTime(new Date());
            couponUser.setUpdateTime(new Date());
            yifeiMallUserCouponRecordMapper.updateByPrimaryKeySelective(couponUser);
        }
        // 生成所有的订单项快照，并保存至数据库
        List<YifeiMallOrderItem> yifeiMallOrderItems = new ArrayList<>();
        for (YifeiMallShoppingCartItemVO yifeiMallShoppingCartItemVO : myShoppingCartItems) {
            YifeiMallOrderItem yifeiMallOrderItem = new YifeiMallOrderItem();
            // 使用BeanUtil工具类将yifeiMallShoppingCartItemVO中的属性复制到yifeiMallOrderItem对象中
            BeanUtil.copyProperties(yifeiMallShoppingCartItemVO, yifeiMallOrderItem);
            // yifeiMallOrderMapper文件insert()方法中使用了useGeneratedKeys因此orderId可以获取到
            yifeiMallOrderItem.setOrderId(yifeiMallOrder.getOrderId());
            yifeiMallOrderItems.add(yifeiMallOrderItem);
        }
        // 保存至数据库
        if (yifeiMallOrderItemMapper.insertBatch(yifeiMallOrderItems) <= 0) {
            YifeiMallException.fail(ServiceResultEnum.DB_ERROR.getResult());
        }
        // 订单支付超期任务，超过300秒自动取消订单
        taskService.addTask(new OrderUnPaidTask(yifeiMallOrder.getOrderId(), ProjectConfig.getOrderUnpaidOverTime() * 1000));
        // 所有操作成功后，将订单号返回，以供Controller方法跳转到订单详情
        return orderNo;
    }

    @Override
    public String seckillSaveOrder(Long seckillSuccessId, Long userId) {
        YifeiMallSeckillSuccess yifeiMallSeckillSuccess = yifeiMallSeckillSuccessMapper.selectByPrimaryKey(seckillSuccessId);
        if (!yifeiMallSeckillSuccess.getUserId().equals(userId)) {
            throw new YifeiMallException("当前登陆用户与抢购秒杀商品的用户不匹配");
        }
        Long seckillId = yifeiMallSeckillSuccess.getSeckillId();
        YifeiMallSeckill yifeiMallSeckill = yifeiMallSeckillMapper.selectByPrimaryKey(seckillId);
        Long goodsId = yifeiMallSeckill.getGoodsId();
        YifeiMallGoods yifeiMallGoods = yifeiMallGoodsMapper.selectByPrimaryKey(goodsId);
        // 生成订单号
        String orderNo = NumberUtil.genOrderNo();
        // 保存订单
        YifeiMallOrder yifeiMallOrder = new YifeiMallOrder();
        yifeiMallOrder.setOrderNo(orderNo);
        yifeiMallOrder.setTotalPrice(yifeiMallSeckill.getSeckillPrice());
        yifeiMallOrder.setUserId(userId);
        yifeiMallOrder.setUserAddress("秒杀测试地址");
        yifeiMallOrder.setOrderStatus((byte) YifeiMallOrderStatusEnum.ORDER_PAID.getOrderStatus());
        yifeiMallOrder.setPayStatus((byte) PayStatusEnum.PAY_SUCCESS.getPayStatus());
        yifeiMallOrder.setPayType((byte) PayTypeEnum.WEIXIN_PAY.getPayType());
        yifeiMallOrder.setPayTime(new Date());
        String extraInfo = "";
        yifeiMallOrder.setExtraInfo(extraInfo);
        if (yifeiMallOrderMapper.insertSelective(yifeiMallOrder) <= 0) {
            throw new YifeiMallException("生成订单内部异常");
        }
        // 保存订单商品项
        YifeiMallOrderItem yifeiMallOrderItem = new YifeiMallOrderItem();
        Long orderId = yifeiMallOrder.getOrderId();
        yifeiMallOrderItem.setOrderId(orderId);
        yifeiMallOrderItem.setSeckillId(seckillId);
        yifeiMallOrderItem.setGoodsId(yifeiMallGoods.getGoodsId());
        yifeiMallOrderItem.setGoodsCoverImg(yifeiMallGoods.getGoodsCoverImg());
        yifeiMallOrderItem.setGoodsName(yifeiMallGoods.getGoodsName());
        yifeiMallOrderItem.setGoodsCount(1);
        yifeiMallOrderItem.setSellingPrice(yifeiMallSeckill.getSeckillPrice());
        if (yifeiMallOrderItemMapper.insert(yifeiMallOrderItem) <= 0) {
            throw new YifeiMallException("生成订单内部异常");
        }
        // 订单支付超期任务
        taskService.addTask(new OrderUnPaidTask(yifeiMallOrder.getOrderId(), 30 * 1000));
        return orderNo;
    }

    @Override
    public YifeiMallOrderDetailVO getOrderDetailByOrderNo(String orderNo, Long userId) {
        YifeiMallOrder yifeiMallOrder = yifeiMallOrderMapper.selectByOrderNo(orderNo);
        if (yifeiMallOrder == null) {
            YifeiMallException.fail(ServiceResultEnum.ORDER_NOT_EXIST_ERROR.getResult());
        }
        //验证是否是当前userId下的订单，否则报错
        if (!userId.equals(yifeiMallOrder.getUserId())) {
            YifeiMallException.fail(ServiceResultEnum.NO_PERMISSION_ERROR.getResult());
        }
        List<YifeiMallOrderItem> orderItems = yifeiMallOrderItemMapper.selectByOrderId(yifeiMallOrder.getOrderId());
        //获取订单项数据
        if (CollectionUtils.isEmpty(orderItems)) {
            YifeiMallException.fail(ServiceResultEnum.ORDER_ITEM_NOT_EXIST_ERROR.getResult());
        }
        List<YifeiMallOrderItemVO> yifeiMallOrderItemVOS = BeanUtil.copyList(orderItems, YifeiMallOrderItemVO.class);
        YifeiMallOrderDetailVO yifeiMallOrderDetailVO = new YifeiMallOrderDetailVO();
        BeanUtil.copyProperties(yifeiMallOrder, yifeiMallOrderDetailVO);
        yifeiMallOrderDetailVO.setOrderStatusString(YifeiMallOrderStatusEnum.getYifeiMallOrderStatusEnumByStatus(yifeiMallOrderDetailVO.getOrderStatus()).getName());
        yifeiMallOrderDetailVO.setPayTypeString(PayTypeEnum.getPayTypeEnumByType(yifeiMallOrderDetailVO.getPayType()).getName());
        yifeiMallOrderDetailVO.setYifeiMallOrderItemVOS(yifeiMallOrderItemVOS);
        return yifeiMallOrderDetailVO;
    }

    @Override
    public YifeiMallOrder getYifeiMallOrderByOrderNo(String orderNo) {
        return yifeiMallOrderMapper.selectByOrderNo(orderNo);
    }

    @Override
    public PageResult getMyOrders(PageQueryUtil pageUtil) {
        int total = yifeiMallOrderMapper.getTotalYifeiMallOrders(pageUtil);
        List<YifeiMallOrder> yifeiMallOrders = yifeiMallOrderMapper.findYifeiMallOrderList(pageUtil);
        List<YifeiMallOrderListVO> orderListVOS = new ArrayList<>();
        if (total > 0) {
            // 数据转换 将实体类转成vo
            orderListVOS = BeanUtil.copyList(yifeiMallOrders, YifeiMallOrderListVO.class);
            // 设置订单状态中文显示值
            for (YifeiMallOrderListVO yifeiMallOrderListVO : orderListVOS) {
                yifeiMallOrderListVO.setOrderStatusString(YifeiMallOrderStatusEnum.getYifeiMallOrderStatusEnumByStatus(yifeiMallOrderListVO.getOrderStatus()).getName());
            }
            List<Long> orderIds = yifeiMallOrders.stream().map(YifeiMallOrder::getOrderId).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(orderIds)) {
                List<YifeiMallOrderItem> orderItems = yifeiMallOrderItemMapper.selectByOrderIds(orderIds);
                Map<Long, List<YifeiMallOrderItem>> itemByOrderIdMap = orderItems.stream().collect(groupingBy(YifeiMallOrderItem::getOrderId));
                for (YifeiMallOrderListVO yifeiMallOrderListVO : orderListVOS) {
                    // 封装每个订单列表对象的订单项数据
                    if (itemByOrderIdMap.containsKey(yifeiMallOrderListVO.getOrderId())) {
                        List<YifeiMallOrderItem> orderItemListTemp = itemByOrderIdMap.get(yifeiMallOrderListVO.getOrderId());
                        // 将yifeiMallOrderItem对象列表转换成yifeiMallOrderItemVO对象列表
                        List<YifeiMallOrderItemVO> yifeiMallOrderItemVOS = BeanUtil.copyList(orderItemListTemp, YifeiMallOrderItemVO.class);
                        yifeiMallOrderListVO.setYifeiMallOrderItemVOS(yifeiMallOrderItemVOS);
                    }
                }
            }
        }
        return new PageResult(orderListVOS, total, pageUtil.getLimit(), pageUtil.getPage());
    }

    @Override
    public String cancelOrder(String orderNo, Long userId) {
        YifeiMallOrder yifeiMallOrder = yifeiMallOrderMapper.selectByOrderNo(orderNo);
        if (yifeiMallOrder != null) {
            // 验证是否是当前userId下的订单，否则报错
            if (!userId.equals(yifeiMallOrder.getUserId())) {
                YifeiMallException.fail(ServiceResultEnum.NO_PERMISSION_ERROR.getResult());
            }
            // 订单状态判断
            if (yifeiMallOrder.getOrderStatus().intValue() == YifeiMallOrderStatusEnum.ORDER_SUCCESS.getOrderStatus()
                    || yifeiMallOrder.getOrderStatus().intValue() == YifeiMallOrderStatusEnum.ORDER_CLOSED_BY_MALLUSER.getOrderStatus()
                    || yifeiMallOrder.getOrderStatus().intValue() == YifeiMallOrderStatusEnum.ORDER_CLOSED_BY_EXPIRED.getOrderStatus()
                    || yifeiMallOrder.getOrderStatus().intValue() == YifeiMallOrderStatusEnum.ORDER_CLOSED_BY_JUDGE.getOrderStatus()) {
                return ServiceResultEnum.ORDER_STATUS_ERROR.getResult();
            }
            if (yifeiMallOrderMapper.closeOrder(Collections.singletonList(yifeiMallOrder.getOrderId()), YifeiMallOrderStatusEnum.ORDER_CLOSED_BY_MALLUSER.getOrderStatus()) > 0) {
                return ServiceResultEnum.SUCCESS.getResult();
            } else {
                return ServiceResultEnum.DB_ERROR.getResult();
            }
        }
        return ServiceResultEnum.ORDER_NOT_EXIST_ERROR.getResult();
    }

    @Override
    public String finishOrder(String orderNo, Long userId) {
        YifeiMallOrder yifeiMallOrder = yifeiMallOrderMapper.selectByOrderNo(orderNo);
        if (yifeiMallOrder != null) {
            // 验证是否是当前userId下的订单，否则报错
            if (!userId.equals(yifeiMallOrder.getUserId())) {
                return ServiceResultEnum.NO_PERMISSION_ERROR.getResult();
            }
            // 订单状态判断 非出库状态下不进行修改操作
            if (yifeiMallOrder.getOrderStatus().intValue() != YifeiMallOrderStatusEnum.ORDER_EXPRESS.getOrderStatus()) {
                return ServiceResultEnum.ORDER_STATUS_ERROR.getResult();
            }
            yifeiMallOrder.setOrderStatus((byte) YifeiMallOrderStatusEnum.ORDER_SUCCESS.getOrderStatus());
            yifeiMallOrder.setUpdateTime(new Date());
            if (yifeiMallOrderMapper.updateByPrimaryKeySelective(yifeiMallOrder) > 0) {
                return ServiceResultEnum.SUCCESS.getResult();
            } else {
                return ServiceResultEnum.DB_ERROR.getResult();
            }
        }
        return ServiceResultEnum.ORDER_NOT_EXIST_ERROR.getResult();
    }

    @Override
    public String paySuccess(String orderNo, int payType) {
        YifeiMallOrder yifeiMallOrder = yifeiMallOrderMapper.selectByOrderNo(orderNo);
        if (yifeiMallOrder == null) {
            return ServiceResultEnum.ORDER_NOT_EXIST_ERROR.getResult();
        }
        // 订单状态判断 非待支付状态下不进行修改操作
        if (yifeiMallOrder.getOrderStatus().intValue() != YifeiMallOrderStatusEnum.ORDER_PRE_PAY.getOrderStatus()) {
            return ServiceResultEnum.ORDER_STATUS_ERROR.getResult();
        }
        yifeiMallOrder.setOrderStatus((byte) YifeiMallOrderStatusEnum.ORDER_PAID.getOrderStatus());
        yifeiMallOrder.setPayType((byte) payType);
        yifeiMallOrder.setPayStatus((byte) PayStatusEnum.PAY_SUCCESS.getPayStatus());
        yifeiMallOrder.setPayTime(new Date());
        yifeiMallOrder.setUpdateTime(new Date());
        if (yifeiMallOrderMapper.updateByPrimaryKeySelective(yifeiMallOrder) <= 0) {
            return ServiceResultEnum.DB_ERROR.getResult();
        }
        taskService.removeTask(new OrderUnPaidTask(yifeiMallOrder.getOrderId()));
        return ServiceResultEnum.SUCCESS.getResult();
    }

    @Override
    public List<YifeiMallOrderItemVO> getOrderItems(Long id) {
        YifeiMallOrder yifeiMallOrder = yifeiMallOrderMapper.selectByPrimaryKey(id);
        if (yifeiMallOrder != null) {
            List<YifeiMallOrderItem> orderItems = yifeiMallOrderItemMapper.selectByOrderId(yifeiMallOrder.getOrderId());
            // 获取订单项数据
            if (!CollectionUtils.isEmpty(orderItems)) {
                return BeanUtil.copyList(orderItems, YifeiMallOrderItemVO.class);
            }
        }
        return null;
    }
}
