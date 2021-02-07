package me.roan.fringeremover;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

import me.roan.util.Dialog;

/**
 * Worker class responsible for processing all the files.
 * @author Roan
 */
public class Worker{
	/**
	 * Path filter that only matches PNG files.
	 */
	private static final PathMatcher PNG_PATTERN = FileSystems.getDefault().getPathMatcher("glob:*.png");
	/**
	 * Input directory.
	 */
	private static Path inputDir;
	/**
	 * Output directory or file.
	 */
	private static Path outputDir;
	/**
	 * Whether to overwrite existing files.
	 */
	private static boolean overwrite;
	/**
	 * List of files to process.
	 */
	private static List<Path> files = new ArrayList<Path>();
	/**
	 * Whether working threads are currently processing files.
	 */
	private static volatile boolean running = false;
	
	/**
	 * Prepares all the files to process based on the given input.
	 * @param input Input folder or file.
	 * @param output Output folder or file.
	 * @param subdirs Whether to parse sub-directories.
	 * @param overwriteFiles Whether to overwrite existing files.
	 * @return The number of files that were found for processing.
	 * @throws IOException When an IOException occurs.
	 */
	public static final int prepare(Path input, Path output, boolean subdirs, boolean overwriteFiles) throws IOException{
		files.clear();
		if(Files.isRegularFile(input, LinkOption.NOFOLLOW_LINKS)){
			inputDir = input.getParent();
			if(PNG_PATTERN.matches(input.getFileName())){
				files.add(input);
			}else{
				Dialog.showErrorDialog("Input file is not a PNG file.");
			}
		}else{
			inputDir = input;
			Files.find(input, subdirs ? Integer.MAX_VALUE : 1, (path, attr)->{
				return attr.isRegularFile() && PNG_PATTERN.matches(path.getFileName());
			}).forEach(files::add);
		}
		outputDir = output;
		overwrite = overwriteFiles;
		return files.size();
	}
	
	/**
	 * Sets whether the working is currently processing files.
	 * @param shouldRun True if the worker should process files.
	 */
	public static final void setRunning(boolean shouldRun){
		running = shouldRun;
	}
	
	/**
	 * Checks if the worker is currently running and processing files.
	 * @return True if the worker is currently processing files.
	 */
	public static boolean isRunning(){
		return running;
	}
	
	/**
	 * Starts the worker with the give number of concurrent threads
	 * and listener to report progress updates to.
	 * @param threads The number of concurrent threads to use.
	 * @param listener The progress listener.
	 */
	public static final void start(int threads, ProgressListener listener){
		ExecutorService executor = Executors.newFixedThreadPool(threads);
		AtomicInteger completed = new AtomicInteger(0);
		running = true;
		
		for(Path file : files){
			executor.submit(()->{
				while(!running){
					try{
						Thread.sleep(1000);
					}catch(InterruptedException e){
					}
				}
				
				try{
					if(!processFile(file)){
						synchronized(listener){
							listener.error(file, "Target file already exists and overwriting is disabled.");
						}
					}
				}catch(Exception e){
					synchronized(listener){
						listener.error(file, e.getMessage());
					}
					e.printStackTrace();
				}finally{
					synchronized(listener){
						int finished = completed.incrementAndGet();
						listener.progress(finished);
						if(finished == files.size()){
							listener.done();
							executor.shutdown();
							running = false;
						}
					}
				}
			});
		}
	}
	
	/**
	 * Processes the given file.
	 * @param file The file to process.
	 * @return True if the file was successfully processed,
	 *         false if the file already exists and overwriting
	 *         is not enabled.
	 * @throws IOException When an IOException occurs.
	 */
	private static final boolean processFile(Path file) throws IOException{
		Path target = Files.isDirectory(outputDir) ? outputDir.resolve(inputDir.relativize(file)) : outputDir;
		if(overwrite || !Files.exists(target)){
			BufferedImage img = ImageIO.read(file.toFile());
			Files.createDirectories(target.getParent());
			processImage(img, target.toFile());
			img.flush();
			return true;
		}else{
			return false;
		}
	}

	/**
	 * Processes the given image and writes it to the given file.
	 * @param input The image to process.
	 * @param output The output file to write to.
	 * @throws IOException When an IOException occurs.
	 */
	private static final void processImage(BufferedImage input, File output) throws IOException{
		BufferedImage copy = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_ARGB);

		for(int x = 0; x < input.getWidth(); x++){
			for(int y = 0; y < input.getHeight(); y++){
				copy.setRGB(x, y, computeColor(x, y, input));
			}
		}

		ImageIO.write(copy, "png", output);
		copy.flush();
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
	
	/**
	 * Listener that subscribes to worker progress events.
	 * @author Roan
	 */
	public static abstract interface ProgressListener{
		
		/**
		 * Called when a file has finished processing.
		 * @param done The total number of files that have finished processing.
		 */
		public abstract void progress(int done);
		
		/**
		 * Called when some error occurred.
		 * @param file The file that caused the error.
		 * @param error The error message.
		 */
		public abstract void error(Path file, String error);
		
		/**
		 * Called when all files have finished processing.
		 */
		public abstract void done();
	}
}
