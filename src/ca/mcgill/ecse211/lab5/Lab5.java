// Lab4.java

package ca.mcgill.ecse211.lab5;


import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class Lab5 {


  private static final Port usPort = LocalEV3.get().getPort("S3");
  private static final EV3ColorSensor colorSensor = new EV3ColorSensor(SensorPort.S2);
  private static final EV3LargeRegulatedMotor leftMotor =
      new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
  private static final EV3LargeRegulatedMotor rightMotor =
      new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));


  public static final int targetRing = 0;
	public static final int startingCorner = 2;
  public static final double WHEEL_RADIUS = 2.15;
  public static final double TRACK = 10;
  public static final double TAU = 360;

  public static void main(String[] args) throws OdometerExceptions {
    int buttonChoice;

    @SuppressWarnings("resource")
    SensorModes usSensor = new EV3UltrasonicSensor(usPort); // usSensor is the instance
    SampleProvider usDistance =
        usSensor.getMode("Distance"); // usDistance provides samples from this instance
    float[] usData =
        new float[usDistance.sampleSize()]; // usData is the buffer in which data are returned

  

    // buffer for sensor values
    float[] colorData = new float[colorSensor.getRedMode().sampleSize()];

    TextLCD t = LocalEV3.get().getTextLCD();

   
    do {
      // clear the display
      t.clear();

      // ask the user whether rising or falling edge
      t.drawString("< Left | Right >", 0, 0);
      t.drawString("       |        ", 0, 1);
      t.drawString("Rising | Falling", 0, 2);
      t.drawString(" edge  |   edge ", 0, 3);
      t.drawString("       |        ", 0, 4);

      buttonChoice = Button.waitForAnyPress();
    } while (buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT);

  
    t.clear();
    Odometer odometer;
	
		odometer = Odometer.getOdometer(leftMotor, rightMotor, TRACK, WHEEL_RADIUS);

		OdometryDisplay odometryDisplay = new OdometryDisplay(t);
	
	Thread odoThread = new Thread(odometer);
    odoThread.start();
    Thread odoDisplayThread=new Thread(odometryDisplay);
    odoDisplayThread.start();
    Navigation navigation =
        new Navigation(
            odometer, leftMotor, rightMotor,  Lab5.TRACK,Lab5.WHEEL_RADIUS);


    UltrasonicLocalizer ultrasoniclocal;
    if (buttonChoice == Button.ID_LEFT) {
    	
      ultrasoniclocal =
          new UltrasonicLocalizer(
              odometer,
              usSensor,
              usData,
              UltrasonicLocalizer.LocalizationType.RISING_EDGE,
              navigation,
              leftMotor,
              rightMotor, t);
    } else {
    	//Thread odoThread = new Thread(odometer);
        //odoThread.start();
      ultrasoniclocal =
          new UltrasonicLocalizer(
              odometer,
              usSensor,
              usData,
              UltrasonicLocalizer.LocalizationType.FALLING_EDGE,
              navigation,
              leftMotor,
              rightMotor, t);
    }
    ultrasoniclocal.localize();

    
    while (Button.waitForAnyPress() != Button.ID_ENTER) ;

    // initializing the light localizer
    LightLocalizer ll =
        new LightLocalizer(odometer, colorSensor, colorData, navigation, leftMotor, rightMotor);
    ll.localize();

   
    while (Button.waitForAnyPress() != Button.ID_ENTER) ;
    System.exit(0);
  }
}
