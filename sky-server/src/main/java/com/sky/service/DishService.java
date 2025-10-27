package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;

import java.util.List;

public interface DishService {

    /**
     * 新增菜品和对应的口味
     * @param dto
     */
    public void save(DishDTO dto);

    PageResult pageQuery(DishPageQueryDTO dto);

    Result deleteBatch(List<Integer> p);

    DishDTO getByIdWithFlavors(int id);

    void updateWithFlavor(DishDTO dto);
}
