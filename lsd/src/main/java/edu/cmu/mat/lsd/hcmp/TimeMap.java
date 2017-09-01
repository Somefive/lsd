package edu.cmu.mat.lsd.hcmp;

import edu.cmu.mat.lsd.logger.HCMPLogger;

import java.util.Date;

public class TimeMap {
	private double _m;
	private double _b;

	public TimeMap(double m, double b) {
		_m = m;
		_b = b;
	}

	public static TimeMap Create(TimeMap tm1, TimeMap tm2) {
		double x1 = 0;
		double y1 = tm2.at(tm1.at(x1));

		double x2 = 10;
		double y2 = tm2.at(tm1.at(x2));

		double slope = (y2 - y1) / (x2 - x1);
		return Create(x1, y1, slope);
	}

	public static TimeMap Create(double x, double y, double slope) {
		double b = y - slope * x;
		return new TimeMap(slope, b);
	}

	public double at(double x) {
		return _m * x + _b;
	}

	public double from(double y) {
		return (y - _b) / _m;
	}
	
	public static double tempo = -1;
	public static double offset = 0;
	public static double bias = 0;
	public static int getCurrentBeatIndex() {
		return (int) Math.round((new Date().getTime() / 1000.0 + offset) * tempo + bias);
	}
	public static long getNextEventDelay() {
		int currentEvent = (getCurrentBeatIndex()+1)/4;
		int nextBeat = (currentEvent+1)*4;
		return (long)(((nextBeat - bias)/tempo - offset)*1000) - new Date().getTime();
	}
	public static void setTimeMap(double real, double virtual, double tempo) {
		TimeMap.tempo = tempo;
		TimeMap.bias = virtual - real * TimeMap.tempo;
		TimeMap.offset = - TimeMap.bias / TimeMap.tempo - new Date().getTime() / 1000.0;
	}
}
