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

public class Controller extends Thread {

	private Odometer odo;
	private USLocalizer usl;
	private static final int ROTATE_SPEED = 300;	
	public static int ROTATION_SPEED = 80;
	private static double turnAngle;
	private static double newTheta;
	private boolean navigating = false;
	private final int motorStraight = 200;
	
	private static final double tile_spacing=30.48; //Tile spacing
	
	private double PlatX; // The x coordinate of the bottom left corner of the platform
	private double PlatY; // The y coordinate of the bottom left corner of the platform
	private double UPlatX; // **************The x coordinate of the upper right corner of the platform
	private double UPlatY; // **************The y coordinate of the upper right corner of the platform
	private double EdgeToCenter = 3.81;  //The distance from the edge of the platform to the center of the first ball
	private double SensorToWheels = 3.50;  //The distance between the light sensor and the front wheels.
	private double DistTwoBalls = 7.62; //The distance from the center of one ball to the other
	private double Color1;
	private double Color2;
	private int BallsPicked = 0;
	private int BallsChecked = 0;
	
	public double deltaX,deltaY;

	public Controller(Odometer odo, USLocalizer usl) {
		this.odo = odo;
		this.usl = usl;
	}

	public Controller(Odometer odo) {
		this.odo = odo;
	}
	
	public void run(){
		pickUp();
		deltaX = odo.getX();
		deltaY = odo.getY() - soccerVehicle.SC*tile_spacing;
		turnTo(Math.atan2(deltaX, deltaY));
		shoot();
	}
	public void travelTo(double x, double y){
		navigating = true;

		// Getting odometer data and storing it into variables
		double curX = odo.getX();
		double curY = odo.getY();
		double curTheta = odo.getTheta();
		// Some math to calculate the changes in x, y and the linear distance to
		// be travelled
		deltaX = x - curX;
		deltaY = y - curY;
		double travelDis = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
		// The following conditions will return the difference between the
		// destination angle and the current one
		// This result varies depending on which quadrant the destination lies
		// in
		if (deltaX > 0 && deltaY > 0) {
			newTheta = 90 - (Math.atan2(deltaY, deltaX) * (180 / Math.PI));
		} else if ((deltaX < 0) && (deltaY > 0)) {
			newTheta = 270 + (180 - ((Math.atan2(deltaY, deltaX) * (180 / Math.PI))));
		} else if ((deltaX < 0) && (deltaY < 0)) {
			newTheta = 90 - (Math.atan2(deltaY, deltaX) * (180 / Math.PI));
		} else if ((deltaX > 0) && (deltaY < 0)) {
			newTheta = 90 - (Math.atan2(deltaY, deltaX) * (180 / Math.PI));
		} else if (deltaX >= -5 && deltaX <= 5 && deltaY > 0) {
			newTheta = 0;
		} else if (deltaX >= -5 && deltaX <= 5 && deltaY < 0) {
			newTheta = 180;
		} else if (deltaY >= -5 && deltaY <= 5 && deltaX > 0) {
			newTheta = 90;
		} else if (deltaY >= -5 && deltaY <= 5 && deltaX < 0) {
			newTheta = 270;
		}
		// Call to the turnTo() method so that the robot faces the correct
		// direction
		turnTo(newTheta);
		// After turning, set the motors to forward speed and drive for the
		// previously calculated traveDis
		soccerVehicle.getLeftMotor().setSpeed(motorStraight);
		soccerVehicle.getRightMotor().setSpeed(motorStraight);
		//soccerVehicle.getLeftMotor().forward();
		//soccerVehicle.getRightMotor().forward();
		soccerVehicle.getLeftMotor().rotate(convertDistance(soccerVehicle.WHEEL_RADIUS, travelDis), true);
		soccerVehicle.getRightMotor().rotate(convertDistance(soccerVehicle.WHEEL_RADIUS, travelDis), false);	
	}


	public void turnTo(double theta){
		// Store the current angle in a variable
		double curTheta = odo.getTheta();
		// Using the angle difference calculated in travelTo()
		// check if it the minimal angle and if not, change it accordingly
		if((theta - curTheta) >= -180 && (theta - curTheta) <= 180){
			turnAngle = theta - curTheta;
		}
		else if((theta - curTheta) < -180){
			turnAngle = (theta - curTheta) +360;
		}
		else if((theta - curTheta) > 180){
			turnAngle = (theta - curTheta) -360;
		}
		// Set the motors to rotate speed and rotate the minimal angle
		soccerVehicle.getLeftMotor().setSpeed(ROTATE_SPEED);
		soccerVehicle.getRightMotor().setSpeed(ROTATE_SPEED);
		soccerVehicle.getLeftMotor().rotate(convertAngle(soccerVehicle.WHEEL_RADIUS, soccerVehicle.TRACK, turnAngle), true);
		soccerVehicle.getRightMotor().rotate(-convertAngle(soccerVehicle.WHEEL_RADIUS, soccerVehicle.TRACK, turnAngle), false);
			
			navigating = true;
		}

		public boolean isNavigating(){
			boolean answer = false;
			if(navigating){
				answer = true;
			}
			return answer;
		}

	public static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	public static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}

	public void Stop() {
		soccerVehicle.getLeftMotor().stop(true);
		soccerVehicle.getRightMotor().stop(false);
	}

	public void forward() {
		soccerVehicle.getLeftMotor().setSpeed(motorStraight);
		soccerVehicle.getRightMotor().setSpeed(motorStraight);
		soccerVehicle.getLeftMotor().forward();
		soccerVehicle.getRightMotor().forward();
	}

	public void pickUp() {
		if (soccerVehicle.BC == 0) {
			Color1 = 7.0;
			Color2 = 7.0;
		}
		else if(soccerVehicle.BC == 1) {
			Color1 = 2.0;
			Color2 = 2.0;
		}
		else if(soccerVehicle.BC == 2) {
			Color1 = 7.0;
			Color2 = 2.0;
		}
		
		PlatX = (soccerVehicle.llx)*tile_spacing;
		PlatY = (soccerVehicle.lly)*tile_spacing;
		
		if (BallsChecked == 0) {
		   travelTo(PlatX - 20, PlatY);
		   Stop();
		   turnTo(0.0);
		   travelTo(odo.getX(), (odo.getY() + EdgeToCenter));
		   Stop();
		}

		else {
			travelTo(odo.getX(), odo.getY() + DistTwoBalls);
			Stop();
		}
		
		turnTo(95);
		soccerVehicle.getLeftMotor().forward();
		soccerVehicle.getRightMotor().forward();
		
		soccerVehicle.sweepSensor.fetchSample(soccerVehicle.sweepData, 0);
		float color = soccerVehicle.sweepData[0];
		
		while (color != 7.0 && color != 2.0) {
			soccerVehicle.sweepSensor.fetchSample(soccerVehicle.sweepData, 0);
			color = soccerVehicle.sweepData[0];
		}
		
		if (color == Color1 || color == Color2) {
			Stop();
			
			soccerVehicle.shootMotor.setAcceleration(1000000);
			soccerVehicle.shootMotor.setSpeed(1000000);
            soccerVehicle.shootMotor.forward();
			try {
				Thread.sleep(2000);
			}
	        catch (InterruptedException e) {	
	        }
			soccerVehicle.getLeftMotor().setSpeed(200);
			soccerVehicle.getRightMotor().setSpeed(200);
			soccerVehicle.getLeftMotor().forward();
			soccerVehicle.getRightMotor().forward();
			try {
				Thread.sleep(800);
			}
	        catch (InterruptedException e) {	
	        }
		    Stop();
		    soccerVehicle.upperMotor.stop();
		    
		    soccerVehicle.getLeftMotor().setSpeed(100);
			soccerVehicle.getRightMotor().setSpeed(100);
			soccerVehicle.getLeftMotor().backward();
			soccerVehicle.getRightMotor().backward();
			try {
				Thread.sleep(5000);
			} 
			catch (InterruptedException e) {
			}
			Stop();
					
		    turnTo(0.0);
		    BallsPicked++;
		    BallsChecked++;
		    if (BallsChecked != 4){
		       //pickUp();
		    }
		}
		else {
			Stop();
			soccerVehicle.getLeftMotor().setSpeed(100);
			soccerVehicle.getRightMotor().setSpeed(100);
			soccerVehicle.getLeftMotor().backward();
			soccerVehicle.getRightMotor().backward();
			try {
				Thread.sleep(3000);
			} 
			catch (InterruptedException e) {
			}
			Stop();
			
			turnTo(0.0);
			BallsChecked++;
			if (BallsChecked != 4);
			   pickUp();
		}
	}
	
	public void shoot() {
		soccerVehicle.shootMotor.setAcceleration(1000000);
        soccerVehicle.shootMotor.setSpeed(1000000);
        soccerVehicle.shootMotor.backward();
        
        try {
			Thread.sleep(2000);
		}
        catch (InterruptedException e) {
        }
        
        soccerVehicle.shootMotor.stop();
        BallsPicked--;
	}

	
}