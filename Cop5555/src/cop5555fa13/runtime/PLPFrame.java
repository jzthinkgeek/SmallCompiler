package cop5555fa13.runtime;

import java.awt.Container;
import java.awt.Insets;
import java.lang.reflect.InvocationTargetException;
import javax.swing.*;


/** This class provides a container for displaying PLPImages.
 * 
 * Usage:  
 * 
 * Create a PLPFrame with the createFrame factory method, passing the PLPImage as a parameter.
 * This frame is henceforth used to display this PLPImage.
 * 
 * Update a frame (change its size, visibility, BufferedImage to display, etc.) by updating these values in the 
 * associated PLPImage, then call updateFrameState.
 * 
 *
 */
@SuppressWarnings("serial")
public class PLPFrame extends JFrame {

	//private Insets insets;
	private ImageIcon icon;
	private final PLPImage im;
	
	
	/**
	 * Private constructor.  Use factory method createFrame instead.
	 * 
	 * @param im
	 */
	private PLPFrame(PLPImage im) {
		this.im = im;
	}

	/**
	 * Static method to create a new frame. The Swing framework is not
	 * thread-safe and requires all updates on its data structures to be
	 * performed by the event-dispatch thread. The call to invokeAndWait
	 * arranges this. For more information see documentation of the Java Swing
	 * package.
	 * 
	 * @param im PLPImage associated with the frame
	 * @return a reference to the new frame
	 */
	static final PLPFrame createFrame(PLPImage im) {
		final PLPFrame frame = new PLPFrame(im);
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					frame.initializeFrame();
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			// This shouldn't happen.  If it does, print stack trace and continue with undefined behavior.
			e.printStackTrace();
		}
		return frame;
	}

	/**
	 * private method to set up frame contents and basic behavior
	 * 
	 * @param im
	 *            PLPImage displayed by the frame
	 */
	private final void initializeFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		Container contentPane = getContentPane();
		icon = new ImageIcon();
		contentPane.add(new JLabel(icon));
		updateFrameState();
	}

	/**
	 * updates the frame with the attributes from the given PLP image
	 * 
	 * @param im
	 */
	private final void updateFrameState() {
		Insets insets = getInsets();
		int w = im.getWidth();
		w = w + insets.left + insets.right;
		int h = im.getHeight();
		h = h + insets.top + insets.bottom;
		setSize(w, h);
		icon.setImage(im.image);
		revalidate();
		setLocation(im.x_loc, im.y_loc);		
		repaint();
		setVisible(im.isVisible);
	}

	/**
	 * Invoked to update existing frame with attributes of given image. The
	 * Swing framework is not thread-safe and requires all updates on its data
	 * structures to be performed by the event-dispatch thread. The call
	 * toinvokeAndWait arranges this. For more information see documentation of
	 * the Java Swing package.
	 * 
	 * @param im
	 */
//	public final void update() {
//		try {
//			SwingUtilities.invokeAndWait(new Runnable() {
//				public void run() {
//					updateFrameState();
//				}
//			});
//		} catch (InvocationTargetException | InterruptedException e) {
//			// This shouldn't happen.  If it does, print stack trace and continue with undefined behavior.
//			e.printStackTrace();
//		}
//	}
	
	public final void update() {
	SwingUtilities.invokeLater(new Runnable() {
		public void run() {
			updateFrameState();
		}
	});
}	

}