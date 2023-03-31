
package com.yifei.mall.dao;

import com.yifei.mall.entity.YifeiMallGoods;
import com.yifei.mall.entity.StockNumDTO;
import com.yifei.mall.util.PageQueryUtil;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface YifeiMallGoodsMapper {
    int deleteByPrimaryKey(Long goodsId);

    int insert(YifeiMallGoods record);

    int insertSelective(YifeiMallGoods record);

    YifeiMallGoods selectByPrimaryKey(Long goodsId);

    YifeiMallGoods selectByCategoryIdAndName(@Param("goodsName") String goodsName, @Param("goodsCategoryId") Long goodsCategoryId);

    int updateByPrimaryKeySelective(YifeiMallGoods record);

    int updateByPrimaryKeyWithBLOBs(YifeiMallGoods record);

    int updateByPrimaryKey(YifeiMallGoods record);

    List<YifeiMallGoods> findYifeiMallGoodsList(PageQueryUtil pageUtil);

    int getTotalYifeiMallGoods(PageQueryUtil pageUtil);

    List<YifeiMallGoods> selectByPrimaryKeys(List<Long> goodsIds);

    List<YifeiMallGoods> findYifeiMallGoodsListBySearch(PageQueryUtil pageUtil);

    int getTotalYifeiMallGoodsBySearch(PageQueryUtil pageUtil);

    int batchInsert(@Param("yifeiMallGoodsList") List<YifeiMallGoods> yifeiMallGoodsList);

    int updateStockNum(@Param("stockNumDTOS") List<StockNumDTO> stockNumDTOS);

    int batchUpdateSellStatus(@Param("orderIds")Long[] orderIds,@Param("sellStatus") int sellStatus);

    boolean addStock(Long goodsId, Integer goodsCount);
}
