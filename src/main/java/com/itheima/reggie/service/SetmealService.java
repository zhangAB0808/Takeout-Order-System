package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    /**
     * 新增套餐
     */
    void saveWithDish(SetmealDto setmealDto);

    //删除套餐，同时删除套餐和菜品的关联数据
    void removeWithDish(List<Long> ids);

    //根据id查询套餐和套餐菜品关联信息，返回setmealDto，修改时套餐时回显
    SetmealDto getSetmeal(Long id);

    //修改套餐和菜品关联数据
    void updateSetmeal(SetmealDto setmealDto);
}
