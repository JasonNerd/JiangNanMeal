package com.rain.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rain.reggie.entity.OrderDetail;

public interface OrderDetailService extends IService<OrderDetail> {
    public Integer saveCurrentShoppingCart(Long orderId);
}
