/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
 */
package logstaff;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import logstaff.model.User;
import logstaff.security.Authenticator;

/**
 * Main Application. This class handles navigation and user session.
 */
public class Main extends Application {

    private Stage stage;
    private User loggedUser;
    private final double MINIMUM_WINDOW_WIDTH = 260.0;
    private final double MINIMUM_WINDOW_HEIGHT = 400.0;
    private TrayIcon trayIcon;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Application.launch(Main.class, (java.lang.String[])null);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            stage = primaryStage;
            stage.setTitle("Log Staff");
            stage.setResizable(false);
            stage.setMinWidth(MINIMUM_WINDOW_WIDTH);
            stage.setMinHeight(MINIMUM_WINDOW_HEIGHT);
            stage.getIcons().add(new Image(Main.class.getResourceAsStream("/images/icon.PNG" ))); 
            gotoLogin();
            primaryStage.show();
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        Platform.setImplicitExit(false);
        createTrayIcon(primaryStage);
    }

    public User getLoggedUser() {
        return loggedUser;
    }
    
    public void setLoggedUser(User user) {
        loggedUser=user;
    }
        
    public String userLogging(String userId, String password){
       // hideApp();
        String result=Authenticator.validate(userId, password);
        if (result.equalsIgnoreCase("true")) {
             Platform.runLater(() -> {
                 gotoProfile();
             });
           
           return "Loading profile...";
        }
        return result;
    }
    
    public void userLogout(){
        loggedUser = null;
        gotoLogin();
    }
    
    private void gotoProfile() {
        try {
            ProfileController profile = (ProfileController) replaceSceneContent("profile.fxml");
            profile.setApp(this);
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void gotoLogin() {
        try {
            LoginController login = (LoginController) replaceSceneContent("login.fxml");
            login.setApp(this);
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Initializable replaceSceneContent(String fxml) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        InputStream in = Main.class.getResourceAsStream(fxml);
        loader.setBuilderFactory(new JavaFXBuilderFactory());
        loader.setLocation(Main.class.getResource(fxml));
        AnchorPane page;
        try {
            page = (AnchorPane) loader.load(in);
        } finally {
            in.close();
        } 
        Scene scene = new Scene(page, MINIMUM_WINDOW_WIDTH, MINIMUM_WINDOW_HEIGHT);
        stage.setScene(scene);
        
        stage.widthProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                     stage.setMinWidth(MINIMUM_WINDOW_WIDTH);
                     stage.setMinHeight(MINIMUM_WINDOW_HEIGHT);
                }
            });

            stage.heightProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                    stage.setMinWidth(MINIMUM_WINDOW_WIDTH);
                     stage.setMinHeight(MINIMUM_WINDOW_HEIGHT);
                }
            });

            
        stage.sizeToScene();
        stage.centerOnScreen();
        return (Initializable) loader.getController();
    }
    
    public void createTrayIcon(final Stage stage) {
		if (SystemTray.isSupported()) {
			// get the SystemTray instance
			SystemTray tray = SystemTray.getSystemTray();
			// load an image
			java.awt.Image image = Toolkit.getDefaultToolkit().getImage(Main.class.getResource("/images/icon.gif"));
			stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent t) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							if (SystemTray.isSupported()) {
								showProgramIsMinimizedMsg();
							} else {
								new Timer().schedule(new TimerTask() {
                                                                    public void run () { Platform.exit(); }
                                                                }, 10000);
							}
						}
					});
				}
			});
			// create a action listener to listen for default action executed on the tray icon
			final ActionListener closeListener = new ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.exit(0);
				}
			};

			ActionListener showListener = new ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
                                                        stage.sizeToScene();
                                                        stage.setMinWidth(MINIMUM_WINDOW_WIDTH);
                                                        stage.setMinHeight(MINIMUM_WINDOW_HEIGHT);
							stage.show();
                                                  
						}
					});
				}
			};

			// create a popup menu
			PopupMenu popup = new PopupMenu();

			MenuItem showItem = new MenuItem("Show");
			showItem.addActionListener(showListener);
			popup.add(showItem);

			MenuItem closeItem = new MenuItem("Exit");
			closeItem.addActionListener(closeListener);
			popup.add(closeItem);

			trayIcon = new TrayIcon(image, "LogStaff", popup);
                        trayIcon.setImageAutoSize(true);
			//trayIcon.addActionListener(showListener);
			trayIcon.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(java.awt.event.MouseEvent e) {
					//System.out.println(e.getButton());
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							if(e.getButton()!=3){
                                                        stage.sizeToScene();
                                                        stage.setMinWidth(MINIMUM_WINDOW_WIDTH);
                                                        stage.setMinHeight(MINIMUM_WINDOW_HEIGHT);
							stage.show();
                                                        }
							
						}
					});
				}
			});
			
			try {
				tray.add(trayIcon);
			} catch (AWTException e) {
				System.err.println(e);
			}

		}
	}

	public void showProgramIsMinimizedMsg() {
		trayIcon.displayMessage("LogStaff.", "Application is still running.You can access from here.", TrayIcon.MessageType.INFO);
	}
        
        public void hideApp() {
		stage.hide();
	}
        
        public void showApp() {
		stage.show();
	}
}
