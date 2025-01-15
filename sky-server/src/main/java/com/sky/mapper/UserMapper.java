package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.DishFlavor;
import com.sky.entity.User;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {

    Integer getUserStatistic(Map map);

    /**
     * 根据openid查找用户
     * @param openId
     * @return
     */
    @Select("select * from user where openid=#{openId}")
    User selectByOpenId(String openId);

    /**
     * 创建新用户到数据库
     * @param user
     */
    void insert(User user);
    @Select("select * from user where id=#{id}")
    User getById(Long userId);
}
