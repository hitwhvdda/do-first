package com.sky.controller.admin;

import com.sky.dto.CategoryPageQueryDTO;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("admin/dish")
@Slf4j
@Api(tags = "菜品相关接口")
public class DishController {
    @Autowired
    private DishService dishService;
    @PostMapping
    @ApiOperation("新增菜品")
    public Result saveWithFlavor(@RequestBody DishDTO dishDTO){
        log.info("新增菜品{}",dishDTO);
        dishService.saveWithFlavor(dishDTO);
        return Result.success();
    }

    /**
     * 分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<PageResult> pageQuery(DishPageQueryDTO dishPageQueryDTO){
        log.info("开始分页查询：{}",dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation(("删除菜品"))
    // 这个注解会帮我们把逗号分割的字符串弄成
    public Result deleteDish(@RequestParam List<Long> ids){
        log.info("删除菜品:{}",ids);
        dishService.deleteDish(ids);
        return Result.success();
    }
    @GetMapping("/{id}")
    @ApiOperation("回现菜品接口")
    public Result<DishVO> getDishById(@PathVariable Long id){
        log.info("查询菜品根据id{}",id);
        DishVO dishVo = dishService.getDishById(id);
        return Result.success(dishVo);
    }
    @PutMapping
    @ApiOperation("修改菜品")
    public Result updateDish(@RequestBody DishDTO dishDTO){
        log.info("修改菜品内容{}",dishDTO);
        dishService.updateDish(dishDTO);
        return Result.success();
    }
    @PostMapping("/status/{status}")
    @ApiOperation("停售与否")
    public Result sellOrNot(@PathVariable Integer status,Long id){
        log.info("更改售卖状态{} {}",status,id);
        dishService.sellOrNot(status,id);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> listQuery(Integer categoryId){
        log.info("根据分类id查询菜品:{}",categoryId);
        List<Dish> dishList = dishService.listQuery(categoryId);
        return Result.success(dishList);
    }
}
