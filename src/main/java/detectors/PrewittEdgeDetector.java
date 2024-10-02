package detectors;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

public class PrewittEdgeDetector {

    // Prewitt Filter Implementation
    public BufferedImage applyPrewittFilter(BufferedImage grayImage) {
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

        return applyConvolution(grayImage, prewittX, prewittY);
    }

    // Canny Filter Implementation (placeholder)
    private BufferedImage applyCannyFilter(BufferedImage grayImage) {
        // Implement Canny edge detection
        return grayImage; // Placeholder
    }

    // Convolution Method
    private BufferedImage applyConvolution(BufferedImage grayImage, float[] kernelX, float[] kernelY) {
        BufferedImage resultImage = new BufferedImage(grayImage.getWidth(), grayImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

        // Create ConvolveOp instances for both kernels
        ConvolveOp convolveX = new ConvolveOp(new Kernel(3, 3, kernelX));
        ConvolveOp convolveY = new ConvolveOp(new Kernel(3, 3, kernelY));

        // Apply convolution for both kernels
        BufferedImage convolvedX = convolveX.filter(grayImage, null);
        BufferedImage convolvedY = convolveY.filter(grayImage, null);

        // Calculate the magnitude of the gradient
        for (int x = 0; x < grayImage.getWidth(); x++) {
            for (int y = 0; y < grayImage.getHeight(); y++) {
                int valX = convolvedX.getRaster().getSample(x, y, 0);
                int valY = convolvedY.getRaster().getSample(x, y, 0);
                int edgeVal = (int) Math.min(255, Math.sqrt(valX * valX + valY * valY));
                resultImage.getRaster().setSample(x, y, 0, edgeVal);
            }
        }

        return resultImage;
    }
}
