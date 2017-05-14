/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package record;

import java.util.HashMap;

/**
 *
 * @author Mukesh
 */
public class DayRecord {

    private static HashMap<Integer, Integer> data;
    private int totalTime;
    private static DayRecord instance=null;
    
    public DayRecord() { 
    }
    
    public static DayRecord getInstance(){
        if(instance==null){
            instance= new DayRecord();
            data=new HashMap<Integer, Integer>();
        }
        return instance;
    }

    public void updateTime(int id, int time) {
        if (data.containsKey(id)) {
            data.put(id, time + data.get(id));
        } else {
            data.put(id, time);
        }
        getTotalTime();
    }

    public int getTimeById(int id) {
        return data.get(id);
    }

    public HashMap<Integer, Integer> getData() {
        return data;
    }

    public int getTotalTime() {
        totalTime = 0;
        for (Integer value : data.values()) {
            totalTime += value;
        }
        return totalTime;
    }

    @Override
    public String toString() {
        return "DayRecord{" + "totalTime=" + getTotalTime() + '}';
    }
    
}
