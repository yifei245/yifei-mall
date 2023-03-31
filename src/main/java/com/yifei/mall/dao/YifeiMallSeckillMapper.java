package com.yifei.mall.dao;

import com.yifei.mall.entity.YifeiMallSeckill;
import com.yifei.mall.util.PageQueryUtil;

import java.util.List;
import java.util.Map;

public interface YifeiMallSeckillMapper {
    int deleteByPrimaryKey(Long seckillId);

    int insert(YifeiMallSeckill record);

    int insertSelective(YifeiMallSeckill record);

    YifeiMallSeckill selectByPrimaryKey(Long seckillId);

    int updateByPrimaryKeySelective(YifeiMallSeckill record);

    int updateByPrimaryKey(YifeiMallSeckill record);

    List<YifeiMallSeckill> findSeckillList(PageQueryUtil pageUtil);

    int getTotalSeckills(PageQueryUtil pageUtil);

    List<YifeiMallSeckill> findHomeSeckillList();

    int getHomeTotalSeckills(PageQueryUtil pageUtil);

    void killByProcedure(Map<String, Object> map);

    boolean addStock(Long seckillId);
}
