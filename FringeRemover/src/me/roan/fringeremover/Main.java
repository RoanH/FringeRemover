package me.roan.fringeremover;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

public class Main{

	public static void main(String[] args){
		File test = new File("C:\\Users\\RoanH\\Downloads\\selection-mod-doubletime@2x.png");
		
		
		
		try{
			BufferedImage img = ImageIO.read(test);
			
			Set<Integer> values = new HashSet<Integer>();
			
			for(int x = 0; x < img.getWidth(); x++){
				for(int y = 0; y < img.getHeight(); y++){
					int argb = img.getRGB(x, y);
					int alpha = (argb & 0xFF000000) >>> 24;
					//System.out.println(alpha);
					values.add(alpha);
					if(alpha == 0){
						System.out.println("a0: " + ((argb & 0xFF0000) >> 16) + " | " + ((argb & 0xFF00) >> 8) + " | " + (argb & 0xFF));
					}
				}
			}
			
			values.forEach(System.out::println);
			
			
			
			
		}catch(IOException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		
		
		
	}
}
