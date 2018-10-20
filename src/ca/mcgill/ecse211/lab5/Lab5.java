package ca.mcgill.ecse211.lab5;


import ca.mcgill.ecse211.lab5.Display;
import ca.mcgill.ecse211.lab5.Odometer;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class Lab5 {
	
	protected static final EV3LargeRegulatedMotor leftMotor =
			new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));

	protected static final EV3LargeRegulatedMotor rightMotor =
			new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));

	private static final Port usPort = LocalEV3.get().getPort("S3");
	private static final Port csPort1 = LocalEV3.get().getPort("S4");
	private static final Port csPort2 = LocalEV3.get().getPort("S2");
	
	private static final TextLCD lcd = LocalEV3.get().getTextLCD();
	public static final double WHEEL_RADIUS = 2.15;
	public static final double TRACK = 8.96;
	
	public static final int targetRing = 0;
	public static final int startingCorner = 2;
	
	// Initialize the search region
	// All points are stored as (x,y);
	public static final int[][] lowerLeftCorner = {{ 0, 0 }};
	public static final int[][] upperRightCorner = {{ 0, 0 }};
	
	
	public static void main(String[] args) throws OdometerExceptions {
		
		int buttonChoice;
		
		final TextLCD t = LocalEV3.get().getTextLCD();

		Odometer odometer = Odometer.getOdometer(leftMotor, rightMotor, TRACK, WHEEL_RADIUS);
	    Display odometryDisplay = new Display(lcd); // No need to change
	    

		// Set up ultrasonic sensor
		@SuppressWarnings("resource") // Because we don't bother to close this resource
		SensorModes usSensor = new EV3UltrasonicSensor(usPort); // usSensor is the instance
		SampleProvider usDistance = usSensor.getMode("Distance"); // usDistance provides samples from this instance
		float[] usData = new float[usDistance.sampleSize()]; // usData is the buffer in which data are returned

		
		// Set up color sensor
		EV3ColorSensor csSensor = new EV3ColorSensor(csPort2);
		csSensor.setFloodlight(lejos.robotics.Color.RED);
		SampleProvider csColor = csSensor.getRedMode();
		float[] colorData = new float[csColor.sampleSize()];
		Navigation navigator = new Navigation(odometer, leftMotor, rightMotor, csColor, colorData);


		do {
			// clear the display
			t.clear();

			// ask the user whether the motors should drive in a square or float
			t.drawString("<  Left|Right  >", 0, 0);
			t.drawString("       |        ", 0, 1);
			t.drawString("   Fall|Rise  ", 0, 2);

			buttonChoice = Button.waitForAnyPress();
		} while (buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT);

		// Left: falling edge; Right: rising edge
		USLocaliser usLocaliser = new USLocaliser(odometer, leftMotor, rightMotor, usDistance, usData, buttonChoice, navigator);
	
	    Thread odoThread = new Thread(odometer);
	    odoThread.start();
	    Thread odoDisplayThread = new Thread(odometryDisplay);
	    odoDisplayThread.start();
        Thread localiserThread = new Thread(usLocaliser);
        localiserThread.start();

        
        try {
			localiserThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        
		LightLocaliser lightLocaliser = new LightLocaliser(navigator, odometer, leftMotor, rightMotor, csColor, colorData);	
		
		// Start light localization
		lightLocaliser.doLocalization();


		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}


}
