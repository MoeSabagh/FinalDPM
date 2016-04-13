/*Final Project TEAM02
 * Gareth Peters | ID: 260678626
 * Catherine Bittar | ID: 260687735
 * Ryan (LuoQing) Wang | ID: 260524744
 * Ammar Sarfaraz Azir | ID: 260565232
 * Kevin-Rafael Sorto-Ventura | ID: 260692767
 * Mohamed Elsabagh | ID: 260603261
 * */
package finalProject;

public class Defense extends Thread {
	private Controller controller;
	private Avoidance avoidance;
	private Odometer odometer;
	private static final double tile_spacing = 30.48;
	public Defense(Controller controller, Avoidance avoidance, Odometer odometer){
		this.controller = controller;
		this.avoidance = avoidance;
		this.odometer = odometer;
	}
	@SuppressWarnings("deprecation")
	public void run(){
		if(soccerVehicle.DSC == 1 || soccerVehicle.DSC == 2){
			controller.turnTo(0);
			if(soccerVehicle.DSC == 1){
				controller.travelTo(0, 9*tile_spacing);
			}else if(soccerVehicle.DSC == 2){
				controller.travelTo(10*tile_spacing, 9*tile_spacing);
			}
		}else if(soccerVehicle.DSC == 3 || soccerVehicle.DSC == 4){
			controller.turnTo(180);
			if(soccerVehicle.DSC == 4){
				controller.travelTo(0, 9*tile_spacing);
			}else if(soccerVehicle.DSC == 3){
				controller.travelTo(10*tile_spacing, 9*tile_spacing);
			}
		}
		avoidance.stop();
		controller.travelTo(5*tile_spacing, 9*tile_spacing);
		while(true){
			if(odometer.getX()== (5*tile_spacing-soccerVehicle.w1/2)|| odometer.getX()==5*tile_spacing){
				while(odometer.getX() != (5*tile_spacing + soccerVehicle.w1/2)){
					soccerVehicle.getLeftMotor().setSpeed(200);
					soccerVehicle.getRightMotor().setSpeed(200);
					soccerVehicle.getLeftMotor().forward();
					soccerVehicle.getRightMotor().forward();
				}
			}else if(odometer.getX()== (5*tile_spacing+soccerVehicle.w1/2)|| odometer.getX()==5*tile_spacing){
				while(odometer.getX() != (5*tile_spacing-soccerVehicle.w1/2)){
					soccerVehicle.getLeftMotor().setSpeed(200);
					soccerVehicle.getRightMotor().setSpeed(200);
					soccerVehicle.getLeftMotor().forward();
					soccerVehicle.getRightMotor().forward();
				}
			}
			soccerVehicle.getLeftMotor().stop();
			soccerVehicle.getRightMotor().stop();
			if(odometer.getX()== (5*tile_spacing-soccerVehicle.w1/2)){
				while(odometer.getX() != (5*tile_spacing + soccerVehicle.w1/2)){
					soccerVehicle.getLeftMotor().setSpeed(200);
					soccerVehicle.getRightMotor().setSpeed(200);
					soccerVehicle.getLeftMotor().backward();
					soccerVehicle.getRightMotor().backward();
				}
			}else if(odometer.getX() == (5*tile_spacing + soccerVehicle.w1/2)){
				while(odometer.getX() != (5*tile_spacing - soccerVehicle.w1/2)){
					soccerVehicle.getLeftMotor().setSpeed(200);
					soccerVehicle.getRightMotor().setSpeed(200);
					soccerVehicle.getLeftMotor().backward();
					soccerVehicle.getRightMotor().backward();
				}
			}
			soccerVehicle.getLeftMotor().stop();
			soccerVehicle.getRightMotor().stop();	
		}
	}
}
