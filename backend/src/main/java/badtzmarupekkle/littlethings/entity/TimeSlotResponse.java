package badtzmarupekkle.littlethings.entity;

import java.util.List;

public class TimeSlotResponse extends Response {
    private long id;
    private List<TimeSlot> timeSlots;
    private TimeSlot timeSlot;

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public List<TimeSlot> getTimeSlots() {
        return timeSlots;
    }
    public void setTimeSlots(List<TimeSlot> timeSlots) {
        this.timeSlots = timeSlots;
    }
    public TimeSlot getTimeSlot() {
        return timeSlot;
    }
    public void setTimeSlot(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }
}
