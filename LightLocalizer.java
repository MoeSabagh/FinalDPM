/*Final Project TEAM02
 * Gareth Peters | ID: 260678626
 * Catherine Bittar | ID: 260687735
 * Ryan (LuoQing) Wang | ID: 260524744
 * Ammar Sarfaraz Azir | ID: 260565232
 * Kevin-Rafael Sorto-Ventura | ID: 260692767
 * Mohamed Elsabagh | ID: 260603261
 * */
package finalProject;

import lejos.hardware.Sound;
import lejos.hardware.sensor.SensorMode;

public class LightLocalizer {
	private Odometer odo;
	private SensorMode colorSensor;
	private float[] colorData;
	private Controller controller;
	private int start;
	private double Black_Line = 0.35;
	private double Sensor_Offset = 14.0;
	private boolean crossLine = false;
	private static final int ROTATE_SPEED = 300;
	private static final int ROTATE_SLOW = 100;
	private final double ERROR_X = 2.0;
	private final double ERROR_Y = 3.50;

	public LightLocalizer(Odometer odo, SensorMode colorSensor, float[] colorData, Controller controller,int start) {
		this.odo = odo;
		this.colorSensor = colorSensor;
		this.colorData = colorData;
		this.controller = controller;
		this.start = start;
	}

	public void doLocalization() {

		while (!crossLine()) {
			controller.forward();
		}
		controller.Stop();
		odo.setY(ERROR_Y + Sensor_Offset);
		controller.turnTo(0);
		soccerVehicle.getLeftMotor().setSpeed(ROTATE_SPEED);
		soccerVehicle.getRightMotor().setSpeed(ROTATE_SPEED);
		soccerVehicle.getLeftMotor().rotate(convertAngle(soccerVehicle.WHEEL_RADIUS, soccerVehicle.TRACK, 90.0), true);
		soccerVehicle.getRightMotor().rotate(-convertAngle(soccerVehicle.WHEEL_RADIUS, soccerVehicle.TRACK, 90.0),
				false);
		while (!crossLine()) {
			controller.forward();
		}
		controller.Stop();
		odo.setX(Sensor_Offset + ERROR_X);
		odo.setTheta(70);
		controller.travelTo(0, 0);
		controller.turnTo(0);
		boolean[] setPos = { true, true, true };
		if (start == 1) {
			odo.setPosition(new double[] { 0, 0, 0 }, setPos);
		} else if (start == 4) {
			odo.setPosition(new double[] { 0, 300, 90 }, setPos);
		} else if (start == 3) {
			odo.setPosition(new double[] { 300, 300, 180 }, setPos);
		} else if (start == 2) {
			odo.setPosition(new double[] { 300, 0, 270 }, setPos);
		}

	}

	public boolean crossLine() {

		// check if it pass a line
		crossLine = false;
		colorSensor.fetchSample(colorData, 0);
		if (colorData[0] < Black_Line) {
			crossLine = true;
			Sound.setVolume(Sound.VOL_MAX);
			Sound.playNote(Sound.PIANO, 784, 250);
		}

		return crossLine;
	}

	// convert distance from lab2
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	// convert angle from lab2
	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
}