package com.sky.service;

import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

public interface StatisticsService {
    TurnoverReportVO TurnoverReport(LocalDate begin, LocalDate end);

    UserReportVO userStatistic(LocalDate begin, LocalDate end);

    OrderReportVO orderStatistics(LocalDate begin, LocalDate end);

    SalesTop10ReportVO saleReport(LocalDate begin, LocalDate end);

    void exportBusinessData(HttpServletResponse response);
}
