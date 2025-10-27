package com.sky.aspect;

import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import com.sky.annotation.AutoFill;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    @Pointcut("execution(* com.sky.mapper.*.*(..)) &&@annotation(com.sky.annotation.AutoFill) ")
//    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(AutoFill) ")
    public void autoFillPointCut(){}

    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("开始进行数据填充");
        //insert？or update
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
//        System.out.println( signature);
        AutoFill autoFill=signature.getMethod().getAnnotation(AutoFill.class);
        //获取参数：实体对象
        OperationType op=autoFill.value();
        Object[] args=joinPoint.getArgs();
        if (args==null||args.length==0){
            return;
        }
        Object object=args[0];
        log.info("--- "+ object);
        LocalDateTime now=LocalDateTime.now();
        Long currentId= BaseContext.getCurrentId();

        //赋值
        if(op==OperationType.INSERT){
            try{
                Method ct=object.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME,LocalDateTime.class);
                Method cu=object.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER,Long.class);
                Method ut=object.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME,LocalDateTime.class);
                Method uu=object.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER,Long.class);
                ct.invoke(object,now);
                cu.invoke(object,currentId);
                ut.invoke(object,now);
                uu.invoke(object,currentId);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        else{
            try{
                Method ut=object.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME,LocalDateTime.class);
                Method uu=object.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER,Long.class);
                ut.invoke(object,now);
                uu.invoke(object,currentId);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
