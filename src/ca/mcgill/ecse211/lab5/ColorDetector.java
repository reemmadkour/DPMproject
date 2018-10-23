package ca.mcgill.ecse211.lab5;

import java.util.Arrays;

import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;


public class ColorDetector{

	private EV3ColorSensor cSensor;
	private float[] colorSample;


	private double mBlueR = 0.1732410055;
	private double mBlueG = 0.6778531281;
	private double mBlueB = 0.7144947101;
	private double mGreenR = 0.4777487339;
	private double mGreenG = 0.8592604804;
	private double mGreenB = 0.1828320925;
	private double mYellowR = 0.8541708187;
	private double mYellowG = 0.5005476676;
	private double mYellowB = 0.140869603;
	private double mOrangeR = 0.9547663589;
	private double mOrangeG = 0.2766071505;
	private double mOrangeB = 0.1091314998;

	private float R;
	private float G; 
	private float B;

	private double nR; 
	private double nG;
	private double nB;

	private double Blue;
	private double Green;
	private double Yellow;
	private double Orange;
	//calculate normalized samples

	//calculate deviation for each one

	//sort the for deviations

	//the lowest deviation is the actual color
	
	public ColorDetector(EV3ColorSensor cSensor) {
		this.cSensor = cSensor;
	}
	
	
	public boolean detect(String color) {

		cSensor.setFloodlight(true);
		SampleProvider csColors = cSensor.getRGBMode();
		colorSample = new float[csColors.sampleSize()];

		while(true) {
			csColors.fetchSample(colorSample, 0);
			R = colorSample[0];
			G = colorSample[1];
			B = colorSample[2];
			
			//normalize
			nR = R/(Math.sqrt(R*R + G*G + B*B));
			nG = G/(Math.sqrt(R*R + G*G + B*B));
			nB = B/(Math.sqrt(R*R + G*G + B*B));

			//calculate deviation for each
			Blue = Math.sqrt(Math.pow(nR - mBlueR, 2) + Math.pow(nG - mBlueG, 2) + Math.pow(nB - mBlueB, 2));
			Green = Math.sqrt(Math.pow(nR - mGreenR, 2) + Math.pow(nG - mGreenG, 2) + Math.pow(nB - mGreenB, 2));
			Yellow = Math.sqrt(Math.pow(nR - mYellowR, 2) + Math.pow(nG - mYellowG, 2) + Math.pow(nB - mYellowB, 2));
			Orange = Math.sqrt(Math.pow(nR - mOrangeR, 2) + Math.pow(nG - mOrangeG, 2) + Math.pow(nB - mOrangeB, 2));

			double[] list = {Blue, Green, Yellow, Orange};

			//sorted array
			Arrays.sort(list);

			//print list[0] which is going to the detected color
			
			if(list[0] == Blue && "Blue".equals(color)) {
				return true;
			}
			
			if(list[0] == Green && "Green".equals(color)) {
				return true;
			}
			
			if(list[0] == Yellow && "Yellow".equals(color)) {
				return true;
			}
			
			if(list[0] == Orange && "Orange".equals(color)) {
				return true;
			}
			return false;
		}
		// TODO Auto-generated method stub

	}

}
