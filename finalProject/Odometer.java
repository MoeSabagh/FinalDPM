/*
 * Odometer.java
 */
//DPM Group 8
//Gareth Peters
//ID:260678626
//LuoQing(Ryan) Wang
//ID:260524744


package soccerRobot;

public class Odometer extends Thread {
	// robot position
	private double x, y, theta;

	// odometer update period, in ms
	private static final long ODOMETER_PERIOD = 25;

	// lock object for mutual exclusion
	private Object lock;

	// default constructor
	public Odometer() {
		x = 0.0;
		y = 0.0;
		theta = 90.0;
		lock = new Object();
	}

	// run method (required for Thread)
	public void run() {
		long updateStart, updateEnd;
		;
		double TachoBeforeR=0;
		double TachoBeforeL=0;
		while (true) {
			double TachoNowR = soccerVehicle.getRightMotor().getTachoCount();
			double TachoNowL = soccerVehicle.getLeftMotor().getTachoCount();
			updateStart = System.currentTimeMillis();
			//Takes the difference of the Tacho counters and converts to radians for a later 
			double deltaLeft = Math.toRadians(TachoNowL - TachoBeforeL);
			double deltaRight = Math.toRadians(TachoNowR - TachoBeforeR);
			double radiusW = soccerVehicle.WHEEL_RADIUS;
			double track = soccerVehicle.TRACK;
			
			
			
			synchronized (lock) {
				//The change in arc and theta
				double deltaC = radiusW * (deltaLeft + deltaRight) / 2;
				double deltaTheta = radiusW * (deltaLeft - deltaRight) / track;
				theta=Math.toRadians(theta);
				//Add the change in x to the old value of x (similarly for y and theta)
				y+=deltaC*Math.cos(theta+deltaTheta/2);
				x+=deltaC*Math.sin(theta+deltaTheta/2);
				theta=Math.toDegrees(theta+deltaTheta);
				//Sets the value of now Tacho to old Tacho for next iteration
				TachoBeforeL=TachoNowL;
				TachoBeforeR=TachoNowR;
				//If theta!=(0,360] then set it so that it is.
				if(theta>360){
					theta=theta-360;
				}
				if(theta<0){
					theta=360+theta;
				}
			}

			// this ensures that the odometer only runs once every period
			updateEnd = System.currentTimeMillis();
			if (updateEnd - updateStart < ODOMETER_PERIOD) {
				try {
					Thread.sleep(ODOMETER_PERIOD - (updateEnd - updateStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometer will be interrupted by
					// another thread
				}
			}
		}
	}

	// accessors
	public void getPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				position[0] = x;
			if (update[1])
				position[1] = y;
			if (update[2])
				position[2] = theta;
		}
	}

	public double getX() {
		double result;

		synchronized (lock) {
			result = x;
		}

		return result;
	}

	public double getY() {
		double result;

		synchronized (lock) {
			result = y;
		}

		return result;
	}

	public double getTheta() {
		double result;

		synchronized (lock) {
			result = theta;
		}

		return result;
	}

	// mutators
	public void setPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				x = position[0];
			if (update[1])
				y = position[1];
			if (update[2])
				theta = position[2];
		}
	}

	public void setX(double x) {
		synchronized (lock) {
			this.x = x;
		}
	}

	public void setY(double y) {
		synchronized (lock) {
			this.y = y;
		}
	}

	public void setTheta(double theta) {
		synchronized (lock) {
			this.theta = theta;
		}
	}
}