package ch.skyfy.fk.logic.data;


public class TimelineData {

    private int day, minutes, seconds;

    public TimelineData() {}

    public TimelineData(int day, int minutes, int seconds) {
        this.day = day;
        this.minutes = minutes;
        this.seconds = seconds;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }
}
