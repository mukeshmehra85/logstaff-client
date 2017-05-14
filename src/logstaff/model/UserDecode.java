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

import com.google.gson.JsonArray;
import java.lang.reflect.Type;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.Iterator;
import java.util.Map;
public class UserDecode implements JsonDeserializer<User>{
    @Override
    public User deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
      throws JsonParseException{
        
        final JsonObject jsonObject = json.getAsJsonObject(); 
        
        final JsonObject juser=jsonObject.get("user").getAsJsonObject();
        
        Organisation[] org = context.deserialize(jsonObject.get("org"), Organisation[].class);
        
        final User user=User.getInstance();
        user.setUid(juser.get("id").getAsInt());
        user.setName(juser.get("name").getAsString());
        user.setEmail(juser.get("email").getAsString());
        user.setDayTime(juser.get("time").getAsInt());
        user.setProfileImg(juser.get("image").getAsString());
        user.setOrg(org);
        return user;
    }
    
}
