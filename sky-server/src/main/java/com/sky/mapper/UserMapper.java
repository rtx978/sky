package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Mapper
public interface UserMapper {
    @Select("select * from user where openid=#{openid} ")
    User getByOpenid(String openid);;

    void insert(User user);
    @Select("select * from user where id=#{userId} ")
    User getById(Long userId);

    Integer countByMap(Map map);

    @Select("select count(1) from user where create_time <= #{end}")
    Integer countTotal(LocalDateTime end);
}
