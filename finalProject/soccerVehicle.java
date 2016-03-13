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
	
	private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	private static final Port usPort = LocalEV3.get().getPort("S1");		
	private static final Port colorPort = LocalEV3.get().getPort("S2");
	public static final double WHEEL_RADIUS = 2.1;
	public static final double TRACK = 23.5;

	public static void main (String[] args){
		@SuppressWarnings("resource")							    	
		SensorModes usSensor = new EV3UltrasonicSensor(usPort);
		SampleProvider usValue = usSensor.getMode("Distance");			
		float[] usData = new float[usValue.sampleSize()];
		
		EV3ColorSensor colorSensor = new EV3ColorSensor(colorPort);
		SensorMode colorValue = colorSensor.getRedMode();			
		float[] colorData = new float[colorValue.sampleSize()];
		
		Odometer odo = new Odometer();
		odo.start();
		LCDInfo lcd = new LCDInfo(odo);
		USLocalizer usl = new USLocalizer(odo, usValue, usData);
		usl.doLocalization();
		Controller controller = new Controller(odo,usl);	
		LightLocalizer lsl = new LightLocalizer(odo, colorValue, colorData);
		lsl.doLocalization();		
	}
	public static EV3LargeRegulatedMotor getLeftMotor() {
		return leftMotor;
	}

	public static EV3LargeRegulatedMotor getRightMotor() {
		return rightMotor;
	}
}
