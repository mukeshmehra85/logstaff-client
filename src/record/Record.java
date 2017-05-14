/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package record;

/**
 *
 * @author Mukesh
 */
public class Record {
    private int pid=0;
    private String uid="";
    private String ts;
    private int userActivity;
    private String imgName;
    private String notes;
    private int mouse;
    private int keys;
    public Record() {
       
    }

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }


    public int getId() {
        return pid;
    }

    public void setId(int id) {
        this.pid = id;
    }

        
    public String getImgName() {
        return imgName;
    }

    public void setImgName(String imgName) {
        this.imgName = imgName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
 
    
    public void setRecord(String uid,int pid,int userActivity,int ms,int keys,String ts,String notes){
        this.pid=pid;
        this.userActivity = userActivity;
        this.ts=ts;
        this.uid=uid;
        this.notes=notes;
        this.keys=keys;
        this.mouse=ms;
    }
}
