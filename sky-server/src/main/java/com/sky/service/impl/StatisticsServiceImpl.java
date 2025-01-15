package com.sky.service.impl;

import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.apache.commons.lang3.StringUtils;
import com.sky.constant.MessageConstant;
import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.StatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;
    @Override
    public TurnoverReportVO TurnoverReport(LocalDate begin, LocalDate end) {
        TurnoverReportVO turnoverReportVO = new TurnoverReportVO();
        if(begin.isAfter(end))
            throw new DateTimeException(MessageConstant.TIME_SCOPE_EXSIST);
        List<LocalDate> localDateList = new ArrayList<>();
        List<Double> moneyList = new ArrayList<>();
        while(!begin.isEqual(end)){
            localDateList.add(begin);
            Map map = new HashMap<>();
            LocalDateTime min = LocalDateTime.of(begin, LocalTime.MIN);
            LocalDateTime max = LocalDateTime.of(begin,LocalTime.MAX);
            map.put("status",Orders.COMPLETED);
            map.put("begin",min);
            map.put("end",max);
            Double amount = orderMapper.getAmountByDay(map);
            amount = amount==null? 0.0:amount;
            moneyList.add(amount);
            begin=begin.plusDays(1);
        }
        turnoverReportVO.setTurnoverList(StringUtils.join(moneyList,","));
        turnoverReportVO.setDateList(StringUtils.join(localDateList,","));
        return turnoverReportVO;
    }

    @Override
    public UserReportVO userStatistic(LocalDate begin, LocalDate end) {
        UserReportVO userReportVO = new UserReportVO();
        if(begin.isAfter(end))
            throw new DateTimeException(MessageConstant.TIME_SCOPE_EXSIST);
        List<LocalDate> localDateList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();
        List<Integer> newUserList = new ArrayList<>();
        while(!begin.isEqual(end)){
            localDateList.add(begin);
            Map map = new HashMap<>();
            LocalDateTime min = LocalDateTime.of(begin, LocalTime.MIN);
            LocalDateTime max = LocalDateTime.of(begin,LocalTime.MAX);
            map.put("end",max);
            Integer total = userMapper.getUserStatistic(map);
            total=total==null?0:total;
            totalUserList.add(total);
            map.put("begin",min);
            log.info(String.valueOf(map));
            Integer newUser = userMapper.getUserStatistic(map);
            newUser=newUser==null?0:newUser;
            newUserList.add(newUser);
            begin=begin.plusDays(1);
        }
        userReportVO.setDateList(StringUtils.join(localDateList,","));
        userReportVO.setTotalUserList(StringUtils.join(totalUserList,","));
        userReportVO.setNewUserList(StringUtils.join(newUserList,","));
        return userReportVO;
    }

    @Override
    public OrderReportVO orderStatistics(LocalDate begin, LocalDate end) {
        if(begin.isAfter(end))
            throw new DateTimeException(MessageConstant.TIME_SCOPE_EXSIST);
        OrderReportVO orderReportVO = new OrderReportVO();
        List<LocalDate> localDateList = new ArrayList<>();
        List<Integer> totalList = new ArrayList<>();
        List<Integer> validList = new ArrayList<>();
        Integer total=0;
        Integer valid=0;
        while (!begin.isEqual(end)){
            localDateList.add(begin);
            Map map = new HashMap<>();
            LocalDateTime min = LocalDateTime.of(begin, LocalTime.MIN);
            LocalDateTime max = LocalDateTime.of(begin,LocalTime.MAX);
            map.put("begin",min);
            map.put("end",max);
            Integer dayTotal = orderMapper.getOrderCount(map);
            map.put("status",Orders.COMPLETED);
            Integer dayValid = orderMapper.getOrderCount(map);
            dayTotal = dayTotal==null?0:dayTotal;
            dayValid = dayValid==null?0:dayValid;
            total+=dayTotal;
            valid+=dayValid;
            totalList.add(dayTotal);
            validList.add(dayValid);
            begin=begin.plusDays(1);
        }
        Double ratios = 0.0;
        if(total!=0)
            ratios = valid.doubleValue() / total;
        orderReportVO.setDateList(StringUtils.join(localDateList,","));
        orderReportVO.setOrderCountList(StringUtils.join(totalList,","));
        orderReportVO.setValidOrderCountList(StringUtils.join(validList,","));
        orderReportVO.setTotalOrderCount(total);
        orderReportVO.setValidOrderCount(valid);
        orderReportVO.setOrderCompletionRate(ratios);
        return orderReportVO;
    }

    @Override
    public SalesTop10ReportVO saleReport(LocalDate begin, LocalDate end) {
        if(begin.isAfter(end))
            throw new DateTimeException(MessageConstant.TIME_SCOPE_EXSIST);
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end,LocalTime.MAX);
        List<GoodsSalesDTO> goodsSalesDTOList = orderMapper.getGoodSale(beginTime,endTime);
        List<String> namelist  = new ArrayList<>();
        List<Integer> salelist = new ArrayList<>();
        for(GoodsSalesDTO goodsSalesDTO: goodsSalesDTOList){
            namelist.add(goodsSalesDTO.getName());
            salelist.add(goodsSalesDTO.getNumber());
        }
        SalesTop10ReportVO salesTop10ReportVO = new SalesTop10ReportVO();
        salesTop10ReportVO.setNameList(StringUtils.join(namelist,","));
        salesTop10ReportVO.setNumberList(StringUtils.join(salelist,","));
        return salesTop10ReportVO;
    }
    /**
     * 导出运营数据报表
     * @param response
     */
    public void exportBusinessData(HttpServletResponse response) {
        //1. 查询数据库，获取营业数据---查询最近30天的运营数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);

        //查询概览数据
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(dateBegin, LocalTime.MIN), LocalDateTime.of(dateEnd, LocalTime.MAX));

        //2. 通过POI将数据写入到Excel文件中
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/template.xlsx");

        try {
            //基于模板文件创建一个新的Excel文件
            XSSFWorkbook excel = new XSSFWorkbook(in);

            //获取表格文件的Sheet页
            XSSFSheet sheet = excel.getSheet("Sheet1");

            //填充数据--时间
            sheet.getRow(1).getCell(1).setCellValue("时间：" + dateBegin + "至" + dateEnd);

            //获得第4行
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());

            //获得第5行
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

            //填充明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = dateBegin.plusDays(i);
                //查询某一天的营业数据
                BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));

                //获得某一行
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }

            //3. 通过输出流将Excel文件下载到客户端浏览器
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            //关闭资源
            out.close();
            excel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
