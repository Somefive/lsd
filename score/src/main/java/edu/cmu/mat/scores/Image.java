package edu.cmu.mat.scores;

import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;

public class Image {
	private static GraphicsConfiguration GRAPHICS = GraphicsEnvironment
			.getLocalGraphicsEnvironment().getDefaultScreenDevice()
			.getDefaultConfiguration();

	private BufferedImage _image;

	public static int DIMENSION_WIDTH = 0;
	public static int DIMENSION_HEIGHT = 1;

	public Image(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		_image = COLOUR_IMAGE(image, width, height);
	}

	public BufferedImage getImage() {
		return _image;
	}
	
	public BufferedImage RESIZE(int size, int dimension, int hint) {
		return RESIZE(_image, size, dimension, hint);
	}

	public static BufferedImage RESIZE(BufferedImage image, int size, int dimension) {
		return RESIZE(image, size, BufferedImage.SCALE_SMOOTH, BufferedImage.SCALE_SMOOTH);
	}
	public static BufferedImage RESIZE(BufferedImage image, int size,
			int dimension, int hint) {
		int width;
		int height;
		double factor;

		if (dimension == DIMENSION_WIDTH) {
			width = size;
			factor = width / ((double) image.getWidth());
			height = (int) (image.getHeight() * factor);
		} else {
			height = size;
			factor = height / ((double) image.getHeight());
			width = (int) (image.getWidth() * factor);
		}

		java.awt.Image resizedImage = image.getScaledInstance(width, height,
				hint);
		return COLOUR_IMAGE(resizedImage, width, height);
	}

	public static BufferedImage CROP(BufferedImage image, int top, int bottom) {
		int width = image.getWidth();
		int height = image.getHeight();

		int cropHeight = Math.min(bottom, height) - Math.max(top, 0);
		if (cropHeight <= 0) {
			return null;
		}

		BufferedImage cropped = GRAPHICS.createCompatibleImage(width, height);
		cropped.getGraphics().drawImage(image, 0, 0, width, cropHeight, 0, top,
				width, bottom, null);
		return cropped;

	}

	private static BufferedImage COLOUR_IMAGE(java.awt.Image image, int width,
			int height) {
		BufferedImage coloured = GRAPHICS.createCompatibleImage(width, height);
		coloured.getGraphics().setColor(Color.WHITE);
		coloured.getGraphics().fillRect(0, 0, width, height);
		coloured.getGraphics().drawImage(image, 0, 0, width, height, 0, 0,
				width, height, null);
		return coloured;
	}
}
