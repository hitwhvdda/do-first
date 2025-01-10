package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@Api(value = "店铺营业接口")
@RequestMapping("admin/shop")
public class ShopController {
    @Autowired
    private RedisTemplate redisTemplate;
    private static final String key = "SHOP_STATUS";
    @PutMapping("/{status}")
    @ApiOperation("设置营业状态")
    public Result setStatus(@PathVariable Integer status){
        log.info("设置营业状态为:{}",status==1?"营业中":"停业中");
        redisTemplate.opsForValue().set(key,status);
        return Result.success();
    }
    @GetMapping("/status")
    @ApiOperation("获取营业状态")
    public Result<Integer> getStatus(){
        Integer status = (Integer) redisTemplate.opsForValue().get(key);
        log.info("当前营业状态{}",status==1?"营业中":"停业中");
        return Result.success(status);
    }
}
