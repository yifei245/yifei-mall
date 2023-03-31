
package com.yifei.mall.service;

import com.yifei.mall.controller.vo.YifeiMallOrderDetailVO;
import com.yifei.mall.controller.vo.YifeiMallOrderItemVO;
import com.yifei.mall.controller.vo.YifeiMallShoppingCartItemVO;
import com.yifei.mall.controller.vo.YifeiMallUserVO;
import com.yifei.mall.entity.YifeiMallOrder;
import com.yifei.mall.util.PageQueryUtil;
import com.yifei.mall.util.PageResult;

import java.util.List;

public interface YifeiMallOrderService {
    /**
     * 后台分页
     *
     * @param pageUtil
     * @return
     */
    PageResult getYifeiMallOrdersPage(PageQueryUtil pageUtil);

    /**
     * 订单信息修改
     *
     * @param yifeiMallOrder
     * @return
     */
    String updateOrderInfo(YifeiMallOrder yifeiMallOrder);

    /**
     * 根据主键修改订单信息
     *
     * @param yifeiMallOrder
     * @return
     */
    boolean updateByPrimaryKeySelective(YifeiMallOrder yifeiMallOrder);

    /**
     * 配货
     *
     * @param ids
     * @return
     */
    String checkDone(Long[] ids);

    /**
     * 出库
     *
     * @param ids
     * @return
     */
    String checkOut(Long[] ids);

    /**
     * 关闭订单
     *
     * @param ids
     * @return
     */
    String closeOrder(Long[] ids);

    /**
     * 保存订单
     *
     * @param user
     * @param couponUserId
     * @param myShoppingCartItems
     * @return
     */
    String saveOrder(YifeiMallUserVO user, Long couponUserId, List<YifeiMallShoppingCartItemVO> myShoppingCartItems);

    /**
     * 生成秒杀订单
     *
     * @param seckillSuccessId
     * @param userId
     * @return
     */
    String seckillSaveOrder(Long seckillSuccessId, Long userId);

    /**
     * 获取订单详情
     *
     * @param orderNo
     * @param userId
     * @return
     */
    YifeiMallOrderDetailVO getOrderDetailByOrderNo(String orderNo, Long userId);

    /**
     * 获取订单详情
     *
     * @param orderNo
     * @return
     */
    YifeiMallOrder getYifeiMallOrderByOrderNo(String orderNo);

    /**
     * 我的订单列表
     *
     * @param pageUtil
     * @return
     */
    PageResult getMyOrders(PageQueryUtil pageUtil);

    /**
     * 手动取消订单
     *
     * @param orderNo
     * @param userId
     * @return
     */
    String cancelOrder(String orderNo, Long userId);

    /**
     * 确认收货
     *
     * @param orderNo
     * @param userId
     * @return
     */
    String finishOrder(String orderNo, Long userId);

    String paySuccess(String orderNo, int payType);

    List<YifeiMallOrderItemVO> getOrderItems(Long id);
}
