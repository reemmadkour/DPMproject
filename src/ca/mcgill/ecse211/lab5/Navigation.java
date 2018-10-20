package ca.mcgill.ecse211.lab5;

import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;

public class Navigation extends Thread{

	private Odometer odo;
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;
	private boolean isNavigating;
	private SampleProvider cs;
	private float[] colorData;
	private float color;

	private static final double TRACK = Lab5.TRACK;
	private static final double WHEELRAD = Lab5.WHEEL_RADIUS;
	private static final int FORWARD_SPEED = 250;
	private static final int ROTATE_SPEED = 70;

	private static final double GRID_SIZE = 30.48;
	private static final double DIST_ERROR = 1.0; //Error between position and destination
	private final double BLACK_LOWER_BOUND = 0.01;
	private final double BLACK_UPPER_BOUND = 0.35;
	
	private final double DISTANCE_OFFSET = 14.5;
	
    int counter_X; // Record the number of black lines in Y direction
    int counter_Y; // Record the number of black lines in X direction

	public Navigation(Odometer odo,EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, SampleProvider cs, float[] colorData) {
		this.odo = odo;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.isNavigating = true;
		this.cs = cs;
		this.colorData = colorData;
	}

	public void run() {
		try {
			Thread.sleep(2000);
		}
		catch(InterruptedException e) {
			// there is nothing to be done here because it is not expected that
			// the odometer will be interrupted by another thread
		}
	}

	/**
	 * Let the robot travel to the specified destination
	 * @param x The x value of the destination
	 * @param y The y value of the destination
	 */
	public void travelTo(double x, double y) {
		
		this.isNavigating = true;

		double currentX = odo.getXYT()[0];
		double currentY = odo.getXYT()[1];
		
		// The unit length in the actual world is 30.48cm, while the unit length in our program is 1cm
		// For example, when we need to head to the point (1,0) in the actual world, we actually need to reach (30.48*1, 30.48*0)
		// We need to convert the reading from the odometer using the actual world's measurement convention 
		currentX = (int)Math.round(currentX/GRID_SIZE);
		currentY = (int)Math.round(currentY/GRID_SIZE);
//
//		// Calculate the distance between the robot's current position and the destination
		double distance = Math.sqrt( Math.pow( ( x - currentX ), 2) + Math.pow( ( y - currentY ),2) );
		distance = distance * GRID_SIZE;

		double newTheta = 0.0; // This is the heading that the robot should have in order to reach the destination
//		
//		// This is the angle between the x-/y-axis and the line that both the current position and the destination are on
		double alpha = Math.toDegrees( Math.atan( Math.abs(x - currentX ) / Math.abs( y - currentY ) ) ); 

//		
//		// Calculate the new heading using the robot's (x,y,theta) convention
		if (x==currentX) {
			if (y-currentY<0) {
				newTheta = 180;   ////////
			}
			else if (y-currentY>0) {
				newTheta = 0;      //////
			}
		}
		else if (y==currentY) {
			if (x-currentX<0) {
				newTheta = 270;     /////////
			}
			else if (x-currentX>0) {
				newTheta = 90;         /////////
			}	
		}
		else if ( (x-currentX)>0 && (y-currentY)>0 ) {
			newTheta = 360 - alpha;
		}
		else if ( (x-currentX)>0 && (y-currentY)<0 ) {
			newTheta = 180 + alpha;      ////////////
		}
		else if ( (x-currentX)<0 && (y-currentY)>0 ) {
			newTheta = alpha;       //checked
		}
		else if ( (x-currentX)<0 && (y-currentY)<0 ) {
			newTheta = 180 - alpha;     //checking
		}
//
//		
//		// Let the robot rotate to have the correct heading to reach the destination
		turnTo(newTheta);

		// Let the robot move to the destination by specifying the distance to it 
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);
		leftMotor.rotate(convertDistance(WHEELRAD, distance), true);
		rightMotor.rotate(convertDistance(WHEELRAD, distance), false);

	}
	
	
	
	/**
	 * Let the robot travel to the specified destination
	 * @param x The x value of the destination
	 * @param y The y value of the destination
	 */
	public void travelToLLC(double x, double y) {
		
		this.isNavigating = true;

		double currentX = odo.getXYT()[0];
		double currentY = odo.getXYT()[1];
		double theta = odo.getXYT()[2];
		double tolerance = 2.0;
		
		while(Math.abs(y - currentY) <=DIST_ERROR) {
			leftMotor.setSpeed(FORWARD_SPEED);
			rightMotor.setSpeed(FORWARD_SPEED);
			leftMotor.forward();
			rightMotor.forward();
			
			if ( getColor()>=BLACK_LOWER_BOUND && getColor()<=BLACK_UPPER_BOUND ) {
				Sound.beep();  // Beep to signal black detection 
				correctPosition();	
			}
		}
		
		if(currentX > x) {
			turnTo(270);
		}
		else if(currentX < x) {
			turnTo(90);
		}
		
		while(Math.abs(x - currentX) <=DIST_ERROR) {
			leftMotor.setSpeed(FORWARD_SPEED);
			rightMotor.setSpeed(FORWARD_SPEED);
			leftMotor.forward();
			rightMotor.forward();
			
			if ( getColor()>=BLACK_LOWER_BOUND && getColor()<=BLACK_UPPER_BOUND ) {
				Sound.beep();  // Beep to signal black detection 
				correctPosition();	
			}
		}

		//after the robot arrived, stop motors
		leftMotor.setSpeed(0); 
		rightMotor.setSpeed(0);
		
	}
	

	/**
	 * Let the robot get to the specified heading by turning a minimal rotation angle
	 * @param newTheta The heading that the robot needs to rotate to
	 */
	public void turnTo(double newTheta) {

		// Calculate how much the robot needs to rotate from its current heading to the specified heading
		double theta = newTheta - odo.getXYT()[2];
		
		// Calculate the "minimal" angle to rotate
		// Positive angle: angle to rotate clockwise; Negative angle: angle to rotate counter-clockwise
		// If the absolute value of the angle to rotate is > 180, then the angle to rotate in the opposite direction must be smaller
		// We can +/- 360 to find that smaller angle
		
		if ( theta > 180) {
			theta = theta - 360;
		}
		else if ( theta < -180 ) {
			theta = theta + 360;
		}
		

		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);

		leftMotor.rotate(convertAngle(WHEELRAD, TRACK, theta), true);
		rightMotor.rotate(-convertAngle(WHEELRAD, TRACK, theta), false);

	}

	/**
	 * Move the robot forward or backward for a specified distance
	 * @param distance Distance to travel 
	 * @param forward  Represent if the robot moves forward or backward
	 */
	public void move(double distance, boolean forward) {
		
		if (forward==true) {
			leftMotor.rotate(convertDistance(WHEELRAD, distance),true);
			rightMotor.rotate(convertDistance(WHEELRAD, distance), false);
		}
		
		else {
			leftMotor.rotate(-convertDistance(WHEELRAD, distance),true);
			rightMotor.rotate(-convertDistance(WHEELRAD, distance), false);
		}
		
	}

	public boolean isNavigating() {
		return isNavigating;
	}

	private int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	private int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
	
	/**
	 * Retrieve data from the color sensor 
	 * @return Color sample data from the color sensor
	 */
	private float getColor() {
		cs.fetchSample(colorData, 0);
		this.color = colorData[0];
		return this.color;
	}
	
	private void correctPosition() {
		double x;
		double y;
		double theta = odo.getXYT()[2];
		double tolerance = 2.0;

		// Driving in positive Y direction
		// current Y position + SENSOR_DIST = corrected Y position
		if ( Math.abs(theta-360)<=tolerance || Math.abs(theta-0)<= tolerance ) {
			y = counter_Y*30.48+DISTANCE_OFFSET;
			counter_Y++;
			odo.setY(y);
		}

		// Driving in negative Y direction
		// current Y position - SENSOR_DIST = corrected Y position
		else if ( Math.abs(theta-180)<=tolerance ) {

			// The number of black lines in this direction equals that in the positive Y direction
			// However, y value is decreasing here
			counter_Y--;
			y = counter_Y*30.48-DISTANCE_OFFSET;
			odo.setY(y);
			
		}

		// Driving in positive X direction
		// current X position + SENSOR_DIST = corrected X position
		else if  (Math.abs(theta-90)<=tolerance) {
			x = counter_X*30.48+DISTANCE_OFFSET;
			counter_X++;
			odo.setX(x);

		}

		// Driving in negative X direction
		// current X position - SENSOR_DIST = corrected X position
		else if ( Math.abs(theta-270)<=tolerance) { 
			counter_X--;
			x = counter_X*30.48-DISTANCE_OFFSET;
			odo.setX(x);
			
		}
	}
  
}
