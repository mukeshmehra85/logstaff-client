/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package logstaff.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;

/**
 *
 * @author Mukesh
 */
public class ProjectDecoder implements JsonDeserializer<Project>{

    @Override
    public Project deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
       final Project proj=new Project();
       final JsonObject jsonObject = json.getAsJsonObject();
       
       // System.out.println("new "+jsonObject.get("time").getAsInt());
        
        proj.setId(jsonObject.get("id").getAsInt());
        proj.setTitle(jsonObject.get("title").getAsString());
        proj.setTime(jsonObject.get("time").getAsInt());
       return proj;
    }
    
}
