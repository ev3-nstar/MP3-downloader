package mp3_app;

import java.io.File;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import mp3_app.DownloadService;

public class YoutubeMP3App extends Application {

    private TextField urlField;
    private TextArea outputArea;
    private Button downloadButton;
    private Button stopButton;
    private ProgressBar progressBar;
    private TextField limitField;

    private Button chooseFolderButton;
    private Label folderLabel;
    private String selectedFolder;

    private DownloadService downloadService = new DownloadService();

    @Override
    public void start(Stage stage) {


        urlField = new TextField();
        urlField.setPromptText("Enter YouTube URL...");


        chooseFolderButton = new Button("Choose Folder");
        folderLabel = new Label("No folder selected");

        chooseFolderButton.setOnAction(e -> chooseFolder(stage));


        downloadButton = new Button("Download MP3");
        stopButton = new Button("Stop");
        stopButton.setDisable(true);


        limitField = new TextField();
        limitField.setPromptText("Amount to download: ");


        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(Double.MAX_VALUE);


        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPrefHeight(180);


        downloadButton.setOnAction(e -> startDownload());

        stopButton.setOnAction(e -> {
            downloadService.stopDownload();
            appendOutput("Download stopped.");
            stopButton.setDisable(true);
            downloadButton.setDisable(false);
        });


        HBox topRow = new HBox(10, urlField, chooseFolderButton);
        HBox actionRow = new HBox(10, downloadButton, stopButton, limitField);

        VBox root = new VBox(12,
                topRow,
                folderLabel,
                actionRow,
                progressBar,
                outputArea
        );

        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 500, 380);

        java.net.URL css = getClass().getResource("/style.css");

        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        stage.setTitle("YouTube to MP3");
        stage.setScene(scene);
        stage.show();
    }

    private void startDownload() {

        String url = urlField.getText();

        if (url.isEmpty()) {
            appendOutput("Please provide a URL.");
            return;
        }

        if (selectedFolder == null) {
            appendOutput("Please choose a download folder.");
            return;
        }


        int limit = 50;

        try {
            if (!limitField.getText().isEmpty()) {
                limit = Integer.parseInt(limitField.getText());
            }
        } catch (Exception e) {
            appendOutput("Invalid number. Using default amount (50).");
        }

        downloadButton.setDisable(true);
        stopButton.setDisable(false);
        progressBar.setProgress(0);

        downloadService.downloadMP3(url, selectedFolder, limit,
                new DownloadService.OutputListener() {

                    @Override
                    public void onOutput(String text) {
                        Platform.runLater(() -> outputArea.appendText(text + "\n"));
                    }

                    @Override
                    public void onProgress(double progress) {
                        Platform.runLater(() -> progressBar.setProgress(progress));

                        if (progress >= 1.0) {
                            Platform.runLater(() -> {
                                downloadButton.setDisable(false);
                                stopButton.setDisable(true);
                            });
                        }
                    }
                });
    }

    private void appendOutput(String text) {
        Platform.runLater(() -> outputArea.appendText(text + "\n"));
    }

    private void chooseFolder(Stage stage) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Download Folder");

        File selectedDir = chooser.showDialog(stage);

        if (selectedDir != null) {
            selectedFolder = selectedDir.getAbsolutePath();
            folderLabel.setText("Folder: " + selectedFolder);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}