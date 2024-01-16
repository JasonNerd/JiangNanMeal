package com.rain.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rain.reggie.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {
}
