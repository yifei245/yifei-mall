
package com.yifei.mall.service.impl;

import com.yifei.mall.controller.vo.YifeiMallSearchGoodsVO;
import com.yifei.mall.common.YifeiMallCategoryLevelEnum;
import com.yifei.mall.common.YifeiMallException;
import com.yifei.mall.common.ServiceResultEnum;
import com.yifei.mall.dao.GoodsCategoryMapper;
import com.yifei.mall.dao.YifeiMallGoodsMapper;
import com.yifei.mall.entity.GoodsCategory;
import com.yifei.mall.entity.YifeiMallGoods;
import com.yifei.mall.service.YifeiMallGoodsService;
import com.yifei.mall.util.BeanUtil;
import com.yifei.mall.util.PageQueryUtil;
import com.yifei.mall.util.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class YifeiMallGoodsServiceImpl implements YifeiMallGoodsService {

    @Autowired
    private YifeiMallGoodsMapper goodsMapper;
    @Autowired
    private GoodsCategoryMapper goodsCategoryMapper;

    @Override
    public PageResult getYifeiMallGoodsPage(PageQueryUtil pageUtil) {
        List<YifeiMallGoods> goodsList = goodsMapper.findYifeiMallGoodsList(pageUtil);
        int total = goodsMapper.getTotalYifeiMallGoods(pageUtil);
        return new PageResult(goodsList, total, pageUtil.getLimit(), pageUtil.getPage());
    }

    @Override
    public String saveYifeiMallGoods(YifeiMallGoods goods) {
        GoodsCategory goodsCategory = goodsCategoryMapper.selectByPrimaryKey(goods.getGoodsCategoryId());
        // 分类不存在或者不是三级分类，则该参数字段异常
        if (goodsCategory == null || goodsCategory.getCategoryLevel().intValue() != YifeiMallCategoryLevelEnum.LEVEL_THREE.getLevel()) {
            return ServiceResultEnum.GOODS_CATEGORY_ERROR.getResult();
        }
        if (goodsMapper.selectByCategoryIdAndName(goods.getGoodsName(), goods.getGoodsCategoryId()) != null) {
            return ServiceResultEnum.SAME_GOODS_EXIST.getResult();
        }
        if (goodsMapper.insertSelective(goods) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public void batchSaveYifeiMallGoods(List<YifeiMallGoods> yifeiMallGoodsList) {
        if (!CollectionUtils.isEmpty(yifeiMallGoodsList)) {
            goodsMapper.batchInsert(yifeiMallGoodsList);
        }
    }

    @Override
    public String updateYifeiMallGoods(YifeiMallGoods goods) {
        GoodsCategory goodsCategory = goodsCategoryMapper.selectByPrimaryKey(goods.getGoodsCategoryId());
        // 分类不存在或者不是三级分类，则该参数字段异常
        if (goodsCategory == null || goodsCategory.getCategoryLevel().intValue() != YifeiMallCategoryLevelEnum.LEVEL_THREE.getLevel()) {
            return ServiceResultEnum.GOODS_CATEGORY_ERROR.getResult();
        }
        YifeiMallGoods temp = goodsMapper.selectByPrimaryKey(goods.getGoodsId());
        if (temp == null) {
            return ServiceResultEnum.DATA_NOT_EXIST.getResult();
        }
        YifeiMallGoods temp2 = goodsMapper.selectByCategoryIdAndName(goods.getGoodsName(), goods.getGoodsCategoryId());
        if (temp2 != null && !temp2.getGoodsId().equals(goods.getGoodsId())) {
            //name和分类id相同且不同id 不能继续修改
            return ServiceResultEnum.SAME_GOODS_EXIST.getResult();
        }
        goods.setUpdateTime(new Date());
        if (goodsMapper.updateByPrimaryKeySelective(goods) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public YifeiMallGoods getYifeiMallGoodsById(Long id) {
        YifeiMallGoods yifeiMallGoods = goodsMapper.selectByPrimaryKey(id);
        if (yifeiMallGoods == null) {
            YifeiMallException.fail(ServiceResultEnum.GOODS_NOT_EXIST.getResult());
        }
        return yifeiMallGoods;
    }

    @Override
    public Boolean batchUpdateSellStatus(Long[] ids, int sellStatus) {
        return goodsMapper.batchUpdateSellStatus(ids, sellStatus) > 0;
    }

    @Override
    public PageResult searchYifeiMallGoods(PageQueryUtil pageUtil) {
        List<YifeiMallGoods> goodsList = goodsMapper.findYifeiMallGoodsListBySearch(pageUtil);
        int total = goodsMapper.getTotalYifeiMallGoodsBySearch(pageUtil);
        List<YifeiMallSearchGoodsVO> yifeiMallSearchGoodsVOS = new ArrayList<>();
        if (!CollectionUtils.isEmpty(goodsList)) {
            yifeiMallSearchGoodsVOS = BeanUtil.copyList(goodsList, YifeiMallSearchGoodsVO.class);
            for (YifeiMallSearchGoodsVO yifeiMallSearchGoodsVO : yifeiMallSearchGoodsVOS) {
                String goodsName = yifeiMallSearchGoodsVO.getGoodsName();
                String goodsIntro = yifeiMallSearchGoodsVO.getGoodsIntro();
                // 字符串过长导致文字超出的问题
                if (goodsName.length() > 28) {
                    goodsName = goodsName.substring(0, 28) + "...";
                    yifeiMallSearchGoodsVO.setGoodsName(goodsName);
                }
                if (goodsIntro.length() > 30) {
                    goodsIntro = goodsIntro.substring(0, 30) + "...";
                    yifeiMallSearchGoodsVO.setGoodsIntro(goodsIntro);
                }
            }
        }
        return new PageResult(yifeiMallSearchGoodsVOS, total, pageUtil.getLimit(), pageUtil.getPage());
    }
}
