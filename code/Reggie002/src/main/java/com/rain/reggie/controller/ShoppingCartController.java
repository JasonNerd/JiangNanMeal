package com.rain.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rain.reggie.common.R;
import com.rain.reggie.entity.ShoppingCart;
import com.rain.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("shoppingCart")
@Slf4j
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService cartService;

    @GetMapping("list")
    public R<List<ShoppingCart>> list(HttpServletRequest request){
        Long userId = (Long) request.getSession().getAttribute("user");
        log.info("查询当前用户的购物车信息: {}", userId);
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId);
        return R.success(cartService.list(wrapper));
    }

    @PostMapping("add")
    public R<ShoppingCart> add(HttpServletRequest request, @RequestBody ShoppingCart shoppingCart){
        log.info("添加菜品/套餐到购物车: {}", shoppingCart);
        Long userId = (Long) request.getSession().getAttribute("user");
        Long setmealId = shoppingCart.getSetmealId();
        Long dishId = shoppingCart.getDishId();
        shoppingCart.setUserId(userId);
        // 注意需要查询当前菜品是否已经在购物车
        LambdaQueryWrapper<ShoppingCart> shopWrapper = new LambdaQueryWrapper<>();
        shopWrapper.eq(ShoppingCart::getUserId, userId);
        shopWrapper.eq(setmealId!=null, ShoppingCart::getSetmealId, setmealId);
        shopWrapper.eq(dishId!=null, ShoppingCart::getDishId, dishId);
        ShoppingCart cart = cartService.getOne(shopWrapper);
        if (cart == null){
            shoppingCart.setNumber(1);
            cartService.save(shoppingCart);
            return R.success(shoppingCart);
        }else {
            cart.setNumber(cart.getNumber()+1);
            cartService.updateById(cart);
            return R.success(cart);
        }
    }
}
