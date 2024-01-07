package com.rain.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rain.reggie.dto.DishDto;
import com.rain.reggie.entity.Dish;

public interface DishService extends IService<Dish> {
    void addWithFlavor(DishDto dishDto);

    DishDto getDishWithFlavor(Long dishId);

    void updateWithFlavor(DishDto dishDto);
}
