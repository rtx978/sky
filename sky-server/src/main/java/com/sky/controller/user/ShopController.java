package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("userShopController")
@Slf4j
@Api(tags = "店铺打样吗")
@RequestMapping("/user/shop")
public class ShopController {
    public static final String SHOP_STATUS = "SHOP_STATUS";
    @Autowired
    private RedisTemplate<String, Integer> redisTemplate;
    @PutMapping("/{set_status}")
    @ApiOperation("设置店铺打样状态")
    public Result<Integer> setStatus(@PathVariable("set_status") Integer status){
        log.info("设置店铺打样状态：{}",status==1?"营业中":"打烊");
        redisTemplate.opsForValue().set(SHOP_STATUS,status);
        return Result.success();
    }
    @GetMapping("/status")
    @ApiOperation("查询店铺打样状态")
    public Result<Integer> getStatus(){
        log.info("获取店铺打样状态");

        return Result.success((Integer)redisTemplate.opsForValue().get("SHOP_STATUS"));
    }
}
