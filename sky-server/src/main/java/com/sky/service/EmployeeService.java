package com.sky.service;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;
import com.sky.result.Result;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    /**
     *
     * @param e
     */
    void save(EmployeeDTO e);

    PageResult pageQuery(EmployeePageQueryDTO e);

    void startOrStop(Integer s, Long id);

    public Employee getById(Long id);


    void update(EmployeeDTO d);

    Result editPassword(PasswordEditDTO d);
}
