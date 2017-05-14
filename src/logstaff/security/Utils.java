/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logstaff.security;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import encrypt.Encryption;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.imageio.ImageIO;
import logstaff.model.Organisation;
import logstaff.model.OrganisationDecode;
import logstaff.model.Project;
import logstaff.model.ProjectDecoder;
import logstaff.model.User;
import logstaff.model.UserDecode;
import static logstaff.security.Config.imgPath;
import screenCapture.MultipartUtility;

/**
 *
 * @author Mukesh
 */
public class Utils {

    private final String userId = Config.userId;

    private String sessionId = "";
    private String fileName = "";
    private int crtPrjId = -1;// Project id to which logging

    static Logger logger = Logger.getLogger("MyLog");

    public int getCrtPrjId() {
        return crtPrjId;
    }

    public void setCrtPrjId(int crtPrjId) {
        this.crtPrjId = crtPrjId;
    }

    StringBuilder responseSB = new StringBuilder();
    GsonBuilder gsonBuilder = new GsonBuilder();
    Gson gson;

    MessageDigest md5 = null;
    private static Random myRand = null;
    private static final AtomicLong LAST_TIME_MS = new AtomicLong();

    private Encryption enc = new Encryption();
    private MultipartUtility uploader;

    public Utils() {
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
            System.out.print(ex);
        }
        if (myRand == null) {
            myRand = new Random();
        }

        try {
            FileHandler fh = new FileHandler("D:/Final Logstaff/log.log");
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            //logger.setUseParentHandlers(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Fill the user values from the server
    public User loadUser() {

        System.out.println("loadUser called");
        User user = User.getInstance();
        System.out.println("user");
        HttpURLConnection connection;
        responseSB = new StringBuilder();
        try {
            String query = "id=" + URLEncoder.encode(userId, "UTF-8")+"&tzdn="+ URLEncoder.encode(Config.timeZone.getDisplayName(), "UTF-8")
                    +"&tzid="+ URLEncoder.encode(Config.timeZone.getID(), "UTF-8");;

            // System.out.println(query);
            URL url = new URL(Config.updatePath);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", String.valueOf(query.length()));

            // Write data
            OutputStream os = connection.getOutputStream();
            os.write(query.getBytes());
            System.out.println(url);
            // Read response
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line = "";
            while ((line = br.readLine()) != null) {
                responseSB.append(line);
            }

            System.out.println(responseSB);
            gsonBuilder.registerTypeAdapter(User.class, new UserDecode());
            gsonBuilder.registerTypeAdapter(Organisation.class, new OrganisationDecode());
            gsonBuilder.registerTypeAdapter(Project.class, new ProjectDecoder());
            gson = gsonBuilder.create();

            user = gson.fromJson(responseSB.toString(), User.class);
            connection.disconnect();
            //System.out.println(user);
        } catch (MalformedURLException ex) {
            System.out.println(2);
        } catch (IOException ex) {
            System.out.println(ex);
        }
        return user;
    }

    // Function to check if internet is available
    public boolean isInternetReachable(int pid) {
        try {

            URL url = new URL(Config.checkPath);
            HttpURLConnection urlConnect = (HttpURLConnection) url.openConnection();

            String query = "id=" + URLEncoder.encode(userId + "", "UTF-8")
                    + "&pid=" + URLEncoder.encode(pid + "", "UTF-8")+"&tzdn="+ URLEncoder.encode(Config.timeZone.getDisplayName(), "UTF-8")
                    +"&tzid="+ URLEncoder.encode(Config.timeZone.getID(), "UTF-8");

            urlConnect.setDoOutput(true);
            urlConnect.setRequestMethod("POST");
            urlConnect.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnect.setRequestProperty("Content-Length", String.valueOf(query.length()));

            // Write data
            OutputStream os = urlConnect.getOutputStream();
            os.write(query.getBytes());
            //System.out.println(query);

            Object objData = urlConnect.getContent();

        } catch (UnknownHostException e) {
            // e.printStackTrace();
            return false;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
            return false;
        }
        return true;
    }

    // Function to save image at grabs/userid/filename.jpg
    public String saveSnap(String name) {
        try {
            Toolkit tk = Toolkit.getDefaultToolkit();
            Dimension d = tk.getScreenSize();

            Rectangle rec = new Rectangle(0, 0, d.width, d.height);

            Robot ro = new Robot();
            BufferedImage img = ro.createScreenCapture(rec);

            File f = new File("grabs/" + userId + "/" + name + ".png");
            f.getParentFile().mkdirs();
            ImageIO.write(img, "png", f);

            if (uploadGrab(f, sessionId, userId)) {
                System.out.print(f + " uploaded");
                f.delete();
            } else {
                //make an entry to txt file
                saveRecord("snaps", sessionId + "-" + name, false);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return "";
    }

    private Boolean uploadGrab(File file, String sessionid, String userId) {
        Boolean result = false;
        Long fileNumber = Long.valueOf(file.getName().substring(0, 13));
        try {
            uploader = new MultipartUtility(imgPath, Config.charset);
            uploader.addFormField("id", userId + "");
            uploader.addFormField("pid", crtPrjId + "");
            uploader.addFormField("sessionid", sessionid);
            uploader.addFormField("timestamp", formatTS(fileNumber));
            uploader.addFormField("tzdn", "" + Config.timeZone.getDisplayName());
            uploader.addFormField("tzid", "" + Config.timeZone.getID());        
            //uploader.addHeaderField("User-Agent", "LogStaff");
            uploader.addFilePart("fileUpload", file);
            result = uploader.finish();
        } catch (IOException ex) {
            System.out.print("Error in uploading pic " + ex);
        }
        return result;
    }

    // Function to upload Start data to server
    public Boolean uploadStart(int pid) {
        System.out.println("uploadStart currentPid " + pid + " " + sessionId);
        Boolean result = false;

        try {
            String query = "id=" + URLEncoder.encode(userId + "", "UTF-8")
                    + "&pid=" + URLEncoder.encode(pid + "", "UTF-8")
                    + "&sessionid=" + URLEncoder.encode(sessionId, "UTF-8")
                    + "&timestamp=" + URLEncoder.encode(formatTS(uniqueCurrentTimeMS()), "UTF-8")
                    +"&tzdn="+ URLEncoder.encode(Config.timeZone.getDisplayName(), "UTF-8")
                    +"&tzid="+ URLEncoder.encode(Config.timeZone.getID(), "UTF-8");;

            URL url = new URL(Config.startPath);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", String.valueOf(query.length()));

            // Write data
            OutputStream os = connection.getOutputStream();
            os.write(query.getBytes());

            // Read response
            StringBuilder responseSB = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;
            while ((line = br.readLine()) != null) {
                responseSB.append(line);
            }

            if (responseSB.toString().indexOf("true") != -1) {
                result = true;
            }

            // System.out.println("uploadData response " + responseSB);
        } catch (UnsupportedEncodingException ex) {
            System.out.println(1);
        } catch (MalformedURLException ex) {
            System.out.println(2);
        } catch (ProtocolException ex) {
            System.out.println(3);
        } catch (IOException ex) {
            System.out.println(4);
        }

        return result;
    }

    public Boolean uploadStop(String pid) {

        System.out.println("uploadStop currentPid " + pid);
        Boolean result = false;
        try {
            String query = "id=" + URLEncoder.encode(userId + "", "UTF-8")
                    + "&pid=" + URLEncoder.encode(pid, "UTF-8")
                    + "&sessionid=" + URLEncoder.encode(sessionId, "UTF-8")
                    + "&timestamp=" + URLEncoder.encode(formatTS(uniqueCurrentTimeMS()), "UTF-8")
                    +"&tzdn="+ URLEncoder.encode(Config.timeZone.getDisplayName(), "UTF-8")
                    +"&tzid="+ URLEncoder.encode(Config.timeZone.getID(), "UTF-8");

            URL url = new URL(Config.stopPath);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", String.valueOf(query.length()));

            // Write data
            OutputStream os = connection.getOutputStream();
            os.write(query.getBytes());

            // Read response
            StringBuilder responseSB = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;
            while ((line = br.readLine()) != null) {
                responseSB.append(line);
            }

            if (responseSB.toString().indexOf("true") != -1) {
                result = true;
            }

            //System.out.println("uploadData response " + responseSB);
        } catch (UnsupportedEncodingException ex) {
            System.out.println(1);
        } catch (MalformedURLException ex) {
            System.out.println(2);
        } catch (ProtocolException ex) {
            System.out.println(3);
        } catch (IOException ex) {
            System.out.println(4);
        }

        return result;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    // Function to save 1 minute data at app/userid/filname.dat
    public void saveRecord(String name, String JsonData, Boolean encode) {
        String data = "";
        String path = "app/" + userId + "/" + name + ".dat";
        File file = new File(path);
        file.getParentFile().mkdirs();

        try {
            FileWriter writer = new FileWriter(file, true);

            if (encode) {
                data = enc.bytesToHex(enc.encrypt(JsonData));
                writer.write(data);
            } else {
                writer.write(JsonData);
            }
            writer.append("|");
            writer.close();

        } catch (IOException ex) {
            System.out.println(ex);
        } catch (Exception ex) {
            System.out.println(ex);
        }

    }

    // Function to upload segment data to server
    public Boolean uploadData(String data, int pid) {
        Boolean result = false;
        try {
            String query = "id=" + URLEncoder.encode(userId + "", "UTF-8")
                    + "&timeframe=" + URLEncoder.encode(data, "UTF-8")
                    + "&sessionid=" + URLEncoder.encode(sessionId, "UTF-8")
                    + "&timestamp=" + URLEncoder.encode(uniqueCurrentTimeMS() + "", "UTF-8")
                    + "&pid=" + URLEncoder.encode(pid + "", "UTF-8")
                    +"&tzdn="+ URLEncoder.encode(Config.timeZone.getDisplayName(), "UTF-8")
                    +"&tzid="+ URLEncoder.encode(Config.timeZone.getID(), "UTF-8");

            URL url = new URL(Config.dataPath);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", String.valueOf(query.length()));

            // Write data
            OutputStream os = connection.getOutputStream();
            os.write(query.getBytes());

            // Read response
            StringBuilder responseSB = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;
            while ((line = br.readLine()) != null) {
                responseSB.append(line);
            }

            if (responseSB.toString().indexOf("true") != -1) {
                result = true;
            }

            //System.out.println("uploadData response " + responseSB);
        } catch (UnsupportedEncodingException ex) {
            System.out.println(1);
        } catch (MalformedURLException ex) {
            System.out.println(2);
        } catch (ProtocolException ex) {
            System.out.println(3);
        } catch (IOException ex) {
            System.out.println(4);
        }

        return result;
    }

    public String readFile(String file, Boolean offline) {
        String path = "";
        if(offline){
            path = file;
        }else{
            path = "app/" + userId + "/" + file + ".dat";
        }

        StringBuilder str = new StringBuilder("");

        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            String sCurrentLine = "";

            while ((sCurrentLine = br.readLine()) != null) {
                str.append(sCurrentLine);
            }
            br.close();
        } catch (IOException e) {
            //System.out.println("Reading File " + e);
            return "";

        }
        return str.toString();
    }

    public void deleteFile(String name) {

        String path = "app/" + userId + "/" + name + ".dat";
        File file = new File(path);
        System.out.println("File Deleted " + file + " " + file.delete());
    }

    // Function to move file at server/userid/filname.dat to uploas later
    public void moveFile(String name) {
        InputStream inStream = null;
        OutputStream outStream = null;
        try {

            File src = new File("app/" + userId + "/" + name + ".dat");
            File dest = new File("server/" + userId + "/" + name + ".dat");
            dest.getParentFile().mkdirs();
            inStream = new FileInputStream(src);
            outStream = new FileOutputStream(dest);

            byte[] buffer = new byte[1024];

            int length;
            //copy the file content in bytes
            while ((length = inStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, length);
            }
            outStream.write(sessionId.getBytes());
            outStream.flush();

            inStream.close();
            outStream.close();

            //delete the original file
            System.out.println("File is copied successful!");
            src.delete();
        } catch (Exception e) {
            System.out.println("Moving file failed " + e);
            e.printStackTrace();
        }
    }

    public void syncOfflinedata() {
        String snapsPath="app/" + userId+"/snaps.dat";
        String offlineData=readFile(snapsPath,true);   
       
        if(offlineData==""){
            return;
        }
        
        String[] sod= offlineData.split("\\|", -1);
        
        for(int i=0;i<sod.length-1;i++){
           String[] ssimg=sod[i].split("\\-", -1);
           
           File img=new File("grabs/"+userId+"/"+ssimg[1]+".png");
           String dataPath="server/" + userId+"/"+ssimg[1]+".dat";
           if(uploadGrab(img,ssimg[0],userId)){
                img.delete();
           }
            
            if (uploadOffData(readFile(dataPath,true),ssimg[0])) {
                File file = new File(dataPath);
                file.delete();
            }
            if(i==sod.length-2){
                //System.out.println("offline file done complete");
                File file = new File(snapsPath);
                file.delete();
            }
           
        }
    }
    
    // Function to upload offline segment data to server
   public Boolean uploadOffData(String data,String ssid) {
        Boolean result = false;
        try {
            String query = "id=" + URLEncoder.encode(userId + "", "UTF-8")
                    + "&timeframe=" + URLEncoder.encode(data, "UTF-8")
                    + "&sessionid=" + URLEncoder.encode(ssid, "UTF-8")
                    +"&tzdn="+ URLEncoder.encode(Config.timeZone.getDisplayName(), "UTF-8")
                    +"&tzid="+ URLEncoder.encode(Config.timeZone.getID(), "UTF-8");
                  //  + "&timestamp=" + URLEncoder.encode(uniqueCurrentTimeMS() + "", "UTF-8")
                 //   + "&pid=" + URLEncoder.encode(pid + "", "UTF-8");

            URL url = new URL(Config.offlinePath);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", String.valueOf(query.length()));

            // Write data
            OutputStream os = connection.getOutputStream();
            os.write(query.getBytes());

            // Read response
            StringBuilder responseSB = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;
            while ((line = br.readLine()) != null) {
                responseSB.append(line);
            }

            if (responseSB.toString().indexOf("true") != -1) {
                result = true;
            }

            System.out.println("upload offline Data response " + responseSB);
        } catch (UnsupportedEncodingException ex) {
            System.out.println(1);
        } catch (MalformedURLException ex) {
            System.out.println(2);
        } catch (ProtocolException ex) {
            System.out.println(3);
        } catch (IOException ex) {
            System.out.println(4);
        }
        return result;
    }

    public String getMd5(String str) {
        md5.update(str.toString().getBytes());
        byte[] array = md5.digest();
        StringBuffer sb2 = new StringBuffer();
        for (int j = 0; j < array.length; ++j) {
            int b = array[j] & 0xFF;
            sb2.append(Integer.toHexString(b));
        }
        int begin = myRand.nextInt();
        if (begin < 0) {
            begin = begin * -1;
        }
        begin = begin % 8;
        return sb2.toString().substring(begin, begin + 15).toUpperCase();
    }

    public static long uniqueCurrentTimeMS() {
        long now = System.currentTimeMillis();

        while (true) {
            long lastTime = LAST_TIME_MS.get();
            if (lastTime >= now) {
                now = lastTime + 1;
            }
            if (LAST_TIME_MS.compareAndSet(lastTime, now)) {
                return now;
            }
        }
    }

    public static void trace(String msg) {
        logger.info(msg);
    }

    // Converts ctr(secs) into HH:MM:SS format 
    public String formatTime(long ctr) {
        int hours = 0;
        int minutes = 0;
        long seconds;

        if (ctr >= 3600) {
            hours = (int) (ctr / 3600);
            ctr = ctr % 3600;
        }

        if (ctr >= 60) {
            minutes = (int) (ctr / 60);
            ctr = ctr % 60;
        }
        seconds = ctr;

        String h = "";
        String m = "";
        String s = "";

        h = (hours < 10) ? ("0" + hours) : (hours + "");
        m = (minutes < 10) ? ("0" + minutes) : (minutes + "");
        s = (seconds < 10) ? ("0" + seconds) : (seconds + "");

        return h + ":" + m + ":" + s;
    }

    // Converts timestamp into yyyy-MM-dd HH:mm:ss format 
    public String formatTS(Long ts) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date resultdate = new Date(ts);
        return (sdf.format(resultdate));
    }

    public static int getRandomInteger(int aStart, int aEnd) {
        if (aStart > aEnd) {
            throw new IllegalArgumentException("Start cannot exceed End.");
        }
        //get the range, casting to long to avoid overflow problems
        long range = (long) aEnd - (long) aStart + 1;
        // compute a fraction of the range, 0 <= frac < range
        long fraction = (long) (range * myRand.nextDouble());
        int randomNumber = (int) (fraction + aStart);
        return randomNumber;
    }

}// End Of Class
