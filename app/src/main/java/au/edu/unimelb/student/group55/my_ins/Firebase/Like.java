package au.edu.unimelb.student.group55.my_ins.Firebase;


// The class for like event
public class Like {
    private String user_id;

    public Like(String user_id) {
        this.user_id = user_id;
    }

    public Like() {

    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    @Override
    public String toString() {
        return "Like{" +
                "user_id='" + user_id + '\'' +
                '}';
    }
}
