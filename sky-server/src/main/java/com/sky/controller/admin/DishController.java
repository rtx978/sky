package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品管理")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;
    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dish){
//        log.info("新增菜品：{}",dish);
        dishService.save(dish);
        return Result.success();

    }

    @GetMapping("/page")
    @ApiOperation("分页查询菜品")
    public Result<PageResult> page( DishPageQueryDTO dto){
        PageResult t=dishService.pageQuery(dto);
//        log.info("分页查询：{}",dto);
        return Result.success(t);
    }
    @DeleteMapping
    @ApiOperation("批量删除菜品")
    public Result delete(@RequestParam("ids") List<Integer> p){
        log.info("批量删除：{}",p);
        dishService.deleteBatch(p);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishDTO > getById(@PathVariable  int id){
        log.info("根据id查询菜品：{}",id);
        DishDTO temp=dishService.getByIdWithFlavors(id);
        return Result.success(temp);
    }

    @PutMapping
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishDTO dto){
        log.info("修改菜品：{}",dto);
        dishService.updateWithFlavor(dto);
        return Result.success();
    }
}
