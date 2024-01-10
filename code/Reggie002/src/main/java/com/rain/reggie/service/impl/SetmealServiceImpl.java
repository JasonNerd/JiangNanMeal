package com.rain.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rain.reggie.common.BusinessException;
import com.rain.reggie.dto.SetmealDto;
import com.rain.reggie.entity.Dish;
import com.rain.reggie.entity.Setmeal;
import com.rain.reggie.entity.SetmealDish;
import com.rain.reggie.mapper.SetmealMapper;
import com.rain.reggie.service.DishService;
import com.rain.reggie.service.SetmealDishService;
import com.rain.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealDishService setMealDishService;

    @Autowired
    private DishService dishService;

    @Transactional
    @Override
    public void addWithDish(SetmealDto setMealDto) {
        // 1. 将数据插入到 set meal 表, 注意 code 字段未填充
        this.save(setMealDto);
        // 2. 将 set meal Dishes 数据依次插入到 SetMealDish 表, 注意字段填充
        List<SetmealDish> setmealDishes = setMealDto.getSetmealDishes();
        setmealDishes = setmealDishes.stream().map((item)->{
            item.setSetmealId(setMealDto.getId());
            return item;
        }).collect(Collectors.toList());
        setMealDishService.saveBatch(setmealDishes);
    }

    @Override
    public SetmealDto getSetmealWithDish(Long setMealId) {
        Setmeal meal = this.getById(setMealId);
        SetmealDto dto = new SetmealDto();
        BeanUtils.copyProperties(meal, dto);

        LambdaQueryWrapper<SetmealDish> dishWrapper = new LambdaQueryWrapper<>();
        dishWrapper.eq(SetmealDish::getSetmealId, setMealId);
        dishWrapper.orderByAsc(SetmealDish::getSort).orderByDesc(SetmealDish::getUpdateTime);
        List<SetmealDish> setmealDishList = setMealDishService.list(dishWrapper);

        dto.setSetmealDishes(setmealDishList);

        return dto;
    }

    @Transactional
    @Override
    public void updateWithMealDishes(SetmealDto dto) {
        this.updateById(dto);   // 修改基本信息
        // 修改套餐-菜品关联信息: 先删再加
        List<SetmealDish> dishList = dto.getSetmealDishes();
        LambdaQueryWrapper<SetmealDish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SetmealDish::getSetmealId, dto.getId());
        setMealDishService.remove(wrapper);
        dishList = dishList.stream().map((item)->{
            item.setSetmealId(dto.getId());
            return item;
        }).collect(Collectors.toList());
        setMealDishService.saveBatch(dishList);
    }

    @Transactional
    @Override
    public void updateStatusBatch(Integer st, List<Long> setmealIdList) {
        // 批量停售/启售套餐
        LambdaQueryWrapper<SetmealDish> setmealDishWrapper = new LambdaQueryWrapper<>();
        // 注意启售套餐时, 套餐内不可有停售的菜品
        List<Setmeal> setmealList = setmealIdList.stream().map((item) -> {
            Setmeal setmeal = this.getById(item);
            setmeal.setStatus(st);
            if (st == 1) {
                setmealDishWrapper.eq(SetmealDish::getSetmealId, setmeal.getId());
                List<SetmealDish> dishList = setMealDishService.list(setmealDishWrapper);
                for (SetmealDish setmealDish : dishList) {
                    Dish dish = dishService.getById(setmealDish.getDishId());
                    if (dish.getStatus() == 0)
                        throw new BusinessException("套餐内" + dish.getName() + "处于停售状态, 套餐无法启售");
                }
            }
            return setmeal;
        }).toList();
        this.updateBatchById(setmealList);
    }


}
