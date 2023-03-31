
package com.yifei.mall.service;

import com.yifei.mall.controller.vo.YifeiMallShoppingCartItemVO;
import com.yifei.mall.entity.YifeiMallShoppingCartItem;

import java.util.List;

public interface YifeiMallShoppingCartService {

    /**
     * 保存商品至购物车中
     *
     * @param yifeiMallShoppingCartItem
     * @return
     */
    String saveYifeiMallCartItem(YifeiMallShoppingCartItem yifeiMallShoppingCartItem);

    /**
     * 修改购物车中的属性
     *
     * @param yifeiMallShoppingCartItem
     * @return
     */
    String updateYifeiMallCartItem(YifeiMallShoppingCartItem yifeiMallShoppingCartItem);

    /**
     * 获取购物项详情
     *
     * @param yifeiMallShoppingCartItemId
     * @return
     */
    YifeiMallShoppingCartItem getYifeiMallCartItemById(Long yifeiMallShoppingCartItemId);

    /**
     * 删除购物车中的商品
     *
     *
     * @param shoppingCartItemId
     * @param userId
     * @return
     */
    Boolean deleteById(Long shoppingCartItemId, Long userId);

    /**
     * 获取我的购物车中的列表数据
     *
     * @param yifeiMallUserId
     * @return
     */
    List<YifeiMallShoppingCartItemVO> getMyShoppingCartItems(Long yifeiMallUserId);
}
