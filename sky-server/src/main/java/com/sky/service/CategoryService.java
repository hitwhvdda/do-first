package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;

import java.util.List;

public interface CategoryService {
    /**
     * 添加分类
     * @param categoryDTO
     */
    void save(CategoryDTO categoryDTO);

    /**
     * 分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    /**
     * 按照id删除
     * @param id
     */
    void delete(Long id);

    /**
     * 修改
     * @param categoryDTO
     */
    void update(CategoryDTO categoryDTO);

    /**
     * 修改状态
     * @param id
     * @param status
     */
    void startOrStop(Long id, Integer status);

    /**
     * 根据类别查询
     * @param type
     * @return
     */
    List<Category> list(Integer type);
}
