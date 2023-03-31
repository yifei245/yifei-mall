package com.yifei.mall.service;

import com.yifei.mall.controller.vo.YifeiMallCouponVO;
import com.yifei.mall.controller.vo.YifeiMallMyCouponVO;
import com.yifei.mall.controller.vo.YifeiMallShoppingCartItemVO;
import com.yifei.mall.entity.YifeiMallCoupon;
import com.yifei.mall.util.PageQueryUtil;
import com.yifei.mall.util.PageResult;

import java.util.List;

public interface YifeiMallCouponService {

    PageResult getCouponPage(PageQueryUtil pageUtil);

    boolean saveCoupon(YifeiMallCoupon yifeiMallCoupon);

    boolean updateCoupon(YifeiMallCoupon yifeiMallCoupon);

    YifeiMallCoupon getCouponById(Long id);

    boolean deleteCouponById(Long id);

    /**
     * 查询可用优惠券
     *
     * @param userId
     * @return
     */
    List<YifeiMallCouponVO> selectAvailableCoupon(Long userId);

    /**
     * 用户领取优惠劵
     *
     * @param couponId 优惠劵ID
     * @param userId   用户ID
     * @return boolean
     */
    boolean saveCouponUser(Long couponId, Long userId);

    /**
     * 查询我的优惠券
     *
     * @param userId 用户ID
     * @return
     */
    List<YifeiMallCouponVO> selectMyCoupons(Long userId);

    /**
     * 查询当前订单可用的优惠券
     *
     * @param myShoppingCartItems
     * @param priceTotal
     * @param userId
     * @return
     */
    List<YifeiMallMyCouponVO> selectOrderCanUseCoupons(List<YifeiMallShoppingCartItemVO> myShoppingCartItems, int priceTotal, Long userId);

    /**
     * 删除用户优惠券
     *
     * @param couponUserId
     * @return
     */
    boolean deleteCouponUser(Long couponUserId);

    /**
     * 回复未支付的优惠券
     * @param orderId
     */
    void releaseCoupon(Long orderId);
}
