package me.roan.fringeremover;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;

public class Worker extends Thread{
	private static final PathMatcher PNG_PATTERN = FileSystems.getDefault().getPathMatcher("glob:*.png");
	private Path inputDir;
	private Path outputDir;
	private boolean overwrite;
	private List<Path> files = new ArrayList<Path>();

	
	//TODO make sure input file is png
	public Worker(Path input, Path output, boolean subdirs, boolean overwrite) throws IOException{
		if(Files.isRegularFile(input, LinkOption.NOFOLLOW_LINKS)){
			inputDir = input.getParent();
		}else{
			inputDir = input;
			Files.find(input, subdirs ? Integer.MAX_VALUE : 1, (path, attr)->{
				return attr.isRegularFile() && PNG_PATTERN.matches(path.getFileName());
			}).forEach(files::add);
		}
		outputDir = output;
		this.overwrite = overwrite;
	}
	
//	public void start(){
//		for(File file : files){
//			Path relative = file.toPath().relativize(inpu);
//			
//			
//			relative.resolve(relative)
//		}
//	}

	private static final void processImage(BufferedImage input, File output) throws IOException{
		BufferedImage copy = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_ARGB);

		for(int x = 0; x < input.getWidth(); x++){
			for(int y = 0; y < input.getHeight(); y++){
				copy.setRGB(x, y, computeColor(x, y, input));
			}
		}

		ImageIO.write(copy, "png", output);
	}

	/**
	 * Computes the new color of the pixel at the given coordinates
	 * given the current image color data. For all opaque and
	 * semi-transparent pixels this is the current color. For all
	 * fully transparent pixels this is the average color of the (at
	 * most) 8 pixels directly surrounding the target pixel that are
	 * not fully transparent. The alpha component of the new color is
	 * kept at 0.
	 * @param x The x coordinate of the pixel to compute.
	 * @param y The y coordinate of the pixel to compute.
	 * @param data The original image data.
	 * @return The new color for the requested pixel.
	 */
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
	
	public static abstract interface ProgressListener{
		public abstract void progress(int done);
		public abstract void done();
	}
}
