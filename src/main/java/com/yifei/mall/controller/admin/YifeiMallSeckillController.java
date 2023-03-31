package com.yifei.mall.controller.admin;

import com.yifei.mall.common.Constants;
import com.yifei.mall.entity.YifeiMallSeckill;
import com.yifei.mall.redis.RedisCache;
import com.yifei.mall.service.YifeiMallSeckillService;
import com.yifei.mall.util.PageQueryUtil;
import com.yifei.mall.util.Result;
import com.yifei.mall.util.ResultGenerator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
* @author wangfei
* @description : 秒杀
* @date : 2022/11/18 15:03
*/
@Controller
@RequestMapping("admin")
public class YifeiMallSeckillController {

    @Autowired
    private YifeiMallSeckillService yifeiMallSeckillService;
    @Autowired
    private RedisCache redisCache;

    @GetMapping("/seckill")
    public String index(HttpServletRequest request) {
        request.setAttribute("path", "yifei_mall_seckill");
        return "admin/yifei_mall_seckill";
    }

    @ResponseBody
    @GetMapping("/seckill/list")
    public Result list(@RequestParam Map<String, Object> params) {
        if (StringUtils.isEmpty((CharSequence) params.get("page")) || StringUtils.isEmpty((CharSequence) params.get("limit"))) {
            return ResultGenerator.genFailResult("参数异常！");
        }
        PageQueryUtil pageUtil = new PageQueryUtil(params);
        return ResultGenerator.genSuccessResult(yifeiMallSeckillService.getSeckillPage(pageUtil));
    }

    /**
     * 保存
     */
    @ResponseBody
    @PostMapping("/seckill/save")
    public Result save(@RequestBody YifeiMallSeckill yifeiMallSeckill) {
        if (yifeiMallSeckill == null || yifeiMallSeckill.getGoodsId() < 1 || yifeiMallSeckill.getSeckillNum() < 1 || yifeiMallSeckill.getSeckillPrice() < 1) {
            return ResultGenerator.genFailResult("参数异常");
        }
        boolean result = yifeiMallSeckillService.saveSeckill(yifeiMallSeckill);
        if (result) {
            // 虚拟库存预热
            redisCache.setCacheObject(Constants.SECKILL_GOODS_STOCK_KEY + yifeiMallSeckill.getSeckillId(), yifeiMallSeckill.getSeckillNum());
        }
        return ResultGenerator.genDmlResult(result);
    }

    /**
     * 更新
     */
    @PostMapping("/seckill/update")
    @ResponseBody
    public Result update(@RequestBody YifeiMallSeckill yifeiMallSeckill) {
        if (yifeiMallSeckill == null || yifeiMallSeckill.getSeckillId() == null || yifeiMallSeckill.getGoodsId() < 1 || yifeiMallSeckill.getSeckillNum() < 1 || yifeiMallSeckill.getSeckillPrice() < 1) {
            return ResultGenerator.genFailResult("参数异常");
        }
        boolean result = yifeiMallSeckillService.updateSeckill(yifeiMallSeckill);
        if (result) {
            // 虚拟库存预热
            redisCache.setCacheObject(Constants.SECKILL_GOODS_STOCK_KEY + yifeiMallSeckill.getSeckillId(), yifeiMallSeckill.getSeckillNum());
            redisCache.deleteObject(Constants.SECKILL_GOODS_DETAIL + yifeiMallSeckill.getSeckillId());
            redisCache.deleteObject(Constants.SECKILL_GOODS_LIST);
        }
        return ResultGenerator.genDmlResult(result);
    }

    /**
     * 详情
     */
    @GetMapping("/seckill/{id}")
    @ResponseBody
    public Result Info(@PathVariable("id") Long id) {
        YifeiMallSeckill yifeiMallSeckill = yifeiMallSeckillService.getSeckillById(id);
        return ResultGenerator.genSuccessResult(yifeiMallSeckill);
    }

    /**
     * 删除
     */
    @DeleteMapping("/seckill/{id}")
    @ResponseBody
    public Result delete(@PathVariable Long id) {
        redisCache.deleteObject(Constants.SECKILL_GOODS_DETAIL + id);
        redisCache.deleteObject(Constants.SECKILL_GOODS_LIST);
        return ResultGenerator.genDmlResult(yifeiMallSeckillService.deleteSeckillById(id));
    }
}
