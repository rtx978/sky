package com.sky.dto;

import com.sky.entity.DishFlavor;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
@Data
public class ffDTO implements Serializable {
    private List<DishFlavor> flavors = new ArrayList<>();
}
