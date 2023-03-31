
package com.yifei.mall.service.impl;

import com.yifei.mall.controller.vo.YifeiMallShoppingCartItemVO;
import com.yifei.mall.common.Constants;
import com.yifei.mall.common.ServiceResultEnum;
import com.yifei.mall.dao.YifeiMallGoodsMapper;
import com.yifei.mall.dao.YifeiMallShoppingCartItemMapper;
import com.yifei.mall.entity.YifeiMallGoods;
import com.yifei.mall.entity.YifeiMallShoppingCartItem;
import com.yifei.mall.service.YifeiMallShoppingCartService;
import com.yifei.mall.util.BeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class YifeiMallShoppingCartServiceImpl implements YifeiMallShoppingCartService {

    @Autowired
    private YifeiMallShoppingCartItemMapper yifeiMallShoppingCartItemMapper;

    @Autowired
    private YifeiMallGoodsMapper yifeiMallGoodsMapper;

    @Override
    public String saveYifeiMallCartItem(YifeiMallShoppingCartItem yifeiMallShoppingCartItem) {
        YifeiMallShoppingCartItem temp = yifeiMallShoppingCartItemMapper.selectByUserIdAndGoodsId(yifeiMallShoppingCartItem.getUserId(), yifeiMallShoppingCartItem.getGoodsId());
        if (temp != null) {
            //已存在则修改该记录
            temp.setGoodsCount(yifeiMallShoppingCartItem.getGoodsCount());
            return updateYifeiMallCartItem(temp);
        }
        YifeiMallGoods yifeiMallGoods = yifeiMallGoodsMapper.selectByPrimaryKey(yifeiMallShoppingCartItem.getGoodsId());
        //商品为空
        if (yifeiMallGoods == null) {
            return ServiceResultEnum.GOODS_NOT_EXIST.getResult();
        }
        int totalItem = yifeiMallShoppingCartItemMapper.selectCountByUserId(yifeiMallShoppingCartItem.getUserId()) + 1;
        //超出单个商品的最大数量
        if (yifeiMallShoppingCartItem.getGoodsCount() > Constants.SHOPPING_CART_ITEM_LIMIT_NUMBER) {
            return ServiceResultEnum.SHOPPING_CART_ITEM_LIMIT_NUMBER_ERROR.getResult();
        }
        //超出最大数量
        if (totalItem > Constants.SHOPPING_CART_ITEM_TOTAL_NUMBER) {
            return ServiceResultEnum.SHOPPING_CART_ITEM_TOTAL_NUMBER_ERROR.getResult();
        }
        //保存记录
        if (yifeiMallShoppingCartItemMapper.insertSelective(yifeiMallShoppingCartItem) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public String updateYifeiMallCartItem(YifeiMallShoppingCartItem yifeiMallShoppingCartItem) {
        YifeiMallShoppingCartItem yifeiMallShoppingCartItemUpdate = yifeiMallShoppingCartItemMapper.selectByPrimaryKey(yifeiMallShoppingCartItem.getCartItemId());
        if (yifeiMallShoppingCartItemUpdate == null) {
            return ServiceResultEnum.DATA_NOT_EXIST.getResult();
        }
        //超出单个商品的最大数量
        if (yifeiMallShoppingCartItem.getGoodsCount() > Constants.SHOPPING_CART_ITEM_LIMIT_NUMBER) {
            return ServiceResultEnum.SHOPPING_CART_ITEM_LIMIT_NUMBER_ERROR.getResult();
        }
        // 数量相同不会进行修改
        if (yifeiMallShoppingCartItemUpdate.getGoodsCount().equals(yifeiMallShoppingCartItem.getGoodsCount())) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        // userId不同不能修改
        if (!yifeiMallShoppingCartItem.getUserId().equals(yifeiMallShoppingCartItemUpdate.getUserId())) {
            return ServiceResultEnum.NO_PERMISSION_ERROR.getResult();
        }
        yifeiMallShoppingCartItemUpdate.setGoodsCount(yifeiMallShoppingCartItem.getGoodsCount());
        yifeiMallShoppingCartItemUpdate.setUpdateTime(new Date());
        //修改记录
        if (yifeiMallShoppingCartItemMapper.updateByPrimaryKeySelective(yifeiMallShoppingCartItemUpdate) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public YifeiMallShoppingCartItem getYifeiMallCartItemById(Long yifeiMallShoppingCartItemId) {
        return yifeiMallShoppingCartItemMapper.selectByPrimaryKey(yifeiMallShoppingCartItemId);
    }

    @Override
    public Boolean deleteById(Long shoppingCartItemId, Long userId) {
        YifeiMallShoppingCartItem yifeiMallShoppingCartItem = yifeiMallShoppingCartItemMapper.selectByPrimaryKey(shoppingCartItemId);
        if (yifeiMallShoppingCartItem == null) {
            return false;
        }
        //userId不同不能删除
        if (!userId.equals(yifeiMallShoppingCartItem.getUserId())) {
            return false;
        }
        return yifeiMallShoppingCartItemMapper.deleteByPrimaryKey(shoppingCartItemId) > 0;
    }

    @Override
    public List<YifeiMallShoppingCartItemVO> getMyShoppingCartItems(Long yifeiMallUserId) {
        List<YifeiMallShoppingCartItemVO> yifeiMallShoppingCartItemVOS = new ArrayList<>();
        List<YifeiMallShoppingCartItem> yifeiMallShoppingCartItems = yifeiMallShoppingCartItemMapper.selectByUserId(yifeiMallUserId, Constants.SHOPPING_CART_ITEM_TOTAL_NUMBER);
        if (!CollectionUtils.isEmpty(yifeiMallShoppingCartItems)) {
            //查询商品信息并做数据转换
            List<Long>yifeiMallGoodsIds = yifeiMallShoppingCartItems.stream().map(YifeiMallShoppingCartItem::getGoodsId).collect(Collectors.toList());
            List<YifeiMallGoods> yifeiMallGoods = yifeiMallGoodsMapper.selectByPrimaryKeys(yifeiMallGoodsIds);
            Map<Long, YifeiMallGoods> yifeiBeeMallGoodsMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(yifeiMallGoods)) {
                yifeiBeeMallGoodsMap = yifeiMallGoods.stream().collect(Collectors.toMap(YifeiMallGoods::getGoodsId, Function.identity(), (entity1, entity2) -> entity1));
            }
            for (YifeiMallShoppingCartItem yifeiMallShoppingCartItem : yifeiMallShoppingCartItems) {
                YifeiMallShoppingCartItemVO yifeiMallShoppingCartItemVO = new YifeiMallShoppingCartItemVO();
                BeanUtil.copyProperties(yifeiMallShoppingCartItem, yifeiMallShoppingCartItemVO);
                if (yifeiBeeMallGoodsMap.containsKey(yifeiMallShoppingCartItem.getGoodsId())) {
                    YifeiMallGoods yifeiMallGoodsTemp = yifeiBeeMallGoodsMap.get(yifeiMallShoppingCartItem.getGoodsId());
                    yifeiMallShoppingCartItemVO.setGoodsCoverImg(yifeiMallGoodsTemp.getGoodsCoverImg());
                    String goodsName = yifeiMallGoodsTemp.getGoodsName();
                    // 字符串过长导致文字超出的问题
                    if (goodsName.length() > 28) {
                        goodsName = goodsName.substring(0, 28) + "...";
                    }
                    yifeiMallShoppingCartItemVO.setGoodsName(goodsName);
                    yifeiMallShoppingCartItemVO.setSellingPrice(yifeiMallGoodsTemp.getSellingPrice());
                    yifeiMallShoppingCartItemVOS.add(yifeiMallShoppingCartItemVO);
                }
            }
        }
        return yifeiMallShoppingCartItemVOS;
    }
}
