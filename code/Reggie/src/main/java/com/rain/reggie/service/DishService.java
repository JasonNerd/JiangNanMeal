package com.rain.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rain.reggie.dto.DishDto;
import com.rain.reggie.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {
    void addWithFlavor(DishDto dishDto);

    DishDto getDishWithFlavor(Long dishId);

    void updateWithFlavor(DishDto dishDto);

    void updateStatusBatch(Integer dishStatus, List<Long> dishIds);
}
