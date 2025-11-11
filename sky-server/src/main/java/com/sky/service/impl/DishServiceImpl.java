package com.sky.service.impl;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.dto.ffDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    /**
     * 新增菜品和对应的口味
     * @param dto
     */
    @Transactional
    public void  save(DishDTO dto){
        //插入菜品
        log.info("新增菜品：{}",dto);
        dishMapper.insert(dto);
        Long id=dto.getId();
        log.info("id="+id);
        //插入口味
        List<DishFlavor> flavors = dto.getFlavors();
        if(flavors!=null&&flavors.size()>0){
            flavors.forEach(e->{
                e.setDishId(id);
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }


    public PageResult pageQuery(DishPageQueryDTO dto){
        log.info("分页查询：{}",dto);
        PageHelper.startPage(dto.getPage(),dto.getPageSize());
        Page<DishVO> page=dishMapper.pageQuery(dto);
        return new PageResult(page.getTotal()   ,page.getResult());
    }
    @Transactional
    public Result deleteBatch(List<Integer> ids){
        if(ids.size()==0||ids==null)
        {
            throw new DeletionNotAllowedException(MessageConstant.UNKNOWN_ERROR);
        }

//        log.info("批量删除：{}",ids);
        for(Integer id:ids){
            Dish d=dishMapper.getById(id);
            if(d.getStatus()== StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        List<Long> col=setmealDishMapper.getSetmealIdByDishId(ids);
        if(col!=null&&col.size()>0)
        {
            throw  new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        dishMapper.deleteBatch(ids);
        return Result.success();
    }

    @Override
    public DishDTO getByIdWithFlavors(int id) {
        Dish d=dishMapper.getById(id);
        List<DishFlavor> df=dishFlavorMapper.getById(id);
        DishDTO dd=new DishDTO();
        BeanUtils.copyProperties(d,dd);
//        BeanUtils.copyProperties(df,dd);
        dd.setFlavors(df);
        return dd;
    }

    @Override
    public void updateWithFlavor(DishDTO dto) {
        dishMapper.update(new Dish(dto));

        dishFlavorMapper.deleteByDishId(dto.getId());
        List<DishFlavor> a=dto.getFlavors();
        if(a!=null&&a.size()>0){
            a.forEach(e->{
                e.setDishId(dto.getId());
            });
            dishFlavorMapper.insertBatch(dto.getFlavors());
        }

    }
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getById((int)(long)d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    public List<Dish> list(Long categoryId) {
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        return dishMapper.list(dish);
    }
    public void startOrStop(Integer status, Long id){
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();
        dishMapper.update(dish);
    }
}
