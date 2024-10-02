package detectors;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

public class GaussianEdgeDetector {

    // Method to apply Gaussian blur
    private BufferedImage applyGaussianBlur(BufferedImage image) {
        // Gaussian kernel (3x3)
        float[] gaussianKernel = {
                1f/16, 2f/16, 1f/16,
                2f/16, 4f/16, 2f/16,
                1f/16, 2f/16, 1f/16
        };

        ConvolveOp gaussianOp = new ConvolveOp(new Kernel(3, 3, gaussianKernel));
        return gaussianOp.filter(image, null);
    }

    // Method to apply Sobel edge detection after Gaussian blur
    public BufferedImage detectEdges(BufferedImage image) {
        BufferedImage blurredImage = applyGaussianBlur(image);
        SobelEdgeDetector sobelEdgeDetector = new SobelEdgeDetector(); // Create an instance of SobelEdgeDetector
        return sobelEdgeDetector.applySobelFilter(blurredImage); // Apply Sobel filter on the blurred image
    }
}
