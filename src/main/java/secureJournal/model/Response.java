package secureJournal.model;


public class Response { //class to create response for the entry controller
    private Object data;
    private String message;

    public Response(Object data, String message) { //constructor
        this.data = data;
        this.message = message;
    }

    public Object getData() {
        return data;
    } //getter and setter

    public void setData(Object data) {
        this.data = data;
    }


}
