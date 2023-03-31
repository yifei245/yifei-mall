package com.yifei.mall.entity;

import java.time.LocalDate;
import java.util.Date;

public class YifeiMallCoupon {
    private Long couponId;

    private String couponName;

    private String couponDesc;

    private Integer couponTotal;

    private Integer discount;

    private Integer min;

    private Byte couponLimit;

    private Byte couponType;

    private Byte couponStatus;

    private Byte goodsType;

    private String goodsValue;

    private String couponCode;

    private LocalDate couponStartTime;

    private LocalDate couponEndTime;

    private Date createTime;

    private Date updateTime;

    private Byte isDeleted;

    public Long getCouponId() {
        return couponId;
    }

    public YifeiMallCoupon setCouponId(Long couponId) {
        this.couponId = couponId;
        return this;
    }

    public String getCouponName() {
        return couponName;
    }

    public YifeiMallCoupon setCouponName(String couponName) {
        this.couponName = couponName;
        return this;
    }

    public String getCouponDesc() {
        return couponDesc;
    }

    public YifeiMallCoupon setCouponDesc(String couponDesc) {
        this.couponDesc = couponDesc;
        return this;
    }

    public Integer getCouponTotal() {
        return couponTotal;
    }

    public YifeiMallCoupon setCouponTotal(Integer couponTotal) {
        this.couponTotal = couponTotal;
        return this;
    }

    public Integer getDiscount() {
        return discount;
    }

    public YifeiMallCoupon setDiscount(Integer discount) {
        this.discount = discount;
        return this;
    }

    public Integer getMin() {
        return min;
    }

    public YifeiMallCoupon setMin(Integer min) {
        this.min = min;
        return this;
    }

    public Byte getCouponLimit() {
        return couponLimit;
    }

    public YifeiMallCoupon setCouponLimit(Byte couponLimit) {
        this.couponLimit = couponLimit;
        return this;
    }

    public Byte getCouponType() {
        return couponType;
    }

    public YifeiMallCoupon setCouponType(Byte couponType) {
        this.couponType = couponType;
        return this;
    }

    public Byte getCouponStatus() {
        return couponStatus;
    }

    public YifeiMallCoupon setCouponStatus(Byte couponStatus) {
        this.couponStatus = couponStatus;
        return this;
    }

    public Byte getGoodsType() {
        return goodsType;
    }

    public YifeiMallCoupon setGoodsType(Byte goodsType) {
        this.goodsType = goodsType;
        return this;
    }

    public String getGoodsValue() {
        return goodsValue;
    }

    public YifeiMallCoupon setGoodsValue(String goodsValue) {
        this.goodsValue = goodsValue;
        return this;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public YifeiMallCoupon setCouponCode(String couponCode) {
        this.couponCode = couponCode;
        return this;
    }

    public LocalDate getCouponStartTime() {
        return couponStartTime;
    }

    public YifeiMallCoupon setCouponStartTime(LocalDate couponStartTime) {
        this.couponStartTime = couponStartTime;
        return this;
    }

    public LocalDate getCouponEndTime() {
        return couponEndTime;
    }

    public YifeiMallCoupon setCouponEndTime(LocalDate couponEndTime) {
        this.couponEndTime = couponEndTime;
        return this;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public YifeiMallCoupon setCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public YifeiMallCoupon setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    public Byte getIsDeleted() {
        return isDeleted;
    }

    public YifeiMallCoupon setIsDeleted(Byte isDeleted) {
        this.isDeleted = isDeleted;
        return this;
    }
}
