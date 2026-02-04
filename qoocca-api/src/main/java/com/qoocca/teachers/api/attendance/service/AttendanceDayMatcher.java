package com.qoocca.teachers.api.attendance.service;

import com.qoocca.teachers.db.classInfo.entity.ClassInfoEntity;

public final class AttendanceDayMatcher {

    private AttendanceDayMatcher() {
    }

    public static boolean isClassOnDay(ClassInfoEntity c, String day) {
        return switch (day) {
            case "monday" -> c.isMonday();
            case "tuesday" -> c.isTuesday();
            case "wednesday" -> c.isWednesday();
            case "thursday" -> c.isThursday();
            case "friday" -> c.isFriday();
            case "saturday" -> c.isSaturday();
            case "sunday" -> c.isSunday();
            default -> false;
        };
    }
}
