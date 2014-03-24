package cop5555fa13.runtime;

/**
 * This class provides static utility functions for dealing with pixels
 * 
 */
public class Pixel implements ImageConstants {

	public static final String JVMClassName = "cop5555fa13/runtime/Pixel";

	public static final String getSampleSig = "(II)I";

	/**
	 * Extract indicated Sample from Pixel
	 * 
	 * @param pixel
	 * @param colorCode
	 * @return intensity of indicated color in pixel
	 */
	public static int getSample(int pixel, int colorCode) {
		return (pixel & BITMASKS[colorCode]) >> BITOFFSETS[colorCode];
	}

	public static final String getRedSig = "(I)I";

	/**
	 * extract red sample
	 * 
	 * @param pixel
	 * @return intensity of red in pixel
	 */
	public static int getRed(int pixel) {
		return (pixel & SELECT_RED) >> SHIFT_RED;
	}

	public static final String getGrnSig = "(I)I";

	/**
	 * extract green sample
	 * 
	 * @param pixel
	 * @returnintensity of green in pixel
	 */
	public static int getGrn(int pixel) {
		return (pixel & SELECT_GRN) >> SHIFT_GRN;
	}

	public static final String getBluSig = "(I)I";

	/**
	 * extract blue sample
	 * 
	 * @param pixel
	 * @return intensity of blue in pixel
	 */
	public static int getBlu(int pixel) {
		return (pixel & SELECT_BLU) >> SHIFT_BLU;
	}

	public static final String makePixelSig = "(III)I";

	/**
	 * Create a pixel with the given color values.
	 * 
	 * If all the values given have the form 0xffffffkk, it is assumed that
	 * these values were obtained by bitwise not of an int representing a sample
	 * and the value kk is selected. Otherwise a value>255 is truncated to 255.
	 * 
	 * @param redVal
	 * @param grnVal
	 * @param bluVal
	 * @return pixel containing given colof values
	 */
	public static int makePixel(int redVal, int grnVal, int bluVal) {
		int maxUnsigned = 0xffffffff;
		if (((redVal | 0xff) == maxUnsigned)
				&& ((grnVal | 0xff) == maxUnsigned)
				&& ((redVal | 0xff) == maxUnsigned)) {
			return (SELECT_ALPHA | ((redVal & SELECT_BLU) << SHIFT_RED)
					| ((grnVal & SELECT_BLU) << SHIFT_GRN) | ((bluVal & SELECT_BLU) << SHIFT_BLU));
		} else
			return (SELECT_ALPHA | (truncate(redVal) << SHIFT_RED)
					| (truncate(grnVal) << SHIFT_GRN) | (truncate(bluVal) << SHIFT_BLU));
	}

	public static final String notSig = "(I)I";

	/**
	 * negates a pixel
	 * 
	 * @param val
	 * @return
	 */
	public static int not(int pixel) {
		return ~pixel | SELECT_ALPHA;
	}

	/**
	 * truncates an int to value in range of [0,Z] where Z = 255 is defined in
	 * ImageConstants
	 * 
	 * @param z
	 * @return
	 */
	static public int truncate(int z) {
		if (z < 0)
			return 0;
		else if (z > ImageConstants.Z)
			return ImageConstants.Z;
		else
			return z;
	}

	/**
	 * String showing pixel in Hex format, alpha, red, grn, and blu components
	 * are each two digits.
	 * 
	 * @param val
	 * @return
	 */
	public static String toString(int val) {
		return Integer.toHexString(val);
	}
}
