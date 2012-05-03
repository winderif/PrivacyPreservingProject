package util;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.iptc.IptcReader;

public class VideoFrame {
	private BufferedImage mBufferImage = null;
	private int frameIndex;
    private int[] pixels = null;   
    //private double[][] histogram = null;
    // 12.03.12 winderif, histogram to One-dimensional array.
    private double[] histogram = null;

    private String mTags[];
    private String Photographer = "";
   	
    public VideoFrame() {
    	this.mBufferImage = null;
    	this.frameIndex = 0;
    	this.pixels = null;
    	this.histogram = null;
    }
    
    public VideoFrame(File mFile, int mFrameIndex) {
    	try {    		
    		mBufferImage = ImageIO.read(mFile);    		
    		this.pixels = new int [mBufferImage.getWidth(null)*mBufferImage.getHeight(null)];
    		mBufferImage.getRGB(0, 
    							0, 
    							mBufferImage.getWidth(null), 
    							mBufferImage.getHeight(null), 
    							this.pixels, 
    							0, 
    							mBufferImage.getWidth(null));
    		this.frameIndex = mFrameIndex;
    		this.histogram = RGBToHSVHistogram.hsvHistogram(this.pixels);
    		setTags(mFile);
    	} catch(IOException e) {
    		e.printStackTrace();
         	System.out.println("Read video frame is failed.");
    	}
    }
    
    private void setTags(File f) {
    	try {
			Pattern pattern = Pattern.compile("[0-9]+|(nikon)+|(sony)+|(tokina)+|(panasonic)+|(LG)+|(panorama)+|" +
					"(samsung)+|(digital)+|(grd)+|(ricoh)+|(contax)+|(fuji)+|(film)+|(olympus)+|(natura)+|(zeiss)+|" +
					"(canon)+|(Tamron)+|(vivitar)+|(pentax)+|(Leica)+|(smena)+|(USM)+|(EF)+|(EOS)+|(sigma)+|(HSM)+|(schneider)+|" +
					"(agfa)+|(ultra)+|(planar)+|(kodak)+|(PORTRA)+|(Nikkor)+|(TAKUMAR)+|(Cosina)+|(PROPLUS)+|([?])+|(HDR)+|" +
					"(=)+|(urmap)+|(angenieux)+|(geotag)+|(Xtra)+|(X-tra)+|(AF-S)+|(How)+|(what)+|(When)+|(Where)+"
					,Pattern.CASE_INSENSITIVE);
			Matcher matcher;
			
			//System.out.println(f.getParentFile().getName() + " " + f.getName());
			
			
			IptcReader iptc = new IptcReader(f);
			Metadata m = iptc.extract();
			Iterator d = m.getDirectoryIterator();
			String temptag = "";
			String alltag[] = null;
			Directory directory = null;
			Iterator tags = null;
			Metadata metadata = JpegMetadataReader.readMetadata(f);
		
			Iterator directories = metadata.getDirectoryIterator();
			while(directories.hasNext()) {
				directory = (Directory)directories.next();
				// iterate through tags and print to System.out 
				tags = directory.getTagIterator();
				while(tags.hasNext()) {
					Tag tag = (Tag)tags.next(); // use Tag.toString() 
						if(tag.getTagName().equalsIgnoreCase("Artist")){
							Photographer = tag.getDescription();
						}
						
						if(tag.getDirectoryName().equalsIgnoreCase("Iptc")){
							if(tag.getTagName().equalsIgnoreCase("keywords")){
								//System.out.println("[TAG] " + tag.getDescription());
								temptag = tag.getDescription();
								alltag = tag.getDescription().split(" ");
							}
						}
				} 
			}
			
			// 把拍攝者名稱也加入濾除tag動作
			if(!Photographer.contains("?")&& !Photographer.endsWith("*") && !Photographer.contains("+") && !Photographer.endsWith(")") && !Photographer.startsWith("(")){
				pattern = pattern.compile(pattern.pattern()+"|("+Photographer+")+",Pattern.CASE_INSENSITIVE);
			} else {
				;
			}
						
			if(alltag != null) {
				Vector<String> temp1 = new Vector<String>();
				String temp[] = new String[alltag.length];			
				int loop = 0;
				
				for(int i = 0 ;i<alltag.length; i++) {
					matcher = pattern.matcher(alltag[i]);
					
					if(matcher.find() || temp1.contains(alltag[i].toLowerCase())) {
						;
					} else {
						temp1.add(alltag[i].toLowerCase());
						temp[loop] = alltag[i];
						loop++;
					}
				}
				
				//System.out.println("[TAG] size: " + loop);
				mTags = new String[loop];
				for(int i = 0; i<loop;i++) {
					mTags[i] = temp[i].toLowerCase();
					//System.out.print(mTags[i] + " ");
				}
				temp1.toArray(mTags);
			} else {
				mTags = new String[0];
			}
			//System.out.println("");
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println(e);
		}
	}
    	      
    public int[] getPixels() {    
      	return pixels;
    }
    
    public int getFrameIndex() {    
      	return frameIndex;
    }      
   
    public double[] getHistogram() {    
      	return histogram;
    }
    
    public String[] getTags() {
    	return this.mTags;
    }
}