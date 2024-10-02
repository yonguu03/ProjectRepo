package detectors;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

public class LaplacianEdgeDetector {

    // Method to apply Laplacian filter
    public BufferedImage applyLaplacianFilter(BufferedImage grayImage) {
        // Laplacian kernel
        float[] laplacianKernel = {
                0,  1, 0,
                1, -4, 1,
                0,  1, 0
        };

        ConvolveOp laplacianOp = new ConvolveOp(new Kernel(3, 3, laplacianKernel));
        BufferedImage laplacianImage = laplacianOp.filter(grayImage, null);

        // Normalize the output image
        return normalizeImage(laplacianImage);
    }

    // Method to normalize the image values
    private BufferedImage normalizeImage(BufferedImage image) {
        BufferedImage resultImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int value = image.getRaster().getSample(x, y, 0);
                // Normalize to the range [0, 255]
                int normalizedValue = (int) Math.min(255, Math.max(0, value + 128)); // Offset for visibility
                resultImage.getRaster().setSample(x, y, 0, normalizedValue);
            }
        }
        return resultImage;
    }
}
