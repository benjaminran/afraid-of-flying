package com.bran.afraidofflying;

public class DataFilter {
	private int bufferLength;
	private float[][] terms;
	private float[] sums;
	private int bufferCounter;
	
	public DataFilter(int bufferLength){
		this.bufferLength= bufferLength;
		terms = new float[3][bufferLength]; //x y z readings by sample number
		sums = new float[3];// sums of all x readings, y readings, and z readings individually
//		for(float[] f:terms){
//			for(float g:f) g = MainActivity.STATIONARY/3;
//		}
//		for(float f:sums) f = MainActivity.STATIONARY * bufferLength;
		bufferCounter = 0;
	}
	
	public float lowPassFilter(float[] values){
//		sums[0] -= terms[0][bufferCounter%bufferLength];
//		terms[0][bufferCounter%bufferLength] = values[0];
//		sums[0] += values[0];
//
//		sums[1] -= terms[1][bufferCounter%bufferLength];
//		terms[1][bufferCounter%bufferLength] = values[1];
//		sums[1] += values[1];
//
//		sums[2] -= terms[2][bufferCounter%bufferLength];
//		terms[2][bufferCounter%bufferLength] = values[2];
//		sums[2] += values[2];

		sums[0] -= Math.abs(terms[0][bufferCounter%bufferLength]);
		terms[0][bufferCounter%bufferLength] = values[0];
		sums[0] += Math.abs(values[0]);

		sums[1] -= Math.abs(terms[1][bufferCounter%bufferLength]);
		terms[1][bufferCounter%bufferLength] = values[1];
		sums[1] += Math.abs(values[1]);

		sums[2] -= Math.abs(terms[2][bufferCounter%bufferLength]);
		terms[2][bufferCounter%bufferLength] = values[2];
		sums[2] += Math.abs(values[2]);
		
		bufferCounter++;
		return sums[0] + sums[1] + sums[2];
	}
}
