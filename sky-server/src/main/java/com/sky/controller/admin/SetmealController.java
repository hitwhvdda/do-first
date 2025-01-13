package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("admin/setmeal")
@Slf4j
@Api(tags = "套餐相关接口")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @PostMapping
    @ApiOperation("新增套餐")
    @CacheEvict(cacheNames="setmealCache",key="#setmealDTO.categoryId")
    public Result save(@RequestBody SetmealDTO setmealDTO){
        log.info("新增套餐:{}",setmealDTO);
        setmealService.save(setmealDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("套餐分页查询")
    public Result<PageResult> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("套餐分页查询");
        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }
    @DeleteMapping
    @ApiOperation("批量删除")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true) //清理所有
    public Result<String> deleteBatch(@RequestParam List<Long> ids){
        log.info("批量删除:{}",ids);
        setmealService.deleteBatch(ids);
        return Result.success();
    }
    @PostMapping("/status/{status}")
    @ApiOperation("修改售卖状态")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true) //清理所有
    public Result sellOrNot(@PathVariable Integer status,Long id){
        log.info("修改售卖状态{} {}",status,id);
        setmealService.sellOrNot(status,id);
        return Result.success();
    }
    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐信息")
    public Result<SetmealVO> backInformation(@PathVariable Long id){
        log.info("根据id查询套餐信息");
        SetmealVO setmealVO = setmealService.selectById(id);
        return Result.success(setmealVO);
    }
    @PutMapping
    @ApiOperation("修改套餐")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true) //清理所有
    public Result<String> updateSetmeal(@RequestBody SetmealDTO setmealDTO){
        log.info("修改套餐");
        setmealService.updateSetmeal(setmealDTO);
        return Result.success();
    }
}
