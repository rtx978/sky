package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);
        password=DigestUtils.md5DigestAsHex(password.getBytes());
        System.out.println(password+" == "+employee.getPassword());
        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // TODO 后期需要进行md5加密，然后再进行比对
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     *新增员工
     * @param e
     */
    public void save(EmployeeDTO e) {
       Employee em=new Employee();
        BeanUtils.copyProperties(e,em);
        em.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        em.setStatus(StatusConstant.ENABLE);
//        em.setCreateTime(LocalDateTime.now());
//        em.setUpdateTime(LocalDateTime.now());
//        //TODO 后去修改为当前登录用户ID  已do
//        em.setCreateUser(BaseContext.getCurrentId());
//        em.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper .insert(em);
    }
    /**
     *员工分页查询
     * @param e
     * @return
     */
    @Override
    public PageResult pageQuery(EmployeePageQueryDTO e) {
        PageHelper.startPage(e.getPage(),e.getPageSize());
        Page<Employee> page=employeeMapper.pageQuery(e);
        long tot=page.getTotal();
        List<Employee> records=page.getResult();
        return new PageResult(tot,records);
    }

    @Override
    public void startOrStop(Integer s, Long id) {
        Employee em= Employee.builder().status(s).id(id).build();
        employeeMapper.update(em);
    }

    @Override
    public Employee getById(Long id) {
        Employee employee = employeeMapper.getById(id);
        employee.setPassword("****");
        return employee;
    }

    public void update(EmployeeDTO d){
        Employee e=new Employee();
        BeanUtils.copyProperties(d,e);
//        e.setUpdateUser(BaseContext.getCurrentId());
//        e.setUpdateTime(LocalDateTime.now());
        employeeMapper.update(e);
    }

    @Override
    public Result editPassword(PasswordEditDTO d) {
        Employee employee = employeeMapper.getById(d.getEmpId());
        String okPassword = DigestUtils.md5DigestAsHex(employee.getPassword().getBytes());
        String oldPassword = DigestUtils.md5DigestAsHex(d.getOldPassword().getBytes());
        if(!okPassword.equals(oldPassword)){
            employee.setPassword(DigestUtils.md5DigestAsHex(d.getNewPassword().getBytes()));
            employeeMapper.update(employee);
            return Result.success();
        }else{
            return Result.error(MessageConstant.PASSWORD_EDIT_FAILED);
        }
    }

}
