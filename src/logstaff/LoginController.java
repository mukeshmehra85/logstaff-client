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
package logstaff;

import java.awt.Color;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

/**
 * Login Controller.
 */
public class LoginController extends AnchorPane implements Initializable {

    @FXML
    TextField usrId;
    @FXML
    PasswordField usrPwd;
    @FXML
    Button btnLogin;
    @FXML
    Label errMsg;

    private Main application;
    private Service<Void> loginService;
    
    public void setApp(Main application){
        this.application = application;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        errMsg.setText("");
        errMsg.setOpacity(0);
        usrId.setPromptText("username");
        usrPwd.setPromptText("password");
        
    }
    
    @FXML
    private void loginMe(ActionEvent event) {

        btnLogin.setDisable(true);
        errMsg.setText("Signing in...");
        doLogin();
    }
    
     private void doLogin() {
         if (application == null){
            errMsg.setText("Hello ");
            return;
        } 
        usrId.setDisable(true);
        usrPwd.setDisable(true);
        
        loginService = new Service<Void>() {

            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {  
                        try {
                            String response = application.userLogging(usrId.getText(), usrPwd.getText()).trim();
                            if (response.equalsIgnoreCase("No_Network")) {
                                Platform.runLater(() -> errMsg.setText("Check Internet Connection!"));
                            } else {
                                Platform.runLater(() -> errMsg.setText(response));
                            }
                            Platform.runLater(() -> {
                                animateMessage();
                            });
                        } catch (Exception ex) {
                            System.out.print("Exception Login Controller" + ex);
                        }
                        return null;
                    }
                };
            }
        };

        loginService.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                btnLogin.setDisable(false);
            }
        });

        loginService.restart();
    }
    
    @FXML
    private void onEnter(KeyEvent event) {
         errMsg.setText("");
         errMsg.setOpacity(0);
        if(!"".equals(usrId.getText().trim())&& !"".equals(usrPwd.getText().trim())){
             btnLogin.setDisable(false);
        }else{
            return;
        }
        if (event.getCode() == KeyCode.ENTER) {
             btnLogin.setDisable(true);
             errMsg.setText("Signing in...");
             doLogin();
        }
    }
    
    @FXML
    private void quitApp(MouseEvent me){
        System.exit(0);
    }
    
    @FXML
    private void forgetPsw(MouseEvent me){
        //System.exit(0);
    }
    
    private void animateMessage() {
        FadeTransition ft = new FadeTransition(Duration.millis(1000), errMsg);
        ft.setFromValue(0.0);
        ft.setToValue(1);
        ft.play();
        usrId.setDisable(false);
        usrPwd.setDisable(false);
    }
}
