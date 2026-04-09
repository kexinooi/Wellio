package my.edu.utar.assignment_2_v2.Mood;

public class Mood {
    private String feel;
    private String mood;
    private String note;

    public Mood() {
        feel=null;
        mood=null;
        note=null;
    }

    public Mood(String feel, String mood, String note){
        this.feel=feel;
        this.mood=mood;
        this.note=note;
    }

    public String getFeel(){
        return feel;
    }

    public String getMood() {
        return mood;
    }

    public String getNote() {
        return note;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public void setFeel(String feel) {
        this.feel = feel;
    }


    public void setNote(String note) {
        this.note = note;
    }

}
