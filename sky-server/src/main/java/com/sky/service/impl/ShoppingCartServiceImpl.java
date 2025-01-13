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
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        //这时候里面应该是有菜品id或者套餐id  有菜品id的时候口味可能存在，那么用动态sql查询是否已经存在
        ShoppingCart shoppingCart=new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        //还要获得用户的id 保证购物车不串
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        //一定最多只能查到一个
        if(list!=null && !list.isEmpty()){
            //查到了说明只需要修改
            ShoppingCart have = list.get(0);
            have.setNumber(have.getNumber()+1);
            shoppingCartMapper.updateNumberByid(have);
        }
        else{
            // 没查到的话要去构造然后添加，这时候还要考虑到底是菜品还是套餐，牵扯如何构造
            Long dishId = shoppingCart.getDishId();
            Long setmealId = shoppingCart.getSetmealId();
            if(dishId!=null){
                //说明是菜品，去查菜品 然后设置信息准备插入
                Dish dish = dishMapper.selectById(dishId);
                shoppingCart.setAmount(dish.getPrice());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setName(dish.getName());
            }else{
                //说明是套餐 去查套餐
                Setmeal setmeal = setmealMapper.selectById(setmealId);
                shoppingCart.setAmount(setmeal.getPrice());
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
            }
            //设置公共部分
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }

    }

    @Override
    public List<ShoppingCart> showShoppingCart() {
        Long currentId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(currentId);
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        return list;
    }

    @Override
    public void deleteall() {
        Long currentId = BaseContext.getCurrentId();
        shoppingCartMapper.delete(currentId);
    }

    @Override
    public void deleteSub(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        Long dishId = shoppingCart.getDishId();
        Long setmealId = shoppingCart.getSetmealId();
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        ShoppingCart sub = list.get(0);
        Integer number = sub.getNumber();
        if(number==1){
            shoppingCartMapper.deleteSub(sub);
        }
        else{
            sub.setNumber(sub.getNumber()-1);
            shoppingCartMapper.updateNumberByid(sub);
        }
    }
}
