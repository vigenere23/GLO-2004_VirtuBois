package presentation.controllers;

import domain.controllers.LarmanController;
import domain.entities.Bundle;
import domain.entities.Yard;
import helpers.JavafxHelper;
import javafx.event.ActionEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import presentation.Main;

import java.io.*;
import java.util.Map;

public abstract class BaseController implements IController {

    protected Stage stage;
    public LarmanController larmanController;
    public File lastPath;

    public BaseController() {
        larmanController = LarmanController.getInstance();
        lastPath = null;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void newFile(ActionEvent actionEvent) {
        JavafxHelper.loadView(this.stage, "Main", "Nouvelle Cour", true);
    }

    public void openFile(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SER", "*.ser"));
        fileChooser.setTitle("Open File");
        File file = fileChooser.showOpenDialog(stage);
        lastPath = file;
        FileInputStream fileInputStream;
        ObjectInputStream objectInputStream;
        try {
            fileInputStream = new FileInputStream(file);
            objectInputStream = new ObjectInputStream(fileInputStream);
            Yard yardInit = (Yard) objectInputStream.readObject();
            LarmanController.getInstance().setYard(yardInit);
            JavafxHelper.loadView(this.stage, "Main", file.getName(), true);
        } catch (IOException | ClassNotFoundException ex){
            System.out.println(ex);
        }
    }
    public void saving(File file) {
        Yard yard;
        yard = LarmanController.getInstance().getYard();
        FileOutputStream fileOutputStream;
        ObjectOutputStream objectOutputStream;
        try{
            fileOutputStream = new FileOutputStream(file);
            objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(yard);
            objectOutputStream.flush();
            objectOutputStream.close();
        } catch (IOException ex){
            System.out.println(ex);
        }

    }
    public void saveAs(ActionEvent actionEvent){
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SER", "*.ser"));
        fileChooser.setTitle("Save As");
        File file = fileChooser.showSaveDialog(stage);
        lastPath = file;
        saving(file);
    }

    public void save(ActionEvent actionEvent){
        if (lastPath == null){
            saveAs(actionEvent);
        } else {
            saving(lastPath);
        }

    }

    public void quit(ActionEvent actionEvent) {
        JavafxHelper.quitApplication();
    }

    public void handleMenuHelpAbout(ActionEvent actionEvent) {
        JavafxHelper.popupView("About", "À propos", false, false);
    }

}
