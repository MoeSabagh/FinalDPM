/*Final Project TEAM02
 * Gareth Peters | ID: 260678626
 * Catherine Bittar | ID: 260687735
 * Ryan (LuoQing) Wang | ID: 260524744
 * Ammar Sarfaraz Azir | ID: 260565232
 * Kevin-Rafael Sorto-Ventura | ID: 260692767
 * Mohamed Elsabagh | ID: 260603261
 * */
package finalProject;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Odometer extends Thread {
	// robot position
	
	private double x, y, theta;
	public static int lastTachoL;			// Tacho L at last sample
	public static int lastTachoR;			// Tacho R at last sample 
	public static int nowTachoL;			// Current tacho L
	public static int nowTachoR;			// Current tacho R
	public static double X;					// Current X position
	public static double Y;					// Current Y position
	public static double Theta;				// Current orientation

	
	// odometer update period, in ms
	private static final long ODOMETER_PERIOD = 25;

	// lock object for mutual exclusion
	private Object lock;

	// default constructor
	public Odometer() {
		x = 0.0;
		y = 0.0;
		theta = 0.0;
		lock = new Object();
	}
	
	// run method (required for Thread)
	public void run() {
		long updateStart, updateEnd;
		double distL, distR, deltaD, deltaT, dX, dY;                // Initialization of some variables for the odometer calculations
	    
		while (true) {
			updateStart = System.currentTimeMillis();
			// put (some of) your odometer code here
			
			nowTachoL = soccerVehicle.getLeftMotor().getTachoCount();      		            // get tacho counts
			nowTachoR = soccerVehicle.getRightMotor().getTachoCount();
			distL = Math.PI*soccerVehicle.WHEEL_RADIUS*(nowTachoL-lastTachoL)/180;		// compute L and R wheel displacements
			distR = Math.PI*soccerVehicle.WHEEL_RADIUS*(nowTachoR-lastTachoR)/180;
			lastTachoL = nowTachoL;								                // save tacho counts for next iteration
			lastTachoR = nowTachoR;
			deltaD = 0.5*(distL+distR);							                // compute vehicle displacement
			deltaT = Math.atan((distL-distR)/soccerVehicle.TRACK);						// compute change in heading
		    dX = deltaD * Math.sin(Theta);						                // compute X component of displacement
			dY = deltaD * Math.cos(Theta);	                                    // compute Y component of displacement
			Theta = Theta + deltaT;									            // update heading				
			X = X + dX;											                // update estimates of X and Y position
			Y = Y + dY;		
			
			this.x = X;                // Passing the values of X and Y in x and y respectively
			this.y = Y;                // so that they can be used by other classes like OdometryDisplay. 

			synchronized (lock) {
				// don't use the variables x, y, or theta anywhere but here!
				
				// Setting boundaries (0, 360) on Theta,
				// converting Theta from radians to degrees 
				// and passing its value to theta so that it can be used by other classes.
				if(((Theta)*180/Math.PI) > 360){
					theta = (Theta*180/Math.PI) - 360;
				}
				else if(((Theta)*180/Math.PI) <0){
					theta = (Theta*180/Math.PI) + 360;
				}
				else if((Theta*180/Math.PI) > 0 && (Theta*180/Math.PI) <360){
					theta = (Theta*180/Math.PI);
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