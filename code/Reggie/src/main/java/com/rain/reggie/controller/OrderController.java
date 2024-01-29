package com.rain.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rain.reggie.common.BaseContext;
import com.rain.reggie.common.BusinessException;
import com.rain.reggie.common.R;
import com.rain.reggie.entity.AddressBook;
import com.rain.reggie.entity.Orders;
import com.rain.reggie.entity.ShoppingCart;
import com.rain.reggie.entity.User;
import com.rain.reggie.service.AddressBookService;
import com.rain.reggie.service.OrdersService;
import com.rain.reggie.service.ShoppingCartService;
import com.rain.reggie.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("order")
@Slf4j
public class OrderController {
    @Autowired
    private OrdersService ordersService;

    @PostMapping("submit")
    public R<String> submit(@RequestBody Orders orders) {
        log.info("用户下单: {}", orders);
        ordersService.submit(orders);
        return R.success("下单成功");
    }

    @GetMapping("userPage")
    public R<Page> orderList(int page, int pageSize){
        log.info("查看历史订单");
        Page<Orders> oderPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getUserId, BaseContext.getId());
        wrapper.orderByDesc(Orders::getOrderTime);
        ordersService.page(oderPage, wrapper);
        return R.success(oderPage);
    }
}