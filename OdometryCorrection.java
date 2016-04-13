/*Final Project TEAM02
 * Gareth Peters | ID: 260678626
 * Catherine Bittar | ID: 260687735
 * Ryan (LuoQing) Wang | ID: 260524744
 * Ammar Sarfaraz Azir | ID: 260565232
 * Kevin-Rafael Sorto-Ventura | ID: 260692767
 * Mohamed Elsabagh | ID: 260603261
 * */
package finalProject;

import lejos.hardware.sensor.SensorMode;

public class OdometryCorrection extends Thread {

	// Parameters
	private Odometer odometer;
	private SensorMode colorValue;
	private float[] colorData;
	private static double x, y, theta;

	// Distance of the sensor from the wheel axis
	private static final double sensor_offset = 13.5;
	// tile spacing
	private static final double tile_spacing = 30.48;

	private static final long CORRECTION_PERIOD = 10;

	// Constructor
	public OdometryCorrection(Odometer odometer, SensorMode colorValue, float[] colorData) {
		this.odometer = odometer;
		this.colorValue = colorValue;
		this.colorData = colorData;
	}

	// Runs Odometry Correction. Uses the light sensor to correct the robot's
	// position.
	public void run() {
		long correctionStart, correctionEnd;
		// Fetches the sample from the light sensor and saves it into a set
		// light value
		colorValue.fetchSample(colorData, 0);
		double colourLast = colorData[0];
		double colourNow = 0;
		double pos[] = new double[3];
		while (true) {
			correctionStart = System.currentTimeMillis();
			// A new value that is compared to the value from before to see if a
			// blackline was hit.
			colorValue.fetchSample(colorData, 0);
			colourNow = colorData[0];
			// Setting of parameters
			x = odometer.getX();
			y = odometer.getY();
			theta = odometer.getTheta();
			boolean setX = false;
			boolean setY = false;
			if (colourLast - colourNow > 0.05) {
				int i = 0;
				int j = 0;
				// Subtract the position of x/y the tile-spacing until it
				// achieves >-tile-spacing.
				// The purpose of this is to see if the expected value of x/y
				// and the actual value of x/y
				// are in sync. A counter is then increased for every time
				// tile-spacing is subtracted
				while (x > -tile_spacing) {
					x -= tile_spacing;
					i++;
					if (x <= 3 && x >= -3) {
						setX = true;
					}
				}
				while (y > -tile_spacing) {
					y -= tile_spacing;
					j++;
					if (y <= 3 && y >= -3) {
						setY = true;
					}
				}
				// The light sensor messes up at intersections, so if the robot
				// is expected to be close to an intersection, ignore the value
				// given
				if (setX && setY) {
				}
				// Set the position of the odometer to the corrected values
				else {
					pos[0] = i * x + sensor_offset * Math.sin(theta);
					pos[1] = j * y + sensor_offset * Math.cos(theta);
					odometer.setPosition(pos, new boolean[] { setX, setY, false });
				}
			}
			// Set the old value of colour to the new one.
			colourLast = colourNow;
			// this ensure the odometry correction occurs only once every period
			correctionEnd = System.currentTimeMillis();
			if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
				try {
					Thread.sleep(CORRECTION_PERIOD - (correctionEnd - correctionStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometry correction will be
					// interrupted by another thread
				}
			}
		}
	}
}
