
package com.yifei.mall.controller.admin;

import com.yifei.mall.common.Constants;
import com.yifei.mall.common.YifeiMallCategoryLevelEnum;
import com.yifei.mall.common.YifeiMallException;
import com.yifei.mall.common.ServiceResultEnum;
import com.yifei.mall.entity.GoodsCategory;
import com.yifei.mall.entity.YifeiMallGoods;
import com.yifei.mall.service.YifeiMallCategoryService;
import com.yifei.mall.service.YifeiMallGoodsService;
import com.yifei.mall.util.PageQueryUtil;
import com.yifei.mall.util.Result;
import com.yifei.mall.util.ResultGenerator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Controller
@RequestMapping("/admin")
public class YifeiMallGoodsController {

    @Resource
    private YifeiMallGoodsService yifeiMallGoodsService;
    @Resource
    private YifeiMallCategoryService yifeiMallCategoryService;

    @GetMapping("/goods")
    public String goodsPage(HttpServletRequest request) {
        request.setAttribute("path", "yifei_mall_goods");
        return "admin/yifei_mall_goods";
    }

    @GetMapping("/goods/edit")
    public String edit(HttpServletRequest request) {
        request.setAttribute("path", "edit");
        //查询所有的一级分类
        List<GoodsCategory> firstLevelCategories = yifeiMallCategoryService.selectByLevelAndParentIdsAndNumber(Collections.singletonList(0L), YifeiMallCategoryLevelEnum.LEVEL_ONE.getLevel());
        if (!CollectionUtils.isEmpty(firstLevelCategories)) {
            //查询一级分类列表中第一个实体的所有二级分类
            List<GoodsCategory> secondLevelCategories = yifeiMallCategoryService.selectByLevelAndParentIdsAndNumber(Collections.singletonList(firstLevelCategories.get(0).getCategoryId()), YifeiMallCategoryLevelEnum.LEVEL_TWO.getLevel());
            if (!CollectionUtils.isEmpty(secondLevelCategories)) {
                //查询二级分类列表中第一个实体的所有三级分类
                List<GoodsCategory> thirdLevelCategories = yifeiMallCategoryService.selectByLevelAndParentIdsAndNumber(Collections.singletonList(secondLevelCategories.get(0).getCategoryId()), YifeiMallCategoryLevelEnum.LEVEL_THREE.getLevel());
                request.setAttribute("firstLevelCategories", firstLevelCategories);
                request.setAttribute("secondLevelCategories", secondLevelCategories);
                request.setAttribute("thirdLevelCategories", thirdLevelCategories);
                request.setAttribute("path", "goods-edit");
                request.setAttribute("content", "");
                return "admin/yifei_mall_goods_edit";
            }
        }
        YifeiMallException.fail("分类数据不完善");
        return null;
    }

    @GetMapping("/goods/edit/{goodsId}")
    public String edit(HttpServletRequest request, @PathVariable("goodsId") Long goodsId) {
        request.setAttribute("path", "edit");
        YifeiMallGoods yifeiMallGoods = yifeiMallGoodsService.getYifeiMallGoodsById(goodsId);
        if (yifeiMallGoods.getGoodsCategoryId() > 0) {
            if (yifeiMallGoods.getGoodsCategoryId() != null || yifeiMallGoods.getGoodsCategoryId() > 0) {
                //有分类字段则查询相关分类数据返回给前端以供分类的三级联动显示
                GoodsCategory currentGoodsCategory = yifeiMallCategoryService.getGoodsCategoryById(yifeiMallGoods.getGoodsCategoryId());
                //商品表中存储的分类id字段为三级分类的id，不为三级分类则是错误数据
                if (currentGoodsCategory != null && currentGoodsCategory.getCategoryLevel() == YifeiMallCategoryLevelEnum.LEVEL_THREE.getLevel()) {
                    //查询所有的一级分类
                    List<GoodsCategory> firstLevelCategories = yifeiMallCategoryService.selectByLevelAndParentIdsAndNumber(Collections.singletonList(0L), YifeiMallCategoryLevelEnum.LEVEL_ONE.getLevel());
                    //根据parentId查询当前parentId下所有的三级分类
                    List<GoodsCategory> thirdLevelCategories = yifeiMallCategoryService.selectByLevelAndParentIdsAndNumber(Collections.singletonList(currentGoodsCategory.getParentId()), YifeiMallCategoryLevelEnum.LEVEL_THREE.getLevel());
                    //查询当前三级分类的父级二级分类
                    GoodsCategory secondCategory = yifeiMallCategoryService.getGoodsCategoryById(currentGoodsCategory.getParentId());
                    if (secondCategory != null) {
                        //根据parentId查询当前parentId下所有的二级分类
                        List<GoodsCategory> secondLevelCategories = yifeiMallCategoryService.selectByLevelAndParentIdsAndNumber(Collections.singletonList(secondCategory.getParentId()), YifeiMallCategoryLevelEnum.LEVEL_TWO.getLevel());
                        //查询当前二级分类的父级一级分类
                        GoodsCategory firestCategory = yifeiMallCategoryService.getGoodsCategoryById(secondCategory.getParentId());
                        if (firestCategory != null) {
                            //所有分类数据都得到之后放到request对象中供前端读取
                            request.setAttribute("firstLevelCategories", firstLevelCategories);
                            request.setAttribute("secondLevelCategories", secondLevelCategories);
                            request.setAttribute("thirdLevelCategories", thirdLevelCategories);
                            request.setAttribute("firstLevelCategoryId", firestCategory.getCategoryId());
                            request.setAttribute("secondLevelCategoryId", secondCategory.getCategoryId());
                            request.setAttribute("thirdLevelCategoryId", currentGoodsCategory.getCategoryId());
                        }
                    }
                }
            }
        }
        if (yifeiMallGoods.getGoodsCategoryId() == 0) {
            //查询所有的一级分类
            List<GoodsCategory> firstLevelCategories = yifeiMallCategoryService.selectByLevelAndParentIdsAndNumber(Collections.singletonList(0L), YifeiMallCategoryLevelEnum.LEVEL_ONE.getLevel());
            if (!CollectionUtils.isEmpty(firstLevelCategories)) {
                //查询一级分类列表中第一个实体的所有二级分类
                List<GoodsCategory> secondLevelCategories = yifeiMallCategoryService.selectByLevelAndParentIdsAndNumber(Collections.singletonList(firstLevelCategories.get(0).getCategoryId()), YifeiMallCategoryLevelEnum.LEVEL_TWO.getLevel());
                if (!CollectionUtils.isEmpty(secondLevelCategories)) {
                    //查询二级分类列表中第一个实体的所有三级分类
                    List<GoodsCategory> thirdLevelCategories = yifeiMallCategoryService.selectByLevelAndParentIdsAndNumber(Collections.singletonList(secondLevelCategories.get(0).getCategoryId()), YifeiMallCategoryLevelEnum.LEVEL_THREE.getLevel());
                    request.setAttribute("firstLevelCategories", firstLevelCategories);
                    request.setAttribute("secondLevelCategories", secondLevelCategories);
                    request.setAttribute("thirdLevelCategories", thirdLevelCategories);
                }
            }
        }
        request.setAttribute("goods", yifeiMallGoods);
        request.setAttribute("content", yifeiMallGoods.getGoodsDetailContent());
        request.setAttribute("path", "goods-edit");
        return "admin/yifei_mall_goods_edit";
    }

    /**
     * 列表
     */
    @RequestMapping(value = "/goods/list", method = RequestMethod.GET)
    @ResponseBody
    public Result list(@RequestParam Map<String, Object> params) {
        if (StringUtils.isEmpty((CharSequence) params.get("page")) || StringUtils.isEmpty((CharSequence) params.get("limit"))) {
            return ResultGenerator.genFailResult("参数异常！");
        }
        PageQueryUtil pageUtil = new PageQueryUtil(params);
        return ResultGenerator.genSuccessResult(yifeiMallGoodsService.getYifeiMallGoodsPage(pageUtil));
    }

    /**
     * 添加
     */
    @RequestMapping(value = "/goods/save", method = RequestMethod.POST)
    @ResponseBody
    public Result save(@RequestBody YifeiMallGoods yifeiMallGoods) {
        if (StringUtils.isEmpty(yifeiMallGoods.getGoodsName())
                || StringUtils.isEmpty(yifeiMallGoods.getGoodsIntro())
                || StringUtils.isEmpty(yifeiMallGoods.getTag())
                || Objects.isNull(yifeiMallGoods.getOriginalPrice())
                || Objects.isNull(yifeiMallGoods.getGoodsCategoryId())
                || Objects.isNull(yifeiMallGoods.getSellingPrice())
                || Objects.isNull(yifeiMallGoods.getStockNum())
                || Objects.isNull(yifeiMallGoods.getGoodsSellStatus())
                || StringUtils.isEmpty(yifeiMallGoods.getGoodsCoverImg())
                || StringUtils.isEmpty(yifeiMallGoods.getGoodsDetailContent())) {
            return ResultGenerator.genFailResult("参数异常！");
        }
        String result = yifeiMallGoodsService.saveYifeiMallGoods(yifeiMallGoods);
        if (ServiceResultEnum.SUCCESS.getResult().equals(result)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(result);
        }
    }


    /**
     * 修改
     */
    @RequestMapping(value = "/goods/update", method = RequestMethod.POST)
    @ResponseBody
    public Result update(@RequestBody YifeiMallGoods yifeiMallGoods) {
        if (Objects.isNull(yifeiMallGoods.getGoodsId())
                || StringUtils.isEmpty(yifeiMallGoods.getGoodsName())
                || StringUtils.isEmpty(yifeiMallGoods.getGoodsIntro())
                || StringUtils.isEmpty(yifeiMallGoods.getTag())
                || Objects.isNull(yifeiMallGoods.getOriginalPrice())
                || Objects.isNull(yifeiMallGoods.getSellingPrice())
                || Objects.isNull(yifeiMallGoods.getGoodsCategoryId())
                || Objects.isNull(yifeiMallGoods.getStockNum())
                || Objects.isNull(yifeiMallGoods.getGoodsSellStatus())
                || StringUtils.isEmpty(yifeiMallGoods.getGoodsCoverImg())
                || StringUtils.isEmpty(yifeiMallGoods.getGoodsDetailContent())) {
            return ResultGenerator.genFailResult("参数异常！");
        }
        String result = yifeiMallGoodsService.updateYifeiMallGoods(yifeiMallGoods);
        if (ServiceResultEnum.SUCCESS.getResult().equals(result)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(result);
        }
    }

    /**
     * 详情
     */
    @GetMapping("/goods/info/{id}")
    @ResponseBody
    public Result info(@PathVariable("id") Long id) {
        YifeiMallGoods goods = yifeiMallGoodsService.getYifeiMallGoodsById(id);
        return ResultGenerator.genSuccessResult(goods);
    }

    /**
     * 批量修改销售状态
     */
    @RequestMapping(value = "/goods/status/{sellStatus}", method = RequestMethod.PUT)
    @ResponseBody
    public Result delete(@RequestBody Long[] ids, @PathVariable("sellStatus") int sellStatus) {
        if (ids.length < 1) {
            return ResultGenerator.genFailResult("参数异常！");
        }
        if (sellStatus != Constants.SELL_STATUS_UP && sellStatus != Constants.SELL_STATUS_DOWN) {
            return ResultGenerator.genFailResult("状态异常！");
        }
        if (yifeiMallGoodsService.batchUpdateSellStatus(ids, sellStatus)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult("修改失败");
        }
    }

}
