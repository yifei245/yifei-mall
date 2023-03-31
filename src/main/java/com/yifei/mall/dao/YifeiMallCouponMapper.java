package com.yifei.mall.dao;

import com.yifei.mall.entity.YifeiMallCoupon;
import com.yifei.mall.util.PageQueryUtil;

import java.util.List;

public interface YifeiMallCouponMapper {
    int deleteByPrimaryKey(Long couponId);

    int deleteBatch(Integer[] couponIds);

    int insert(YifeiMallCoupon record);

    int insertSelective(YifeiMallCoupon record);

    YifeiMallCoupon selectByPrimaryKey(Long couponId);

    int updateByPrimaryKeySelective(YifeiMallCoupon record);

    int updateByPrimaryKey(YifeiMallCoupon record);

    List<YifeiMallCoupon> findCouponlList(PageQueryUtil pageUtil);

    int getTotalCoupons(PageQueryUtil pageUtil);

    List<YifeiMallCoupon> selectAvailableCoupon();

    int reduceCouponTotal(Long couponId);

    List<YifeiMallCoupon> selectByIds(List<Long> couponIds);

    List<YifeiMallCoupon> selectAvailableGiveCoupon();

}
