package com.rain.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rain.reggie.dto.SetmealDto;
import com.rain.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    void addWithDish(SetmealDto setMealDto);

    SetmealDto getSetmealWithDish(Long setMealId);

    void updateWithMealDishes(SetmealDto dto);

    void updateStatusBatch(Integer st, List<Long> setmealIdList);
}
