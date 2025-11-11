package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.val;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    UserMapper userMapper;
    @Autowired
    WorkspaceService workspaceService;

    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> list=new ArrayList<>();
        while(begin.equals(end)==false){
            list.add(begin);
            begin=begin.plusDays(1);
        }
        List<BigDecimal> turnoverList=new ArrayList<>();
        list.add(end);
        for (LocalDate localDate : list) {
            Map<Object, Object> m=new HashMap<>();
            LocalDateTime be=LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime en=LocalDateTime.of(localDate, LocalTime.MAX);
            m.put("begin",be);
            m.put("end",en);
            m.put("status",5);
            BigDecimal result = orderMapper.sumByMap(m);
            turnoverList.add(result != null ? result : BigDecimal.ZERO);
        }
        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(list,","))
                .turnoverList(StringUtils.join(turnoverList,","))
                .build();
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> list=new ArrayList<>();
        LocalDate g6=begin;
        while(begin.equals(end)==false){
            list.add(begin);
            begin=begin.plusDays(1);
        }
        List<Integer> tot=new ArrayList<>();
        List<Integer> dif=new ArrayList<>();
        list.add(end);
        for (LocalDate localDate : list) {
            LocalDateTime endOfDay = LocalDateTime.of(localDate, LocalTime.MAX);
            Integer total = userMapper.countTotal(endOfDay);
            tot.add(total != null ? total : 0);
        }
        LocalDateTime haha=LocalDateTime.of(g6.plusDays(-1), LocalTime.MAX);
        Integer g=userMapper.countTotal(haha);
        Integer pre=g==null?0:g;

        int n=tot.size();
        for(int i=0;i<n;i++)
        {
            dif.add(tot.get(i)-pre);
            pre=tot.get(i);

        }

        return UserReportVO.builder()
                .dateList(StringUtils.join(list,","))
                .totalUserList(StringUtils.join( tot,","))
                .newUserList(StringUtils.join(dif,","))
                .build();
    }

    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> list=getBetweenDates(begin,end);


        List<Integer> tot=new ArrayList<>();
        List<Integer> ok_List=new ArrayList<>();
        for(LocalDate data: list){
            Map m=new HashMap<>();
            LocalDateTime beginOfDay = LocalDateTime.of(data, LocalTime.MIN);
            LocalDateTime endOfDay = LocalDateTime.of(data, LocalTime.MAX);
            m.put("begin",beginOfDay);
            m.put("end",endOfDay);
            Integer total = orderMapper.countByMap(m);
            m.put("status",5);
            Integer ok_num= orderMapper.countByMap(m);
            tot.add( total);
            ok_List.add(ok_num);
        }
        Integer g1=tot.stream().reduce(Integer::sum).get();
        Integer g2=ok_List.stream().reduce(Integer::sum).get();
        Double g3=g2.doubleValue()/g1.doubleValue();
        return OrderReportVO.builder()
                .dateList(StringUtils.join(list,","))
                .orderCountList(StringUtils.join(tot,","))
                .validOrderCountList(StringUtils.join(ok_List,","))
                .totalOrderCount(g1)
                .validOrderCount(g2)
                .orderCompletionRate(g3)
                .build();
    }

    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        List<LocalDate> list=getBetweenDates(begin,end);
        Map m=new HashMap<>();
        m.put("begin",LocalDateTime.of(begin, LocalTime.MIN));
        m.put("end",LocalDateTime.of(end, LocalTime.MAX));
        List<GoodsSalesDTO> l=orderMapper.getSalesTop10(m);
        for (GoodsSalesDTO goodsSalesDTO : l){
            System.out.println(goodsSalesDTO);
        }
        List<String> names=l.stream().map((GoodsSalesDTO name)->{return name.getName();}).collect(Collectors.toList());
        List<Integer> numbers=l.stream().map((GoodsSalesDTO number)->{return number.getNumber();}).collect(Collectors.toList());
        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(names,","))
                .numberList(StringUtils.join(numbers,","))
                .build();
    }

    @Override
    public void exportBusinessData(HttpServletResponse response) {
        InputStream in = null;
        ServletOutputStream outputStream = null;
        XSSFWorkbook excel = null;

        try {
            BusinessDataVO businessDataVO = workspaceService.getBusinessData(
                    LocalDateTime.of(LocalDate.now().minusDays(30), LocalTime.MIN),
                    LocalDateTime.of(LocalDate.now(), LocalTime.MAX)
            );

            in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
            if (in == null) {
                throw new RuntimeException("模板文件未找到");
            }

            excel = new XSSFWorkbook(in);
            XSSFSheet sheet = excel.getSheet("Sheet1");

            sheet.getRow(1).getCell(1).setCellValue("时间:" + LocalDate.now().minusDays(30) + "至:" + LocalDate.now().minusDays(1));
            sheet.getRow(3).getCell(2).setCellValue(businessDataVO.getTurnover());
            XSSFRow row = sheet.getRow(3);
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());
            int cnt=7;
            for(int i=  30;i>=0;i--)
            {
                LocalDate t1 = LocalDate.now().minusDays(i);
                BusinessDataVO b= workspaceService.getBusinessData(LocalDateTime.of(t1, LocalTime.MIN),LocalDateTime.of(t1, LocalTime.MAX));

                row=sheet.getRow(cnt++);
                row.getCell(1).setCellValue(t1.toString());
                row.getCell(2).setCellValue(b.getTurnover());
                row.getCell(3).setCellValue(b.getValidOrderCount());
                row.getCell(4).setCellValue(b.getOrderCompletionRate());
                row.getCell(6).setCellValue(b.getNewUsers());
                row.getCell(5).setCellValue(b.getUnitPrice());
            }

            outputStream = response.getOutputStream();
            excel.write(outputStream);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (excel != null) {
                try {
                    excel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private List<LocalDate> getBetweenDates(LocalDate begin, LocalDate end){
        List<LocalDate> list=new ArrayList<>();

        while(begin.equals(end)==false){
            list.add(begin);
            begin=begin.plusDays(1);
        }
        list.add(end);
        return list;
    }
}
