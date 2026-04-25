package my.edu.utar.assignment_2_v2.model;

import java.util.Date;

public class Mood {
    private String id;
    private String userId;
    private String feel;
    private String mood;
    private String note;
    private Date timestamp;
    private double sleepHours;

    public Mood() {
        // Required for Firebase deserialization
        this.timestamp = new Date();
    }

    public Mood(String userId, String feel, String mood, String note){
        this.userId = userId;
        this.feel = feel;
        this.mood = mood;
        this.note = note;
        this.timestamp = new Date();
    }

    public Mood(String userId, String feel, String mood, String note, double sleepHours){
        this.userId = userId;
        this.feel = feel;
        this.mood = mood;
        this.note = note;
        this.sleepHours = sleepHours;
        this.timestamp = new Date();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFeel(){
        return feel;
    }

    public void setFeel(String feel) {
        this.feel = feel;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public double getSleepHours() {
        return sleepHours;
    }

    public void setSleepHours(double sleepHours) {
        this.sleepHours = sleepHours;
    }
}