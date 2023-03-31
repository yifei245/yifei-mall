package com.yifei.mall.dao;

import com.yifei.mall.entity.YifeiMallUserCouponRecord;

import java.util.List;

public interface YifeiMallUserCouponRecordMapper {
    int deleteByPrimaryKey(Long couponUserId);

    int insert(YifeiMallUserCouponRecord record);

    int insertSelective(YifeiMallUserCouponRecord record);

    YifeiMallUserCouponRecord selectByPrimaryKey(Long couponUserId);

    int updateByPrimaryKeySelective(YifeiMallUserCouponRecord record);

    int updateByPrimaryKey(YifeiMallUserCouponRecord record);

    int getUserCouponCount(Long userId, Long couponId);

    int getCouponCount(Long couponId);

    List<YifeiMallUserCouponRecord> selectMyCoupons(Long userId);

    List<YifeiMallUserCouponRecord> selectMyAvailableCoupons(Long userId);

    YifeiMallUserCouponRecord getUserCouponByOrderId(Long orderId);
}
