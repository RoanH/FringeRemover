package me.roan.fringeremover;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import me.roan.fringeremover.Worker.ProgressListener;
import me.roan.util.ClickableLink;
import me.roan.util.Dialog;
import me.roan.util.FileSelector;
import me.roan.util.FileSelector.FileExtension;
import me.roan.util.FileTextField;
import me.roan.util.Util;

/**
 * Program used to alter the color of fully transparent
 * pixels to improve the result of OpenGL image rendering.
 * @author Roan
 */
public class Main{
	/**
	 * File chooser extension filter that allows only PNG files.
	 */
	private static final FileExtension PNG_EXTENSION = FileSelector.registerFileExtension("PNG", "png");
	
	/**
	 * Starts the program and shows the GUI.
	 * @param args No valid command line arguments.
	 */
	public static void main(String[] args){
		Util.installUI();
		
		JFrame frame = new JFrame("Fringe Remover");
		Dialog.setDialogTitle("Fringe Remover");
		Dialog.setParentFrame(frame);
		try{
			Image small = ImageIO.read(ClassLoader.getSystemResourceAsStream("icon_small.png"));
			Dialog.setDialogIcon(small);
			frame.setIconImages(Arrays.asList(
				ImageIO.read(ClassLoader.getSystemResourceAsStream("icon.png")),
				small
			));
		}catch(IOException e1){
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
			if(selected != null){
				inputField.setText(selected.getAbsolutePath());
			}
		});
		
		openFolder.addActionListener(e->{
			File selected = Dialog.showFolderOpenDialog();
			if(selected != null){
				inputField.setText(selected.getAbsolutePath());
			}
		});
		
		JPanel output = new JPanel(new BorderLayout());
		output.setBorder(BorderFactory.createTitledBorder("Output"));
		output.add(new JLabel("Target: "), BorderLayout.LINE_START);
		FileTextField outputField = new FileTextField();
		output.add(outputField, BorderLayout.CENTER);
		JPanel saveButtons = new JPanel(new GridLayout(1, 2));
		JButton saveFile = new JButton("File");
		JButton saveFolder = new JButton("Folder");
		saveButtons.add(saveFile);
		saveButtons.add(saveFolder);
		output.add(saveButtons, BorderLayout.LINE_END);
		
		saveFile.addActionListener(e->{
			File selected = Dialog.showFileSaveDialog(PNG_EXTENSION, inputField.getFile().getName());
			if(selected != null){
				outputField.setText(selected.getAbsolutePath());
			}
		});
		
		saveFolder.addActionListener(e->{
			File selected = Dialog.showFolderOpenDialog();
			if(selected != null){
				outputField.setText(selected.getAbsolutePath());
			}
		});
		
		JPanel options = new JPanel(new GridLayout(3, 1));
		options.setBorder(BorderFactory.createTitledBorder("Options"));
		JCheckBox parseSubDir = new JCheckBox("Parse subdirectories", true);
		options.add(parseSubDir);
		JCheckBox overwrite = new JCheckBox("Overwrite existing files", false);
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
			saveFile.setEnabled(enabled);
			saveFolder.setEnabled(enabled);
			parseSubDir.setEnabled(enabled);
			overwrite.setEnabled(enabled);
			threadCount.setEnabled(enabled);
		};
		enableFun.accept(true);
		
		pause.addActionListener((e)->{
			if(Worker.isRunning()){
				Worker.setRunning(false);
				pause.setText("Resume");
			}else{
				Worker.setRunning(true);
				pause.setText("Pause");
			}
		});
		
		start.addActionListener(e->{
			enableFun.accept(false);
			
			Path inputPath = inputField.getFile().toPath();
			Path outputPath = outputField.getFile().toPath();
			
			if(Files.isRegularFile(outputPath) && Files.isDirectory(inputPath)){
				Dialog.showMessageDialog("Cannot save a folder of files to a single output file.");
				enableFun.accept(true);
				return;
			}
			
			try{
				int total = Worker.prepare(inputPath, outputPath, parseSubDir.isSelected(), overwrite.isSelected());
				if(total == 0){
					ptext.setText("No files to process");
					bar.setMaximum(1);
					bar.setValue(1);
					enableFun.accept(true);
					return;
				}
				bar.setMaximum(total);
				bar.setValue(0);
				
				List<String> errors = new ArrayList<String>(0);
				Worker.start((int)threadCount.getValue(), new ProgressListener(){
					@Override
					public void progress(int done){
						bar.setValue(done);
						ptext.setText(done + "/" + total);
						progress.repaint();
					}

					@Override
					public void error(Path file, String error){
						errors.add(file.toString() + ": " + error);						
					}

					@Override
					public void done(){
						if(!errors.isEmpty()){
							JPanel msg = new JPanel(new BorderLayout());
							msg.add(new JLabel("Processing finished with " + (errors.size() == 1 ? "1 error" : (errors.size() + " errors")) + ". These files were consequently skipped:"), BorderLayout.PAGE_START);
							msg.add(new JScrollPane(new JList<String>(errors.toArray(new String[0]))), BorderLayout.CENTER);
							Dialog.showMessageDialog(msg);
						}
						
						enableFun.accept(true);
						pause.setText("Pause");
					}
				});
			}catch(IOException e1){
				e1.printStackTrace();
				Dialog.showErrorDialog("An internal error occurred:\nCause: " + e1.getMessage());
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
		forum.addMouseListener(new ClickableLink("https://osu.ppy.sh/community/forums/topics/1244643"));
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
