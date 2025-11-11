package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    DishMapper dishMapper;
    @Autowired
    SetmealMapper setmealMapper;
    public void add(ShoppingCartDTO shoppingCartDTO){
        ShoppingCart sc=new ShoppingCart();

        BeanUtils.copyProperties(shoppingCartDTO,sc);
        sc.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> list=shoppingCartMapper.list(sc);
        if(list!=null&&list.size()>0){
            ShoppingCart temp=list.get(0);
            temp.setNumber(temp.getNumber()+1);
            shoppingCartMapper.update(temp);
        }
        else{
            if(sc.getDishId()!=null){
                Dish dish=dishMapper.getById((int)(long)sc.getDishId());
                sc.setAmount(dish.getPrice());
                sc.setName(dish.getName());
                sc.setImage(dish.getImage());
                sc.setNumber(1);
                sc.setCreateTime(LocalDateTime.now());
            }
            else{
                Setmeal sm=setmealMapper.getById(sc.getSetmealId());
                sc.setAmount(sm.getPrice());
                sc.setName(sm.getName());
                sc.setImage(sm.getImage());
                sc.setNumber(1);
                sc.setCreateTime(LocalDateTime.now());
            }
            shoppingCartMapper.insert(sc);
        }
    }

    @Override
    public List<ShoppingCart> showShoppingCart() {
        ShoppingCart sc=new ShoppingCart().builder()
                .userId(BaseContext.getCurrentId())
                .build();
        List<ShoppingCart> list=shoppingCartMapper.list( sc);
        return list;
    }

    @Override
    public void clean() {
        shoppingCartMapper.clean(BaseContext.getCurrentId());
    }

    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart sc=new ShoppingCart();

        BeanUtils.copyProperties(shoppingCartDTO,sc);
        sc.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> list=shoppingCartMapper.list(sc);
        if(list!=null&&list.size()>0){
            ShoppingCart temp=list.get(0);
            temp.setNumber(temp.getNumber()-1);
            if(temp.getNumber()==0){
                shoppingCartMapper.deleteById(temp.getId());

            }
            else{
                shoppingCartMapper.update(temp);
            }
//            shoppingCartMapper.update(temp);
        }
//        else{
//            if(sc.getDishId()!=null){
//                Dish dish=dishMapper.getById((int)(long)sc.getDishId());
//                sc.setAmount(dish.getPrice());
//                sc.setName(dish.getName());
//                sc.setImage(dish.getImage());
//                sc.setNumber(1);
//                sc.setCreateTime(LocalDateTime.now());
//            }
//            else{
//                Setmeal sm=setmealMapper.getById(sc.getSetmealId());
//                sc.setAmount(sm.getPrice());
//                sc.setName(sm.getName());
//                sc.setImage(sm.getImage());
//                sc.setNumber(1);
//                sc.setCreateTime(LocalDateTime.now());
//            }
//            shoppingCartMapper.insert(sc);
//        }
//        shoppingCartMapper.sub(shoppingCartDTO);

    }
}
