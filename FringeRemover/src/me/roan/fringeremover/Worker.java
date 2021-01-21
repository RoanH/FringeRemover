package me.roan.fringeremover;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;

public class Worker extends Thread{
	private File inputDir;
	private File outputDir;
	private boolean overwrite;
	private List<File> files = new ArrayList<File>();

	
	//TODO make sure input file is png
	public Worker(File input, File output, boolean subdirs, boolean overwrite){
		if(input.isFile()){
			inputDir = input.getParentFile();
		}else{
			inputDir = input;
			findImages(input, subdirs);
		}
		outputDir = output;
		this.overwrite = overwrite;
	}
	
	
	
	
	
	
	private final void findImages(File input, boolean subdirs){
		for(File file : input.listFiles()){
			if(file.isDirectory() && subdirs){
				findImages(file, true);
			}else if(file.isFile() && file.getName().toLowerCase(Locale.ROOT).endsWith(".png")){
				files.add(file);
			}
		}
	}

	private static final void processImage(BufferedImage input, File output) throws IOException{
		BufferedImage copy = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_ARGB);

		for(int x = 0; x < input.getWidth(); x++){
			for(int y = 0; y < input.getHeight(); y++){
				copy.setRGB(x, y, computeColor(x, y, input));
			}
		}

		ImageIO.write(copy, "png", output);
	}

	private static final int computeColor(int x, int y, BufferedImage data){
		int argb = data.getRGB(x, y);
		if((argb & 0xFF000000) == 0){
			int a = 0;
			int r = 0;
			int g = 0;
			int b = 0;

			for(int dx = -1; dx <= 1; dx++){
				for(int dy = -1; dy <= 1; dy++){
					if(x + dx >= 0 && y + dy >= 0 && x + dx < data.getWidth() && y + dy < data.getHeight() && !(dx == 0 && dy == 0)){
						int color = data.getRGB(x + dx, y + dy);
						int alpha = (color & 0xFF000000) >>> 24;
						if(alpha != 0){
							a += alpha;
							r += ((color & 0xFF0000) >> 16) * alpha;
							g += ((color & 0xFF00) >> 8) * alpha;
							b += (color & 0xFF) * alpha;
						}
					}
				}
			}

			argb = 0x0;
			if(a != 0){
				argb |= (r / a) << 16;
				argb |= (g / a) << 8;
				argb |= b / a;
			}
		}

		return argb;
	}
}
