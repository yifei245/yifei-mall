
package com.yifei.mall.controller.mall;

import com.yifei.mall.common.Constants;
import com.yifei.mall.common.IndexConfigTypeEnum;
import com.yifei.mall.common.YifeiMallException;
import com.yifei.mall.controller.vo.YifeiMallIndexCarouselVO;
import com.yifei.mall.controller.vo.YifeiMallIndexCategoryVO;
import com.yifei.mall.controller.vo.YifeiMallIndexConfigGoodsVO;
import com.yifei.mall.service.YifeiMallCarouselService;
import com.yifei.mall.service.YifeiMallCategoryService;
import com.yifei.mall.service.YifeiMallIndexConfigService;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class IndexController {

    @Resource
    private YifeiMallCarouselService yifeiMallCarouselService;

    @Resource
    private YifeiMallIndexConfigService yifeiMallIndexConfigService;

    @Resource
    private YifeiMallCategoryService yifeiMallCategoryService;

    @GetMapping({"/index", "/", "/index.html"})
    public String indexPage(HttpServletRequest request) {
        List<YifeiMallIndexCategoryVO> categories = yifeiMallCategoryService.getCategoriesForIndex();
        if (CollectionUtils.isEmpty(categories)) {
            YifeiMallException.fail("分类数据不完善");
        }
        List<YifeiMallIndexCarouselVO> carousels = yifeiMallCarouselService.getCarouselsForIndex(Constants.INDEX_CAROUSEL_NUMBER);
        List<YifeiMallIndexConfigGoodsVO> hotGoodses = yifeiMallIndexConfigService.getConfigGoodsesForIndex(IndexConfigTypeEnum.INDEX_GOODS_HOT.getType(), Constants.INDEX_GOODS_HOT_NUMBER);
        List<YifeiMallIndexConfigGoodsVO> newGoodses = yifeiMallIndexConfigService.getConfigGoodsesForIndex(IndexConfigTypeEnum.INDEX_GOODS_NEW.getType(), Constants.INDEX_GOODS_NEW_NUMBER);
        List<YifeiMallIndexConfigGoodsVO> recommendGoodses = yifeiMallIndexConfigService.getConfigGoodsesForIndex(IndexConfigTypeEnum.INDEX_GOODS_RECOMMOND.getType(), Constants.INDEX_GOODS_RECOMMOND_NUMBER);
        request.setAttribute("categories", categories);//分类数据
        request.setAttribute("carousels", carousels);//轮播图
        request.setAttribute("hotGoodses", hotGoodses);//热销商品
        request.setAttribute("newGoodses", newGoodses);//新品
        request.setAttribute("recommendGoodses", recommendGoodses);//推荐商品
        return "mall/index";
    }
}
