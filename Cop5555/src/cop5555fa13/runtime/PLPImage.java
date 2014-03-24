package cop5555fa13.runtime;

import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;


/**
 * This class is a wrapper around a BufferedImage and a JFrame. It contains
 * explicit representations of width, height, x_loc, y_loc, and the visibility
 * state. An instance of this class is created for every image variable declared
 * in the PLPLanguage program. BufferedImage and Frame instances are created
 * when needed.
 * 
 * Generally, operations on images result in changing the state of the PLPImage
 * object (or its BufferedImage) then invoking updateFrame.
 * 
 */
public class PLPImage implements ImageConstants {
	
	public static final String className = "cop5555fa13/runtime/PLPImage";
	public static final String classDesc = "Lcop5555fa13/runtime/PLPImage;";
	public static final String loadImageDesc = "(Ljava/lang/String;)V";
	public static final String updateFrameDesc = "()V";

	public static class ImageException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public ImageException(String msg) {
			super(msg);
		}
	}

	BufferedImage image;
	public int width;
	public int height;
	PLPFrame frame;
	public int x_loc;
	public int y_loc;
	public boolean isVisible;

	public final static int SCREENSIZE;

	static {
		Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getMaximumWindowBounds();
		SCREENSIZE = screen.width > screen.height ? screen.height
				: screen.width;
	}

	public PLPImage() {
	}

	public PLPImage(BufferedImage image) {
		this.image = image;
		this.width = image.getWidth();
		this.height = image.getHeight();
	}

    /** load the image from the given file or URL.  
     * If width or height is 0, the value of that dimension is
     * obtained from the source image. If not, the image is scaled
     * to the indicated size.
     * 
     * @param fileOrURL  File or URL of the image
     */
	public final void loadImage(String fileOrURL) {
		try {
			try {
				image = ImageIO.read(new URL(fileOrURL));
			} catch (MalformedURLException e) {
				// not a URL, maybe it is a file
				image = ImageIO.read(new File(fileOrURL));
			}
		} catch (IOException e) {
			System.out.println("Cannot load image " + fileOrURL);
			return;
		}
		if (width == 0) {
			width = image.getWidth();
		}
		if (height == 0) {
			height = image.getHeight();
		}
		updateImageSize();
	}

	/**Invoke this method after updating the width or height values for
	 * the image to synchronize the actual size with these values.
	 */
	public final void updateImageSize() {
		if (image == null)
			return;
		if (width != image.getWidth() || height != image.getHeight()) {
			image = getScaledImage(image, width, height);
		}
	}

	/** Invoke this method after updating the image parameters to
	 * synchronize what is displayed by the PLPFrame with the 
	 * values in this PLPImage
	 */
	public final void updateFrame() {
		if (frame == null) {
			if (!isVisible)
				return;
			frame = PLPFrame.createFrame(this);
		} else
			frame.update();
	}

	/**
	 * static method that returns a scaled version of the src image, or a copy
	 * if the size is unchanged.
	 * 
	 * @param src
	 * @param newWidth
	 * @param newHeight
	 * @return copy of scr scaled to newWidth and newHeight
	 */
	static final BufferedImage getScaledImage(BufferedImage src, int newWidth,
			int newHeight) {
		if (src == null) {
			return null;
		}
		if (newWidth == 0 && newHeight == 0)
			return null;
		BufferedImage scaledImage = new BufferedImage(newWidth, newHeight,
				BufferedImage.TYPE_INT_RGB);
		;
		int w = src.getWidth();
		int h = src.getHeight();
		if ((w == newWidth && h == newHeight)) {
			// no scaling needed, just return a copy
			scaledImage.setRGB(0, 0, w, h, src.getRGB(0, 0, w, h, null, 0, w),
					0, w);
		} else // scale image
		{
			Graphics2D graphics2D = scaledImage.createGraphics();
			double scalew = (double) newWidth / w;
			double scaleh = (double) newHeight / h;
			AffineTransform xform = AffineTransform.getScaleInstance(scalew,
					scaleh);
			graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,

			RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			graphics2D.drawImage(src, xform, null);
			graphics2D.dispose();
		}
		return scaledImage;
	}


	private static String format = "JPEG";
	/**
	 * writes an image to a file, provided for convenience, not used in
	 * language, but may be useful for debugging and grading.
	 * 
	 * @param plpImage
	 * @param fileName
	 */
	public final static void writeImage(PLPImage plpImage, String fileName) {
		File file = new File(fileName);
		try {
			ImageIO.write(plpImage.image, format, file);
		} catch (IOException e) {
			System.err.println("Write failed");
			System.exit(0);
		}
	}

	/**
	 * Gets the sample of the indicated color at the indicate pixel location
	 * 
	 * @param x
	 * @param y
	 * @param colorCode
	 *            RED, GRN, or BLU, defined in ImageConstants
	 * @return sample
	 */
	public int getSample(int x, int y, int colorCode) {
		return (image.getRGB(x, y) & BITMASKS[colorCode]) >>> BITOFFSETS[colorCode];
	}

	/**
	 * Sets the indicated color at the indicated pixel location
	 * 
	 * @param x
	 * @param y
	 * @param colorCode
	 * @param val
	 */
	public void setSample(int x, int y, int colorCode, int val) {
		int pixel = image.getRGB(x, y) & ZERO[colorCode]
				| (Pixel.truncate(val) << BITOFFSETS[colorCode]);
		image.setRGB(x, y, pixel);
	}

	public int getX_loc() {
		return x_loc;
	}

	public void setX_loc(int x_loc) {
		this.x_loc = x_loc;
	}

	public int getY_loc() {
		return y_loc;
	}

	public void setY_loc(int y_loc) {
		this.y_loc = y_loc;
	}

	public boolean isVisible() {
		return isVisible;
	}

	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getPixel(int x, int y) {
		return image.getRGB(x, y);
	}

	/**
	 * sets the pixel at the given location. This will create a BufferedImage if
	 * one does not exist and both the width and length are non-zero.
	 * 
	 * @param x
	 * @param y
	 * @param newPixel
	 */
	public void setPixel(int x, int y, int newPixel) throws ImageException {
		if (image == null) {
			if (width == 0 || height == 0)
				throw new ImageException(
						"attempt to create image with undefined size");
			image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		}
		image.setRGB(x, y, newPixel);
	}

	// returns a new image that is the negation of the given image
	public static final String notSig = "(Lcop5555/runtime/Image;)Lcop5555/runtime/Image;";


	public static PLPImage not(PLPImage src) {
		PLPImage dest = new PLPImage(new BufferedImage(src.getWidth(),
				src.getHeight(), BufferedImage.TYPE_INT_ARGB));
		for (int y = 0; y != src.getHeight(); y++) {
			for (int x = 0; x != src.getWidth(); x++) {
				dest.image.setRGB(x, y,
						(~src.image.getRGB(x, y) | Pixel.SELECT_ALPHA));
			}
		}
		return dest;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
	
	public static final String pauseDesc = "(I)V";
	public static void pause(int msec){
		try {
			Thread.sleep(msec);
		} catch (InterruptedException e) {
			// Should not happen.  If it does, print stack trace and continue.
			e.printStackTrace();
		}
		
	}

}
