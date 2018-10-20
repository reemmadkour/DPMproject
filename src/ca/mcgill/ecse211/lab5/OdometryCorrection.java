/*
 * OdometryCorrection.java
 */
package ca.mcgill.ecse211.lab5;
import lejos.hardware.Sound;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;

public class OdometryCorrection implements Runnable {
  private static final long CORRECTION_PERIOD = 15;
  private static final double SENSOR_DIST = 2.5; // The vertical distance between the sensor and the wheel center
//  private static final double OFFSET_CONSTANT = 3;
  private Odometer odometer;
  private EV3ColorSensor cSensor;
  private float[] colorSample;
  private float color;

  private boolean first_time_Y; // A boolean representing if the robot has crossed the first line in positive Y direction

  private int counter_X; // Record the number of black lines in Y direction
  private int counter_Y; // Record the number of black lines in X direction


  /**
   * This is the default class constructor. An existing instance of the odometer is used. This is to
   * ensure thread safety.
   * 
   * @throws OdometerExceptions
   */
  public OdometryCorrection(Odometer odometer, EV3ColorSensor cSensor) throws OdometerExceptions {

    this.odometer = Odometer.getOdometer();
    
	this.cSensor = cSensor;
	this.colorSample = null;
	this.first_time_Y = true;
	this.counter_X = 0;
	this.counter_Y = 0;
  }

  /**
   * Here is where the odometer correction code should be run.
   * 
   * @throws OdometerExceptions
   */
  // run method (required for Thread)
  public void run() {
    long correctionStart, correctionEnd;
	boolean detectBlack = false;

	// Set up color sensor and sample buffer
	cSensor.setFloodlight(lejos.robotics.Color.RED);
	
	SampleProvider csColor = cSensor.getRedMode();
	colorSample = new float[csColor.sampleSize()];

    while (true) {
      correctionStart = System.currentTimeMillis();

      // TODO Trigger correction (When do I have information to correct?)
      // TODO Calculate new (accurate) robot position


		// Get color sample data from the sensor
		csColor.fetchSample(colorSample, 0);
		color = colorSample[0];

//		System.out.println(color);
		
		// Check if the sensor has detected black
		// Sample value should be between 0.01 and 0.09 if the sample color is black
		if (color > 0.01 && color < 0.35) {
			detectBlack = true;
			Sound.beep();  // Beep to signal black detection 
		}
		else {
			detectBlack = false;
		}

		// If the robot has reached a black line, correct its position
		if (detectBlack == true) {
			correctPosition();	  		
		}
      
		

      // this ensure the odometry correction occurs only once every period
      correctionEnd = System.currentTimeMillis();
      if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
        try {
          Thread.sleep(CORRECTION_PERIOD - (correctionEnd - correctionStart));
        } catch (InterruptedException e) {
          // there is nothing to be done here because it is not
			// expected that the odometry correction will be
			// interrupted by another thread
        }
      }
    }
  }
	private void correctPosition() {
		double x = odometer.getXYT()[0];
		double y = odometer.getXYT()[1];
		double theta = odometer.getXYT()[2];
		double tolerance = 2.0;

		// Driving in positive Y direction
		// current Y position + SENSOR_DIST = corrected Y position
		if ( Math.abs(theta-360)<=tolerance || Math.abs(theta-0)<= tolerance ) {
			// Set the first black line that the robot has crossed in increasing Y direction to be the line y=0
			// i.e. y(robot) + SENSOR_DIST = 0
			if (first_time_Y==true){  
				first_time_Y = false;
				y = -SENSOR_DIST;
			}
			// Count the black lines that the robot has reached in positive Y direction
			else {
				counter_Y++;
				y = counter_Y*30.48-SENSOR_DIST;
				
			}

			odometer.setY(y);
		}

		// Driving in negative Y direction
		// current Y position - SENSOR_DIST = corrected Y position
		else if ( Math.abs(theta-180)<=tolerance ) {

			// The number of black lines in this direction equals that in the positive Y direction
			// However, y value is decreasing here
			y = counter_Y*30.48+SENSOR_DIST;
			counter_Y--;
			
			if(counter_Y == -1) {
				y = y+7;
			}
			odometer.setY(y);
			
		}

		// Driving in positive X direction
		// current X position + SENSOR_DIST = corrected X position
		else if  (Math.abs(theta-90)<=tolerance) {

			// Set the first black line that the robot has crossed in increasing X direction to be the line x=0
			
			// Count the number of black lines in the positive X direction
			x = counter_X*30.48-SENSOR_DIST;
			counter_X++;

			odometer.setX(x);

		}

		// Driving in negative X direction
		// current X position - SENSOR_DIST = corrected X position
		else if ( Math.abs(theta-270)<=tolerance) { 

			// The number of black lines in this direction equals that in the positive X direction
			// However, x value is decreasing here
			x = counter_X*30.48+SENSOR_DIST;
			counter_X--;
			
			if(counter_X == -3) {
				counter_X = -2;
			}
			
			odometer.setX(x);
			
		}
	}
  
}
