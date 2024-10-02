package detectors;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

public class CannyEdgeDetector {

    // Canny Filter Implementation
    public BufferedImage applyCannyFilter(BufferedImage grayImage) {
        // Step 1: Apply Gaussian Blur
        BufferedImage blurredImage = applyGaussianBlur(grayImage);

        // Step 2: Calculate Gradient Intensity and Direction
        BufferedImage gradientMagnitude = new BufferedImage(grayImage.getWidth(), grayImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        BufferedImage gradientDirection = new BufferedImage(grayImage.getWidth(), grayImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

        calculateGradients(blurredImage, gradientMagnitude, gradientDirection);

        // Step 3: Non-Maximum Suppression
        BufferedImage suppressedImage = nonMaxSuppression(gradientMagnitude, gradientDirection);

        // Step 4: Double Thresholding
        BufferedImage thresholdedImage = doubleThreshold(suppressedImage, 50, 150);

        // Step 5: Edge Tracking by Hysteresis

        return edgeTracking(thresholdedImage);
    }

    // Gaussian Blur Implementation
    private BufferedImage applyGaussianBlur(BufferedImage image) {
        float[] gaussianKernel = {
                1/16f, 2/16f, 1/16f,
                2/16f, 4/16f, 2/16f,
                1/16f, 2/16f, 1/16f
        };
        ConvolveOp gaussianFilter = new ConvolveOp(new Kernel(3, 3, gaussianKernel));
        return gaussianFilter.filter(image, null);
    }

    // Gradient Calculation using Sobel Operators
    private void calculateGradients(BufferedImage image, BufferedImage magnitude, BufferedImage direction) {
        float[] sobelX = {
                -1, 0, 1,
                -2, 0, 2,
                -1, 0, 1
        };

        float[] sobelY = {
                -1, -2, -1,
                0, 0, 0,
                1, 2, 1
        };

        BufferedImage sobelXImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        BufferedImage sobelYImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

        ConvolveOp convolveX = new ConvolveOp(new Kernel(3, 3, sobelX));
        ConvolveOp convolveY = new ConvolveOp(new Kernel(3, 3, sobelY));

        convolveX.filter(image, sobelXImage);
        convolveY.filter(image, sobelYImage);

        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int gradX = sobelXImage.getRaster().getSample(x, y, 0);
                int gradY = sobelYImage.getRaster().getSample(x, y, 0);

                // Calculate gradient magnitude
                int mag = (int) Math.min(255, Math.sqrt(gradX * gradX + gradY * gradY));
                magnitude.getRaster().setSample(x, y, 0, mag);

                // Calculate gradient direction
                double directionValue = Math.atan2(gradY, gradX) * (180 / Math.PI);
                if (directionValue < 0) directionValue += 180; // Normalize to [0, 180]
                direction.getRaster().setSample(x, y, 0, (int) directionValue);
            }
        }
    }

    // Non-Maximum Suppression
    private BufferedImage nonMaxSuppression(BufferedImage magnitude, BufferedImage direction) {
        BufferedImage suppressed = new BufferedImage(magnitude.getWidth(), magnitude.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

        for (int x = 1; x < magnitude.getWidth() - 1; x++) {
            for (int y = 1; y < magnitude.getHeight() - 1; y++) {
                double angle = direction.getRaster().getSample(x, y, 0);
                int mag = magnitude.getRaster().getSample(x, y, 0);
                int neighbor1, neighbor2;

                // Determine pixel's neighbors based on the gradient direction
                if ((angle >= 0 && angle < 22.5) || (angle >= 157.5 && angle <= 180)) {
                    neighbor1 = magnitude.getRaster().getSample(x + 1, y, 0);
                    neighbor2 = magnitude.getRaster().getSample(x - 1, y, 0);
                } else if (angle >= 22.5 && angle < 67.5) {
                    neighbor1 = magnitude.getRaster().getSample(x + 1, y - 1, 0);
                    neighbor2 = magnitude.getRaster().getSample(x - 1, y + 1, 0);
                } else if (angle >= 67.5 && angle < 112.5) {
                    neighbor1 = magnitude.getRaster().getSample(x, y + 1, 0);
                    neighbor2 = magnitude.getRaster().getSample(x, y - 1, 0);
                } else {
                    neighbor1 = magnitude.getRaster().getSample(x - 1, y - 1, 0);
                    neighbor2 = magnitude.getRaster().getSample(x + 1, y + 1, 0);
                }

                // Suppress non-maximum pixels
                if (mag >= neighbor1 && mag >= neighbor2) {
                    suppressed.getRaster().setSample(x, y, 0, mag);
                } else {
                    suppressed.getRaster().setSample(x, y, 0, 0);
                }
            }
        }

        return suppressed;
    }

    // Double Thresholding
    private BufferedImage doubleThreshold(BufferedImage image, int lowThreshold, int highThreshold) {
        BufferedImage thresholded = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int mag = image.getRaster().getSample(x, y, 0);
                if (mag >= highThreshold) {
                    thresholded.getRaster().setSample(x, y, 0, 255); // Strong edge
                } else if (mag >= lowThreshold) {
                    thresholded.getRaster().setSample(x, y, 0, 75); // Weak edge
                } else {
                    thresholded.getRaster().setSample(x, y, 0, 0); // Non-edge
                }
            }
        }

        return thresholded;
    }

    // Edge Tracking by Hysteresis
    private BufferedImage edgeTracking(BufferedImage image) {
        BufferedImage finalEdges = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

        for (int x = 1; x < image.getWidth() - 1; x++) {
            for (int y = 1; y < image.getHeight() - 1; y++) {
                int pixel = image.getRaster().getSample(x, y, 0);
                if (pixel == 255) {
                    finalEdges.getRaster().setSample(x, y, 0, 255); // Strong edge
                } else if (pixel == 75) { // Weak edge
                    // Check for connectivity to strong edges
                    if (isConnectedToStrongEdge(image, x, y)) {
                        finalEdges.getRaster().setSample(x, y, 0, 255); // Convert weak to strong
                    } else {
                        finalEdges.getRaster().setSample(x, y, 0, 0); // Suppress weak edges
                    }
                }
            }
        }

        return finalEdges;
    }

    // Check if weak edge is connected to strong edge
    private boolean isConnectedToStrongEdge(BufferedImage image, int x, int y) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (image.getRaster().getSample(x + i, y + j, 0) == 255) {
                    return true; // Connected to a strong edge
                }
            }
        }
        return false;
    }
}

//bro this one is wacky, no clue why it isn't workin properly tbh. ig i gotta take the L
