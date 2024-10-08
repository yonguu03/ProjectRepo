package detectors;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

public class LaplacianEdgeDetector {

    public BufferedImage applyLaplacianFilter(BufferedImage grayImage, int strength) {
        // Define Laplacian kernel
        float[] laplacianKernel = {
                0,  1, 0,
                1, -4, 1,
                0,  1, 0
        };

        // Create image to hold result of convolution
        BufferedImage laplacianImage = new BufferedImage(grayImage.getWidth(), grayImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

        // Create convolution operation
        ConvolveOp convolve = new ConvolveOp(new Kernel(3, 3, laplacianKernel));

        // Apply convolution
        convolve.filter(grayImage, laplacianImage);

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
                int laplacianVal = laplacianImage.getRaster().getSample(x, y, 0);

                // Calculate the magnitude of the gradient
                int edgeVal = (int) Math.min(255, Math.abs(laplacianVal) * strength / 100);

                // Set the pixel value in the edge image
                edgeImage.getRaster().setSample(x, y, 0, 255 - edgeVal);
            }
        }

        return edgeImage;
    }
}