package cop5555fa13.runtime;

/**
 * Interface defining a set of constants used in manipulating packed pixels
 * 
 */
public interface ImageConstants {

	/**
	 * Maximum color intensity
	 * 
	 */
	public static final int Z = 255;

	/**
	 * Constants representing color values
	 * 
	 */
	public static final int RED = 0, GRN = 1, BLU = 2;

	/**
	 * bit masks for selecting individual color samples in packed pixels
	 * 
	 */
	public static final int SELECT_RED = 0x00ff0000, SELECT_GRN = 0x0000ff00,
			SELECT_BLU = 0x000000ff, SELECT_ALPHA = 0xff000000;

	/**
	 * number of bits to shift to convert to and from int
	 * 
	 */
	public static final int SHIFT_RED = 16, SHIFT_GRN = 8, SHIFT_BLU = 0;

	/**
	 * bit masks for zeroing individual colors in packed pixels
	 * 
	 */
	public static final int ZERO_RED = 0xff00ffff, ZERO_GRN = 0xffff00ff,
			ZERO_BLU = 0xffffff00;

	/**
	 * Array holding values of bit mask to select color, for convenience
	 * 
	 */
	public static final int[] BITMASKS = { SELECT_RED, SELECT_GRN, SELECT_BLU };

	/**
	 * Array holding values of bit masks to zero a color sample, for convenience
	 */
	public static final int[] ZERO = { ZERO_RED, ZERO_GRN, ZERO_BLU };

	/**
	 * Array holding offsets, for convenience
	 * 
	 */
	public static final int[] BITOFFSETS = { SHIFT_RED, SHIFT_GRN, SHIFT_BLU };

}
