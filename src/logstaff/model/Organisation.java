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
public class Organisation {
    private String name;
    private Project[] projects;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Project[] getProjects() {
        return projects;
    }

    public void setProjects(Project[] projects) {
        this.projects = projects;
    }

    @Override
    public String toString() {
        return  name;
    }
    
    
}
