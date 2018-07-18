package com.apw.oldglobal;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class LogFX extends Application {
    private List<Line> lines = new ArrayList<>();
    private TextArea ta = null;
    private int level = 0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        VBox box = new VBox();

        Button b = new Button();
        b.setOnAction(value -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Log File");
            File logs = new File("./logs");
            if (logs.exists()) {
                fileChooser.setInitialDirectory(logs);
            } else {
                fileChooser.setInitialDirectory(new File("."));
            }

            fileChooser.setSelectedExtensionFilter(new ExtensionFilter("Logfile", ".log"));
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                lines.clear();
                Scanner sc;
                try {
                    sc = new Scanner(file);
                    while (sc.hasNextLine()) {
                        lines.add(new Line(sc.nextLine()));
                    }
                    sc.close();

                    updateTextArea();
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        b.setText("Open File");

        ta = new TextArea();
        ta.setEditable(false);
        ta.setPrefWidth(1024);
        ta.setPrefHeight(768);

        ComboBox<String> comboBox = new ComboBox<>(
                FXCollections.observableArrayList("INFO", "DEBUG", "TRACE", "WARN", "ERROR", "FATAL"));
        comboBox.getSelectionModel().select(0);
        comboBox.valueProperty().addListener((obsVal, oldVal, newVal) -> {
            this.level = Log.Level.valueOf(newVal).ordinal();
            updateTextArea();
        });

        stage.setTitle("Log Reader");

        box.getChildren().addAll(b, comboBox, ta);

        Group rootNode = new Group();
        rootNode.getChildren().addAll(box);

        Scene myScene = new Scene(rootNode);
        stage.setScene(myScene);
        stage.show();
    }

    private void updateTextArea() {
        StringBuilder s = new StringBuilder();
        for (Line line : lines) {
            if (line.level.ordinal() >= level) {
                s.append(line);
                s.append("\n");
            }
        }
        ta.setText(s.toString());
    }

    private static class Line {
        private String s;
        private Log.Level level;

        public Line(String s) {
            int index = Integer.MAX_VALUE;

            for (Log.Level possibleLevel : Log.Level.values()) {
                int currentIndex = s.indexOf(possibleLevel.toString());
                if (currentIndex != -1 && currentIndex < index) {
                    level = possibleLevel;
                    index = currentIndex;
                }
            }

            this.s = s;
        }

        public String toString() {
            return s;
        }
    }
}
