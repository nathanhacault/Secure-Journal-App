package secureJournal.model;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;


@Entity(name = "entries")
public class Entry { //class for journal Entries

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id; //variable for the journal ID, auto generated

    @Column(name = "date", nullable = false)
    private String date; //string variable for data of the entry

    @Column(name = "text", nullable = false, columnDefinition = "TEXT")
    private String text; //variable for journal text

    @Lob
    @Column(name = "img", nullable = false)
    private char[] img; //variable for journal img.


    @Column(name="userID",nullable = false)
    private long userID; //variable for the userID attached to an entry


    public Entry(String text, String date, char[] img) { //Constructor for entry object.
        this.text = text;
        this.date = date;
        this.img = img;
    }

    public Entry() { } // basic constructor

    public void setDate(String date) { //setter function
        this.date = date;
    }

    public long getId() { return id; } //getter function

    public String getText() { return text; } //getter function

    public void setUserID(long userID) {
        this.userID = userID;
    } //setter function

    public void setText(String text) {
        this.text = text;
    } //setter function


    public String getDate() {
        return date;
    } //getter function

    public String getImg() { return new String(img); } //getter function

    public void setImg(String img) {
        this.img = img.toCharArray();
    } //setter function

    @Override
    public String toString() { //to string method for the entry class
        return "Contact{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", date='" + date + '\'' +
                ", img='" + Arrays.toString(img) + '\'' +
                '}';
    }
}
