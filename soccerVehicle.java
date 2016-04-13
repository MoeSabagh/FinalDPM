/*Final Project TEAM02
 * Gareth Peters | ID: 260678626
 * Catherine Bittar | ID: 260687735
 * Ryan (LuoQing) Wang | ID: 260524744
 * Ammar Sarfaraz Azir | ID: 260565232
 * Kevin-Rafael Sorto-Ventura | ID: 260692767
 * Mohamed Elsabagh | ID: 260603261
 * */
package finalProject;

import java.io.IOException;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class soccerVehicle {
	public static final EV3LargeRegulatedMotor shootMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));
	private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("C"));
	public static final EV3LargeRegulatedMotor upperMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));

	private static final Port usPort = LocalEV3.get().getPort("S1");
	private static final Port colorPort = LocalEV3.get().getPort("S2");
	private static final Port lightPort = LocalEV3.get().getPort("S3");
	public static final EV3ColorSensor sweepSensor = new EV3ColorSensor(LocalEV3.get().getPort("S4"));
	public static final SensorMode sweepValue = sweepSensor.getColorIDMode();
	public static final float[] sweepData = new float[sweepSensor.sampleSize()];

	public static final double WHEEL_RADIUS = 2.1;
	public static final double TRACK = 19.6;
	public static final double TRACK_ODO = 20.2;

	// Parameters from the wifi
	public static int DSC,OSC, OTN , DTN,  w1, d1, d2, llx;
	public static int lly, urx, ury, BC;

	public static void main(String[] args) throws IOException {

	    // The setting of wifi variables
		WifiConnection wifi = new WifiConnection("192.168.10.105", 2);
		DSC = wifi.StartData.get("DSC"); // Defensive Starting corner
		OSC = wifi.StartData.get("OSC"); // Offensive Starting corner
	    OTN = wifi.StartData.get("OTN");//Offensive Team
	    DTN = wifi.StartData.get("DTN");//Defensive Team
		w1 = wifi.StartData.get("w1"); // Width of the net
		d1 = wifi.StartData.get("d1");// Position of defender line from North wall [1,5] (tiles)
		d2 = wifi.StartData.get("d2");// Position of offensive line from South wall [1,5] (tiles)
		llx = wifi.StartData.get("ll-x");// x position of the lower left of the platform [-1,11] (tiles)
		lly = wifi.StartData.get("ll-y");// y position of the lower left of the platform [-1,11] (tiles)
		urx = wifi.StartData.get("ur-x");// x position of the upper right of the platform [-1,11] (tiles)
		ury = wifi.StartData.get("ur-y");// y position of the upper right of the platform [-1,11] (tiles)
		BC = wifi.StartData.get("BC");// Forward ball color: 0 = red, 1 = blue, 2 = any
		@SuppressWarnings("resource")
		SensorModes usSensor = new EV3UltrasonicSensor(usPort);
		SampleProvider usValue = usSensor.getMode("Distance");
		float[] usData = new float[usValue.sampleSize()];

		@SuppressWarnings("resource")
		EV3ColorSensor colorSensor = new EV3ColorSensor(colorPort);
		SensorMode rightValue = colorSensor.getRedMode();
		float[] rightData = new float[rightValue.sampleSize()];

		Odometer odo = new Odometer();
		OdometryCorrection odoC = new OdometryCorrection(odo, rightValue, rightData);
		LCDInfo lcd = new LCDInfo(odo);

		odo.start();
		USLocalizer usl = new USLocalizer(odo, usValue, usData);
		Controller controller1 = new Controller(odo);
		LightLocalizer lsl = new LightLocalizer(odo, rightValue, rightData, controller1);
		Controller controller = new Controller(odo, usl, lsl);
		Avoidance avoid = new Avoidance(usl, controller, odo);
		Defense defense = new Defense(controller, avoid, odo);
	

		usl.doLocalization();
		lsl.doLocalization();
		controller.travelTo(0, 0);
		controller.turnTo(0);
		
		odoC.start();
		avoid.start();

		if (OTN == 2) {
			boolean[] setPos = { true, true, true };
			if (OSC == 1) {
				odo.setPosition(new double[] { 0, 0, 0 }, setPos);
			} else if (OSC == 4) {
				odo.setPosition(new double[] { 0, 300, 90 }, setPos);
			} else if (OSC == 3) {
				odo.setPosition(new double[] { 300, 300, 180 }, setPos);
			} else if (OSC == 2) {
				odo.setPosition(new double[] { 300, 0, 270 }, setPos);
			}
			controller.start();
		} else if (DTN == 2) {
			boolean[] setPos = { true, true, true };
			if (DSC == 1) {
				odo.setPosition(new double[] { 0, 0, 0 }, setPos);
			} else if (DSC == 4) {
				odo.setPosition(new double[] { 0, 300, 90 }, setPos);
			} else if (DSC == 3) {
				odo.setPosition(new double[] { 300, 300, 180 }, setPos);
			} else if (DSC == 2) {
				odo.setPosition(new double[] { 300, 0, 270 }, setPos);
			}
			defense.start();
		}

		while (Button.waitForAnyPress() != Button.ID_ESCAPE)
			System.exit(0);
	}

	public static EV3LargeRegulatedMotor getLeftMotor() {
		return leftMotor;
	}

	public static EV3LargeRegulatedMotor getRightMotor() {
		return rightMotor;
	}
}