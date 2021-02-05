package me.roan.fringeremover;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import me.roan.fringeremover.Worker.ProgressListener;
import me.roan.util.ClickableLink;
import me.roan.util.Dialog;
import me.roan.util.FileSelector;
import me.roan.util.FileSelector.FileExtension;
import me.roan.util.FileTextField;
import me.roan.util.Util;

public class Main{
	private static final FileExtension PNG_EXTENSION = FileSelector.registerFileExtension("PNG", "png");
	private static Worker worker = null;
	
	public static void main(String[] args){
		Util.installUI();
		
		JFrame frame = new JFrame("Fringe Remover");
		Dialog.setDialogTitle("Fringe Remover");
		Dialog.setParentFrame(frame);
		//TODO set icon
		try{
			frame.setIconImages(Arrays.asList(
				ImageIO.read(new File("C:\\Users\\RoanH\\Downloads\\Downloads\\text_shapes64.png")),
				ImageIO.read(new File("C:\\Users\\RoanH\\Downloads\\Downloads\\text_shapes24simple.png"))
			));
		}catch(IOException e1){
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		JPanel input = new JPanel(new BorderLayout());
		input.setBorder(BorderFactory.createTitledBorder("Input"));
		input.add(new JLabel("Target: "), BorderLayout.LINE_START);
		FileTextField inputField = new FileTextField();
		input.add(inputField, BorderLayout.CENTER);
		JPanel openButtons = new JPanel(new GridLayout(1, 2));
		JButton openFile = new JButton("File");
		JButton openFolder = new JButton("Folder");
		openButtons.add(openFile);
		openButtons.add(openFolder);
		input.add(openButtons, BorderLayout.LINE_END);
		
		openFile.addActionListener(e->{
			File selected = Dialog.showFileOpenDialog(PNG_EXTENSION);
			inputField.setText(selected == null ? null : selected.getAbsolutePath());
		});
		
		openFolder.addActionListener(e->{
			File selected = Dialog.showFolderOpenDialog();
			inputField.setText(selected == null ? null : selected.getAbsolutePath());
		});
		
		JPanel output = new JPanel(new BorderLayout());
		output.setBorder(BorderFactory.createTitledBorder("Output"));
		output.add(new JLabel("Target: "), BorderLayout.LINE_START);
		FileTextField outputField = new FileTextField();
		output.add(outputField, BorderLayout.CENTER);
		JButton saveLoc = new JButton("Select");
		output.add(saveLoc, BorderLayout.LINE_END);
		
		
		//TODO only sync if not edited yet?
		inputField.setListener(path->{
			outputField.setText(path);
		});
		
		outputField.setListener(path->{
			if(!inputField.getText().equals(path)){
				System.out.println("Fire");
			}
		});
		
		saveLoc.addActionListener(e->{
			File selected = Dialog.showFolderOpenDialog();
			outputField.setText(selected == null ? null : selected.getAbsolutePath());
		});
		
		JPanel options = new JPanel(new GridLayout(3, 1));
		options.setBorder(BorderFactory.createTitledBorder("Options"));
		JCheckBox parseSubDir = new JCheckBox("Parse subdirectories", false);//TODO disable if input is file
		options.add(parseSubDir);
		JCheckBox overwrite = new JCheckBox("Overwrite existing files", false);//TODO default value
		options.add(overwrite);
		JPanel threadsPanel = new JPanel(new BorderLayout());
		threadsPanel.add(new JLabel("Threads: "), BorderLayout.LINE_START);
		int maxThreads = Runtime.getRuntime().availableProcessors();
		JSpinner threadCount = new JSpinner(new SpinnerNumberModel(Math.min(4, maxThreads), 1, maxThreads, 1));
		threadsPanel.add(threadCount, BorderLayout.CENTER);
		options.add(threadsPanel);
		
		JPanel progress = new JPanel(new BorderLayout());
		progress.setBorder(BorderFactory.createTitledBorder("Progress"));
		JProgressBar bar = new JProgressBar();
		bar.setMinimum(0);
		progress.add(bar, BorderLayout.CENTER);
		JLabel ptext = new JLabel("Waiting...", SwingConstants.CENTER);
		progress.add(ptext, BorderLayout.PAGE_START);
		
		JPanel controls = new JPanel(new GridLayout(1, 2, 5, 0));
		controls.setBorder(BorderFactory.createTitledBorder("Controls"));
		JButton pause = new JButton("Pause");
		JButton start = new JButton("Start");
		controls.add(start);
		controls.add(pause);
		
		Consumer<Boolean> enableFun = enabled->{
			inputField.setEnabled(enabled);
			outputField.setEnabled(enabled);
			start.setEnabled(enabled);
			pause.setEnabled(!enabled);
			openFile.setEnabled(enabled);
			openFolder.setEnabled(enabled);
			saveLoc.setEnabled(enabled);
			parseSubDir.setEnabled(enabled);
			overwrite.setEnabled(enabled);
		};
		enableFun.accept(true);
		
		start.addActionListener(e->{
			enableFun.accept(false);
			
			Path inputPath = inputField.getFile().toPath();
			Path outputPath = outputField.getFile().toPath();
			
			//TODO handle output being a file?
			
			
			
			try{
				int total = Worker.prepare(inputPath, outputPath, parseSubDir.isSelected(), overwrite.isSelected());
				bar.setMaximum(total);
				bar.setValue(0);
				
				//TODO make threads configurable
				Worker.start(4, new ProgressListener(){

					@Override
					public void progress(int done){
						bar.setValue(done);
					}

					@Override
					public void error(Path file, Exception error){
						// TODO Auto-generated method stub
						
					}

					@Override
					public void done(){
						// TODO Auto-generated method stub
						
					}
				});
			}catch(IOException e1){
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
		
		JPanel version = new JPanel(new GridLayout(2, 1, 0, 2));
		version.setBorder(BorderFactory.createTitledBorder("Information"));
		JPanel links = new JPanel(new GridLayout(1, 2, -2, 0));
		JLabel forum = new JLabel("<html><font color=blue><u>Forums</u></font> -</html>", SwingConstants.RIGHT);
		JLabel git = new JLabel("<html>- <font color=blue><u>GitHub</u></font></html>", SwingConstants.LEFT);
		links.add(forum);
		links.add(git);
		version.add(links);
		version.add(Util.getVersionLabel("FringeRemover", "v1.0"));//XXX the version number - don't forget build.gradle
		//TODO forum.addMouseListener(new ClickableLink("https://osu.ppy.sh/community/forums/topics/xxxxxx"));
		git.addMouseListener(new ClickableLink("https://github.com/RoanH/FringeRemover"));
		
		panel.add(input);
		panel.add(output);
		panel.add(options);
		panel.add(progress);
		panel.add(controls);
		panel.add(version);
		
		frame.add(panel);
		frame.pack();
		frame.setSize(400, panel.getPreferredSize().height + frame.getInsets().top + frame.getInsets().bottom);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
