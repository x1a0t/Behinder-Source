package net.rebeyond.behinder.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import net.rebeyond.behinder.core.ShellService;
import org.json.JSONObject;

import java.util.List;

public class ZipViewController {
    @FXML
    public TextField sourceDirPathInput;
    @FXML
    public TextField zipFilePathInput;
    @FXML
    public TextField excludeExtInput;
    @FXML
    public Button submit;

    private ShellService currentShellService;
    private JSONObject shellEntity;
    private Label statusLabel;
    private List<Thread> workList;

    public void init(ShellService shellService, List<Thread> workList, Label statusLabel) {
        this.currentShellService = shellService;
        this.shellEntity = shellService.getShellEntity();
        this.statusLabel = statusLabel;
        this.workList = workList;
        this.initZipView();
    }

    private void initZipView() {
        this.submit.setOnAction(
                (event) -> {
                    if (!this.sourceDirPathInput.getText().equals("")
                                && !this.zipFilePathInput.getText().equals("")
                                && !this.excludeExtInput.getText().equals("")
                    ) {
                        try {
                            JSONObject jsonObject = this.currentShellService.zipCompress(
                                    this.sourceDirPathInput.getText().replace("\\", "/"),
                                    this.zipFilePathInput.getText().replace("\\", "/"),
                                    this.excludeExtInput.getText()
                            );
                            this.statusLabel.setText(jsonObject.get("status")+": "+jsonObject.get("msg"));
                        } catch (Exception e) {
                            this.statusLabel.setText(e.getMessage());
                        }
                    }
        });
    }
}
