package soccerRobot;

import lejos.robotics.SampleProvider;
import soccerRobot.UltrasonicController;

public class UltrasonicPoller extends Thread {
	private SampleProvider us;
	private UltrasonicController cont;
	private float[] usData;
	public static int distance;
	
	public UltrasonicPoller(SampleProvider us, float[] usData, UltrasonicController cont) {
		this.us = us;
		this.cont = cont;
		this.usData = usData;
	}

//  Sensors now return floats using a uniform protocol.
//  Need to convert US result to an integer [0,255]
	
	public void run() {
		while (true) {
			us.fetchSample(usData,0);							// acquire data
			distance=(int)(usData[0]*100.0);					// extract from buffer, cast to int
			cont.processUSData(distance);						// now take action depending on value
			try { Thread.sleep(50); } catch(Exception e){}		// Poor man's timed sampling
		}
	}


}
