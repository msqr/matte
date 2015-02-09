/* ===================================================================
 * TestJMagickScale.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Apr 13, 2004 3:04:15 PM.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation; either version 2 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 * 02111-1307 USA
 * ===================================================================
 * $Id: TestJMagickScale.java,v 1.1 2006/06/03 22:26:19 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.test;

import java.io.File;

import magick.FilterType;
import magick.ImageInfo;
import magick.MagickImage;

/**
 * Test application for testing out JMagick image scaling.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/06/03 22:26:19 $
 */
public class TestJMagickScale 
{
	
private static final int[] FILTER_TYPES = {
		FilterType.BesselFilter,
		FilterType.BlackmanFilter,
		FilterType.BoxFilter,
		FilterType.CatromFilter,
		FilterType.CubicFilter,
		FilterType.GuassianFilter,
		FilterType.HammingFilter,
		FilterType.HanningFilter,
		FilterType.HermiteFilter,
		FilterType.LanczosFilter,
		FilterType.MitchellFilter,
		FilterType.PointFilter,
		FilterType.QuadraticFilter,
		FilterType.SincFilter,
		FilterType.TriangleFilter
};

private static final String[] FILTER_NAMES = new String[16];

static {
	FILTER_NAMES[FilterType.BesselFilter] = "Bessel";
	FILTER_NAMES[FilterType.BlackmanFilter] = "Blackman";
	FILTER_NAMES[FilterType.BoxFilter] = "Box";
	FILTER_NAMES[FilterType.CatromFilter] = "Catrom";
	FILTER_NAMES[FilterType.CubicFilter] = "Cubic";
	FILTER_NAMES[FilterType.GuassianFilter] = "Guassian";
	FILTER_NAMES[FilterType.HammingFilter] = "Hamming";
	FILTER_NAMES[FilterType.HanningFilter] = "Hanning";
	FILTER_NAMES[FilterType.HermiteFilter] = "Hermite";
	FILTER_NAMES[FilterType.LanczosFilter] = "Lanczos";
	FILTER_NAMES[FilterType.MitchellFilter] = "Mitchell";
	FILTER_NAMES[FilterType.PointFilter] = "Point";
	FILTER_NAMES[FilterType.QuadraticFilter] = "Quadratic";
	FILTER_NAMES[FilterType.SincFilter] = "Sinc";
	FILTER_NAMES[FilterType.TriangleFilter] = "Triangle";
}


private static final String[][] DEFAULT_ARGS = {
		{"slow1.jpg","1608","980","640","390"},
		{"fast1.jpg","1944","2592","360","480"},
		{"slow2.jpg","1944","2592","360","480"},
		{"fast2.jpg","1600","1200","640","480"},
};

private static class Profile {
	long samples = 0;
	long totalRead = 0;
	long highRead = 0;
	long lowRead = Long.MAX_VALUE;
	long totalWrite = 0;
	long highWrite = 0;
	long lowWrite = Long.MAX_VALUE;
	public void read(long start, long end) {
		long time = end-start;
		if ( time > highRead ) {
			highRead = time;
		}
		if ( time < lowRead) {
			lowRead = time;
		}
		totalRead += time;
		samples++;
	}
	public void write(long start, long end) {
		long time = end-start;
		if ( time > highWrite ) {
			highWrite = time;
		}
		if ( time < lowWrite) {
			lowWrite = time;
		}
		totalWrite += time;
	}
	public double getWriteAverage() {
		return (double)totalWrite/(double)samples;
	}
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("Read: ").append(totalRead).append(", ave = ")
			.append((double)totalRead/(double)samples).append(", high = ")
			.append(highRead).append(", low = ").append(lowRead);
		buf.append("\nWrite: ").append(totalWrite).append(", ave = ")
		.append(getWriteAverage()).append(", high = ")
		.append(highWrite).append(", low = ").append(lowWrite);
		
		return buf.toString();
	}
}
		
public static void main(String[] args) 
{
	String[][] data = null;
	
	if ( args.length < 5 ) {
		data = DEFAULT_ARGS;
	} else {
		data = new String[][] {args};
	}
	
	Profile[] profiles = new Profile[FILTER_TYPES.length];
	
	int numPasses = 5;
	
	try {
		for ( int i = 0; i < data.length; i++ ) {
			String path = data[i][0];
			int ext = path.lastIndexOf('.');
			String outName = path.substring(0,ext);
			String extension = path.substring(ext);
			
			System.out.println("==================================");
			System.out.println("Processing image " +path);
			System.out.println("");
			
			for ( int j = 0; j < FILTER_TYPES.length; j++ ) {
				Profile p = null;
				if ( profiles[j] == null ) {
					p = new Profile();
					profiles[j] = p;
				} else {
					p = profiles[j];
				}
				long start = 0;
				
				String outPath = "J-" +outName +"-"
					+FILTER_NAMES[FILTER_TYPES[j]] +extension;
			
				int inWidth = Integer.parseInt(data[i][1]);
				int inHeight = Integer.parseInt(data[i][2]);
				int outWidth = Integer.parseInt(data[i][3]);
				int outHeight = Integer.parseInt(data[i][4]);
				
				System.out.println("Testing filter " +FILTER_NAMES[FILTER_TYPES[j]]);
				System.out.println("Scaling image " +path +" to " +outWidth +"x" +outHeight);
				System.out.println("Output file: " +outPath);
				
				System.out.println("");
				System.out.print("Pass (out of " +numPasses +"): ");
				System.out.flush();
				
				for ( int k = 0; k < numPasses; k++ ) {
					System.out.print((k+1) +"... ");
					System.out.flush();
					try {
						
						File inFile = new File(path);
						File outFile = new File(outPath);
						
						start = System.currentTimeMillis();
						ImageInfo inInfo = new ImageInfo(inFile.getAbsolutePath());
						MagickImage image = new MagickImage(inInfo);
						p.read(start,System.currentTimeMillis());
						
						image.setFilter(FILTER_TYPES[j]);
						
						start = System.currentTimeMillis();
						MagickImage scaledImage = image.zoomImage(
							outWidth,outHeight);
						scaledImage.setFileName(outPath);
						scaledImage.writeImage(inInfo);
						p.write(start,System.currentTimeMillis());
						
					} finally {
						// force GC to run 
						Runtime runtime = Runtime.getRuntime();
						runtime.runFinalization();
						runtime.gc();
					}
				} // k
				System.out.println("\n");
				System.out.println(p);
				System.out.println("\n");
				System.out.flush();
			} // j
		} // i
	} catch ( Exception e ) {
		System.err.println("Exception:  " +e);
			e.printStackTrace(System.err);
			System.exit(1);
	}
	
	System.out.println("\nResults:");
	
	// process profiles
	StringBuffer buf = new StringBuffer();
	for ( int i = 0; i < FILTER_TYPES.length; i++ ) {
		buf.append(FILTER_NAMES[FILTER_TYPES[i]])
			.append(",").append(profiles[i].getWriteAverage())
			.append("\n");
	}
	System.out.println(buf.toString());
}

}
