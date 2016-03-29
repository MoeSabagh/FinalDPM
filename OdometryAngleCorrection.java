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

public class OdometryAngleCorrection extends Thread {
	private Odometer odometer;
	private float[] leftData, rightData;
	private SensorMode leftValue, rightValue;
	private boolean leftLine = false, rightLine = false, lock;
	private double x1, y1, x2, y2;
	private double calculatedTheta;
	// distance between the two sensors
	private static final double DISTANCE = 8.7;

	private static final long CORRECTION_PERIOD = 10;

	public OdometryAngleCorrection(Odometer odometer, float[] leftData, float[] rightData, SensorMode leftValue, SensorMode rightValue) {
		this.odometer = odometer;
		this.leftData = leftData;
		this.rightData = rightData;
		this.leftValue = leftValue;
		this.rightValue = rightValue;
		lock = false;
	}

	public void run() {
		long correctionStart, correctionEnd;
		float leftNow, rightNow;
		float leftLast = fetchLeftLine();
		float rightLast = fetchRightLine();
		while (true) {
			correctionStart = System.currentTimeMillis();
			leftNow = fetchLeftLine();
			rightNow = fetchRightLine();
			//If the right sensor detects a black line first
			if ((rightLast - rightNow > 0.1) && !lock) {
				x1 = odometer.getX();
				y1 = odometer.getY();
				while (!leftLine) {
					leftNow = fetchLeftLine();
					if (leftLast - leftNow > 0.1) {
						x2 = odometer.getX();
						y2 = odometer.getY();
						calculatedTheta = calculateCorrect(true);
						leftLine = true;
					}
					leftLast = leftNow;
				}
				odometer.setTheta(correctTheta(calculatedTheta));
				//If the left sensor detects a black line first.
			} else if ((leftLast - leftNow > 0.1) && !lock) {
				x1 = odometer.getX();
				y1 = odometer.getY();
				while (!rightLine) {
					rightNow = fetchRightLine();
					if (rightLast - rightNow > 0.1) {
						x2 = odometer.getX();
						y2 = odometer.getY();
						calculatedTheta = calculateCorrect(false);
						rightLine = true;
					}
					rightLast = rightNow;
				}
				odometer.setTheta(correctTheta(calculatedTheta));
			}
			rightLast = rightNow;
			leftLast = leftNow;

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

	public double calculateCorrect(boolean rightFirst) {
		double deltaX = x2 - x1;
		double deltaY = y2 - y1;
		double correct = 0;

		double position = Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));
		double angle = Math.atan2(position, DISTANCE);
		if (rightFirst) {
			correct = 0.0 - angle;
		} else if (!rightFirst) {
			correct = 0.0 + angle;
		}
		return correct;
	}

	public double correctTheta(double theta) {
		double theta1 = odometer.getTheta();
		double theta2 = 0;

		if (theta1 <= 45 || theta1 >= 315) {// Facing North
			if (theta > 0) {
				theta2 = theta;
			} else if (theta < 360) {
				theta2 = 360 + theta;
			}
		} else if (theta1 >= 45 && theta1 <= 135) {// Facing East
			theta2 = 90 + theta;
		} else if (theta1 >= 135 && theta1 <= 225) {// Facing South
			theta2 = 180 + theta;
		} else if (theta1 >= 225 && theta1 <= 315) {// Facing West
			theta2 = 270 + theta;
		}
		return theta2;
	}

	public float fetchRightLine() {
		rightValue.fetchSample(rightData, 0);
		return rightData[0];

	}

	public float fetchLeftLine() {
		leftValue.fetchSample(leftData, 0);
		return leftData[0];
	}
}
