package ca.mcgill.ecse211.lab5;

import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;

public class ObjectLocalizer {
	Navigation navigation;
	Odometer odometer;
	private double x;
	private double y;
	private double xx; 
	private double yy;
	SampleProvider usSensor;
	boolean obs; 
	private float Distance;
	private float[] distance;
	private float initialDistance;

	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;
static boolean bigFound=false;
	public ObjectLocalizer(SampleProvider usSensor, float[] distance, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, Odometer odometer, Navigation navigation) {
		this.distance = distance;
		this.usSensor = usSensor;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.odometer = odometer;
		this.navigation = navigation;
	}



	public void processUSData() {/** processing the data measured by the US sensor  **/
		ColorDetector detector = new ColorDetector(Lab5.colorSensor);


		int counter =0;
        boolean found=false;
		// 
		//leftMotor.setSpeed(200);
		//rightMotor.setSpeed(200);
		//rightMotor.forward();
		//leftMotor.forward();

		if( bigFound==(false)) {
			xx = odometer.getXYT()[0];
			yy = odometer.getXYT()[1];

			if(yy > 2) {
				usSensor.fetchSample(distance, 0);
				Distance= distance[0]*100;
				//System.out.println(Distance);

				if(Distance < 60)  /** if the value measure is within the range compared to the intial value measured increase the counter value**/
				{
					initialDistance = Distance;
					while(counter <= 1000&&bigFound==false) {
						if((Distance >= initialDistance-10 && Distance <= initialDistance + 10) ) {
							counter ++; 
							usSensor.fetchSample(distance, 0);
							Distance= distance[0]*100;
							//System.out.println(+Distance);
							if(counter >= 40&&found==false) {
								//System.out.println("inside loop");
								x=odometer.getXYT()[0];
								y=odometer.getXYT()[1];
								
								Lab5.inSquare=false;
								navigation.turnCW2(70);
								navigation.travelTo((x+(Distance/1.5))/30.48, y/30.48, false);
								leftMotor.stop();
							
							   rightMotor.stop();
							    if(detector.detect(Lab5.color)==true) {
							    System.out.println("true");
							    Sound.beep();
							    found=true;
							    bigFound=true;
							    navigation.i=6;
							   // break;
							    }
							    else {
							    	System.out.println("FALSE");
							    	Sound.beep();
							    	Sound.beep();
							    	
							    	leftMotor.backward();
							    	rightMotor.backward();
							    	int k=0;
							    	while (k<7) {
							    		k++;
							    	}
							    leftMotor.stop(true);
							    rightMotor.stop(false);
							    found=true;
							    navigation.i--;} 
								//...do comparison code here, if not the thingy then :
								//Lab5.inSquare=true;
								
							    break;
								
						}

						}
						else {
							counter = 0;
							break;
						}

					}

				}

			}
		}
	}




}