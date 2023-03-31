package com.yifei.mall.service;

import com.yifei.mall.controller.vo.ExposerVO;
import com.yifei.mall.controller.vo.SeckillSuccessVO;
import com.yifei.mall.entity.YifeiMallSeckill;
import com.yifei.mall.util.PageQueryUtil;
import com.yifei.mall.util.PageResult;

import java.util.List;

public interface YifeiMallSeckillService {

    PageResult getSeckillPage(PageQueryUtil pageUtil);

    boolean saveSeckill(YifeiMallSeckill yifeiMallSeckill);

    boolean updateSeckill(YifeiMallSeckill yifeiMallSeckill);

    YifeiMallSeckill getSeckillById(Long id);

    boolean deleteSeckillById(Long id);

    List<YifeiMallSeckill> getHomeSeckillPage();

    ExposerVO exposerUrl(Long seckillId);

    SeckillSuccessVO executeSeckill(Long seckillId, Long userId);
}
