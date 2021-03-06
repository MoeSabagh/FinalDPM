/*Final Project TEAM02
 * Gareth Peters | ID: 260678626
 * Catherine Bittar | ID: 260687735
 * Ryan (LuoQing) Wang | ID: 260524744
 * Ammar Sarfaraz Azir | ID: 260565232
 * Kevin-Rafael Sorto-Ventura | ID: 260692767
 * Mohamed Elsabagh | ID: 260603261
 * */
package finalProject;

public class Controller extends Thread {

	private Odometer odo;
	private USLocalizer usl;
	private static final int FORWARD_SPEED = 400;	
	private static final int ROTATE_SPEED = 300;	
	public static int ROTATION_SPEED = 80;

	private boolean navigating = false;
	private final int bandCenter = 24, bandwidth = 3;
	private final int motorStraight = 200 , FILTER_OUT = 20;
	private int distance;
	private int filterControl;
	//tile spacing
	private static final double tile_spacing=30.48;
	
	private double PlatX ; // **************The x coordinate of the bottom left corner of the platform
	private double PlatY ; // **************The y coordinate of the bottom left corner of the platform
	private double UPlatX; // **************The x coordinate of the upper right corner of the platform
	private double UPlatY; // **************The y coordinate of the upper right corner of the platform
	private double EdgeToCenter = 3.81;  //The distance from the edge of the platform to the center of the first ball
	private double SensorToWheels = 3.50;  //*******************The distance between the light sensor and the front wheels.
	private double DistTwoBalls = 7.62; //The distance from the center of one ball to the other
	private double BallColor = 7.0; // the color of the ball to be picked
	private double Ball_Oponent; //*********** Oponents ball color
	private int BallsPicked = 0;
	private int BallsChecked = 0;

	public Controller(Odometer odo, USLocalizer usl) {
		this.odo = odo;
		this.usl = usl;
	}

	public Controller(Odometer odo) {
		this.odo = odo;
	}
	public void run(){
		pickUp();
		double deltaX = odo.getX();
		double deltaY = odo.getY() - soccerVehicle.SC*tile_spacing;
		turnTo(Math.atan2(deltaX, deltaY));
		shoot();
	}

	public void travelTo(double x, double y) {
		
		this.navigating = true;
		// The first thing the ev3 does is turn to
		// the destination position and goes forward
		double deltaX = x - odo.getX();
		double deltaY = y - odo.getY();
		turnTo(Math.atan2(deltaX, deltaY));
		soccerVehicle.getLeftMotor().setSpeed(200);
		soccerVehicle.getRightMotor().setSpeed(200);
		soccerVehicle.getLeftMotor().forward();
		soccerVehicle.getRightMotor().forward();
		// Getting the values of the US sensor to
		// check the distance from an obstacle.
		distance = usl.getFilteredData();
		while (isNavigating()) {
			distance = usl.getFilteredData();
			// Redeclaration of values for when the Navigator avoids an obstacle
			deltaX = x - odo.getX();
			deltaX = y - odo.getY();
			// Checks if there's an obstacle in the way.
			// If so, avoid it
			if (bandCenter > distance) {
				doAvoidance(deltaX, deltaY);
				deltaX = x - odo.getX();
				deltaX = y - odo.getY();
				turnTo(Math.atan2(deltaX, deltaY));
			}
			
			// After it clears the obstacle, turn
			// to the designated position and go forward
			if (Math.toDegrees(Math.atan2(deltaX, deltaY)) - odo.getTheta() > 3) {
				turnTo(Math.atan2(deltaX, deltaY));
			}
			if ((Math.pow(deltaX, 2) + Math.pow(deltaY, 2)) <= 0.5) {
				soccerVehicle.getLeftMotor().stop(true);
				soccerVehicle.getRightMotor().stop(false);
				navigating = false;
				break;
			}
			soccerVehicle.getLeftMotor().setSpeed(200);
			soccerVehicle.getRightMotor().setSpeed(200);
			soccerVehicle.getLeftMotor().forward();
			soccerVehicle.getRightMotor().forward();
		}
	}

	public void turnTo(double theta) {
		
		// Takes the difference in theta to see how much the robot must rotate.
		theta = Math.toDegrees(theta);
		double thetaNow = odo.getTheta();
		double thetaDifference = theta - thetaNow;
		// Minimizes the amount it needs to turn.
		while (thetaDifference <= -180) {
			thetaDifference += 360;
		}
		while (thetaDifference >= 180) {
			thetaDifference -= 360;
		}
		// Rotates to the desired angle
		soccerVehicle.getLeftMotor().setSpeed(ROTATE_SPEED);
		soccerVehicle.getRightMotor().setSpeed(ROTATE_SPEED);
		soccerVehicle.getLeftMotor()
				.rotate(convertAngle(soccerVehicle.WHEEL_RADIUS, soccerVehicle.TRACK, thetaDifference), true);
		soccerVehicle.getRightMotor()
				.rotate(-convertAngle(soccerVehicle.WHEEL_RADIUS, soccerVehicle.TRACK, thetaDifference), false);
	}

	public boolean isNavigating() {
		return this.navigating;
	}

	public static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	public static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}

	public void Stop() {
		soccerVehicle.getLeftMotor().setSpeed(0);
		soccerVehicle.getRightMotor().setSpeed(0);
		soccerVehicle.getLeftMotor().forward();
		soccerVehicle.getRightMotor().forward();
	}

	public void forward() {
		soccerVehicle.getLeftMotor().setSpeed(motorStraight);
		soccerVehicle.getRightMotor().setSpeed(motorStraight);
		soccerVehicle.getLeftMotor().forward();
		soccerVehicle.getRightMotor().forward();
	}

	public void pickUp() {
		if (BallsChecked == 0) {
		   travelTo(PlatX - 10, PlatY);
		   soccerVehicle.getLeftMotor().stop();
		   soccerVehicle.getRightMotor().stop();
		   turnTo(0.0);
		   travelTo(odo.getX(), odo.getY() + EdgeToCenter);
		   soccerVehicle.getLeftMotor().stop();
		   soccerVehicle.getRightMotor().stop();
		}
		
		else {
			travelTo(odo.getX(), odo.getY() + DistTwoBalls);
			soccerVehicle.getLeftMotor().stop();
			soccerVehicle.getRightMotor().stop();
		}
		
		turnTo(90);
		soccerVehicle.getLeftMotor().forward();
		soccerVehicle.getRightMotor().forward();
		
		soccerVehicle.sweepSensor.fetchSample(soccerVehicle.sweepData, 0);
		float color = soccerVehicle.sweepData[0];
		
		while (color != BallColor && color != Ball_Oponent) {
			soccerVehicle.sweepSensor.fetchSample(soccerVehicle.sweepData, 0);
			color = soccerVehicle.sweepData[0];
		}
		
		soccerVehicle.getLeftMotor().stop();
		soccerVehicle.getRightMotor().stop();
		
		if (color == BallColor) {
			soccerVehicle.upperMotor.setSpeed(5000);
            soccerVehicle.upperMotor.forward();
		    travelTo(odo.getX() + SensorToWheels, odo.getY());
		    soccerVehicle.getLeftMotor().stop();
			soccerVehicle.getRightMotor().stop();
		    soccerVehicle.upperMotor.stop();
		    travelTo(odo.getX() - SensorToWheels, odo.getY());
		    turnTo(0.0);
		    BallsPicked++;
		    BallsChecked++;
		    if (BallsChecked != 4);
		       //pickUp();

		}
		else {
			turnTo(0.0);
			BallsChecked++;
			if (BallsChecked != 4);
			   //pickUp();
		}
		

	}
	
	public void shoot() {
        soccerVehicle.upperMotor.setSpeed(1000);
        soccerVehicle.upperMotor.backward();
        
        try {
			Thread.sleep(2000);
		}
        catch (InterruptedException e) {
        }
        
        soccerVehicle.upperMotor.stop();
	}

	public void doAvoidance(double x, double y) {
		// Turn 90 degrees so as not to face the obstacle
		int theta;
		turnTo(Math.atan2(x, y) + (Math.PI / 2));
		distance = usl.getFilteredData();
		// Checks to see if there's another obstacle in front of it.
		// If yes, rotates 180 degrees to face opposite direction. Also
		// rotates the direction the ultrasonic sensor is facing.
		if (distance <= bandCenter + 10) {
			turnTo(Math.atan2(x, y) - Math.PI);
			soccerVehicle.upperMotor.setSpeed(ROTATION_SPEED);
			theta = -80;
			soccerVehicle.upperMotor.rotate(theta);
		} else {
			theta = 80;
			soccerVehicle.upperMotor.setSpeed(ROTATION_SPEED);
			soccerVehicle.upperMotor.rotate(theta);
		}
		double thetaNow = odo.getTheta();
		double thetaAfter;
		while (true) {
			distance = usl.getFilteredData();
			processUSData(distance);
			thetaAfter=odo.getTheta();
			if ((thetaNow-thetaAfter>-180&&thetaNow-thetaAfter<-170)||(thetaAfter-thetaNow>-180&&thetaAfter-thetaNow<-170)) {
				soccerVehicle.getLeftMotor().stop(true);
				soccerVehicle.getRightMotor().stop(false);
				soccerVehicle.upperMotor.rotate(-theta);
				return;
			}
		}
	}

	public void processUSData(int distance) {
		if (distance > 100 && filterControl < FILTER_OUT) {
			// bad value, do not set the distance var, however do increment the
			// filter value
			filterControl++;

		} else if (distance > 100) {
			// set distance to 80 for readings between 100 to 255
			this.distance = 100;
			filterControl = 0;

		} else {
			// pass distance value for readings below 100
			filterControl = 0;
			this.distance = distance;
		}
		if (filterControl == 0) {

			// calculate error
			int error = Math.abs(distance - bandCenter) / bandwidth;
			// calculate high speed base on error
			int highSpeed = error * 11 + motorStraight;
			if (distance - bandCenter >= bandwidth) {
				// Turn left when too close to the right wall
				soccerVehicle.getRightMotor().setSpeed(highSpeed);
				soccerVehicle.getLeftMotor().setSpeed(motorStraight);
				soccerVehicle.getRightMotor().forward();
				soccerVehicle.getLeftMotor().forward();
			} else if (distance - bandCenter <= -bandwidth) {
				// Turn right when too close to the left wall
				soccerVehicle.getLeftMotor().setSpeed(highSpeed);
				soccerVehicle.getRightMotor().setSpeed(motorStraight);
				soccerVehicle.getRightMotor().forward();
				soccerVehicle.getLeftMotor().forward();
			} else {
				soccerVehicle.getRightMotor().setSpeed(motorStraight);
				soccerVehicle.getLeftMotor().setSpeed(motorStraight);
				soccerVehicle.getRightMotor().forward();
				soccerVehicle.getLeftMotor().forward();
			}

		}
	}
}