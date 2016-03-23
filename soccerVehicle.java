/*Final Project TEAM02
 * Gareth Peters | ID: 260678626
 * Catherine Bittar | ID: 260687735
 * Ryan (LuoQing) Wang | ID: 260524744
 * Ammar Sarfaraz Azir | ID: 260565232
 * Kevin-Rafael Sorto-Ventura | ID: 260692767
 * Mohamed Elsabagh | ID: 260603261
 * */
package finalProject;

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
	
	private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));
	private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("C"));
	public static final EV3LargeRegulatedMotor upperMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));

	private static final Port usPort = LocalEV3.get().getPort("S1");		
	private static final Port colorPort = LocalEV3.get().getPort("S2");
	public static final EV3ColorSensor sweepSensor = new EV3ColorSensor(LocalEV3.get().getPort("S4"));
	public static final SensorMode sweepValue = sweepSensor.getColorIDMode();			
	public static final float[] sweepData = new float[sweepSensor.sampleSize()];
	

	public static final double WHEEL_RADIUS = 2.1;
	public static final double TRACK = 19.6;
	public static final double TRACK_ODO=21.3;

	public static void main (String[] args){
		@SuppressWarnings("resource")							    	
		SensorModes usSensor = new EV3UltrasonicSensor(usPort);
		SampleProvider usValue = usSensor.getMode("Distance");			
		float[] usData = new float[usValue.sampleSize()];
		
		EV3ColorSensor colorSensor = new EV3ColorSensor(colorPort);
		SensorMode colorValue = colorSensor.getRedMode();			
		float[] colorData = new float[colorValue.sampleSize()];
		
		Odometer odo = new Odometer();
		OdometryCorrection odoC = new OdometryCorrection(odo, colorValue, colorData);
		odo.start();
		LCDInfo lcd = new LCDInfo(odo);
		USLocalizer usl = new USLocalizer(odo, usValue, usData);
		usl.doLocalization();
		Controller controller = new Controller(odo,usl);	
		LightLocalizer lsl = new LightLocalizer(odo, colorValue, colorData,controller);
		lsl.doLocalization();	
		odoC.start();
		controller.turnTo(90);
		//controller.pickUp();
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);	
	}
	public static EV3LargeRegulatedMotor getLeftMotor() {
		return leftMotor;
	}

	public static EV3LargeRegulatedMotor getRightMotor() {
		return rightMotor;
	}
}