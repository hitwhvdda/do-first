package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private FlavorMapper flavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 保存菜品和口味，涉及两个表，开启事务
     * @param dishDTO
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        List<DishFlavor> flavors = dishDTO.getFlavors();
        dishMapper.insert(dish);

        Long id =dish.getId();
        if(flavors!=null && !flavors.isEmpty()){
            flavors.forEach(aFlavor -> aFlavor.setDishId(id));
            flavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 分页查询菜品
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /***
     * 根据id删除菜品
     * @param ids
     */
    @Override
    @Transactional
    public void deleteDish(List<Long> ids) {
        // 起售的菜不能删除
        for(Long id : ids){
            Dish dish = dishMapper.selectById(id);
            if(dish.getStatus() == StatusConstant.ENABLE)
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
        }
        // 在别的套餐里面的菜不能删
        List<Long> meal = setmealDishMapper.getSetmealIdBydishId(ids);
        if(meal!=null && !meal.isEmpty()){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //两项检查做完了，开始删
//        for(Long id : ids){
//            dishMapper.deleteById(id);
//            dishFlavorMapper.deleteByDishId(id);
//        }
        //批量删除
        dishMapper.deleteByIdBatch(ids);
        flavorMapper.deleteByDishIdBatch(ids);
    }

    /**
     * 根据菜品id回显所有信息
     * @param id
     * @return
     */
    @Override
    public DishVO getDishById(Long id) {
        //先查菜品，再查口味，拼成VO类型返回
        Dish dish = dishMapper.selectById(id);
        List<DishFlavor> flavors = flavorMapper.selectByDishId(id);
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(flavors);
        return dishVO;
    }

    /**
     * 修改菜品
     * @param dishDTO
     */
    @Override
    public void updateDish(DishDTO dishDTO) {
        //先改菜品，再改口味，口味不好直接改，先删除对应所有然后再重新插入
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.update(dish);
        flavorMapper.deleteByDishId(dish.getId());
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors!=null && !flavors.isEmpty()){
            flavors.forEach(flavor -> flavor.setDishId(dish.getId()));
            flavorMapper.insertBatch(flavors);
        }
    }

    @Override
    public void sellOrNot(Integer status, Long id) {
        Dish dish = new Dish();
        dish.setId(id);
        dish.setStatus(status);
        dishMapper.update(dish);
    }

    @Override
    public List<Dish> listQuery(Integer categoryId) {
        List<Dish> dishList = dishMapper.listQuery(categoryId);
        return dishList;
    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        // 使用分类id和起售为1的进行动态查询 返回列表
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = flavorMapper.selectByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }
}
