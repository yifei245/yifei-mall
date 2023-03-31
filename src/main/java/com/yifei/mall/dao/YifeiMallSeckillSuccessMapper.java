package com.yifei.mall.dao;

import com.yifei.mall.entity.YifeiMallSeckillSuccess;

public interface YifeiMallSeckillSuccessMapper {
    int deleteByPrimaryKey(Integer secId);

    int insert(YifeiMallSeckillSuccess record);

    int insertSelective(YifeiMallSeckillSuccess record);

    YifeiMallSeckillSuccess selectByPrimaryKey(Long secId);

    int updateByPrimaryKeySelective(YifeiMallSeckillSuccess record);

    int updateByPrimaryKey(YifeiMallSeckillSuccess record);

    YifeiMallSeckillSuccess getSeckillSuccessByUserIdAndSeckillId(Long userId, Long seckillId);
}
