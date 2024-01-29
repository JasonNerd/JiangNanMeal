package com.rain.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rain.reggie.common.BaseContext;
import com.rain.reggie.common.BusinessException;
import com.rain.reggie.entity.OrderDetail;
import com.rain.reggie.entity.ShoppingCart;
import com.rain.reggie.mapper.OrderDetailMapper;
import com.rain.reggie.service.OrderDetailService;
import com.rain.reggie.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {
    @Autowired
    private ShoppingCartService shoppingCartService;

    @Transactional
    @Override
    public Integer saveCurrentShoppingCart(Long orderId) {
        // 将当前用户购物车内的套餐或菜品全部存入订单明细表中, 并与订单号 orderId 关联
        // 返回购物车内商品总金额 amount, 同时清除购物车.
        AtomicInteger amount = new AtomicInteger(0);
        Long userId = BaseContext.getId();

        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(wrapper);
        if (shoppingCarts==null || shoppingCarts.size()==0)
            throw new BusinessException("购物车为空, 无法下单");

        for (ShoppingCart cart: shoppingCarts){
            OrderDetail detail = new OrderDetail();
            BeanUtils.copyProperties(cart, detail);
            detail.setOrderId(orderId);
            amount.addAndGet(detail.getAmount().multiply(new BigDecimal(detail.getNumber())).intValue());
            this.save(detail);
        }

        shoppingCartService.remove(wrapper);
        return amount.get();
    }
}
