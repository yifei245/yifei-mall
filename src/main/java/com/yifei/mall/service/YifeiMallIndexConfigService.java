
package com.yifei.mall.service;

import com.yifei.mall.controller.vo.YifeiMallIndexConfigGoodsVO;
import com.yifei.mall.entity.IndexConfig;
import com.yifei.mall.util.PageQueryUtil;
import com.yifei.mall.util.PageResult;

import java.util.List;

public interface YifeiMallIndexConfigService {
    /**
     * 后台分页
     *
     * @param pageUtil
     * @return
     */
    PageResult getConfigsPage(PageQueryUtil pageUtil);

    String saveIndexConfig(IndexConfig indexConfig);

    String updateIndexConfig(IndexConfig indexConfig);

    IndexConfig getIndexConfigById(Long id);

    /**
     * 返回固定数量的首页配置商品对象(首页调用)
     *
     * @param number
     * @return
     */
    List<YifeiMallIndexConfigGoodsVO> getConfigGoodsesForIndex(int configType, int number);

    Boolean deleteBatch(Long[] ids);
}
