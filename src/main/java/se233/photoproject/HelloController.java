package se233.photoproject;

import detectors.*;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.ListView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javafx.concurrent.Task;
import javafx.application.Platform;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HelloController {

    @FXML
    private ImageView originalImageView;

    @FXML
    public ImageView processedImageView;

    @FXML
    private ListView<String> fileListView;

    @FXML
    private ChoiceBox<String> edgeDetectionChoiceBox;

    @FXML
    private Slider edgeStrengthSlider;

    @FXML
    private VBox dropImagebox;

    @FXML
    private ProgressBar progressBar;

    @FXML
    public Button keepImageButton;

    private List<File> droppedFiles = new ArrayList<>();
    private List<BufferedImage> processedImages = new ArrayList<>();
    private List<BufferedImage> croppedImages = new ArrayList<>();
    private List<BufferedImage> editedImages = new ArrayList<>(); // Add this field

    private static final Logger LOGGER = Logger.getLogger(HelloController.class.getName());

    @FXML
    public void initialize() {
        edgeDetectionChoiceBox.getItems().addAll("Sobel", "Prewitt", "Canny", "Gaussian", "Laplacian", "RobertsCross");
        edgeDetectionChoiceBox.setValue("Sobel");

        edgeStrengthSlider.setMin(0);
        edgeStrengthSlider.setMax(100);
        edgeStrengthSlider.setValue(50);
        edgeStrengthSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("Edge Strength: " + newValue.intValue());
            if (originalImageView.getImage() != null) {
                BufferedImage grayImage = convertToGrayscale(SwingFXUtils.fromFXImage(originalImageView.getImage(), null));
                applyEdgeDetection(grayImage);
            }
        });

        keepImageButton.setVisible(false);

        dropImagebox.setOnDragOver(this::handleDragOver);
        dropImagebox.setOnDragDropped(this::handleDragDropped);
    }

    @FXML
    private void handleDragOver(DragEvent event) {
        if (event.getGestureSource() != fileListView && event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        event.consume();
    }

    @FXML
    private void handleDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            for (File file : db.getFiles()) {
                if (isImageFile(file)) {
                    handleImageFile(file);
                } else if (file.getName().endsWith(".zip")) {
                    try {
                        unzipFiles(file);
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "Error unzipping file", e);
                    }
                }
            }
            success = true;
        }
        event.setDropCompleted(success);
        event.consume();
    }

    private boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg");
    }

    private void handleImageFile(File file) {
        droppedFiles.add(file);
        fileListView.getItems().add(file.getName());
        try {
            Image img = new Image(file.toURI().toString());
            originalImageView.setImage(img);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading image", e);
        }
    }

    private void unzipFiles(File zipFile) throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (isImageFile(new File(entry.getName()))) {
                    File tempFile = File.createTempFile("extracted_", entry.getName());
                    try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zipInputStream.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                    droppedFiles.add(tempFile);
                    fileListView.getItems().add(tempFile.getName());
                    if (originalImageView.getImage() == null) {
                        Image img = new Image(tempFile.toURI().toString());
                        originalImageView.setImage(img);
                    }
                }
                zipInputStream.closeEntry();
            }
        }
    }

    @FXML
    private void handleCroppingImage() {
        for (File imageFile : droppedFiles) {
            CropController croppingController = new CropController(imageFile, croppedImages, this::onCropComplete);
            Stage croppingStage = new Stage();
            croppingController.start(croppingStage);
        }
    }

    private void onCropComplete() {
        keepImageButton.setVisible(true);
    }

    @FXML
    public void handleProcessImage() {
        Task<Void> imageProcessingTask = new Task<>() {
            @Override
            protected Void call() {
                int totalImages = droppedFiles.size();
                updateProgress(0, totalImages);

                for (int i = 0; i < totalImages; i++) {
                    File file = droppedFiles.get(i);
                    try {
                        BufferedImage originalImage = SwingFXUtils.fromFXImage(new Image(file.toURI().toString()), null);
                        if (originalImage != null) {
                            BufferedImage grayImage = convertToGrayscale(originalImage);
                            updateProgress(i + 1, totalImages);

                            BufferedImage edgeImage = applyEdgeDetection(grayImage);
                            Platform.runLater(() -> {
                                processedImageView.setImage(SwingFXUtils.toFXImage(edgeImage, null));
                                processedImages.add(edgeImage);
                            });

                            updateProgress(i + 1, totalImages);
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Error processing image", e);
                    }
                }

                Platform.runLater(() -> keepImageButton.setVisible(true));
                return null;
            }
        };

        progressBar.progressProperty().bind(imageProcessingTask.progressProperty());
        new Thread(imageProcessingTask).start();
    }

    private BufferedImage convertToGrayscale(BufferedImage originalImage) {
        BufferedImage grayImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        ColorConvertOp op = new ColorConvertOp(originalImage.getColorModel().getColorSpace(), grayImage.getColorModel().getColorSpace(), null);
        op.filter(originalImage, grayImage);
        return grayImage;
    }

    private BufferedImage applyEdgeDetection(BufferedImage grayImage) {
        String selectedFilter = edgeDetectionChoiceBox.getValue();
        int strength = (int) edgeStrengthSlider.getValue();
        switch (selectedFilter) {
            case "Sobel":
                return new SobelEdgeDetector().applySobelFilter(grayImage, strength);
            case "Prewitt":
                return new PrewittEdgeDetector().applyPrewittFilter(grayImage, strength);
            case "Canny":
                return new CannyEdgeDetector().applyCannyFilter(grayImage, strength);
            case "Gaussian":
                return new GaussianEdgeDetector().applyGaussianEdgeDetection(grayImage, strength);
            case "Laplacian":
                return new LaplacianEdgeDetector().applyLaplacianFilter(grayImage, strength);
            case "RobertsCross":
                return new RobertsCrossEdgeDetector().applyRobertsCrossFilter(grayImage, strength);
            default:
                return grayImage;
        }
    }

    @FXML
    public void handleKeepImage() throws IOException {
        if (!processedImages.isEmpty() || !croppedImages.isEmpty()) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Processed and Cropped Images");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ZIP Files", "*.zip"));

            Stage stage = (Stage) keepImageButton.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                saveImagesToZip(file, processedImages, croppedImages);
            }
        }
    }

    private void saveImages(File file, List<BufferedImage> images) throws IOException {
        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".zip")) {
            saveImagesToZip(file, images, croppedImages);
        } else {
            String format = fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") ? "jpg" : "png";
            String baseName = file.getAbsolutePath().replaceFirst("[.][^.]+$", ""); // Remove extension if present

            for (int i = 0; i < images.size(); i++) {
                File imageFile = new File(baseName + "_" + (i + 1) + "." + format);
                ImageIO.write(images.get(i), format, imageFile);
            }
        }
    }

    private void saveImagesToZip(File zipFile, List<BufferedImage> images, List<BufferedImage> croppedImages) throws IOException {
        File tempFile = File.createTempFile("tempZip", ".zip");

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempFile))) {
            for (int i = 0; i < images.size(); i++) {
                BufferedImage image = images.get(i);
                String imageName = "processed_image_" + (i + 1) + ".png";
                ZipEntry entry = new ZipEntry(imageName);
                zos.putNextEntry(entry);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "png", baos);
                zos.write(baos.toByteArray());

                zos.closeEntry();
            }

            for (int i = 0; i < this.croppedImages.size(); i++) {
                BufferedImage croppedImage = this.croppedImages.get(i);
                String imageName = "cropped_image_" + (i + 1) + ".png";
                ZipEntry entry = new ZipEntry(imageName);
                zos.putNextEntry(entry);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(croppedImage, "png", baos);
                zos.write(baos.toByteArray());

                zos.closeEntry();
            }
        }

        Files.move(tempFile.toPath(), zipFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private void bundleProcessedImagesToZip(File zipFile) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            for (int i = 0; i < processedImages.size(); i++) {
                BufferedImage image = processedImages.get(i);
                String imageName = "processed_image_" + (i + 1) + ".png";
                ZipEntry entry = new ZipEntry(imageName);
                zos.putNextEntry(entry);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "png", baos);
                zos.write(baos.toByteArray());

                zos.closeEntry();
            }
        }
    }
}

//im actually losing my mind, i took a nap and i dreamt i was coding. like that's actually crazy.