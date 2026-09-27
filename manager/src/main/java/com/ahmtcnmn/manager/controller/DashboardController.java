package com.ahmtcnmn.manager.controller;

import com.ahmtcnmn.manager.service.DashboardService;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ahmtcnmn.manager.dto.Dashboard.DtoDashboardSummary;
import com.ahmtcnmn.manager.dto.Dashboard.DtoTimelinePoint;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public DtoDashboardSummary getDashboardSummary() {
        return dashboardService.getSummary();
    }

    @GetMapping("/events-timeline")
    public List<DtoTimelinePoint> getEventsTimeline(@RequestParam(defaultValue = "24") int hours) {
        return dashboardService.getEventsTimeline(hours);
    }
    
}
