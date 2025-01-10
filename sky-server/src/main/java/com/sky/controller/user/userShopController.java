package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Api(value = "客户端店铺营业接口")
@RequestMapping("user/shop")
public class userShopController {
    @Autowired
    private RedisTemplate redisTemplate;
    private static final String key = "SHOP_STATUS";
    @GetMapping("/status")
    @ApiOperation("用户获取营业状态")
    public Result<Integer> getStatus(){
        Integer status = (Integer) redisTemplate.opsForValue().get(key);
        log.info("当前营业状态{}",status==1?"营业中":"停业中");
        return Result.success(status);
    }
}
