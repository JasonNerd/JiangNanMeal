package com.rain.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rain.reggie.common.BusinessException;
import com.rain.reggie.entity.Category;
import com.rain.reggie.entity.Dish;
import com.rain.reggie.mapper.CategoryMapper;
import com.rain.reggie.service.CategoryService;
import com.rain.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;

    @Override
    @Transactional
    public void removeWithDish(Long ids) {
        LambdaQueryWrapper<Dish> dishWrapper = new LambdaQueryWrapper<>();
        dishWrapper.eq(Dish::getCategoryId, ids);
        long cnt = dishService.count(dishWrapper);
        if (cnt > 0)
            throw new BusinessException("分类冲突");
        removeById(ids);
    }
}
