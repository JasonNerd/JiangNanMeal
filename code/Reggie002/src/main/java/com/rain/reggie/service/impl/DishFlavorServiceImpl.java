package com.rain.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rain.reggie.entity.DishFlavor;
import com.rain.reggie.mapper.FlavorMapper;
import com.rain.reggie.service.FlavorService;
import org.springframework.stereotype.Service;

@Service
public class DishFlavorServiceImpl extends ServiceImpl<FlavorMapper, DishFlavor> implements FlavorService {
}
