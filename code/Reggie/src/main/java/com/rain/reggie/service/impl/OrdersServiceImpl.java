package com.rain.reggie.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rain.reggie.common.BaseContext;
import com.rain.reggie.common.BusinessException;
import com.rain.reggie.entity.AddressBook;
import com.rain.reggie.entity.Orders;
import com.rain.reggie.entity.User;
import com.rain.reggie.mapper.OrdersMapper;
import com.rain.reggie.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private AddressBookService addressBookService;
    @Autowired
    private UserService userService;
    @Override
    public void submit(Orders orders) {
        Long userId = BaseContext.getId();
        User user = userService.getById(userId);

        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);
        if (addressBook == null)
            throw new BusinessException("地址信息有误, 无法下单");

        Long orderId = IdWorker.getId();
        orders.setId(orderId);
        int amount = orderDetailService.saveCurrentShoppingCart(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount));
        orders.setUserId(userId);
        orders.setNumber(String.valueOf(orderId));
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress(
                (addressBook.getProvinceName() == null ? "": addressBook.getProvinceName()) +
                        (addressBook.getCityName() == null ? "": addressBook.getCityName()) +
                        (addressBook.getDistrictName() == null ? "": addressBook.getDistrictName()) +
                        (addressBook.getDetail() == null ? "": addressBook.getDetail())
        );
        this.save(orders);
    }
}
