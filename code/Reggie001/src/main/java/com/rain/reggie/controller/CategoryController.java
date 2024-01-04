package com.rain.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rain.reggie.common.R;
import com.rain.reggie.entity.Category;
import com.rain.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("category")
public class CategoryController {
    @Autowired
    private CategoryService service;

    // http://localhost:8080/category/page?page=1&pageSize=10
    @GetMapping("page")
    public R<Page> page(int page, int pageSize){
        log.info("page={}, pageSize={}", page, pageSize);
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getSort);
        Page<Category> info = new Page<>(page, pageSize);
        service.page(info, wrapper);
        return R.success(info);
    }

    @PostMapping
    public R<String> insert(@RequestBody Category category){
        log.info("category={}", category);
        service.save(category);
        return R.success("添加成功");
    }

    @PutMapping
    public R<String> update(@RequestBody Category category){
        log.info("category={}", category);
        service.updateById(category);
        return R.success("更新成功 ");
    }

    @DeleteMapping
    public R<String> remove(Long ids){
        log.info("category to be del: {}", ids);
        service.removeWithDish(ids);
        return R.success("删除成功");
    }
}
