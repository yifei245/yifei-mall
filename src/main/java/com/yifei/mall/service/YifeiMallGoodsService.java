
package com.yifei.mall.service;

import com.yifei.mall.entity.YifeiMallGoods;
import com.yifei.mall.util.PageQueryUtil;
import com.yifei.mall.util.PageResult;

import java.util.List;

public interface YifeiMallGoodsService {
    /**
     * 后台分页
     *
     * @param pageUtil
     * @return
     */
    PageResult getYifeiMallGoodsPage(PageQueryUtil pageUtil);

    /**
     * 添加商品
     *
     * @param goods
     * @return
     */
    String saveYifeiMallGoods(YifeiMallGoods goods);

    /**
     * 批量新增商品数据
     *
     * @param yifeiMallGoodsList
     * @return
     */
    void batchSaveYifeiMallGoods(List<YifeiMallGoods> yifeiMallGoodsList);

    /**
     * 修改商品信息
     *
     * @param goods
     * @return
     */
    String updateYifeiMallGoods(YifeiMallGoods goods);

    /**
     * 获取商品详情
     *
     * @param id
     * @return
     */
    YifeiMallGoods getYifeiMallGoodsById(Long id);

    /**
     * 批量修改销售状态(上架下架)
     *
     * @param ids
     * @return
     */
    Boolean batchUpdateSellStatus(Long[] ids,int sellStatus);

    /**
     * 商品搜索
     *
     * @param pageUtil
     * @return
     */
    PageResult searchYifeiMallGoods(PageQueryUtil pageUtil);
}
