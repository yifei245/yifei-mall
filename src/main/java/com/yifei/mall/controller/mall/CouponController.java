package com.yifei.mall.controller.mall;

import com.yifei.mall.common.Constants;
import com.yifei.mall.controller.vo.YifeiMallCouponVO;
import com.yifei.mall.controller.vo.YifeiMallUserVO;
import com.yifei.mall.service.YifeiMallCouponService;
import com.yifei.mall.util.Result;
import com.yifei.mall.util.ResultGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class CouponController {

    @Autowired
    private YifeiMallCouponService yifeiMallCouponService;

    @GetMapping("/couponList")
    public String couponList(HttpServletRequest request, HttpSession session) {
        Long userId = null;
        if (session.getAttribute(Constants.MALL_USER_SESSION_KEY) != null) {
            userId = ((YifeiMallUserVO) request.getSession().getAttribute(Constants.MALL_USER_SESSION_KEY)).getUserId();
        }
        List<YifeiMallCouponVO> coupons = yifeiMallCouponService.selectAvailableCoupon(userId);
        request.setAttribute("coupons", coupons);
        return "mall/coupon-list";
    }

    @GetMapping("/myCoupons")
    public String myCoupons(HttpServletRequest request, HttpSession session) {
        YifeiMallUserVO userVO = (YifeiMallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        List<YifeiMallCouponVO> coupons = yifeiMallCouponService.selectMyCoupons(userVO.getUserId());
        request.setAttribute("myCoupons", coupons);
        request.setAttribute("path", "myCoupons");
        return "mall/my-coupons";
    }

    @ResponseBody
    @PostMapping("coupon/{couponId}")
    public Result save(@PathVariable Long couponId, HttpSession session) {
        YifeiMallUserVO userVO = (YifeiMallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        return ResultGenerator.genDmlResult(yifeiMallCouponService.saveCouponUser(couponId, userVO.getUserId()));
    }

    @ResponseBody
    @DeleteMapping("coupon/{couponUserId}")
    public Result delete(@PathVariable Long couponUserId) {
        return ResultGenerator.genDmlResult(yifeiMallCouponService.deleteCouponUser(couponUserId));
    }
}
