package detectors;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

public class SobelEdgeDetector {

    public BufferedImage applySobelFilter(BufferedImage grayImage, int strength) {
        // Define Sobel kernels
        float[] sobelX = {
                -1, 0, 1,
                -2, 0, 2,
                -1, 0, 1
        };

        float[] sobelY = {
                -1, -2, -1,
                0,  0,  0,
                1,  2,  1
        };

        // Create images to hold results of convolutions
        BufferedImage sobelXImage = new BufferedImage(grayImage.getWidth(), grayImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        BufferedImage sobelYImage = new BufferedImage(grayImage.getWidth(), grayImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

        // Create convolution operations
        ConvolveOp convolveX = new ConvolveOp(new Kernel(3, 3, sobelX));
        ConvolveOp convolveY = new ConvolveOp(new Kernel(3, 3, sobelY));

        // Apply convolutions
        convolveX.filter(grayImage, sobelXImage);
        convolveY.filter(grayImage, sobelYImage);

        // Create final image to hold edges
        BufferedImage edgeImage = new BufferedImage(grayImage.getWidth(), grayImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

        // Set background to white
        for (int x = 0; x < grayImage.getWidth(); x++) {
            for (int y = 0; y < grayImage.getHeight(); y++) {
                edgeImage.getRaster().setSample(x, y, 0, 255);
            }
        }

        // Compute the edge magnitude
        for (int x = 0; x < grayImage.getWidth(); x++) {
            for (int y = 0; y < grayImage.getHeight(); y++) {
                int sobelXVal = sobelXImage.getRaster().getSample(x, y, 0);
                int sobelYVal = sobelYImage.getRaster().getSample(x, y, 0);

                // Calculate the magnitude of the gradient
                int edgeVal = (int) Math.min(255, Math.sqrt(sobelXVal * sobelXVal + sobelYVal * sobelYVal) * strength / 100.0);

                // Set the pixel value in the edge image
                edgeImage.getRaster().setSample(x, y, 0, 255 - edgeVal);
            }
        }

        return edgeImage;
    }
}