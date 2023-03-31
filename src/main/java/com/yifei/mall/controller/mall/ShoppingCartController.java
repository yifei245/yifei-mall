
package com.yifei.mall.controller.mall;

import com.yifei.mall.common.Constants;
import com.yifei.mall.common.YifeiMallException;
import com.yifei.mall.common.ServiceResultEnum;
import com.yifei.mall.controller.vo.YifeiMallMyCouponVO;
import com.yifei.mall.controller.vo.YifeiMallShoppingCartItemVO;
import com.yifei.mall.controller.vo.YifeiMallUserVO;
import com.yifei.mall.entity.YifeiMallShoppingCartItem;
import com.yifei.mall.service.YifeiMallCouponService;
import com.yifei.mall.service.YifeiMallShoppingCartService;
import com.yifei.mall.util.Result;
import com.yifei.mall.util.ResultGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class ShoppingCartController {

    @Resource
    private YifeiMallShoppingCartService yifeiMallShoppingCartService;

    @Autowired
    private YifeiMallCouponService yifeiMallCouponService;

    @GetMapping("/shop-cart")
    public String cartListPage(HttpServletRequest request,
                               HttpSession httpSession) {
        YifeiMallUserVO user = (YifeiMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        int itemsTotal = 0;
        int priceTotal = 0;
        List<YifeiMallShoppingCartItemVO> myShoppingCartItems = yifeiMallShoppingCartService.getMyShoppingCartItems(user.getUserId());
        if (!CollectionUtils.isEmpty(myShoppingCartItems)) {
            //购物项总数
            itemsTotal = myShoppingCartItems.stream().mapToInt(YifeiMallShoppingCartItemVO::getGoodsCount).sum();
            if (itemsTotal < 1) {
                YifeiMallException.fail("购物项不能为空");
            }
            //总价
            for (YifeiMallShoppingCartItemVO yifeiMallShoppingCartItemVO : myShoppingCartItems) {
                priceTotal += yifeiMallShoppingCartItemVO.getGoodsCount() * yifeiMallShoppingCartItemVO.getSellingPrice();
            }
            if (priceTotal < 1) {
                YifeiMallException.fail("购物项价格异常");
            }
        }
        request.setAttribute("itemsTotal", itemsTotal);
        request.setAttribute("priceTotal", priceTotal);
        request.setAttribute("myShoppingCartItems", myShoppingCartItems);
        return "mall/cart";
    }

    @PostMapping("/shop-cart")
    @ResponseBody
    public Result saveYifeiMallShoppingCartItem(@RequestBody YifeiMallShoppingCartItem yifeiMallShoppingCartItem,
                                                 HttpSession httpSession) {
        YifeiMallUserVO user = (YifeiMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        yifeiMallShoppingCartItem.setUserId(user.getUserId());
        String saveResult = yifeiMallShoppingCartService.saveYifeiMallCartItem(yifeiMallShoppingCartItem);
        //添加成功
        if (ServiceResultEnum.SUCCESS.getResult().equals(saveResult)) {
            return ResultGenerator.genSuccessResult();
        }
        //添加失败
        return ResultGenerator.genFailResult(saveResult);
    }

    @PutMapping("/shop-cart")
    @ResponseBody
    public Result updateYifeiMallShoppingCartItem(@RequestBody YifeiMallShoppingCartItem yifeiMallShoppingCartItem,
                                                   HttpSession httpSession) {
        YifeiMallUserVO user = (YifeiMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        yifeiMallShoppingCartItem.setUserId(user.getUserId());
        String updateResult = yifeiMallShoppingCartService.updateYifeiMallCartItem(yifeiMallShoppingCartItem);
        //修改成功
        if (ServiceResultEnum.SUCCESS.getResult().equals(updateResult)) {
            return ResultGenerator.genSuccessResult();
        }
        //修改失败
        return ResultGenerator.genFailResult(updateResult);
    }

    @DeleteMapping("/shop-cart/{yifeiMallShoppingCartItemId}")
    @ResponseBody
    public Result updateYifeiMallShoppingCartItem(@PathVariable("yifeiMallShoppingCartItemId") Long yifeiMallShoppingCartItemId,
                                                   HttpSession httpSession) {
        YifeiMallUserVO user = (YifeiMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        Boolean deleteResult = yifeiMallShoppingCartService.deleteById(yifeiMallShoppingCartItemId, user.getUserId());
        //删除成功
        if (deleteResult) {
            return ResultGenerator.genSuccessResult();
        }
        //删除失败
        return ResultGenerator.genFailResult(ServiceResultEnum.OPERATE_ERROR.getResult());
    }

    @GetMapping("/shop-cart/settle")
    public String settlePage(HttpServletRequest request,
                             HttpSession httpSession) {
        int priceTotal = 0;
        YifeiMallUserVO user = (YifeiMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        List<YifeiMallShoppingCartItemVO> myShoppingCartItems = yifeiMallShoppingCartService.getMyShoppingCartItems(user.getUserId());
        if (CollectionUtils.isEmpty(myShoppingCartItems)) {
            //无数据则不跳转至结算页
            return "/shop-cart";
        } else {
            //总价
            for (YifeiMallShoppingCartItemVO yifeiMallShoppingCartItemVO : myShoppingCartItems) {
                priceTotal += yifeiMallShoppingCartItemVO.getGoodsCount() * yifeiMallShoppingCartItemVO.getSellingPrice();
            }
            if (priceTotal < 1) {
                YifeiMallException.fail("购物项价格异常");
            }
        }
        List<YifeiMallMyCouponVO> myCouponVOS = yifeiMallCouponService.selectOrderCanUseCoupons(myShoppingCartItems, priceTotal, user.getUserId());
        request.setAttribute("coupons", myCouponVOS);
        request.setAttribute("priceTotal", priceTotal);
        request.setAttribute("myShoppingCartItems", myShoppingCartItems);
        return "mall/order-settle";
    }
}
