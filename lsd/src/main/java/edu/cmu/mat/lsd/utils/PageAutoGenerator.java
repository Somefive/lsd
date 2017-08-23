package edu.cmu.mat.lsd.utils;

import edu.cmu.mat.lsd.logger.HCMPLogger;
import edu.cmu.mat.scores.Barline;
import edu.cmu.mat.scores.Page;
import edu.cmu.mat.scores.System;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class PageAutoGenerator {
	
	private Page page;
	private double[][] rawData;
	private int height;
	private int width;
	private boolean fail;
	private ArrayList<ArrayList<Integer>> staffLineGroups = new ArrayList<>();
	protected ArrayList<SystemPack> systemPacks = new ArrayList<>();
	
	final int STAFFLINE_VARIANCE = 1;
	final double MAIN_STAFFLINE_COLOR_DEPTH_THRESHOLD = 0.4;
	final int BARLINE_INTERVAL_THRESHOLD = 20;
	final double BARLINE_COLOR_DEPTH_THRESHOLD = 0.9;
	
	public PageAutoGenerator(Page p) {
		setPage(p);
	}
	
	public void setPage(Page p) {
		page = p;
		page.getSystems().clear();
		initDataArray();
		generate();
	}
	
	private void initDataArray() {
		BufferedImage bufferedImage = page.getImage().getImage();
		height = bufferedImage.getHeight();
		width = bufferedImage.getWidth();
		rawData = new double[width][height];
		for (int y=0;y<height;++y) {
			for (int x=0;x<width;++x) {
				int rgb = bufferedImage.getRGB(x, y);
				int r = (rgb >> 16) & 0xFF;
				int g = (rgb >> 8) & 0xFF;
				int b = (rgb & 0xFF);
				double c = (r+g+b)/3.0/255.0;
				rawData[x][y] = 1 - c*c;
			}
		}
	}
	
	private void generate() {
		fail = false;
		generateStaffLineGroupsWithConvolution();
		generateSystemPacks();
		generatePageSystems();
	}
	
	private void generateStaffLineGroupsWithConvolution() {
		double[] rowAverage = new double[height];
		for (int y=0;y<height;++y)
			for (int x=0;x<width;++x)
					rowAverage[y] += (rawData[x][y]/width);
		double[] rowCorrelation = new double[height];
		for (int y=0;y<height;++y)
			for (int d=0;d+y<height;++d)
				rowCorrelation[y] += rowAverage[d+y]*rowAverage[d];
		
		double[] rowCorrelationSmooth = new double[height];
		rowCorrelationSmooth[0] = .75*rowCorrelation[0] + .25*rowCorrelation[1];
		rowCorrelationSmooth[height-1] = .75*rowCorrelation[height-1] + .25*rowCorrelation[height-2];
		for (int y=1;y<height-1;++y)
			rowCorrelationSmooth[y] = .25*rowCorrelation[y-1] + .5*rowCorrelation[y] + .25*rowCorrelation[y+1];
		
		int interval = 1;
		while (interval<height-1 && (rowCorrelationSmooth[interval] < rowCorrelationSmooth[interval-1] || rowCorrelationSmooth[interval] < rowCorrelationSmooth[interval+1]))
			++interval;
		if (5*interval > height) {
			fail = true;
			return;
		}
		
		double[] staffLineRefCorrelation = new double[height];
		for (int y=0;y<height;++y) {
			for (int i=0;i<5;++i) {
				int d = i*interval;
				if (d+y >= height) break;
				staffLineRefCorrelation[y] += rowAverage[d+y]/5;
			}
		}
		
		ArrayList<Integer> possibleStaffLines = new ArrayList<>();
		for (int y=1;y<height-1;++y)
			if (staffLineRefCorrelation[y] > staffLineRefCorrelation[y-1] && staffLineRefCorrelation[y] > staffLineRefCorrelation[y+1])
				possibleStaffLines.add(y);
		
		staffLineGroups.clear();
		for (int i=4;i<possibleStaffLines.size()-4;++i) {
			boolean flag = true;
			int sIndex = possibleStaffLines.get(i);
			if (staffLineRefCorrelation[sIndex] < MAIN_STAFFLINE_COLOR_DEPTH_THRESHOLD) continue;
			for (int j=1;j<5;++j) {
				int lIndex = possibleStaffLines.get(i-j), rIndex = possibleStaffLines.get(i+j);
				if (staffLineRefCorrelation[sIndex] < staffLineRefCorrelation[lIndex] || staffLineRefCorrelation[sIndex] < staffLineRefCorrelation[rIndex]) {
					flag = false;
					break;
				}
			}
			if (!flag) continue;
			ArrayList<Integer> staffLineGroup = new ArrayList<>();
			staffLineGroup.add(sIndex);
			int count = 1;
			++i;
			for (; i<possibleStaffLines.size() && count < 5; ++i) {
				int nextIndex = possibleStaffLines.get(i);
				if (nextIndex - sIndex > interval*count+STAFFLINE_VARIANCE) {
					count += 1;
					--i;
				}
				else if (nextIndex - sIndex >= interval*count-STAFFLINE_VARIANCE) {
					count += 1;
					staffLineGroup.add(nextIndex);
				}
			}
			if (staffLineGroup.size() > 3)
				staffLineGroups.add(staffLineGroup);
		}
	}
	
	private ArrayList<Integer> getBarline(int staffLineGroupStart, int staffLineGroupEnd, boolean full) {
		double[] columnAverage = new double[width];
		double[] columnAverageSmooth = new double[width];
		int yStart = full ? staffLineGroups.get(staffLineGroupStart).get(0) : staffLineGroups.get(staffLineGroupStart).get(staffLineGroups.get(staffLineGroupStart).size()-1);
		int yEnd = full ? staffLineGroups.get(staffLineGroupEnd).get(staffLineGroups.get(staffLineGroupEnd).size()-1) : staffLineGroups.get(staffLineGroupEnd).get(0);
		for (int x=0; x<width; ++x) {
			columnAverage[x] = 0;
			for (int y=yStart; y<=yEnd; ++y)
				columnAverage[x] += rawData[x][y]/(yEnd-yStart+1);
		}
		columnAverageSmooth[0] = Math.max(columnAverage[0], .75*columnAverage[0]+.25*columnAverage[1]);
		columnAverageSmooth[width-1] = Math.max(columnAverage[width-1], .75*columnAverage[width-1]+.25*columnAverage[width-2]);
		for (int x=1; x<width-1; ++x)
			columnAverageSmooth[x] = Math.max(columnAverage[x], .25*columnAverage[x-1]+.5*columnAverage[x]+.25*columnAverage[x+1]);
		ArrayList<Integer> climax = new ArrayList<>();
		ArrayList<Integer> continuous = new ArrayList<>();
		int sum;
		for (int x=1; x<width-1; ++x) {
			if (columnAverageSmooth[x] > BARLINE_COLOR_DEPTH_THRESHOLD && columnAverageSmooth[x] >= columnAverageSmooth[x - 1] && columnAverageSmooth[x] >= columnAverageSmooth[x + 1]) {
				if (continuous.size() > 0 && x - continuous.get(continuous.size() - 1) > BARLINE_INTERVAL_THRESHOLD) {
					sum = 0;
					for (Integer c : continuous) sum += c;
					climax.add(sum / continuous.size());
					continuous.clear();
				}
				continuous.add(x);
			}
		}
		if (continuous.size() > 0) {
			sum = 0;
			for (Integer c : continuous) sum += c;
			climax.add(sum/continuous.size());
		}
		return climax;
	}
	
	private int getDivideLine(int sIndex, int tIndex) {
		int divideLine = sIndex;
		double minValue = -1;
		for (int y=sIndex; y<=tIndex; ++y) {
			double currentValue = 0;
			for (int x=0; x<width; ++x)
				currentValue += rawData[x][y];
			if (minValue < 0 || currentValue < minValue) {
				divideLine = y;
				minValue = currentValue;
			}
		}
		return divideLine;
	}
	
	private void generateSystemPacks() {
		systemPacks.clear();
		int begin = 0, end = 0;
		for (int i=0; i<staffLineGroups.size(); ++i) {
			boolean connect = (i != staffLineGroups.size() - 1) && getBarline(i, i + 1, false).size() > 1;
			if (connect)
				end = i+1;
			if (!connect) {
				ArrayList<Integer> bs = getBarline(begin, end, true);
				ArrayList<ArrayList<Integer>> slgs = new ArrayList<>();
				for (int j=begin; j<=end; ++j)
					slgs.add(staffLineGroups.get(j));
				systemPacks.add(new SystemPack(slgs, bs));
				++end;
				begin = end;
				i = end;
				--i;
			}
		}
		int upper, lower;
		upper = getDivideLine(0, systemPacks.get(0).getUpperStaffLine());
		for (int i=0; i<systemPacks.size()-1; ++i) {
			lower = getDivideLine(systemPacks.get(i).getLowerStaffLine(), systemPacks.get(i+1).getUpperStaffLine());
			systemPacks.get(i).setBound(upper, lower);
			upper = lower;
		}
		lower = getDivideLine(systemPacks.get(systemPacks.size()-1).getLowerStaffLine(), height-1);
		systemPacks.get(systemPacks.size()-1).setBound(upper, lower);
	}

	private void generatePageSystems() {
		systemPacks.forEach(systemPack -> {
			System system = new System(page, (systemPack.upperBound+0.0)/height, (systemPack.lowerBound+0.0)/height);
			systemPack.barlines.forEach(barline -> system.addBarline(new Barline(system, (barline+0.0)/width)));
			page.addSystem(system);
		});
	}
	
	protected class SystemPack {
		ArrayList<ArrayList<Integer>> staffLineGroups;
		ArrayList<Integer> barlines;
		int upperBound, lowerBound;
		SystemPack(ArrayList<ArrayList<Integer>> slgs, ArrayList<Integer> bs) {
			staffLineGroups = slgs;
			barlines = bs;
		}
		void setBound(int ub, int lb) {
			upperBound = ub;
			lowerBound = lb;
		}
		int getUpperStaffLine() {
			return staffLineGroups.get(0).get(0);
		}
		int getLowerStaffLine() {
			return staffLineGroups.get(staffLineGroups.size()-1).get(staffLineGroups.get(staffLineGroups.size()-1).size()-1);
		}
	}
	
	protected class Chart extends JFrame {
		double[] pts;
		double max, min, size;
		Chart(double[] points) {
			pts = points;
			max = pts[0];
			min = pts[0];
			size = pts.length;
			for (double p : pts) {
				if (p > max) max = p;
				if (p < min) min = p;
			}
			HCMPLogger.fine("max:"+max+" min:"+min);
			setSize(500,500);
			setVisible(true);
		}
		
		@Override
		public void paint(Graphics g) {
			super.paint(g);
			int margin = 20;
			int h = getHeight() - 2*margin, w = getWidth() - 2*margin;
			g.setColor(Color.BLACK);
			for (int i = 0; i < pts.length - 1; i++) {
				int x1 = i*w/pts.length, x2 = (i+1)*w/pts.length;
				int y1 = (int)((pts[i]-min)*h/max), y2 = (int)((pts[i+1]-min)*h/max);
				g.drawLine(x1+margin, h-y1+margin, x2+margin, h-y2+margin);
			}
		}
	}
}
