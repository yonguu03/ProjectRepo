package detectors;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

public class PrewittEdgeDetector {

    public BufferedImage applyPrewittFilter(BufferedImage grayImage, int strength) {
        // Define Prewitt kernels
        float[] prewittX = {
                -1, 0, 1,
                -1, 0, 1,
                -1, 0, 1
        };

        float[] prewittY = {
                -1, -1, -1,
                0,  0,  0,
                1,  1,  1
        };

        // Create images to hold results of convolutions
        BufferedImage prewittXImage = new BufferedImage(grayImage.getWidth(), grayImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        BufferedImage prewittYImage = new BufferedImage(grayImage.getWidth(), grayImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

        // Create convolution operations
        ConvolveOp convolveX = new ConvolveOp(new Kernel(3, 3, prewittX));
        ConvolveOp convolveY = new ConvolveOp(new Kernel(3, 3, prewittY));

        // Apply convolutions
        convolveX.filter(grayImage, prewittXImage);
        convolveY.filter(grayImage, prewittYImage);

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
                int prewittXVal = prewittXImage.getRaster().getSample(x, y, 0);
                int prewittYVal = prewittYImage.getRaster().getSample(x, y, 0);

                // Calculate the magnitude of the gradient
                int edgeVal = (int) Math.min(255, Math.sqrt(prewittXVal * prewittXVal + prewittYVal * prewittYVal) * strength / 100);

                // Set the pixel value in the edge image
                edgeImage.getRaster().setSample(x, y, 0, 255 - edgeVal);
            }
        }

        return edgeImage;
    }
}