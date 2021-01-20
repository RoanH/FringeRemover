package me.roan.fringeremover;

import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import me.roan.util.ClickableLink;
import me.roan.util.Dialog;
import me.roan.util.Util;

public class Main{
	
	public static void main(String[] args){
		Util.installUI();
		
		JFrame frame = new JFrame("Fringe Remover");
		Dialog.setDialogTitle("Fringe Remover");
		Dialog.setParentFrame(frame);
		//TODO set icon
		
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		JPanel input = new JPanel();
		input.setBorder(BorderFactory.createTitledBorder("Input"));
		
		
		
		JPanel output = new JPanel();
		output.setBorder(BorderFactory.createTitledBorder("Output"));
		
		
		//TODO backup same panel as output? or make a new options panel
		
		
		JPanel progress = new JPanel();
		progress.setBorder(BorderFactory.createTitledBorder("Progress"));
		
		
		
		
		JPanel controls = new JPanel();
		controls.setBorder(BorderFactory.createTitledBorder("Controls"));
		
		
		
		
		
		JPanel version = new JPanel(new GridLayout(2, 1, 0, 2));
		version.setBorder(BorderFactory.createTitledBorder("Information"));
		JPanel links = new JPanel(new GridLayout(1, 2, -2, 0));
		JLabel forum = new JLabel("<html><font color=blue><u>Forums</u></font> -</html>", SwingConstants.RIGHT);
		JLabel git = new JLabel("<html>- <font color=blue><u>GitHub</u></font></html>", SwingConstants.LEFT);
		links.add(forum);
		links.add(git);
		version.add(links);
		version.add(Util.getVersionLabel("ImageScaler", "v2.4"));//XXX the version number - don't forget build.gradle
		//TODO forum.addMouseListener(new ClickableLink("https://osu.ppy.sh/community/forums/topics/xxxxxx"));
		git.addMouseListener(new ClickableLink("https://github.com/RoanH/FringeRemover"));
		
		panel.add(input);
		panel.add(output);
		panel.add(progress);
		panel.add(controls);
		panel.add(version);
		
		frame.add(panel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		//TODO check size
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	public static void maintest(String[] args){
		File test = new File("C:\\Users\\RoanH\\Downloads\\selection-mod-doubletime@2x.png");
		//File test = new File("testout.png");

		
		
		try{
			BufferedImage img = ImageIO.read(test);
			BufferedImage copy = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
			
			Set<Integer> values = new HashSet<Integer>();
			
			for(int x = 0; x < img.getWidth(); x++){
				for(int y = 0; y < img.getHeight(); y++){
					int argb = img.getRGB(x, y);
					int alpha = (argb & 0xFF000000) >>> 24;
					//System.out.println(alpha);
					values.add(alpha);
					if(alpha == 0){
						System.out.println("a0: " + ((argb & 0xFF0000) >> 16) + " | " + ((argb & 0xFF00) >> 8) + " | " + (argb & 0xFF));
						//copy.setRGB(x, y, 0);
					}else{
						//copy.setRGB(x, y, argb);
					}
					
					copy.setRGB(x, y, computeColor(x, y, img));
				}
			}
			
			values.forEach(System.out::println);
			
			ImageIO.write(copy, "png", new File("testout.png"));
			
			
		}catch(IOException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static final int computeColor(int x, int y, BufferedImage data){
		int argb = data.getRGB(x, y);
		if((argb & 0xFF000000) == 0){
			int a = 0;
			int r = 0;
			int g = 0;
			int b = 0;
			
			System.out.println("  For: " + x + " | " + y);
			for(int dx = -1; dx <= 1; dx++){
				for(int dy = -1; dy <= 1; dy++){
					if(x + dx >= 0 && y + dy >= 0 && x + dx < data.getWidth() && y + dy < data.getHeight() && !(dx == 0 && dy == 0)){
						int color = data.getRGB(x + dx, y + dy);
						int alpha = (color & 0xFF000000) >>> 24;
						if(alpha != 0){
							System.out.println("  NN: " + alpha + " | " + ((color & 0xFF0000) >> 16) + " | " + ((color & 0xFF00) >> 8) + " | " + (color & 0xFF));
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
				System.out.println("  Result: " + (r / a) + " | " + (g / a) + " | " + (b / a));
			}
		}
		
		return argb;
	}
}
