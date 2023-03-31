package com.yifei.mall.controller.admin;

import com.yifei.mall.entity.YifeiMallCoupon;
import com.yifei.mall.service.YifeiMallCouponService;
import com.yifei.mall.util.PageQueryUtil;
import com.yifei.mall.util.Result;
import com.yifei.mall.util.ResultGenerator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Map;

@Controller
@RequestMapping("admin")
public class YifeiMallCouponController {

    @Autowired
    private YifeiMallCouponService yifeiMallCouponService;

    @GetMapping("/coupon")
    public String index(HttpServletRequest request) {
        request.setAttribute("path", "yifei_mall_coupon");
        return "admin/yifei_mall_coupon";
    }

    @ResponseBody
    @GetMapping("/coupon/list")
    public Result list(@RequestParam Map<String, Object> params) {
        if (StringUtils.isEmpty((CharSequence) params.get("page")) || StringUtils.isEmpty((CharSequence) params.get("limit"))) {
            return ResultGenerator.genFailResult("参数异常！");
        }
        PageQueryUtil pageUtil = new PageQueryUtil(params);
        return ResultGenerator.genSuccessResult(yifeiMallCouponService.getCouponPage(pageUtil));
    }

    /**
     * 保存
     */
    @ResponseBody
    @PostMapping("/coupon/save")
    public Result save(@RequestBody YifeiMallCoupon yifeiMallCoupon) {
        return ResultGenerator.genDmlResult(yifeiMallCouponService.saveCoupon(yifeiMallCoupon));
    }

    /**
     * 更新
     */
    @PostMapping("/coupon/update")
    @ResponseBody
    public Result update(@RequestBody YifeiMallCoupon yifeiMallCoupon) {
        yifeiMallCoupon.setUpdateTime(new Date());
        return ResultGenerator.genDmlResult(yifeiMallCouponService.updateCoupon(yifeiMallCoupon));
    }

    /**
     * 详情
     */
    @GetMapping("/coupon/{id}")
    @ResponseBody
    public Result Info(@PathVariable("id") Long id) {
        YifeiMallCoupon yifeiMallCoupon = yifeiMallCouponService.getCouponById(id);
        return ResultGenerator.genSuccessResult(yifeiMallCoupon);
    }

    /**
     * 删除
     */
    @DeleteMapping("/coupon/{id}")
    @ResponseBody
    public Result delete(@PathVariable Long id) {
        return ResultGenerator.genDmlResult(yifeiMallCouponService.deleteCouponById(id));
    }
}
