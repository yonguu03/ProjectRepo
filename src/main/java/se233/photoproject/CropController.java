package se233.photoproject;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class CropController {

    private RubberBandSelection rubberBandSelection;
    private ImageView imageView;
    private Stage primaryStage;
    private File imageFile;
    private final List<BufferedImage> croppedImages;
    private final CropCallback cropCallback;

    public CropController(File imageFile, List<BufferedImage> croppedImages, CropCallback cropCallback) {
        this.imageFile = imageFile;
        this.croppedImages = croppedImages;
        this.cropCallback = cropCallback;
    }

    public interface CropCallback {
        void onCropComplete();
    }

    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Image Crop");

        BorderPane root = new BorderPane();
        VBox vbox = new VBox();
        ScrollPane scrollPane = new ScrollPane();
        Group imageLayer = new Group();

        imageView = createImageView(imageFile);
        imageLayer.getChildren().add(imageView);
        scrollPane.setContent(imageLayer);

        rubberBandSelection = new RubberBandSelection(imageLayer);

        root.setCenter(scrollPane);
        vbox.getChildren().add(createCropButton());
        root.setTop(vbox);

        addDragAndDropHandlers(root);

        primaryStage.setScene(new Scene(root, 1152, 636));
        primaryStage.show();
    }

    private ImageView createImageView(File imageFile) {
        Image image = new Image(imageFile.toURI().toString());
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(800);
        imageView.setFitHeight(800);
        imageView.setPreserveRatio(true);
        imageView.setOnDragDetected(event -> {
            Dragboard db = imageView.startDragAndDrop(TransferMode.ANY);
            ClipboardContent content = new ClipboardContent();
            content.putImage(imageView.getImage());
            db.setContent(content);
            event.consume();
        });
        return imageView;
    }

    private Button createCropButton() {
        Button cropButton = new Button("Crop");
        cropButton.setPrefWidth(100);
        cropButton.setPrefHeight(30);
        cropButton.setOnAction(this::handleCrop);
        return cropButton;
    }

    private void addDragAndDropHandlers(BorderPane root) {
        root.setOnDragOver(event -> {
            if (event.getGestureSource() != imageView && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        root.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                success = true;
                loadImage(db.getFiles().get(0));
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private void handleCrop(ActionEvent event) {
        Bounds selectionBounds = rubberBandSelection.getBounds();
        if (!selectionBounds.isEmpty()) {
            crop(selectionBounds);
            if (cropCallback != null) {
                cropCallback.onCropComplete();
            }
            primaryStage.close();
        } else {
            System.out.println("No valid selection made for cropping.");
        }
    }

    private void crop(Bounds bounds) {
        int width = (int) bounds.getWidth();
        int height = (int) bounds.getHeight();

        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        parameters.setViewport(new Rectangle2D(bounds.getMinX(), bounds.getMinY(), width, height));

        WritableImage wi = new WritableImage(width, height);
        imageView.snapshot(parameters, wi);

        BufferedImage bufImageARGB = SwingFXUtils.fromFXImage(wi, null);
        BufferedImage bufImageRGB = new BufferedImage(bufImageARGB.getWidth(), bufImageARGB.getHeight(), BufferedImage.OPAQUE);

        Graphics2D graphics = bufImageRGB.createGraphics();
        graphics.drawImage(bufImageARGB, 0, 0, null);
        graphics.dispose();

        croppedImages.add(bufImageRGB);
    }

    private void loadImage(File file) {
        try {
            Image image = new Image(file.toURI().toString());
            imageView.setImage(image);
            this.imageFile = file;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class RubberBandSelection {
        final DragContext dragContext = new DragContext();
        Rectangle rect = new Rectangle();
        Group group;

        public Bounds getBounds() {
            return rect.getBoundsInParent();
        }

        public RubberBandSelection(Group group) {
            this.group = group;
            rect.setStroke(Color.BLUE);
            rect.setStrokeWidth(1);
            rect.setFill(Color.LIGHTBLUE.deriveColor(0, 1.2, 1, 0.6));
            group.addEventHandler(MouseEvent.MOUSE_PRESSED, this::onMousePressed);
            group.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onMouseDragged);
            group.addEventHandler(MouseEvent.MOUSE_RELEASED, this::onMouseReleased);
        }

        private void onMousePressed(MouseEvent event) {
            if (event.isSecondaryButtonDown()) return;

            rect.setX(0);
            rect.setY(0);
            rect.setWidth(0);
            rect.setHeight(0);
            group.getChildren().remove(rect);

            dragContext.mouseAnchorX = event.getX();
            dragContext.mouseAnchorY = event.getY();

            rect.setX(dragContext.mouseAnchorX);
            rect.setY(dragContext.mouseAnchorY);
            rect.setWidth(0);
            rect.setHeight(0);

            group.getChildren().add(rect);
        }

        private void onMouseDragged(MouseEvent event) {
            if (event.isSecondaryButtonDown()) return;

            double offsetX = event.getX() - dragContext.mouseAnchorX;
            double offsetY = event.getY() - dragContext.mouseAnchorY;

            if (offsetX > 0) {
                rect.setWidth(offsetX);
            } else {
                rect.setX(event.getX());
                rect.setWidth(dragContext.mouseAnchorX - rect.getX());
            }

            if (offsetY > 0) {
                rect.setHeight(offsetY);
            } else {
                rect.setY(event.getY());
                rect.setHeight(dragContext.mouseAnchorY - rect.getY());
            }
        }

        private void onMouseReleased(MouseEvent event) {
            if (event.isSecondaryButtonDown()) return;
        }

        private static final class DragContext {
            public double mouseAnchorX;
            public double mouseAnchorY;
        }
    }
} //current time 1:06 pm