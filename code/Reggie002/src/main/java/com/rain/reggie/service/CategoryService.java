package com.rain.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rain.reggie.entity.Category;

public interface CategoryService extends IService<Category> {
    void removeWithDish(Long ids);
}
