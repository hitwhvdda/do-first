package com.sky.service;

import com.sky.dto.CategoryPageQueryDTO;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {
    /**
     * 添加新菜品
     * @param dishDTO
     */
    void saveWithFlavor(DishDTO dishDTO);

    /**
     * 分页查询菜品
     * @param dishPageQueryDTO
     * @return
     */
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 删除菜品
     * @param ids
     */

    void deleteDish(List<Long> ids);

    /**
     * 通过id查询菜品
     * @param id
     * @return
     */
    DishVO getDishById(Long id);

    /**
     * 更新菜品内容
     * @param dishDTO
     */
    void updateDish(DishDTO dishDTO);

    /**
     * 卖不卖
     * @param status
     * @param id
     */
    void sellOrNot(Integer status, Long id);

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    List<Dish> listQuery(Integer categoryId);
}
