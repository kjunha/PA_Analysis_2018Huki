import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import org.apache.commons.math3.stat.descriptive.*;

public class PAData {
	ArrayList<Integer> time = new ArrayList<>();
	ArrayList<DescriptiveStatistics> session = new ArrayList<>();
	ArrayList<Double> rolavg = new ArrayList<>();
	DescriptiveStatistics stdevplot = new DescriptiveStatistics();
	File src;
	
	public PAData(String filename) {
		this.src = new File("src/" + filename);
		try {
			boolean isfirst = true;
			Scanner s = new Scanner(src);
			s.nextLine();
			while(s.hasNextLine()) {
				String[] line = s.nextLine().split(",");
				time.add(Integer.parseInt(line[0]));
				//just for the first time
				while(isfirst) {
					for(int i = 1; i < line.length; i++) {
						session.add(new DescriptiveStatistics());
					}
					isfirst = false;
				}
				for(int i = 1; i < line.length; i++) {
					session.get(i-1).addValue(Double.parseDouble(line[i]));
				}		
			}
			s.close();
		} catch(IOException e) {
			System.out.println("File Not Found!");
		}
		System.out.println("Total data size: " + session.size());
		
		//Clean up!
		DescriptiveStatistics datacleaning = new DescriptiveStatistics();
		ArrayList<DescriptiveStatistics> trimmed = new ArrayList<>();
		DescriptiveStatistics temp = new DescriptiveStatistics();
		
		//-1 and, over 100 out
		for(DescriptiveStatistics d : session) {
			trimmed.add(new DescriptiveStatistics());
			for(int i = 0; i < (int)d.getN(); i++) {
				if(d.getElement(i) > -1 && d.getElement(i) <= 100) {
					datacleaning.addValue(d.getElement(i));
				} else {
					datacleaning.addValue(-1.0d);
				}
			}
			
				//Data Copying Sequence
			for(int i = 0; i < (int)datacleaning.getN(); i++) {
				trimmed.get(trimmed.size()-1).addValue(datacleaning.getElement(i));
			}
			datacleaning.clear();
		}
		//get STDEV of each dial
		for(DescriptiveStatistics d : trimmed) {
			for(int i = 0; i < (int)d.getN(); i++) {
				if(d.getElement(i) != -1.0d) {
					temp.addValue(d.getElement(i));
				}
			}
			stdevplot.addValue(temp.getStandardDeviation());
			temp.clear();
		}
		//copy trimmed to session
		session.clear();
		for(DescriptiveStatistics d : trimmed) {
			session.add(new DescriptiveStatistics());
			for(int i = 0; i < d.getN(); i++) {
				session.get(session.size() -1).addValue(d.getElement(i));
			}
		}
		
	}
	
	public void plotAttendance() {
		System.out.println("The Average STDEV of this session is " + stdevplot.getMean());
		double mid = stdevplot.getMean();
		double p1 = mid / 3;
		double p2 = p1 * 2;
		double max = stdevplot.getMax();
		double p4 = p1 * 4;
		double p5 = p1 * 5;
		ArrayList<Double>sorted = new ArrayList<>();
		for(int i = 0; i < (int)stdevplot.getN(); i++) {
			sorted.add(stdevplot.getElement(i));
		}
		Collections.sort(sorted);
		double[] cats = {0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d};
		for(Double d : sorted) {
			if(d.doubleValue() <= p1) {
				cats[0]++;
			} else if(d.doubleValue() > p1 && d.doubleValue() <= p2) {
				cats[1]++;
			} else if(d.doubleValue() > p2 && d.doubleValue() <= mid) {
				cats[2]++;
			} else if(d.doubleValue() > mid && d.doubleValue() <= p4) {
				cats[3]++;
			} else if(d.doubleValue() > p4 && d.doubleValue() <= p5) {
				cats[4]++;
			} else if(d.doubleValue() > p5 && d.doubleValue() <= max) {
				cats[5]++;
			}
		}
		//display
		System.out.println("Index");
		System.out.println("Cat 0: 0 ~ " + p1);
		System.out.println("Cat 1: " + p1 + " ~ " + p2);
		System.out.println("Cat 2: " + p2 + " ~ " + mid);
		System.out.println("Cat 3: " + mid + " ~ " + p4);
		System.out.println("Cat 4: " + p4 + " ~ " + p5);
		System.out.println("Cat 5: " + p5 + " ~ " + max);
		//plot
		drawStarplot(6, cats);
	}
	
	public void cleanUnattended(double cutline, int size) {
		ArrayList<Integer> index = new ArrayList<>();
		DescriptiveStatistics rowavg = new DescriptiveStatistics();
		DescriptiveStatistics avg_sec = new DescriptiveStatistics();
		double def = cutline;
		for(int i = 0; i < (int)stdevplot.getN(); i++) {
			if(stdevplot.getElement(i) < def) {
				index.add(i);
			}
		}
		for(int i = 0; i < index.size(); i++) {
			session.remove(i);
		}
		
		//set Rolling average
		avg_sec.setWindowSize(size);
		for(int i = 0; i < (int)session.get(0).getN(); i++) {
			for(DescriptiveStatistics d : session) {
				if(d.getElement(i) != -1.0d) {
					rowavg.addValue(d.getElement(i));
				}
			}
			avg_sec.addValue(rowavg.getMean());
			rolavg.add(avg_sec.getMean());
			rowavg.clear();
		}
		
		System.out.println("Successfully calculated!");
		
	}
	
	public void setTimeframe(int start, int end) {
		ArrayList<Double> temp = new ArrayList<>();
		for(DescriptiveStatistics d : session) {
			for(int i = start -1; i < (int)d.getN(); i++) {
				temp.add(d.getElement(i));
			}
		
			for(int i = (int)d.getN() ; i > end; i--) {
				temp.remove(temp.size() - 1);
			}
			d.clear();
			for(int i = 0; i < temp.size(); i++) {
				d.addValue(temp.get(i));
			}
			temp.clear();
		}
		System.out.println("Trimmed size: " + (int)session.get(0).getN());
	}
	
	public int getValidDial() {
		return session.size();
	}
	
	public int[] get5Maxmnt(double time) {
		ArrayList<Integer> index = new ArrayList<>();
		ArrayList<Double> temp = new ArrayList<>();
		for(int i = 0; i < (int)rolavg.size(); i++) {
			index.add(i);
		}
		int[] maxmnt = new int[5];
		double[] maxscore = new double[5];
		
		//copy rolling avg to temp
		for(int i = 0; i < 5; i++) {
			for(int j = 0; j < index.size(); j++) {
				temp.add(rolavg.get(index.get(j).intValue()));
			}
			maxscore[i] = Collections.max(temp);
			maxmnt[i] = rolavg.indexOf(maxscore[i]); //+100, -100 remove indexes
			for(int k = maxmnt[i] - 100; k < maxmnt[i] + 100; k++) {
				if(index.indexOf(k) != -1) {
					index.remove(index.indexOf(k));
				}
			}
			temp.clear();
			int minute = (int)Math.floor((maxmnt[i] + time)/60);
			int second = (int)((maxmnt[i] + time)%60);
			System.out.println(minute + "min " + second + "sec " + " | " + maxscore[i]);
		}
		return maxmnt;
	}
	
	public int[] get5Minmnt(double time) {
		ArrayList<Integer> index = new ArrayList<>();
		ArrayList<Double> temp = new ArrayList<>();
		for(int i = 0; i < (int)rolavg.size(); i++) {
			index.add(i);
		}
		int[] minmnt = new int[12];
		double[] minscore = new double[minmnt.length];
		
		for(int i = 0; i < minmnt.length; i++) {
			for(int j = 0; j < index.size(); j++) {
				temp.add(rolavg.get(index.get(j).intValue()));
			}
			minscore[i] = Collections.min(temp);
			minmnt[i] = rolavg.indexOf(minscore[i]); //+100, -100 remove indexes
			for(int k = minmnt[i] - 100; k < minmnt[i] + 100; k++) {
				if(index.indexOf(k) != -1) {
					index.remove(index.indexOf(k));
				}
			}
			temp.clear();
			int minute = (int)Math.floor((minmnt[i] + time)/60);
			int second = (int)((minmnt[i] + time)%60);
			System.out.println(minute + "min " + second + "sec " + " | " + minscore[i]);
		}
		return minmnt;
	}
	
	private void drawStarplot(int cat, double[] num) {
		double sum = 0;
		int[] cvt = new int[cat];
		for(int i = 0; i < cat; i++) {
			sum = sum + num[i];
		}
		for(int i = 0; i < cat; i++) {
			cvt[i] = (int)Math.ceil((num[i] / sum) * 10.0d);
		}
		for(int i = 0; i < 10; i++) {
			for(int j = 0; j < cat; j++) {
				if(10 - cvt[j] > i) {
					System.out.print("| |");
				} else {
					System.out.print("|*|");
				}
			}
			System.out.println("");
		}
		System.out.println("----------------------------");
		for(int i = 0; i < cat; i++) {
			System.out.print("|" + i + "|");
		}
		System.out.println("");
		
	}
	
	public void exportSessionReview() {
	      try {
	          String content = "TutorialsPoint is one the best site in the world";
	          File file = new File("src/SessionReview.csv");
	          if (!file.exists()) {
	             file.createNewFile();
	          } 
	          FileWriter fw = new FileWriter(file);
	          BufferedWriter bw = new BufferedWriter(fw);
	          int index = 59;
	          for(int i = 1; i < (rolavg.size()/60) + 1; i++) {
	        	  bw.write("00:" +i + ":00" + "," + rolavg.get(index));
	        	  bw.newLine();
	        	  index = index + 60;
	          }
	          bw.close();
	          
	          System.out.println("Done");
	       } catch (IOException e) {
	          e.printStackTrace();
	       }
	}
	
}
