/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logstaff.security;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.TimeZone;

/**
 *
 * @author mmehra
 */
public class Config {

    public static final String basepath = "http://beta.logstaff.com/services/api/v1/";
    public static final String loginPath = basepath + "login";
    public static final String imgPath = basepath + "saveScreen";
    public static final String dataPath = basepath + "saveActivity";
    public static final String updatePath = basepath + "refresh";

    public static final String offlinePath = basepath + "saveOffActivity";
    public static final String startPath = basepath + "startLogger";
    public static final String stopPath = basepath + "stopLogger";
    public static final String checkPath = basepath + "checkNet";

    public static final String localImagesDir = "app";

    public static final TimeZone timeZone = TimeZone.getDefault();

    public static Proxy proxy = null;//new Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.8.103.3", 100));

    public static String userId = null;

    public static String charset = "UTF-8";

    

    public Config() {
        
    }

    

}
