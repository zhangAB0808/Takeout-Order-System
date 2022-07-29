package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {

    //新增菜品，同时新增插入对应的口味数据
    void saveWithFlavor(DishDto dishDto);

    //根据id查询菜品和口味信息,菜品回显
    DishDto getDishWithFlavor(Long id);

    //修改菜品和口味数据
    void updateDishWithFlavor(DishDto dishDto);


}
