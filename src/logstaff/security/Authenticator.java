/*
 * Copyright (c) 2008, 2012 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package logstaff.security;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

public class Authenticator {
   
    public static String validate(String user, String password){
        String result = "";
        StringBuilder responseSB = new StringBuilder();
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        try {
            String query = "id=" + URLEncoder.encode(user, "UTF-8")
                    + "&pswd=" + URLEncoder.encode(password, "UTF-8")
                    +"&tzdn="+ URLEncoder.encode(Config.timeZone.getDisplayName(), "UTF-8")
                    +"&tzid="+ URLEncoder.encode(Config.timeZone.getID(), "UTF-8");
            
            URL url = new URL(Config.loginPath);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", String.valueOf(query.length()));

            // Write data
            OutputStream os = connection.getOutputStream();
            os.write(query.getBytes());

            // Read response
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line="";
            while ((line = br.readLine()) != null) {  
                responseSB.append(line);
            }

            JsonObject jobj = gson.fromJson(responseSB.toString(), JsonObject.class);
            result = jobj.get("status").getAsString().trim();
           // System.out.println(jobj);
            if(!result.equalsIgnoreCase("true")){
                return jobj.get("msg").getAsString();
            }else{
                Config.userId = jobj.get("id").getAsString();
                return "true";
            }
                                   
        } catch (UnsupportedEncodingException ex) {
            //System.out.println(1);
        } catch (MalformedURLException ex) {
            //System.out.println(2);
        } catch (ProtocolException ex) {
        } catch (IOException ex) {
            result = "Check Internet Connection!";
            return result;
        }
        return result;
    }
}