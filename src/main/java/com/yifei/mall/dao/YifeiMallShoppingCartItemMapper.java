
package com.yifei.mall.dao;

import com.yifei.mall.entity.YifeiMallShoppingCartItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface YifeiMallShoppingCartItemMapper {
    int deleteByPrimaryKey(Long cartItemId);

    int insert(YifeiMallShoppingCartItem record);

    int insertSelective(YifeiMallShoppingCartItem record);

    YifeiMallShoppingCartItem selectByPrimaryKey(Long cartItemId);

    YifeiMallShoppingCartItem selectByUserIdAndGoodsId(@Param("yifeiMallUserId") Long yifeiMallUserId, @Param("goodsId") Long goodsId);

    List<YifeiMallShoppingCartItem> selectByUserId(@Param("yifeiMallUserId") Long yifeiMallUserId, @Param("number") int number);

    int selectCountByUserId(Long yifeiMallUserId);

    int updateByPrimaryKeySelective(YifeiMallShoppingCartItem record);

    int updateByPrimaryKey(YifeiMallShoppingCartItem record);

    int deleteBatch(List<Long> ids);
}