package logstaff;

import com.google.gson.Gson;
import customList.ListRow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;

import logstaff.model.Organisation;
import logstaff.model.Project;
import logstaff.model.User;
import logstaff.security.Utils;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.util.Callback;
import javafx.util.Duration;
import keyLogger.KeyDispatcher;
import logstaff.security.Config;

import org.jnativehook.GlobalScreen;
import record.Record;

/**
 * Profile Controller.
 */
public class ProfileController extends AnchorPane implements Initializable {

    @FXML
    ListView projList;

    @FXML
    ToggleButton toggle;

    @FXML
    ComboBox orgsComb;

    @FXML
    Label timeLbl;

    @FXML
    Label crtProj;

    @FXML
    Label totTime;

    @FXML
    Parent root;

    @FXML
    private Group notesDlg;

    @FXML
    private TextArea notesTxt;

    @FXML
    private Button notesDone;

    @FXML
    private Button addNoteBtn;
    
    @FXML
    private Shape netStatus;
    
    @FXML
    private ImageView profilePic;
    
    @FXML
    
    private Group blank_mc;
    
    private String notesStr = "";
    private Main application;
    private Boolean freshlogin = true; 
    private Font font;
    
    Timeline oneSecondTimer;
    private boolean online=true;
    private boolean logging=false;
    
    private int totalTime=0;
    private int prjTime=0;

    private long ctr=0; //  variable which will be incremented every second irrespective of logging.
    private long mintCtr=0;  // variable which will be incremented only when time is logged.
    private int timeInterval=5; // time interval for segmented data
    private int imageTime=-1;
    
    
    private String crtOrgName="";
    private int crtOrgIndex=0; // Combobox org selection index
    private int crtPrjIndex=0; // Combobox project selection index
    
    
    private int crtPrjId=-1;// Project id to which logging
    
    public String sessionId="";
    public String fileName="";
    
    private Utils util= new Utils();
    private KeyDispatcher kd;
     
    User user;
    
    private Record record;
    Gson gson = new Gson();
    
    private javafx.concurrent.Service<Boolean> startService;
    private javafx.concurrent.Service<Boolean> updateService;
    private javafx.concurrent.Service<String>  imgService;
    private javafx.concurrent.Service<Boolean> closeService;
    private javafx.concurrent.Service<Boolean> refreshService;
    private javafx.concurrent.Service<Boolean> offlineService;
    
     
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addNoteBtn.setDisable(true);
        notesDone.setVisible(false);
        blank_mc.setVisible(false);
        
        font = Font.loadFont(getClass().getResourceAsStream("/images/LCD-BOLD.TTF"), 30);
        timeLbl.setFont(font);
        
        record=new Record();
        
    }
    
    
    public void setApp(Main application){
        this.application = application; 
        user = util.loadUser();
        application.setLoggedUser(user);
        populateCombo();
        
        totalTime=user.getDayTime();
        oneSecondTimer = new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                tick();
            }
        }));
        oneSecondTimer.setCycleCount(Timeline.INDEFINITE);
        oneSecondTimer.play();
        
        kd = new KeyDispatcher();
        GlobalScreen.getInstance().addNativeKeyListener(kd);
        GlobalScreen.getInstance().addNativeMouseListener(kd);
        GlobalScreen.getInstance().addNativeMouseMotionListener(kd);
        GlobalScreen.getInstance().addNativeMouseWheelListener(kd);
        
        profilePic.setImage(new Image(user.getProfileImg()));
        updateScreen();
       //fnOfflineService();
    }
    
    public void tick(){
        ++ctr;
        
        //one minute universal interval
        if(ctr%60==0){
            if(logging){
                online=util.isInternetReachable(crtPrjId);
            }else{
                online=util.isInternetReachable(-1);
            }
            
            if(online){
                netStatus.setFill(Color.GREEN);
                fnOfflineService();
            }else{
                netStatus.setFill(Color.RED);
            } 
            
        }
        
        if(logging){
            mintCtr++;
            totalTime++;
            prjTime++;
            kd.setKey();
            updateScreen();
            
        }
        
        //one minute logging interval
        if((mintCtr>0)&&mintCtr%60==0){
            --imageTime;
           saveOneMinuteAct();
        }
       
       
        //1 segment interval
       if((mintCtr>0)&&mintCtr%(60*timeInterval)==0){
            imageTime= Utils.getRandomInteger(1,timeInterval);
            sendDataService();
        }
     
        if(imageTime==0){
            System.out.println(" Taking SS "+imageTime);
            imageTime=-1;
            //Take and upload ScreenShot
            uploadImgService();
        }
        
       
        
        
    }
    
    private void saveOneMinuteAct(){
        int temp[]=kd.getUserAct();
        record.setRecord(Config.userId, crtPrjId,(temp[2]), temp[0], temp[1],""+util.formatTS(util.uniqueCurrentTimeMS()), notesStr);
        String json = gson.toJson(record);
        util.saveRecord(fileName, json, false);
        notesStr = "";
        kd.reset();
    }
    
    
    private void updateScreen() {
        totTime.setText(util.formatTime(totalTime));
        timeLbl.setText(util.formatTime(prjTime));
    }
    
    public void processLogout(ActionEvent event) {
        if (application == null){
            // We are running in isolated FXML, possibly in Scene Builder.
            // NO-OP.
            return;
        }
        
        //application.userLogout();
    }
    
    public void saveProfile(ActionEvent event) {
        if (application == null){
            return;
        }
        User loggedUser = application.getLoggedUser();
    }
    
    @FXML
    private void toggleTimer(ActionEvent ae) {
       if (toggle.isSelected()) {
            logging=true;
            toggle.setText("OFF");
            fileName = "" + util.uniqueCurrentTimeMS();
            sessionId= ""+util.getMd5(fileName);
            util.setFileName(fileName);
            util.setSessionId(sessionId);
            imageTime= Utils.getRandomInteger(1,timeInterval);
            startDataService();
            kd.reset();
            addNoteBtn.setDisable(false);
        } else {
            addNoteBtn.setDisable(true);
            toggle.setText("ON");
            if(imageTime>0){
               //System.out.println(" Sending image before close "+imageTime);
                imageTime=-1;
                //Take and upload ScreenShot
                uploadImgService();
            }
            saveOneMinuteAct();
            sendDataService();
            stopDataService(crtPrjId + "");
            logging=false;
            freshlogin = false;
        }
    }
    
    @FXML
    public void addNote(ActionEvent ae) {
        notesTxt.setText("");
        notesDlg.setVisible(true);
        addNoteBtn.setVisible(false);
        notesDone.setVisible(true);
    }

    @FXML
    private void closeNote(ActionEvent ae) {
        if (!"".equals(notesTxt.getText().trim())) {
            notesStr = (notesTxt.getText());
        }
        notesDlg.setVisible(false);
        notesDone.setVisible(false);
        addNoteBtn.setVisible(true);
    }
    
    @FXML
    private void quitApp(MouseEvent me){
        new Timer().schedule(new TimerTask() {
            public void run () 
            {
                Platform.runLater(() -> {
                 application.userLogout();
             });
            }
        }, 5000);
        
            blank_mc.setOpacity(0.0);
            animateMessage();
            blank_mc.setVisible(true);  
            addNoteBtn.setDisable(true);
            toggle.setText("ON");
            if(logging){
                if(imageTime>0){
                    imageTime=-1;
                    uploadImgService();
                }
                stopDataService(crtPrjId + "");
                saveOneMinuteAct();
                sendDataService();
            }
            
    }

    private void populateCombo() {
        totalTime=user.getDayTime();
        // Organisation Combo 
        orgsComb.setItems(null);
        projList.setItems(null);
        ObservableList<Organisation> combItems = FXCollections.observableArrayList(user.getOrg());

        orgsComb.setItems(combItems);
        orgsComb.valueProperty().addListener(comboListener);
        orgsComb.setCellFactory(new Callback<ListView<Organisation>, ListCell<Organisation>>() {

            @Override
            public ListCell<Organisation> call(ListView<Organisation> p) {

                final ListCell<Organisation> cell = new ListCell<Organisation>() {

                    @Override
                    protected void updateItem(Organisation t, boolean bln) {
                        super.updateItem(t, bln);
                        super.updateItem(t, bln);

                        if (t != null) {
                            setText(t.getName());
                        } else {
                            setText(null);
                        }
                    }
                };

                return cell;
            }
        });

        orgsComb.setPrefWidth(250);
       

        if (freshlogin) {
            orgsComb.getSelectionModel().selectFirst(); //select the first element
        } else {
            orgsComb.getSelectionModel().select(crtOrgIndex);
        }

        Project[] proj = user.getOrg()[crtOrgIndex].getProjects();
        populateList(proj);
    }
    
    // Organisational Combo Change listener
    private final ChangeListener<Organisation> comboListener = new ChangeListener<Organisation>() {
        @Override
        public void changed(ObservableValue ov, Organisation t, Organisation t1) {
            if (toggle.isSelected() || t1 == null) {
                return;
            }

            crtOrgIndex = orgsComb.getSelectionModel().getSelectedIndex();
            if (crtOrgIndex < 0) {
                crtOrgIndex = 0;
            }
            crtOrgName = t1.getName();

            Project[] proj = user.getOrg()[crtOrgIndex].getProjects();
            populateList(proj);
        }
    };
    
    private void populateList(Project[] proj) {

        ObservableList<Project> Listitems = FXCollections.observableArrayList(proj);
        projList.setItems(Listitems);

        projList.setCellFactory(new Callback<ListView<Project>, javafx.scene.control.ListCell<Project>>() {
            @Override
            public ListCell<Project> call(ListView<Project> listView) {
                return new ListRow();
            }
        });

        if (freshlogin) {
            projList.getSelectionModel().selectFirst();
        }else{
            projList.getSelectionModel().select(crtPrjIndex);
        }
        
        Project temp = (Project) projList.getSelectionModel().getSelectedItem();
        crtProj.setText(crtOrgName + " » " + temp.getTitle());
        prjTime = temp.getTime();
        crtPrjId=temp.getId();
        updateScreen();
        util.setCrtPrjId(crtPrjId);
            
        projList.setPrefWidth(250);
        projList.setPrefHeight(150);

        projList.getSelectionModel().selectedItemProperty().addListener(projListener);
    }

    
     private final ChangeListener<Project> projListener = (ObservableValue<? extends Project> observable, Project oldValue, Project temp) -> {
        if (toggle.isSelected() || temp == null) {
            return;
        }
        
        crtPrjIndex=projList.getSelectionModel().getSelectedIndex();
        
        crtPrjId=temp.getId();
        prjTime = temp.getTime();
        crtProj.setText(crtOrgName + " » " + temp.getTitle());
        prjTime = temp.getTime();
        crtPrjId=temp.getId();
        util.setCrtPrjId(crtPrjId);
        updateScreen();
    };

    
    private void animateMessage() {
        FadeTransition ft = new FadeTransition(Duration.millis(2000), blank_mc);
        ft.setFromValue(0.0);
        ft.setToValue(1);
        ft.setCycleCount(1);
       // ft.setAutoReverse(true);
        ft.play();
    }
     
     /* 
        *Various services to communicate with the server while logging
     */
     
     //service to update start data to server 
    private Boolean startDataService() {

        System.out.println("startDataService called");
        startService = new javafx.concurrent.Service<Boolean>() {

            @Override
            protected Task<Boolean> createTask() {
                return new Task<Boolean>() {
                    @Override
                    protected Boolean call() throws Exception {

                        return util.uploadStart(crtPrjId);
                    }
                };
            }
        };

        startService.restart();

        startService.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                //System.out.print("Data update complete");
                Boolean result = (Boolean) (t.getSource().getValue());
                if (result) {
                    //updateDataService();
                }

            }
        });
        return true;
    }
    
    //update current app data to server 
    private void sendDataService() {
        System.out.println("sendDataService called");
        updateService = new javafx.concurrent.Service<Boolean>() {

            @Override
            protected Task<Boolean> createTask() {
                return new Task<Boolean>() {
                    @Override
                    protected Boolean call() throws Exception {

                        if (!util.uploadData(util.readFile(fileName, false),crtPrjId)) {
                            System.out.println("data uploaded failed moving file");
                            util.moveFile(fileName);
                        } else {
                            System.out.println("data uploaded deleting file");
                            util.deleteFile(fileName);
                        }
                        fileName = "" + util.uniqueCurrentTimeMS();
                        return true;
                    }
                };
            }
        };

        updateService.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                System.out.println("Data update completed to sever");
            }
        });

        updateService.restart();
        
    }
     
    //upload image service 
    private void uploadImgService() {

        //System.out.println("uploadImgService called");
        imgService = new javafx.concurrent.Service<String>() {

            @Override
            protected Task<String> createTask() {
                return new Task<String>() {
                    @Override
                    protected String call() throws Exception {

                        return util.saveSnap(fileName);
                    }
                };
            }
        };

        imgService.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                System.out.println("image uploaded");
            }
        });

        imgService.restart();

    }
     
    
    //service to update data on stop to server 
    private Boolean stopDataService(String id) {
        new Timer().schedule(new TimerTask() {
            public void run () 
            {
                
                closeService.restart();
            }
        }, 3000);
        
        closeService = new javafx.concurrent.Service<Boolean>() {

            @Override
            protected Task<Boolean> createTask() {
                return new Task<Boolean>() {
                    @Override
                    protected Boolean call() throws Exception {
                        System.out.println("stopDataService called");
                        return util.uploadStop(id);
                    }
                };
            }
        };


        closeService.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                System.out.println("Close Service succeded ");
                crtPrjId=-1;
                util.setCrtPrjId(crtPrjId);
                logging=false;
                updateDataService();
                /*
                Boolean result = (Boolean) (t.getSource().getValue());
                if (result) {
                    updateDataService();
                    util.setSessionId(null);
                }*/

            }
        });
        return true;
    }
     
    //update current app data from server 
    private void updateDataService() {
        
        user = util.loadUser();
        System.out.println("After Refresh complete "+user);
        populateCombo();
        /*
        refreshService = new javafx.concurrent.Service<Boolean>() {
           
            @Override
            protected Task<Boolean> createTask() {
                return new Task<Boolean>() {
                    @Override
                    protected Boolean call() throws Exception {
                        // user = util.loadUser();
                         System.out.println("After Refresh complete "+user);
                        //freshlogin = true;
                         populateCombo();
                        return true;
                    }
                };
            }
        };

        refreshService.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                System.out.println("Data Refresh complete");
                //String temp = (t.getSource().getValue()).toString();
               // freshlogin = true;
               // populateCombo();
            }
        });

        refreshService.restart();
                */
    }
    //update current app data from server 
    private void fnOfflineService() {
        System.out.println("offline Service called");
        offlineService = new javafx.concurrent.Service<Boolean>() {
           
            @Override
            protected Task<Boolean> createTask() {
                return new Task<Boolean>() {
                    @Override
                    protected Boolean call() throws Exception {
                        util.syncOfflinedata();
                        return true;
                    }
                };
            }
        };

        offlineService.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                System.out.println("offline data complete");
            }
        });

        offlineService.restart();
        
    }  
}// End of Class