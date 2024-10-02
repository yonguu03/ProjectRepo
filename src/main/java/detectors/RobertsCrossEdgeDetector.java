package detectors;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

public class RobertsCrossEdgeDetector {

    // Method to apply Roberts Cross filter
    public BufferedImage applyRobertsFilter(BufferedImage grayImage) {
        // Roberts Cross kernels
        float[] kernelX = {
                1,  0,
                0, -1
        };

        float[] kernelY = {
                0,  1,
                -1, 0
        };

        ConvolveOp convolveX = new ConvolveOp(new Kernel(2, 2, kernelX));
        ConvolveOp convolveY = new ConvolveOp(new Kernel(2, 2, kernelY));

        BufferedImage robertsXImage = convolveX.filter(grayImage, null);
        BufferedImage robertsYImage = convolveY.filter(grayImage, null);

        // Combine the results
        return combineImages(robertsXImage, robertsYImage);
    }

    // Method to combine the two images
    private BufferedImage combineImages(BufferedImage imageX, BufferedImage imageY) {
        BufferedImage resultImage = new BufferedImage(imageX.getWidth(), imageX.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

        for (int x = 0; x < imageX.getWidth(); x++) {
            for (int y = 0; y < imageX.getHeight(); y++) {
                int valueX = imageX.getRaster().getSample(x, y, 0);
                int valueY = imageY.getRaster().getSample(x, y, 0);
                int edgeValue = (int) Math.min(255, Math.sqrt(valueX * valueX + valueY * valueY)); // Gradient magnitude
                resultImage.getRaster().setSample(x, y, 0, edgeValue);
            }
        }

        return resultImage;
    }
}
