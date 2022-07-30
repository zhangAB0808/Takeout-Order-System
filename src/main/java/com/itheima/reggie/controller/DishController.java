package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
public class DishController {

    @Resource
    private DishService dishService;
    @Resource
    private DishFlavorService dishFlavorService;
    @Resource
    private CategoryService categoryService;
    @Resource
    private RedisTemplate redisTemplate;
//    @Resource
//    private DishMapper dishMapper;

    //测试mybatis手写分页
//    @GetMapping("/pageSql")
//    public List<Dish> pageSql(){
//        return   dishMapper.pageSql();
//    }

    /**
     * 新增菜品,同时新增插入对应的口味数据
     *
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        dishService.saveWithFlavor(dishDto);
        //清理所有缓存数据
        //Set keys = redisTemplate.keys("dish_");
        //redisTemplate.delete(keys);

        //清理某个菜品下面的缓存数据
        String key = "dish_"+dishDto.getCategoryId()+"_1";
        redisTemplate.delete(key);

        return R.success("新增菜品成功");
    }


    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        Page<Dish> dishPage = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null, Dish::getName, name);
        queryWrapper.orderByDesc(Dish::getCreateTime);
        dishService.page(dishPage, queryWrapper);

        BeanUtils.copyProperties(dishPage, dishDtoPage, "records");
        List<Dish> dishRecords = dishPage.getRecords();

        //创建 一个新的DishDto集合，存储封装后的返回数据
        List<DishDto> dishDtoList = new ArrayList<>();

        //将dishDto的records单独处理，dish的基本属性拷贝过去，CategoryName进行查询封装，就ok了
        for (Dish dish : dishRecords) {
            //将dish的基本属性拷贝过去
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(dish, dishDto);

            //根据CategoryId查询categoryName.
            Long categoryId = dish.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName();
                //进行封装
                dishDto.setCategoryName(categoryName);
            }
            dishDtoList.add(dishDto);
        }

        dishDtoPage.setRecords(dishDtoList);
        return R.success(dishDtoPage);
    }

    /**
     * 根据id查询菜品和口味信息，返回DishDto，修改时菜品回显
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id) {
        DishDto dishDto = dishService.getDishWithFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 修改菜品和口味数据
     *
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        dishService.updateDishWithFlavor(dishDto);

        //清理所有缓存数据
        //Set keys = redisTemplate.keys("dish_");
        //redisTemplate.delete(keys);

        //清理某个菜品下面的缓存数据
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);
        return R.success("修改菜品成功");
    }

    /**
     * 根据ids删除菜品和口味信息
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    @Transactional
    public R<String> delete(Long[] ids) {
        //判断菜品是否在出售状态，是否能删除
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Dish::getId, ids);
        queryWrapper.eq(Dish::getStatus, 1);
        int count = dishService.count(queryWrapper);
        if (count > 0) {
            throw new CustomException("菜品在出售中，不能删除");
        }

        dishService.removeByIds(Arrays.asList(ids));
        //删除口味信息
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(DishFlavor::getDishId, ids);
        dishFlavorService.remove(lambdaQueryWrapper);
        //清理所有缓存数据
        Set keys = redisTemplate.keys("dish_");
        redisTemplate.delete(keys);

        return R.success("菜品删除成功");
    }

    /**
     * 修改菜品状态
     *
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable int status, Long[] ids) {
        ArrayList<Dish> list = new ArrayList<>();
        for (Long id : ids) {
            Dish dish = new Dish();
            dish.setId(id);
            dish.setStatus(status);
            list.add(dish);
        }
        //清理所有缓存数据
        Set keys = redisTemplate.keys("dish_");
        redisTemplate.delete(keys);

        dishService.updateBatchById(list);
        return R.success("菜品状态修改成功");
    }

    /**
     * 根据条件查询菜品
     *
     * @param dish
     * @return
     */
  /*  @GetMapping("/list")
    public R<List<Dish>> list(Dish dish) {
        //根据分类id查询
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        //查询状态为1（起售）的菜品
        queryWrapper.eq(Dish::getStatus, 1);
        //排序
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);
        return R.success(list);
    }*/
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {
        List<DishDto> dishDtoList = null;

        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();
        //先从redis获取缓存数据
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);
        //如果存在，直接返回，无需查询数据库
        if (dishDtoList != null) {
            return R.success(dishDtoList);
        }

        //根据分类id查询
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        //查询状态为1（起售）的菜品
        queryWrapper.eq(Dish::getStatus, 1);
        //排序
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        dishDtoList = list.stream().map(item -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                dishDto.setCategoryName(category.getName());
            }
            Long dishId = item.getId(); //当前菜品id
            LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DishFlavor::getDishId, dishId);
            //查出对应菜品口味
            List<DishFlavor> dishFlavorList = dishFlavorService.list(wrapper);
            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        }).collect(Collectors.toList());

        //否则， 需要查询数据库，存到redis中
        redisTemplate.opsForValue().set(key, dishDtoList, 60, TimeUnit.MINUTES);

        return R.success(dishDtoList);

    }
}
