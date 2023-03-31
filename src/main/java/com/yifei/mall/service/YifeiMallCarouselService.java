
package com.yifei.mall.service;

import com.yifei.mall.controller.vo.YifeiMallIndexCarouselVO;
import com.yifei.mall.entity.Carousel;
import com.yifei.mall.util.PageQueryUtil;
import com.yifei.mall.util.PageResult;

import java.util.List;

public interface YifeiMallCarouselService {
    /**
     * 后台分页
     *
     * @param pageUtil
     * @return
     */
    PageResult getCarouselPage(PageQueryUtil pageUtil);

    String saveCarousel(Carousel carousel);

    String updateCarousel(Carousel carousel);

    Carousel getCarouselById(Integer id);

    Boolean deleteBatch(Integer[] ids);

    /**
     * 返回固定数量的轮播图对象(首页调用)
     *
     * @param number
     * @return
     */
    List<YifeiMallIndexCarouselVO> getCarouselsForIndex(int number);
}
