package badtzmarupekkle.littlethings.entity;

import java.util.List;

public class TimeSlot extends EntityModel {
    private int color;
    private int endDay;
    private int endHour;
    private int endMinute;
    private int endMonth;
    private int endYear;
    private int startDay;
    private int startHour;
    private int startMinute;
    private int startMonth;
    private int startYear;
    private List<Boolean> days;
    private String description;
    private String location;
    private String name;

    public int getColor() {
        return color;
    }
    public void setColor(int color) {
        this.color = color;
    }
    public int getEndDay() {
        return endDay;
    }
    public void setEndDay(int endDay) {
        this.endDay = endDay;
    }
    public int getEndHour() {
        return endHour;
    }
    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }
    public int getEndMinute() {
        return endMinute;
    }
    public void setEndMinute(int endMinute) {
        this.endMinute = endMinute;
    }
    public int getEndMonth() {
        return endMonth;
    }
    public void setEndMonth(int endMonth) {
        this.endMonth = endMonth;
    }
    public int getEndYear() {
        return endYear;
    }
    public void setEndYear(int endYear) {
        this.endYear = endYear;
    }
    public int getStartDay() {
        return startDay;
    }
    public void setStartDay(int startDay) {
        this.startDay = startDay;
    }
    public int getStartHour() {
        return startHour;
    }
    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }
    public int getStartMinute() {
        return startMinute;
    }
    public void setStartMinute(int startMinute) {
        this.startMinute = startMinute;
    }
    public int getStartMonth() {
        return startMonth;
    }
    public void setStartMonth(int startMonth) {
        this.startMonth = startMonth;
    }
    public int getStartYear() {
        return startYear;
    }
    public void setStartYear(int startYear) {
        this.startYear = startYear;
    }
    public List<Boolean> getDays() {
        return days;
    }
    public void setDays(List<Boolean> days) {
        this.days = days;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
