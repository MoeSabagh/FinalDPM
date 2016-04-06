package finalProject;

public class Avoidance extends Thread {
	private final int bandCenter = 18, bandwidth = 3;
	private final int motorStraight = 200, FILTER_OUT = 20;
	private int distance;
	private int filterControl;
	private USLocalizer usl;
	private Controller controller;
	private Odometer odo;
	private static int ROTATION_SPEED = 80;
	private int theta;

	public int getBandCenter() {
		return bandCenter;
	}

	public Avoidance(USLocalizer usl, Controller controller, Odometer odo) {
		this.usl = usl;
		this.controller = controller;
		this.odo = odo;
	}

	@SuppressWarnings("deprecation")
	public void run() {

		while (true) {
			soccerVehicle.upperMotor.setSpeed(ROTATION_SPEED);
			theta = 30;
			soccerVehicle.upperMotor.rotate(30);
			try {Thread.sleep(500);} catch (Exception e) {};
			if (usl.getFilteredData() < bandCenter) {
				controller.suspend();
				doAvoidance(controller.deltaX, controller.deltaY);
				controller.pickUp();
				controller.resume();
			}
			soccerVehicle.upperMotor.setSpeed(ROTATION_SPEED);
			theta = -30;
			soccerVehicle.upperMotor.rotate(-60);
			try {Thread.sleep(500);} catch (Exception e) {};
			if (usl.getFilteredData() < bandCenter) {
				controller.suspend();
				doAvoidance(controller.deltaX, controller.deltaY);
				controller.resume();
			}
			soccerVehicle.upperMotor.setSpeed(ROTATION_SPEED);
			theta = 0;
			soccerVehicle.upperMotor.rotate(30);
			try {Thread.sleep(500);} catch (Exception e) {};
			if (usl.getFilteredData() < bandCenter) {
				controller.suspend();
				doAvoidance(controller.deltaX, controller.deltaY);
				controller.resume();
			}
			try {Thread.sleep(50);} catch (Exception e) {};
		}
	}

	public void doAvoidance(double x, double y) {
		// Turn 90 degrees so as not to face the obstacle
		double thetaNow = odo.getTheta();
		double thetaAfter;
		if(theta == 0){
		controller.turnTo(Math.toDegrees(Math.atan2(x, y) + (Math.PI / 2)));
	    }else if(theta == 30){
		controller.turnTo(Math.toDegrees(Math.atan2(x, y) + (Math.PI/3)));
	    }else if(theta == -30){
	    	controller.turnTo(Math.toDegrees(Math.atan2(x, y)-(Math.PI/3)));
	    }
		distance = usl.getFilteredData();
		// Checks to see if there's another obstacle in front of it.
		// If yes, rotates 180 degrees to face opposite direction. Also
		// rotates the direction the ultrasonic sensor is facing.
		if (distance <= bandCenter + 10) {
			controller.turnTo(Math.toDegrees(Math.atan2(x, y) - Math.PI));
			soccerVehicle.upperMotor.setSpeed(ROTATION_SPEED);
			if(theta == 0){
				theta = -80;
			}else if(theta == 30){
				theta = -110;
			}else if(theta == -30){
				theta = -50;
			}
			soccerVehicle.upperMotor.rotate(theta);	
		} else {
			if(theta == 0){
				theta = 80;
			}else if(theta == 30){
				theta = 50;
			}else if(theta == -30){
				theta = 110;
			}
			soccerVehicle.upperMotor.setSpeed(ROTATION_SPEED);
			soccerVehicle.upperMotor.rotate(theta);
		}
		
		while (true) {
			distance = usl.getFilteredData();
			processUSData(distance);
			thetaAfter = odo.getTheta();
			if(theta<0){
				if(thetaNow<=90){
					if (thetaNow-thetaAfter>-260 && thetaNow-thetaAfter<-280) {
						soccerVehicle.getLeftMotor().stop(true);
						soccerVehicle.getRightMotor().stop(false);
						soccerVehicle.upperMotor.setSpeed(ROTATION_SPEED);
						theta = 80;
						soccerVehicle.upperMotor.rotate(theta);
						theta = 0;
						return;
					}
				}else{
					if (thetaNow-thetaAfter<-80 && thetaNow-thetaAfter>-100) {
						soccerVehicle.getLeftMotor().stop(true);
						soccerVehicle.getRightMotor().stop(false);
						soccerVehicle.upperMotor.setSpeed(ROTATION_SPEED);
						theta = 80;
						soccerVehicle.upperMotor.rotate(theta);
						theta = 0;
						return;
					}
				}
			}else if(theta>0){
				if(thetaNow>=270){
					if (thetaNow-thetaAfter>280 && thetaNow-thetaAfter<260) {
						soccerVehicle.getLeftMotor().stop(true);
						soccerVehicle.getRightMotor().stop(false);
						soccerVehicle.upperMotor.setSpeed(ROTATION_SPEED);
						theta = -80;
						soccerVehicle.upperMotor.rotate(theta);
						theta = 0;
						return;
					}
				}else{
					if (thetaNow-thetaAfter<-80 && thetaNow-thetaAfter<-100) {
						soccerVehicle.getLeftMotor().stop(true);
						soccerVehicle.getRightMotor().stop(false);
						soccerVehicle.upperMotor.setSpeed(ROTATION_SPEED);	
						theta = -80;
						soccerVehicle.upperMotor.rotate(theta);
						theta = 0;
						return;
					}
				}
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
