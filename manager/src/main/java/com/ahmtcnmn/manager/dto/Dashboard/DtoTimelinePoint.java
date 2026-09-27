package com.ahmtcnmn.manager.dto.Dashboard;

public record DtoTimelinePoint(
    String hour,
    long low,
    long medium,
    long high,
    long critical
) {}
