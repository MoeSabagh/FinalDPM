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
import lejos.robotics.SampleProvider;

public class USLocalizer {

	public static int ROTATION_SPEED = 350;

	private Odometer odo;
	private SampleProvider usSensor;
	private float[] usData;
	private Controller controller;
	private final double sensor_offset = 13.5;

	// Constructor for the USL
	public USLocalizer(Odometer odo, SampleProvider usSensor, float[] usData) {
		this.odo = odo;
		this.usSensor = usSensor;
		this.usData = usData;
		this.controller = new Controller(odo);
	}

	// Performs ultrasonic localization
	public void doLocalization() {
		// Declaration of variables
		Sound.setVolume(Sound.VOL_MAX);
		Sound.playNote(Sound.FLUTE, 784, 250);
		double[] pos = new double[3];
		double thetaA = 0.0;
		double thetaB = 0.0;
		// Falling edge
		// Sets the rotation speed of the wheels and rotates on the spot
		soccerVehicle.getLeftMotor().setSpeed(ROTATION_SPEED);
		soccerVehicle.getRightMotor().setSpeed(ROTATION_SPEED);
		soccerVehicle.getLeftMotor().forward();
		soccerVehicle.getRightMotor().backward();
		// Gets the viewed distance from the wall
		float distance = getFilteredData();
		// For when the robot starts facing a wall
		while (distance < 43) {
			distance = getFilteredData();
			soccerVehicle.getLeftMotor().setSpeed(ROTATION_SPEED);
			soccerVehicle.getRightMotor().setSpeed(ROTATION_SPEED);
			soccerVehicle.getLeftMotor().forward();
			soccerVehicle.getRightMotor().backward();
		}
		// Constantly checks for when the distance from the wall
		// is less than 40. When it is, record the angle the odometer
		// reads and then rotate the opposite direction
		while (true) {
			distance = getFilteredData();
			if (distance < 40) {
				soccerVehicle.getLeftMotor().stop(true);
				soccerVehicle.getRightMotor().stop(false);
				thetaA = odo.getTheta();
				soccerVehicle.getLeftMotor().setSpeed(ROTATION_SPEED);
				soccerVehicle.getRightMotor().setSpeed(ROTATION_SPEED);
				soccerVehicle.getLeftMotor().backward();
				soccerVehicle.getRightMotor().forward();
				break;
			}
		}
		// So that it does read the distance again and record a wrong value
		try {
			Thread.sleep(1000);
		} catch (Exception e) {
		}
		// Same thing as before, but to get a second angle
		while (true) {
			distance = getFilteredData();
			if (distance < 40) {
				soccerVehicle.getLeftMotor().stop(true);
				soccerVehicle.getRightMotor().stop(false);
				thetaB = odo.getTheta();
				soccerVehicle.getLeftMotor().setSpeed(ROTATION_SPEED);
				soccerVehicle.getRightMotor().setSpeed(ROTATION_SPEED);
				soccerVehicle.getLeftMotor().forward();
				soccerVehicle.getRightMotor().backward();
				break;
			}
		}

		double deltaTheta = 0.0;
		// Corrects the angle so that the positive y
		// is considered to be 0 degrees
		if (thetaB > thetaA) {
			deltaTheta = 225 - (thetaA + thetaB) / 2;
		} else if (thetaB < thetaA) {
			deltaTheta = 45 - (thetaA + thetaB) / 2;
		}
		pos[2] = deltaTheta + odo.getTheta();
		odo.setPosition(pos, new boolean[] { false, false, true });
		// Rotates to face the proper 0 degrees
		soccerVehicle.getLeftMotor().setSpeed(ROTATION_SPEED);
		soccerVehicle.getRightMotor().setSpeed(ROTATION_SPEED);
		controller.turnTo(0);
	}

	public int getFilteredData() {
		usSensor.fetchSample(usData, 0);
		int distance = (int)(usData[0] * 100);
		if (distance > 70) {
			distance = 70;
		}
		return distance;
	}

}