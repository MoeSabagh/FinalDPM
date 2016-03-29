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
	public static final double TRACK_ODO = 21.3;

	// Parameters from the wifi
	public static int SC, Role, w1, d1, d2, llx;
	public static int lly, urx, ury, BC;

	public static void main(String[] args) throws IOException {
		
		//The setting of wifi variables
		WifiConnection wifi = new WifiConnection("192.168.10.109", 2);
		int start = 1; //For beta demo
		SC = wifi.StartData.get("SC"); //Starting corner
		Role = wifi.StartData.get("Role");//Position: 0=Forward, 1=Defense
		w1 = wifi.StartData.get("w1");//Width of the goal [1,4] (tiles)
		d1 = wifi.StartData.get("d1");//Position of defender line from North wall [1,5] (tiles)
		d2 = wifi.StartData.get("d2");//Position of offensive line from South wall [1,5] (tiles)
		llx = wifi.StartData.get("ll-x");//x position of the lower left of the platform [-1,11] (tiles)
		lly = wifi.StartData.get("ll-y");//y position of the lower left of the platform [-1,11] (tiles)
		urx = wifi.StartData.get("ur-x");//x position of the upper right of the platform [-1,11] (tiles)
		ury = wifi.StartData.get("ur-y");//y position of the upper right of the platform [-1,11] (tiles)
		BC = wifi.StartData.get("BC");//Forward ball color: 0 = red, 1 = blue, 2 = any

		@SuppressWarnings("resource")
		SensorModes usSensor = new EV3UltrasonicSensor(usPort);
		SampleProvider usValue = usSensor.getMode("Distance");
		float[] usData = new float[usValue.sampleSize()];

		EV3ColorSensor colorSensor = new EV3ColorSensor(colorPort);
		SensorMode rightValue = colorSensor.getRedMode();
		float[] rightData = new float[rightValue.sampleSize()];

		EV3ColorSensor lightSensor = new EV3ColorSensor(lightPort);
		SensorMode leftValue = lightSensor.getRedMode();
		float[] leftData = new float[leftValue.sampleSize()];
		
		Odometer odo = new Odometer();
		OdometryCorrection odoC = new OdometryCorrection(odo, rightValue, rightData);
		OdometryAngleCorrection odoAC = new OdometryAngleCorrection(odo, leftData, rightData, leftValue, rightValue);
		LCDInfo lcd = new LCDInfo(odo);
		
		odo.start();
		USLocalizer usl = new USLocalizer(odo, usValue, usData);
		Controller controller = new Controller(odo, usl);
		LightLocalizer lsl = new LightLocalizer(odo, rightValue, rightData, controller, start);
	
		usl.doLocalization();	
		lsl.doLocalization();
		odoC.start();
		odoAC.start();
		controller.start(); // For beta demo
		/*if(Role == 0){
		 * controller.start();
		 * }else if(Role == 1){
		 * Defense defense = new Defense(controller, odo);
		 * defense.start();
		 * }
		 */
		

		while (Button.waitForAnyPress() != Button.ID_ESCAPE)
			;
		System.exit(0);
	}

	public static EV3LargeRegulatedMotor getLeftMotor() {
		return leftMotor;
	}

	public static EV3LargeRegulatedMotor getRightMotor() {
		return rightMotor;
	}
}