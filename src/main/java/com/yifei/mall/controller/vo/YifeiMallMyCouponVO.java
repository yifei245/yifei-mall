package com.yifei.mall.controller.vo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;

public class YifeiMallMyCouponVO implements Serializable {

    private static final long serialVersionUID = -8182785776876066101L;

    private Long couponUserId;

    private Long userId;

    private Long couponId;

    private String name;

    private String couponDesc;

    private Integer discount;

    private Integer min;

    private Byte goodsType;

    private String goodsValue;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    public Long getCouponUserId() {
        return couponUserId;
    }

    public YifeiMallMyCouponVO setCouponUserId(Long couponUserId) {
        this.couponUserId = couponUserId;
        return this;
    }

    public Long getUserId() {
        return userId;
    }

    public YifeiMallMyCouponVO setUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    public Long getCouponId() {
        return couponId;
    }

    public YifeiMallMyCouponVO setCouponId(Long couponId) {
        this.couponId = couponId;
        return this;
    }

    public String getName() {
        return name;
    }

    public YifeiMallMyCouponVO setName(String name) {
        this.name = name;
        return this;
    }

    public String getCouponDesc() {
        return couponDesc;
    }

    public YifeiMallMyCouponVO setCouponDesc(String couponDesc) {
        this.couponDesc = couponDesc;
        return this;
    }

    public Integer getDiscount() {
        return discount;
    }

    public YifeiMallMyCouponVO setDiscount(Integer discount) {
        this.discount = discount;
        return this;
    }

    public Integer getMin() {
        return min;
    }

    public YifeiMallMyCouponVO setMin(Integer min) {
        this.min = min;
        return this;
    }

    public Byte getGoodsType() {
        return goodsType;
    }

    public YifeiMallMyCouponVO setGoodsType(Byte goodsType) {
        this.goodsType = goodsType;
        return this;
    }

    public String getGoodsValue() {
        return goodsValue;
    }

    public YifeiMallMyCouponVO setGoodsValue(String goodsValue) {
        this.goodsValue = goodsValue;
        return this;
    }

    public Date getStartTime() {
        return startTime;
    }

    public YifeiMallMyCouponVO setStartTime(Date startTime) {
        this.startTime = startTime;
        return this;
    }

    public Date getEndTime() {
        return endTime;
    }

    public YifeiMallMyCouponVO setEndTime(Date endTime) {
        this.endTime = endTime;
        return this;
    }
}
