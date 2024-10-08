package detectors;

import java.awt.image.BufferedImage;

public class RobertsCrossEdgeDetector {

    public BufferedImage applyRobertsCrossFilter(BufferedImage grayImage, int strength) {
        // Create final image to hold edges
        BufferedImage edgeImage = new BufferedImage(grayImage.getWidth(), grayImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

        // Set background to white
        for (int x = 0; x < grayImage.getWidth(); x++) {
            for (int y = 0; y < grayImage.getHeight(); y++) {
                edgeImage.getRaster().setSample(x, y, 0, 255);
            }
        }

        // Compute the edge magnitude using Roberts Cross operator
        for (int x = 0; x < grayImage.getWidth() - 1; x++) {
            for (int y = 0; y < grayImage.getHeight() - 1; y++) {
                int pixel1 = grayImage.getRaster().getSample(x, y, 0);
                int pixel2 = grayImage.getRaster().getSample(x + 1, y + 1, 0);
                int pixel3 = grayImage.getRaster().getSample(x + 1, y, 0);
                int pixel4 = grayImage.getRaster().getSample(x, y + 1, 0);

                // Calculate the magnitude of the gradient
                int edgeVal = (int) Math.min(255, Math.sqrt(Math.pow(pixel1 - pixel2, 2) + Math.pow(pixel3 - pixel4, 2)) * strength / 100);

                // Set the pixel value in the edge image
                edgeImage.getRaster().setSample(x, y, 0, 255 - edgeVal);
            }
        }

        return edgeImage;
    }
}