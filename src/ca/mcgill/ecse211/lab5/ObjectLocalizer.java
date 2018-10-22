package ca.mcgill.ecse211.lab5;

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

	public ObjectLocalizer(SampleProvider usSensor, float[] distance, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, Odometer odometer, Navigation navigation) {
		this.distance = distance;
		this.usSensor = usSensor;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.odometer = odometer;
		this.navigation = navigation;
	}



	public void processUSData() {/** processing the data measured by the US sensor  **/



		int counter =0;
        
		// 
		//leftMotor.setSpeed(200);
		//rightMotor.setSpeed(200);
		//rightMotor.forward();
		//leftMotor.forward();

		//while(true) {
			xx = odometer.getXYT()[0];
			yy = odometer.getXYT()[1];

			if(yy > 2) {
				usSensor.fetchSample(distance, 0);
				Distance= distance[0]*100;
				//System.out.println(Distance);

				if(Distance < 30)  /** if the value measure is within the range compared to the intial value measured increase the counter value**/
				{
					initialDistance = Distance;
					while(counter <= 300) {
						if((Distance >= initialDistance-10 && Distance <= initialDistance + 10) ) {
							counter ++; 
							usSensor.fetchSample(distance, 0);
							Distance= distance[0]*100;
							System.out.println(+Distance);
							if(counter >= 20) {
								System.out.println("inside loop");
								x=odometer.getXYT()[0];
								y=odometer.getXYT()[1];
								
								Lab5.inSquare=false;
								navigation.turnCW2(90);
								navigation.travelTo((x+Distance)/30.48, y/30.48, false);
								leftMotor.stop();
								rightMotor.stop();
								//...do comparison code here, if not the thingy then :
								Lab5.inSquare=true;
								break;
								
						}

						}
						else {
							counter = 0;
							break;
						}

				//	}

				}

			}
		}
	}




}