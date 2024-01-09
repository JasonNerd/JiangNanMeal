package com.rain.reggie.dto;

import com.rain.reggie.entity.Setmeal;
import com.rain.reggie.entity.SetmealDish;
import lombok.Data;

import java.util.List;

@Data
public class SetmealDto extends Setmeal {
    private List<SetmealDish> setmealDishes;
    private String categoryName;
}
