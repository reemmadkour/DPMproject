package ca.mcgill.ecse211.lab5;
import lejos.hardware.sensor.*;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.robotics.SampleProvider;

import lejos.hardware.Button;

/**creates a class to start the navigation
 * @author reem madkour and usaid barlas
 
 */

public class Navigation implements Runnable {

	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;
	private EV3LargeRegulatedMotor usMotor;
	
	private final double TRACK;
	private final double WHEEL_RAD;
	public static final double TILE_SIZE=30.48;;
	public static final int FORWARD_SPEED = 120;
	private static final int ROTATE_SPEED = 120;
	private final int US_ROTATION = 270; //constant for the US sensor rotation when bang bang starts/stops
	
	
	double dx, dy, dt;

	public int i = 0;
	public Odometer odometer;
	//private OdometerData odoData;

	
	/** creates an instance of the navigation class
	 * @param leftmotor, rightmotor: motors passed from the lab2 class
	 * @param TRACK is the distance between the wheels
	 * @param WHEEL_RAD is the radius of the wheel
	 *
	 */
		public Navigation(Odometer odometer,EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, EV3LargeRegulatedMotor sensorMotor,
				final double TRACK, final double WHEEL_RAD) { // constructor
			this.odometer = odometer;
			this.leftMotor = leftMotor;
			this.rightMotor = rightMotor;
			//odoData = OdometerData.getOdometerData();
			//odometer.setXYT(0 , 0 , 0);
			this.TRACK = TRACK;
			this.WHEEL_RAD = WHEEL_RAD;
			this.usMotor = sensorMotor;
			usMotor.resetTachoCount();
		}

		
		
		 
			public void run() {
				for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] {leftMotor, rightMotor}) {
					motor.stop();
					motor.setAcceleration(200);  // reduced the acceleration to make it smooth
				}
				// wait 5 seconds
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not expected that
					// the odometer will be interrupted by another thread
				}
				// implemented this for loop so that navigation will work for any number of points
				
				int waypoints[][] = new int[][] { {Lab5.lowerLeftCorner[0], Lab5.lowerLeftCorner[1]}, {Lab5.lowerLeftCorner[0], Lab5.upperRightCorner[1]}, {Lab5.upperRightCorner[0], Lab5.upperRightCorner[1]}, { Lab5.upperRightCorner[0], Lab5.lowerLeftCorner[1] }, { Lab5.lowerLeftCorner[0], Lab5.lowerLeftCorner[1] },{Lab5.lowerLeftCorner[0]+1, Lab5.lowerLeftCorner[1]}, {Lab5.lowerLeftCorner[0]+1, Lab5.upperRightCorner[1]},{Lab5.upperRightCorner[0], Lab5.upperRightCorner[1]}};
				
				while(i<waypoints.length) {
					if (i>0) {Lab5.inSquare=true;}
					travelTo(waypoints[i][0], waypoints[i][1],true);
					
					i++;
				}
			}
		
		

		private static double odoAngle = 0;
		/**
		 * Let the robot travel to the specified destination
		 * @param x The x value of the destination
		 * @param y The y value of the destination
		 * @param withTurn If 
		 */
		void travelTo(double x, double y,boolean withTurn) {
			double calcTheta = 0, len = 0, deltaX = 0, deltaY = 0;

	
			odoAngle = odometer.getXYT()[2];

			deltaX = x*TILE_SIZE- odometer.getXYT()[0];;
			deltaY = y*TILE_SIZE - odometer.getXYT()[1];
		
			len = Math.hypot(Math.abs(deltaX), Math.abs(deltaY));

			//get angle up to 180
			calcTheta = Math.toDegrees(Math.atan2(deltaX, deltaY));

			//if result is negative subtract it from 360 to get the positive
			if (calcTheta < 0)
				calcTheta = 360 - Math.abs(calcTheta);

			// turn to the found angle
			if(withTurn) {
//				System.out.println("\n\n\n" + calcTheta);
				turnTo(calcTheta);
			}
		

			// go
			leftMotor.setSpeed(FORWARD_SPEED);
			rightMotor.setSpeed(FORWARD_SPEED);
			leftMotor.rotate(convertDistance(WHEEL_RAD, len), true);
			rightMotor.rotate(convertDistance(WHEEL_RAD, len), true);
			//odometer.setX(x*TILE_SIZE);
			//odometer.setY(y*TILE_SIZE);
		while(isNavigating()) {
		if(Lab5.inSquare) {
	//
				Lab5.oLocal.processUSData();
				//rightMotor.stop(true);
				//leftMotor.stop(false);
				
		}
			}
			
			
		}
		
		/** changes heading of the robot to the given angle
		 * @param theta : angle needed to turn to
		 */
		void turnTo(double theta) {
			boolean turnLeft = false; //to do the minimal turn
			double deltaAngle = 0;
			// get the delta angle
			odoAngle=odometer.getXYT()[2];
			deltaAngle = theta - odoAngle;

			// if the delta angle is negative find the equivalent positive
			if (deltaAngle < 0) {
				deltaAngle = 360 - Math.abs(deltaAngle);
			}

			// Check if angle is the minimal or not
			if (deltaAngle > 180) {
				turnLeft = true;
				deltaAngle = 360 - Math.abs(deltaAngle);
			} else {
				turnLeft = false;
			}

			// set to rotation speed
			leftMotor.setSpeed(ROTATE_SPEED);
			rightMotor.setSpeed(ROTATE_SPEED);

			//turn robot to direction we chose
			if (turnLeft) {
				leftMotor.rotate(-convertAngle(WHEEL_RAD, TRACK, deltaAngle), true);
				rightMotor.rotate(convertAngle(WHEEL_RAD, TRACK, deltaAngle), false);
			} else {
				leftMotor.rotate(convertAngle(WHEEL_RAD, TRACK, deltaAngle), true);
				rightMotor.rotate(-convertAngle(WHEEL_RAD, TRACK, deltaAngle), false);
			}

		}

		boolean isNavigating() {
			if((leftMotor.isMoving() || rightMotor.isMoving()))
				return true;
			else 
				return false;

		}
		
		/**gives the angle you need  the wheels to roate by to cover a certain arc length (distance)
		 * @param radius: radius of wheel
		 * @param distance: distance you want the robot to travel 
		 *@return: angle for rotate
		 */
		public static int convertDistance(double radius, double distance) {
		    return (int) ((180.0 * distance) / (Math.PI * radius));
		  }

		  public static int convertAngle(double radius, double width, double angle) {
		    return convertDistance(radius, Math.PI * width * angle / 360.0);
		  }



			/** turns the robot clockwise to a certaina angle
			 * @param degree is and in degrees you want to turn to
			 *
			 */

  public void turnCW(long degree) {
    leftMotor.rotate(
        convertAngle(Lab5.WHEEL_RADIUS, Lab5.TRACK, degree), true);
    rightMotor.rotate(
        -convertAngle(Lab5.WHEEL_RADIUS, Lab5.TRACK, degree), true);
  }
  
  
  public void turnCW2(long degree) {
	    leftMotor.rotate(
	        convertAngle(Lab5.WHEEL_RADIUS, Lab5.TRACK, degree), true);
	    rightMotor.rotate(
	        -convertAngle(Lab5.WHEEL_RADIUS, Lab5.TRACK, degree), false);
	  }
	/** turns the robot counterclockwise to a certaina angle
	 * @param degree is and in degrees you want to turn to
	 *
	 */
  public void turnCCW(long degree) {
    leftMotor.rotate(
        -convertAngle(Lab5.WHEEL_RADIUS, Lab5.TRACK, degree), true);
    rightMotor.rotate(
        convertAngle(Lab5.WHEEL_RADIUS, Lab5.TRACK, degree), true);
  }
	/** moves the robot forward a certain distance with the current heading
	 * @distance distance you want covered
	 *
	 */
  public void advance(long distance) {
    leftMotor.rotate(convertDistance(Lab5.WHEEL_RADIUS, distance), true);
    rightMotor.rotate(convertDistance(Lab5.WHEEL_RADIUS, distance), false);
  }
  
  public void rotateSensorMotor() {
	  usMotor.setSpeed(ROTATE_SPEED);
	  usMotor.rotateTo(-90);
  }
}
