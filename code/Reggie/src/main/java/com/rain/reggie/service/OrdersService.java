package com.rain.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rain.reggie.entity.Orders;

public interface OrdersService extends IService<Orders> {
    void submit(Orders orders);
}
