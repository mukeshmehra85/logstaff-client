package logstaff.model;

public class User {

    private int uid;
    private String name;
    private String email;
    
    private int dayTime;
    private String profileImg;
   
    private static User instance=null;
    
    private Organisation[] org;

    public Organisation[] getOrg() {
        return org;
    }

    public void setOrg(Organisation[] org) {
        this.org = org;
    }
    
    private User(){}
    
    public static User getInstance(){
        if(instance==null){
            instance= new User();
        }
        return instance;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public int getDayTime() {
        return dayTime;
    }

    public void setDayTime(int dayTime) {
        this.dayTime = dayTime;
    }

    public String getProfileImg() {
        return profileImg;
    }

    public void setProfileImg(String profileImg) {
        this.profileImg = profileImg;
    }

    @Override
    public String toString() {
        return "User{" + "uid=" + uid + ", name=" + name + ", email=" + email +", dayTime=" + dayTime + ", profileImg=" + profileImg + ", org=" + org + '}';
    }
    
    
}
