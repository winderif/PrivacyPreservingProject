package util;
import java.awt.Color;

public class RGBToHSVHistogram {   
	public static double[] hsvHistogram(int[] pixels) {    
		int[] histogramH = new int[8];
		int[] histogramS = new int[4];
		int[] histogramV = new int[4];
		double[] doubleHistogramH = new double[8];
		double[] doubleHistogramS = new double[4];
		double[] doubleHistogramV = new double[4];
		double[] histogram = new double[16];
      
		for(int i=0;i<pixels.length;i++) {      
			float af[] = Color.RGBtoHSB(0xff & (pixels[i]>>16), 0xff & (pixels[i]>>8), 0xff & (pixels[i]), null);
			histogramH[(int)(af[0] * 7.0)]++;   
			histogramS[(int)(af[1] * 3.0)]++; 
			histogramV[(int)(af[2] * 3.0)]++; 
		}
      
		for(int i = 0; i < 8; i++) {      
			doubleHistogramH[i] = histogramH[i] / (double)pixels.length;
			histogram[i] = doubleHistogramH[i];
		}
      
		for(int i = 0; i < 4; i++) {      
			doubleHistogramS[i] = histogramS[i] / (double)pixels.length;
			histogram[i+8] = doubleHistogramS[i];
		}
      
		for(int i = 0; i < 4; i++) {      
			doubleHistogramV[i] = histogramV[i] / (double)pixels.length;
			histogram[i+12] = doubleHistogramV[i];
		}
								
		return histogram;
	}
}