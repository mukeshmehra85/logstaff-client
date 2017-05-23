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
import java.lang.reflect.Type;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class OrganisationDecode implements JsonDeserializer<Organisation>{
    @Override
    public Organisation deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
      throws JsonParseException{
        final Organisation org=new Organisation();
        
        final JsonObject jsonObject = json.getAsJsonObject();
        //System.out.println("jsonObject "+jsonObject);
        
        org.setName(jsonObject.get("name").getAsString());
        
        Project[] proj = context.deserialize(jsonObject.get("projects"), Project[].class);
        
        org.setProjects(proj);
        return org;
    }
}
