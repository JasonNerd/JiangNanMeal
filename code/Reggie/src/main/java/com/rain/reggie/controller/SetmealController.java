package com.rain.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rain.reggie.common.R;
import com.rain.reggie.dto.SetmealDto;
import com.rain.reggie.entity.Category;
import com.rain.reggie.entity.Setmeal;
import com.rain.reggie.service.CategoryService;
import com.rain.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService mealService;
    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public R<String> insert(@RequestBody SetmealDto setMealDto){
        log.info("待添加的套餐: {}", setMealDto);
        mealService.addWithDish(setMealDto);
        return R.success("套餐添加成功");
    }

    @GetMapping("page")
    public R<Page> page(int page, int pageSize, String name){
        log.info("page={}, pageSize={}, name={}", page, pageSize, name);
        Page<Setmeal> setmealPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Setmeal> setmealWrapper = new LambdaQueryWrapper<>();
        setmealWrapper.like(name!=null, Setmeal::getName, name);
        setmealWrapper.orderByDesc(Setmeal::getUpdateTime);
        mealService.page(setmealPage, setmealWrapper);

        Page<SetmealDto> dtoPage = new Page<>();
        BeanUtils.copyProperties(setmealPage, dtoPage, "records");
        List<Setmeal> setmealList = setmealPage.getRecords();
        dtoPage.setRecords(setmealList.stream().map((item)->{
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);
            Category category = categoryService.getById(setmealDto.getCategoryId());
            setmealDto.setCategoryName(category.getName());
            return setmealDto;
        }).collect(Collectors.toList()));

        return R.success(dtoPage);
    }

    @GetMapping("/{setMealId}")
    public R<SetmealDto> getById(@PathVariable Long setMealId){
        log.info("依据套餐ID查询基本信息以及套餐包含的菜品: {}", setMealId);
        SetmealDto dto = mealService.getSetmealWithDish(setMealId);
        return R.success(dto);
    }

    @PutMapping
    public R<String> update(@RequestBody SetmealDto dto){
        log.info("修改套餐信息: {}", dto);
        mealService.updateWithMealDishes(dto);
        return R.success("修改成功");
    }

    @PostMapping("/status/{st}")
    public R<String> updateStatusBatch(@PathVariable Integer st, String ids){
        List<Long> setmealIdList = Arrays.stream(ids.split(",")).map(Long::parseLong).toList();
        mealService.updateStatusBatch(st, setmealIdList);
        return R.success("状态修改成功");
    }

    @GetMapping("list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        log.info("依据套餐分类查询套餐: categoryId={}", setmeal.getCategoryId());
        LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Setmeal::getCategoryId, setmeal.getCategoryId());
        wrapper.eq(Setmeal::getStatus, setmeal.getStatus());
        return R.success(mealService.list(wrapper));
    }
}
