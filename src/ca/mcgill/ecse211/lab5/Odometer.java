/**
 * This class is meant as a skeleton for the odometer class to be used.
 * 
 * @author Rodrigo Silva
 * @author Dirk Dubois
 * @author Derek Yu
 * @author Karim El-Baba
 * @author Michael Smith
 */

package ca.mcgill.ecse211.lab5;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Odometer extends OdometerData implements Runnable {

  private OdometerData odoData;
  private static Odometer odo = null; // Returned as singleton

  // Motors and related variables
  private int leftMotorTachoCount;
  private int rightMotorTachoCount;
  private EV3LargeRegulatedMotor leftMotor;
  private EV3LargeRegulatedMotor rightMotor;

  private final double TRACK;
  private final double WHEEL_RAD;

  private double[] position;

  private static final long ODOMETER_PERIOD = 25; // odometer update period in ms

  /**
   * This is the default constructor of this class. It initiates all motors and variables once.It
   * cannot be accessed externally.
   * 
   * @param leftMotor
   * @param rightMotor
   * @throws OdometerExceptions
   */
  private Odometer(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
      final double TRACK, final double WHEEL_RAD) throws OdometerExceptions {
    odoData = OdometerData.getOdometerData(); // Allows access to x,y,z
                                              // manipulation methods
    this.leftMotor = leftMotor;
    this.rightMotor = rightMotor;

    // Reset the values of x, y and z to 0
    odoData.setXYT(0, 0, 0);
    

    this.leftMotorTachoCount = 0;
    this.rightMotorTachoCount = 0;

    this.TRACK = TRACK;
    this.WHEEL_RAD = WHEEL_RAD;

  }

  /**
   * This method is meant to ensure only one instance of the odometer is used throughout the code.
   * 
   * @param leftMotor
   * @param rightMotor
   * @return new or existing Odometer Object
   * @throws OdometerExceptions
   */
  public synchronized static Odometer getOdometer(EV3LargeRegulatedMotor leftMotor,
      EV3LargeRegulatedMotor rightMotor, final double TRACK, final double WHEEL_RAD)
      throws OdometerExceptions {
    if (odo != null) { // Return existing object
      return odo;
    } else { // create object and return it
      odo = new Odometer(leftMotor, rightMotor, TRACK, WHEEL_RAD);
      return odo;
    }
  }

  /**
   * This class is meant to return the existing Odometer Object. It is meant to be used only if an
   * odometer object has been created
   * 
   * @return error if no previous odometer exists
   */
  public synchronized static Odometer getOdometer() throws OdometerExceptions {

    if (odo == null) {
      throw new OdometerExceptions("No previous Odometer exits.");

    }
    return odo;
  }

  /**
   * This method is where the logic for the odometer will run. Use the methods provided from the
   * OdometerData class to implement the odometer.
   */
  // run method (required for Thread)
  public void run() {
    long updateStart, updateEnd;
    double distLeft = 0;
    double distRight = 0;
    double displacement = 0;
    double dX = 0;
    double dY = 0;
    double dTheta = 0;
    double currentTheta = odo.getXYT()[2];
    int currentLeftTacho = 0;
    int currentRightTacho = 0;
    
    leftMotorTachoCount = leftMotor.getTachoCount();
    rightMotorTachoCount = rightMotor.getTachoCount();
    while (true) {
      updateStart = System.currentTimeMillis();

      // TODO Calculate new robot position based on tachometer counts
      currentLeftTacho = leftMotor.getTachoCount();
	  currentRightTacho = rightMotor.getTachoCount();
      
	  distRight = (currentRightTacho-rightMotorTachoCount)*Math.PI*WHEEL_RAD/180; 
	  distLeft =  (currentLeftTacho-leftMotorTachoCount) *Math.PI*WHEEL_RAD/180; 
	  displacement = (distLeft+distRight)/2; // Magnitude of displacement

	  // Update motorTachoCount values for future reference
	  leftMotorTachoCount = currentLeftTacho;
	  rightMotorTachoCount = currentRightTacho;

	  // Update heading in degrees
	  currentTheta = odo.getXYT()[2] + ( (distRight-distLeft)/TRACK )*180/Math.PI;  

	  dX = displacement*Math.sin(Math.toRadians(currentTheta)); //deltaX
	  dY = displacement*Math.cos(Math.toRadians(currentTheta)); //deltaY
	  dTheta = ( (distRight-distLeft)/TRACK )*180/Math.PI;   ///// 
      
	  
      // TODO Update odometer values with new calculated values
      odo.update(dX, dY, -1*dTheta);

      // this ensures that the odometer only runs once every period
      updateEnd = System.currentTimeMillis();
      if (updateEnd - updateStart < ODOMETER_PERIOD) {
        try {
          Thread.sleep(ODOMETER_PERIOD - (updateEnd - updateStart));
        } catch (InterruptedException e) {
          // there is nothing to be done
        }
      }
    }
  }

}
