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
	private LightLocalizer lsl;
	private static final int ROTATE_SPEED = 300;
	public static int ROTATION_SPEED = 80;
	private static double turnAngle;
	private static double newTheta;
	private boolean navigating = false;
	private final int motorStraight = 300;

	private static final double tile_spacing = 30.48; // Tile spacing
	private double PlatX = 6 * tile_spacing; // ***** The x coordinate of the bottom left corner of the platform
	private double PlatY = 5 * tile_spacing; // **** The y coordinate of the bottom left corner of the platform
	private double UPlatX; // **************The x coordinate of the upper right corner of the platform
	private double UPlatY; // **************The y coordinate of the upper right corner of the platform
	private double EdgeToCenter = 3.81; // The distance from the edge of the platform to the center of the first ball
	private double SensorToWheels = 3.50; // The distance between the light sensor and the front wheels.
	private double DistTwoBalls = 7.62; // The distance from the center of one ball to the other
	private double Color1 = 7.0;
	private double Color2 = 2.0;
	private int BallsPicked = 0;
	private int BallsChecked = 0;

	public double deltaX, deltaY;

	public Controller(Odometer odo, USLocalizer usl, LightLocalizer lsl) {
		this.odo = odo;
		this.usl = usl;
		this.lsl = lsl;
	}

	public Controller(Odometer odo) {
		this.odo = odo;
	}

	public void run() {
		if (soccerVehicle.OSC == 1 || soccerVehicle.OSC == 2) {
			turnTo(0);
			if (soccerVehicle.OSC == 1) {
				travelTo(0, 0.5 * tile_spacing);
			} else if (soccerVehicle.OSC == 2) {
				travelTo(10 * tile_spacing, 0.5 * tile_spacing);
			}
		} else if (soccerVehicle.OSC == 3 || soccerVehicle.OSC == 4) {
			turnTo(180);
			if (soccerVehicle.OSC == 4) {
				travelTo(0, 0.5 * tile_spacing);
			} else if (soccerVehicle.OSC == 3) {
				travelTo(10 * tile_spacing, 0.5 * tile_spacing);
			}
		}
		travelTo(5 * tile_spacing, 0.5 * tile_spacing);
		pickUp();
		travelTo(5 * tile_spacing, (soccerVehicle.d2-0.5)*tile_spacing);
		turnTo(0);
		int z = 0;
		while (BallsPicked != 0) {
			z = (int) (Math.random() * 3);
			if (z == 0) {
				turnTo(0);
			} else if (z == 1) {
				deltaX = -soccerVehicle.w1 / 2 + 10;
				deltaY = 11 * tile_spacing - (11.5 - soccerVehicle.d1) * tile_spacing;
				turnTo(newTheta(deltaX, deltaY));
			} else if (z == 2) {
				deltaX = soccerVehicle.w1 / 2 - 10;
				deltaY = 11 * tile_spacing - (11.5 - soccerVehicle.d1) * tile_spacing;
				turnTo(30);
			}
			shoot();
		}
		if(BallsChecked<4){
			pickUp();
			travelTo(5 * tile_spacing, (soccerVehicle.d2-0.5)*tile_spacing);
			turnTo(0);
			z = 0;
			while (BallsPicked != 0) {
				z = (int) (Math.random() * 3);
				if (z == 0) {
					turnTo(0);
				} else if (z == 1) {
					deltaX = -soccerVehicle.w1 / 2 + 10;
					deltaY = 11 * tile_spacing - (11.5 - soccerVehicle.d1) * tile_spacing;
					turnTo(newTheta(deltaX, deltaY));
				} else if (z == 2) {
					deltaX = soccerVehicle.w1 / 2 - 10;
					deltaY = 11 * tile_spacing - (11.5 - soccerVehicle.d1) * tile_spacing;
					turnTo(30);
				}
				shoot();
			}
		}

	}

	public void travelTo(double x, double y) {
		navigating = true;
		double curX = odo.getX();
		double curY = odo.getY();
		deltaX = x - curX;
		deltaY = y - curY;
		newTheta = newTheta(deltaX, deltaY);
		turnTo(newTheta);

		soccerVehicle.getLeftMotor().setSpeed(motorStraight);
		soccerVehicle.getRightMotor().setSpeed(motorStraight);
		soccerVehicle.getLeftMotor().forward();
		soccerVehicle.getRightMotor().forward();

		while (navigating) {
			if ((Math.abs(x - odo.getX())) < 0.5 && (Math.abs(y - odo.getY()) < 0.5)) {
				soccerVehicle.getLeftMotor().stop(true);
				soccerVehicle.getRightMotor().stop(false);
				navigating = false;
				break;
			}
			deltaX = x - odo.getX();
			deltaY = y - odo.getY();
			if ((Math.abs(odo.getTheta() - newTheta(deltaX, deltaY)) > 2)
					&& (Math.abs(360 - odo.getTheta() - newTheta(deltaX, deltaY)) > 2)) {
				turnTo(newTheta(deltaX, deltaY));
				soccerVehicle.getLeftMotor().setSpeed(motorStraight);
				soccerVehicle.getRightMotor().setSpeed(motorStraight);
				soccerVehicle.getLeftMotor().forward();
				soccerVehicle.getRightMotor().forward();
			}

		}
	}

	public double newTheta(double deltaX, double deltaY) {
		double Theta = 0.0;
		if (deltaX > 0 && deltaY > 0) {
			Theta = 90 - (Math.atan2(deltaY, deltaX) * (180 / Math.PI));
		} else if ((deltaX < 0) && (deltaY > 0)) {
			Theta = 270 + (180 - ((Math.atan2(deltaY, deltaX) * (180 / Math.PI))));
		} else if ((deltaX < 0) && (deltaY < 0)) {
			Theta = 90 - (Math.atan2(deltaY, deltaX) * (180 / Math.PI));
		} else if ((deltaX > 0) && (deltaY < 0)) {
			Theta = 90 - (Math.atan2(deltaY, deltaX) * (180 / Math.PI));
		} else if (deltaX >= -5 && deltaX <= 5 && deltaY > 0) {
			Theta = 0;
		} else if (deltaX >= -5 && deltaX <= 5 && deltaY < 0) {
			Theta = 180;
		} else if (deltaY >= -5 && deltaY <= 5 && deltaX > 0) {
			Theta = 90;
		} else if (deltaY >= -5 && deltaY <= 5 && deltaX < 0) {
			Theta = 270;
		}
		return Theta;
	}

	public void turnTo(double theta) {
		// Store the current angle in a variable
		double curTheta = odo.getTheta();
		// Using the angle difference calculated in travelTo()
		// check if it the minimal angle and if not, change it accordingly
		if ((theta - curTheta) >= -180 && (theta - curTheta) <= 180) {
			turnAngle = theta - curTheta;
		} else if ((theta - curTheta) < -180) {
			turnAngle = (theta - curTheta) + 360;
		} else if ((theta - curTheta) > 180) {
			turnAngle = (theta - curTheta) - 360;
		}
		// Set the motors to rotate speed and rotate the minimal angle
		soccerVehicle.getLeftMotor().setSpeed(ROTATE_SPEED);
		soccerVehicle.getRightMotor().setSpeed(ROTATE_SPEED);
		soccerVehicle.getLeftMotor().rotate(convertAngle(soccerVehicle.WHEEL_RADIUS, soccerVehicle.TRACK, turnAngle),
				true);
		soccerVehicle.getRightMotor().rotate(-convertAngle(soccerVehicle.WHEEL_RADIUS, soccerVehicle.TRACK, turnAngle),
				false);

		navigating = true;
	}

	public boolean isNavigating() {
		boolean answer = false;
		if (navigating) {
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
		} else if (soccerVehicle.BC == 1) {
			Color1 = 2.0;
			Color2 = 2.0;
		} else if (soccerVehicle.BC == 2) {
			Color1 = 7.0;
			Color2 = 2.0;
		}

		PlatX = (soccerVehicle.llx) * tile_spacing;
		PlatY = (soccerVehicle.lly) * tile_spacing;

		if (BallsChecked < 2) {
			if (BallsChecked == 0) {
				travelTo(PlatX + 3.62, PlatY - 20);
				Stop();

			}
			turnTo(0.0);
		}
		if (BallsChecked > 1) {
			if (BallsChecked == 2) {
				travelTo(odo.getX() - 30, odo.getY());
				travelTo(PlatX + 3.62, PlatY + tile_spacing + 20.0);
			}
			turnTo(180);
		}
		soccerVehicle.sweepSensor.fetchSample(soccerVehicle.sweepData, 0);
		float color = soccerVehicle.sweepData[0];

		while (color != 7.0 && color != 2.0) {
			soccerVehicle.sweepSensor.fetchSample(soccerVehicle.sweepData, 0);
			color = soccerVehicle.sweepData[0];
			forward();
		}
		Stop();
		BallsChecked++;

		soccerVehicle.shootMotor.setAcceleration(1000000);
		soccerVehicle.shootMotor.setSpeed(1000000);
		soccerVehicle.shootMotor.forward();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
		}
		soccerVehicle.getLeftMotor().setSpeed(200);
		soccerVehicle.getRightMotor().setSpeed(200);
		soccerVehicle.getLeftMotor().forward();
		soccerVehicle.getRightMotor().forward();
		try {
			Thread.sleep(800);
		} catch (InterruptedException e) {

		}
		Stop();
		soccerVehicle.shootMotor.stop();

		if (color == Color1 || color == Color2) {
			BallsPicked++;
			if (BallsChecked != 4 && BallsPicked < 2) {
				pickUp();
			}
		} else {
			soccerVehicle.getLeftMotor().setSpeed(100);
			soccerVehicle.getRightMotor().setSpeed(100);
			soccerVehicle.getLeftMotor().backward();
			soccerVehicle.getRightMotor().backward();
			try {
				Thread.sleep(8000);
			} catch (InterruptedException e) {
			}
			Stop();
			turnTo(45.0);
			shoot();
			turnTo(0.0);
			if (BallsChecked < 2) {
				soccerVehicle.getLeftMotor().setSpeed(100);
				soccerVehicle.getRightMotor().setSpeed(100);
				soccerVehicle.getLeftMotor().forward();
				soccerVehicle.getRightMotor().forward();
				try {
					Thread.sleep(8000);
				} catch (InterruptedException e) {
				}
				if (BallsChecked < 4 && BallsPicked < 2) {
					pickUp();
				}
			}
		}
	}

	public void shoot() {
		soccerVehicle.shootMotor.setAcceleration(1000000);
		soccerVehicle.shootMotor.setSpeed(1000000);
		soccerVehicle.shootMotor.backward();

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
		}

		soccerVehicle.shootMotor.stop();
		BallsPicked--;
	}
}