
package com.yifei.mall.dao;

import com.yifei.mall.entity.YifeiMallOrder;
import com.yifei.mall.util.PageQueryUtil;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface YifeiMallOrderMapper {
    int deleteByPrimaryKey(Long orderId);

    int insert(YifeiMallOrder record);

    int insertSelective(YifeiMallOrder record);

    YifeiMallOrder selectByPrimaryKey(Long orderId);

    YifeiMallOrder selectByOrderNo(String orderNo);

    int updateByPrimaryKeySelective(YifeiMallOrder record);

    int updateByPrimaryKey(YifeiMallOrder record);

    List<YifeiMallOrder> findYifeiMallOrderList(PageQueryUtil pageUtil);

    int getTotalYifeiMallOrders(PageQueryUtil pageUtil);

    List<YifeiMallOrder> selectByPrimaryKeys(@Param("orderIds") List<Long> orderIds);

    int checkOut(@Param("orderIds") List<Long> orderIds);

    int closeOrder(@Param("orderIds") List<Long> orderIds, @Param("orderStatus") int orderStatus);

    int checkDone(@Param("orderIds") List<Long> asList);

    List<YifeiMallOrder> selectPrePayOrders();
}
