/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package logstaff.model;

/**
 *
 * @author Mukesh
 */
public class Project {
    private int pid;
    private String title;
    private int time;

    public int getId() {
        return pid;
    }

    public void setId(int id) {
        this.pid = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
    
}
