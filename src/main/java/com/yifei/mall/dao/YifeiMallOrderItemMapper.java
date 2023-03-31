
package com.yifei.mall.dao;

import com.yifei.mall.entity.YifeiMallOrderItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface YifeiMallOrderItemMapper {
    int deleteByPrimaryKey(Long orderItemId);

    int insert(YifeiMallOrderItem record);

    int insertSelective(YifeiMallOrderItem record);

    YifeiMallOrderItem selectByPrimaryKey(Long orderItemId);

    /**
     * 根据订单id获取订单项列表
     *
     * @param orderId
     * @return
     */
    List<YifeiMallOrderItem> selectByOrderId(Long orderId);

    /**
     * 根据订单ids获取订单项列表
     *
     * @param orderIds
     * @return
     */
    List<YifeiMallOrderItem> selectByOrderIds(@Param("orderIds") List<Long> orderIds);

    /**
     * 批量insert订单项数据
     *
     * @param orderItems
     * @return
     */
    int insertBatch(@Param("orderItems") List<YifeiMallOrderItem> orderItems);

    int updateByPrimaryKeySelective(YifeiMallOrderItem record);

    int updateByPrimaryKey(YifeiMallOrderItem record);
}