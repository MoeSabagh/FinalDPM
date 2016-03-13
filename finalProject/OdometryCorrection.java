package finalProject;

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorMode;


public class OdometryCorrection extends Thread {
	
	//lightSensor
	private static final Port colorPort = LocalEV3.get().getPort("S2");
	private static EV3ColorSensor colorSensor = new EV3ColorSensor(colorPort);
	private static SensorMode colorR=colorSensor.getMode("Red");
	private static float sampleRGB[]=new float[colorR.sampleSize()];
	
	private static double x,y,theta;
	
	//distance of the sensor from the wheel axis
	private static final double sensor_offset=13.5;
	//tile spacing
	private static final double tile_spacing=30.48;
	//find closest line
	
	
	private Odometer odometer;

	// constructor
	public OdometryCorrection(Odometer odometer) {
		this.odometer = odometer;
	}

	// run method (required for Thread)
	public void run() {
	}
}
	
