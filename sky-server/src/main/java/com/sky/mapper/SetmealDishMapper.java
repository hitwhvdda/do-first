package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    /**
     * 根据菜品id得到所属套餐id
     * @param ids
     * @return
     */
    List<Long> getSetmealIdBydishId(List<Long> ids);

    /**
     * 批量保存一个套餐的菜品
     * @param dishList
     */
    void saveList(List<SetmealDish> dishList);

    /**
     * 删除一个套餐对应的所有菜品
     * @param setmeals
     */
    void deleteBySetmealId(List<Setmeal> setmeals);

    List<SetmealDish> selectBySetmealId(Long id);
}
