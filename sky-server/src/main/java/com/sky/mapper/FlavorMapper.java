package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FlavorMapper {
    /**
     * 批量插入口味信息
     * @param flavors
     */
    @AutoFill(OperationType.INSERT)
    void insertBatch(List<DishFlavor> flavors);

    /**
     * 根据dishid删除菜品口味信息
     * @param id
     */
    @Delete("delete from dish_flavor where dish_id=#{id}")
    void deleteByDishId(Long id);

    /**
     * 批量删除
     * @param ids
     */
    void deleteByDishIdBatch(List<Long> ids);

    @Select("select * from dish_flavor where dish_id=#{id}")
    List<DishFlavor> selectByDishId(Long id);
}
