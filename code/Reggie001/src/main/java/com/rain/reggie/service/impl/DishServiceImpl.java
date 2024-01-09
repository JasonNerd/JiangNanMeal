package com.rain.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rain.reggie.dto.DishDto;
import com.rain.reggie.entity.Category;
import com.rain.reggie.entity.Dish;
import com.rain.reggie.entity.DishFlavor;
import com.rain.reggie.mapper.DishMapper;
import com.rain.reggie.service.DishService;
import com.rain.reggie.service.FlavorService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    FlavorService flavorService;

    @Override
    public void addWithFlavor(DishDto dishDto) {
        this.save(dishDto);
        Long dishId = dishDto.getId();
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map(
                (item)->{
                    item.setDishId(dishId);
                    return item;
                }
        ).collect(Collectors.toList());
        flavorService.saveBatch(flavors);
    }

    @Override
    public DishDto getDishWithFlavor(Long dishId) {
        DishDto dishDto = new DishDto();
        Dish dish = this.getById(dishId);
        BeanUtils.copyProperties(dish, dishDto);

        LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DishFlavor::getDishId, dishId);
        List<DishFlavor> flavors = flavorService.list(wrapper);
        dishDto.setFlavors(flavors);

        return dishDto;
    }

    @Override
    public void updateWithFlavor(DishDto dishDto) {
        this.updateById(dishDto);
        LambdaQueryWrapper<DishFlavor> flavorWrapper = new LambdaQueryWrapper<>();
        flavorWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        flavorService.remove(flavorWrapper);
        Long dishId = dishDto.getId();
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map(
                (item)->{
                    item.setDishId(dishId);
                    return item;
                }
        ).collect(Collectors.toList());
        flavorService.saveBatch(flavors);
    }

    @Override
    public void updateStatusBatch(Integer dishStatus, List<Long> dishIds) {
        List<Dish> dishes = this.listByIds(dishIds);
        dishes = dishes.stream().map((item)->{
            item.setStatus(dishStatus);
            return item;
        }).toList();
        this.updateBatchById(dishes);
    }
}
