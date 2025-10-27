package com.sky.entity;

import com.sky.dto.DishDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 菜品
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Dish implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    //菜品名称
    private String name;

    //菜品分类id
    private Long categoryId;

    //菜品价格
    private BigDecimal price;

    //图片
    private String image;

    //描述信息
    private String description;

    //0 停售 1 起售
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long createUser;

    private Long updateUser;

    public Dish(DishDTO dto) {
        this.name = dto.getName();
        this.categoryId = dto.getCategoryId();
        this.price = dto.getPrice();
        this.image = dto.getImage();
        this.description = dto.getDescription();
        this.status = dto.getStatus();
        this.createTime = dto.getCreateTime();
        this.updateTime = dto.getUpdateTime();
        this.createUser = dto.getCreateUser();
        this.updateUser = dto.getUpdateUser();
        this.id = dto.getId();
//        this.status = dto.getStatus();

    }
}
