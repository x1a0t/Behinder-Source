package net.rebeyond.behinder.ui.controller;

import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import net.rebeyond.behinder.core.ShellService;
import net.rebeyond.behinder.dao.ShellManager;
import org.json.JSONObject;

public class CmdViewController
{
    private ShellManager shellManager;
    @FXML
    private TextArea cmdTextArea;
    private ShellService currentShellService;
    private JSONObject shellEntity;
    Map<String, String> basicInfoMap;
    private List<Thread> workList;
    private Label statusLabel;
    private int running;
    private int currentPos;
    
    public void init(ShellService shellService, List<Thread> workList, Label statusLabel, Map<String, String> basicInfoMap) {
        this.currentShellService = shellService;
        this.shellEntity = shellService.getShellEntity();
        this.basicInfoMap = basicInfoMap;
        this.workList = workList;
        this.statusLabel = statusLabel;
        initCmdView();
    }

    
    private void initCmdView() {
        String currentPath = this.basicInfoMap.get("currentPath");
        this.cmdTextArea.setText(currentPath + " >");
        this.currentPos = this.cmdTextArea.getLength();
    }
    
    public void onCMDKeyPressed(KeyEvent keyEvent) {
        KeyCode keyCode = keyEvent.getCode();
        if (keyCode == KeyCode.BACK_SPACE)
        {
            if (this.currentPos >= this.cmdTextArea.getCaretPosition())
            {
                keyEvent.consume();
            }
        }
        if (keyCode == KeyCode.ENTER) {
            int lineCount = this.cmdTextArea.getParagraphs().size();
            String lastLine = ((CharSequence)this.cmdTextArea.getParagraphs().get(lineCount - 1)).toString();
            if (lastLine.trim().length() == 0)
            {
                lastLine = ((CharSequence)this.cmdTextArea.getParagraphs().get(lineCount - 2)).toString();
            }
            this.statusLabel.setText("[!]正在执行命令，请稍后……");
            int cmdStart = lastLine.indexOf(">") + 1;
            String cmd = lastLine.substring(cmdStart).trim();
            Runnable runner = () -> {
                    try {
                        JSONObject resultObj = this.currentShellService.runCmd(cmd);
                        String statusText = resultObj.getString("status").equals("success") ? "[+]命令执行成功。" : "[-]命令执行失败。";
                        Platform.runLater(new Runnable()
                                {
                                    public void run() {
                                        CmdViewController.this.statusLabel.setText(statusText);
                                        CmdViewController.this.cmdTextArea.appendText("\n" + resultObj.getString("msg") + "\n");
                                        CmdViewController.this.cmdTextArea.appendText((String)CmdViewController.this.basicInfoMap.get("currentPath") + " >");
                                        CmdViewController.this.currentPos = CmdViewController.this.cmdTextArea.getLength();
                                    }
                                });
                    } catch (Exception e) {
                        Platform.runLater(new Runnable()
                                {
                                    public void run() {
                                        CmdViewController.this.statusLabel.setText("[-]操作失败:" + e.getMessage());
                                    }
                                });
                    } 
                };
            Thread workThrad = new Thread(runner);
            this.workList.add(workThrad);
            workThrad.start();
            keyEvent.consume();
        } 
    }
}