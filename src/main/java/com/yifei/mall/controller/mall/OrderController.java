
package com.yifei.mall.controller.mall;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.yifei.mall.annotion.RepeatSubmit;
import com.yifei.mall.common.*;
import com.yifei.mall.config.AlipayConfig;
import com.yifei.mall.config.ProjectConfig;
import com.yifei.mall.controller.vo.YifeiMallOrderDetailVO;
import com.yifei.mall.controller.vo.YifeiMallShoppingCartItemVO;
import com.yifei.mall.controller.vo.YifeiMallUserVO;
import com.yifei.mall.dao.MallUserMapper;
import com.yifei.mall.entity.YifeiMallOrder;
import com.yifei.mall.service.YifeiMallOrderService;
import com.yifei.mall.service.YifeiMallShoppingCartService;
import com.yifei.mall.util.MD5Util;
import com.yifei.mall.util.PageQueryUtil;
import com.yifei.mall.util.Result;
import com.yifei.mall.util.ResultGenerator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Controller
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private YifeiMallShoppingCartService yifeiMallShoppingCartService;
    @Autowired
    private YifeiMallOrderService yifeiMallOrderService;
    @Autowired
    private MallUserMapper mallUserMapper;
    @Autowired
    private AlipayConfig alipayConfig;

    @GetMapping("/orders/{orderNo}")
    public String orderDetailPage(HttpServletRequest request, @PathVariable("orderNo") String orderNo, HttpSession httpSession) {
        YifeiMallUserVO user = (YifeiMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        YifeiMallOrderDetailVO orderDetailVO = yifeiMallOrderService.getOrderDetailByOrderNo(orderNo, user.getUserId());
        request.setAttribute("orderDetailVO", orderDetailVO);
        return "mall/order-detail";
    }

    @GetMapping("/orders")
    public String orderListPage(@RequestParam Map<String, Object> params, HttpServletRequest request, HttpSession httpSession) {
        YifeiMallUserVO user = (YifeiMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        params.put("userId", user.getUserId());
        if (StringUtils.isEmpty((CharSequence) params.get("page"))) {
            params.put("page", 1);
        }
        params.put("limit", Constants.ORDER_SEARCH_PAGE_LIMIT);
        //封装我的订单数据
        PageQueryUtil pageUtil = new PageQueryUtil(params);
        request.setAttribute("orderPageResult", yifeiMallOrderService.getMyOrders(pageUtil));
        request.setAttribute("path", "orders");
        return "mall/my-orders";
    }

    @RepeatSubmit
    @GetMapping("/saveOrder")
    public String saveOrder(Long couponUserId, HttpSession httpSession) {
        YifeiMallUserVO user = (YifeiMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        List<YifeiMallShoppingCartItemVO> myShoppingCartItems = yifeiMallShoppingCartService.getMyShoppingCartItems(user.getUserId());
        if (StringUtils.isEmpty(user.getAddress().trim())) {
            //无收货地址
            YifeiMallException.fail(ServiceResultEnum.NULL_ADDRESS_ERROR.getResult());
        }
        if (CollectionUtils.isEmpty(myShoppingCartItems)) {
            //购物车中无数据则跳转至错误页
            YifeiMallException.fail(ServiceResultEnum.SHOPPING_ITEM_ERROR.getResult());
        }
        //保存订单并返回订单号
        String saveOrderResult = yifeiMallOrderService.saveOrder(user, couponUserId, myShoppingCartItems);
        //跳转到订单详情页
        return "redirect:/orders/" + saveOrderResult;
    }

    @RepeatSubmit
    @GetMapping("/saveSeckillOrder/{seckillSuccessId}/{userId}/{seckillSecretKey}")
    public String saveOrder(@PathVariable Long seckillSuccessId,
                            @PathVariable Long userId,
                            @PathVariable String seckillSecretKey) {
        if (seckillSecretKey == null || !seckillSecretKey.equals(MD5Util.MD5Encode(seckillSuccessId + Constants.SECKILL_ORDER_SALT, Constants.UTF_ENCODING))) {
            throw new YifeiMallException("秒杀商品下单不合法");
        }
        // 保存订单并返回订单号
        String saveOrderResult = yifeiMallOrderService.seckillSaveOrder(seckillSuccessId, userId);
        // 跳转到订单详情页
        return "redirect:/orders/" + saveOrderResult;
    }

    @RepeatSubmit
    @GetMapping("/selectPayType")
    public String selectPayType(HttpServletRequest request, @RequestParam("orderNo") String orderNo, HttpSession httpSession) {
        YifeiMallUserVO user = (YifeiMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        YifeiMallOrder yifeiMallOrder = judgeOrderUserId(orderNo, user.getUserId());
        //判断订单状态
        if (yifeiMallOrder.getOrderStatus().intValue() != YifeiMallOrderStatusEnum.ORDER_PRE_PAY.getOrderStatus()) {
            YifeiMallException.fail(ServiceResultEnum.ORDER_STATUS_ERROR.getResult());
        }
        request.setAttribute("orderNo", orderNo);
        request.setAttribute("totalPrice", yifeiMallOrder.getTotalPrice());
        return "mall/pay-select";
    }

    @RepeatSubmit
    @GetMapping("/payPage")
    public String payOrder(HttpServletRequest request, @RequestParam("orderNo") String orderNo, HttpSession httpSession, @RequestParam("payType") int payType) throws UnsupportedEncodingException {
        YifeiMallUserVO mallUserVO = (YifeiMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        Long userId = mallUserVO.getUserId();
        YifeiMallOrder yifeiMallOrder = judgeOrderUserId(orderNo, userId);
        // 判断订单userId
        if (!userId.equals(yifeiMallOrder.getUserId())) {
            YifeiMallException.fail(ServiceResultEnum.NO_PERMISSION_ERROR.getResult());
        }
        // 判断订单状态
        if (yifeiMallOrder.getOrderStatus() != YifeiMallOrderStatusEnum.ORDER_PRE_PAY.getOrderStatus()
                || yifeiMallOrder.getPayStatus() != PayStatusEnum.PAY_ING.getPayStatus()) {
            throw new YifeiMallException("订单结算异常");
        }
        request.setAttribute("orderNo", orderNo);
        request.setAttribute("totalPrice", yifeiMallOrder.getTotalPrice());
        if (payType == 1) {
            request.setCharacterEncoding(Constants.UTF_ENCODING);
            // 初始化
            AlipayClient alipayClient = new DefaultAlipayClient(alipayConfig.getGateway(), alipayConfig.getAppId(),
                    alipayConfig.getRsaPrivateKey(), alipayConfig.getFormat(), alipayConfig.getCharset(), alipayConfig.getAlipayPublicKey(),
                    alipayConfig.getSigntype());
            // 创建API对应的request
            AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
            // 在公共参数中设置回跳和通知地址,通知地址需要公网可访问
            String url = ProjectConfig.getServerUrl() + request.getContextPath();
            alipayRequest.setReturnUrl(url + "/returnOrders/" + yifeiMallOrder.getOrderNo() + "/" + userId);
            alipayRequest.setNotifyUrl(url + "/paySuccess?payType=1&orderNo=" + yifeiMallOrder.getOrderNo());

            // 填充业务参数

            // 必填
            // 商户订单号，需保证在商户端不重复
            String out_trade_no = yifeiMallOrder.getOrderNo() + new Random().nextInt(9999);
            // 销售产品码，与支付宝签约的产品码名称。目前仅支持FAST_INSTANT_TRADE_PAY
            String product_code = "FAST_INSTANT_TRADE_PAY";
            // 订单总金额，单位为元，精确到小数点后两位，取值范围[0.01,100000000]。
            String total_amount = yifeiMallOrder.getTotalPrice() + "";
            // 订单标题
            String subject = "支付宝测试";

            // 选填
            // 商品描述，可空
            String body = "商品描述";

            alipayRequest.setBizContent("{" + "\"out_trade_no\":\"" + out_trade_no + "\"," + "\"product_code\":\""
                    + product_code + "\"," + "\"total_amount\":\"" + total_amount + "\"," + "\"subject\":\"" + subject
                    + "\"," + "\"body\":\"" + body + "\"}");
            // 请求
            String form;
            try {
                // 需要自行申请支付宝的沙箱账号、申请appID，并在配置文件中依次配置AppID、密钥、公钥，否则这里会报错。
                form = alipayClient.pageExecute(alipayRequest).getBody();//调用SDK生成表单
                request.setAttribute("form", form);
            } catch (AlipayApiException e) {
                e.printStackTrace();
            }
            return "mall/alipay";
        } else {
            return "mall/wxpay";
        }
    }

    @GetMapping("/returnOrders/{orderNo}/{userId}")
    public String returnOrderDetailPage(HttpServletRequest request, @PathVariable String orderNo, @PathVariable Long userId) {
        log.info("支付宝return通知数据记录：orderNo: {}, 当前登陆用户：{}", orderNo, userId);
        // 将notifyUrl中逻辑放到此处：未支付订单更新订单状态
        YifeiMallOrderDetailVO orderDetailVO = yifeiMallOrderService.getOrderDetailByOrderNo(orderNo, userId);
        if (orderDetailVO == null) {
            return "error/error_5xx";
        }
        request.setAttribute("orderDetailVO", orderDetailVO);
        return "mall/order-detail";
    }

    @PostMapping("/paySuccess")
    @ResponseBody
    public Result paySuccess(Integer payType, String orderNo) {
        log.info("支付宝paySuccess通知数据记录：orderNo: {}, payType：{}", orderNo, payType);
        String payResult = yifeiMallOrderService.paySuccess(orderNo, payType);
        if (ServiceResultEnum.SUCCESS.getResult().equals(payResult)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(payResult);
        }
    }

    @RepeatSubmit
    @PutMapping("/orders/{orderNo}/cancel")
    @ResponseBody
    public Result cancelOrder(@PathVariable("orderNo") String orderNo, HttpSession httpSession) {
        YifeiMallUserVO user = (YifeiMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        String cancelOrderResult = yifeiMallOrderService.cancelOrder(orderNo, user.getUserId());
        if (ServiceResultEnum.SUCCESS.getResult().equals(cancelOrderResult)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(cancelOrderResult);
        }
    }

    @RepeatSubmit
    @PutMapping("/orders/{orderNo}/finish")
    @ResponseBody
    public Result finishOrder(@PathVariable("orderNo") String orderNo, HttpSession httpSession) {
        YifeiMallUserVO user = (YifeiMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        String finishOrderResult = yifeiMallOrderService.finishOrder(orderNo, user.getUserId());
        if (ServiceResultEnum.SUCCESS.getResult().equals(finishOrderResult)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(finishOrderResult);
        }
    }

    /**
     * 判断订单关联用户id和当前登陆用户是否一致
     *
     * @param orderNo 订单编号
     * @param userId  用户ID
     * @return 验证成功后返回订单对象
     */
    private YifeiMallOrder judgeOrderUserId(String orderNo, Long userId) {
        YifeiMallOrder yifeiMallOrder = yifeiMallOrderService.getYifeiMallOrderByOrderNo(orderNo);
        // 判断订单userId
        if (yifeiMallOrder == null || !yifeiMallOrder.getUserId().equals(userId)) {
            throw new YifeiMallException("当前订单用户异常");
        }
        return yifeiMallOrder;
    }
}
