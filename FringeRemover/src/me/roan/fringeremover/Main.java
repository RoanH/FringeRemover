package me.roan.fringeremover;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import me.roan.util.ClickableLink;
import me.roan.util.Dialog;
import me.roan.util.FileSelector;
import me.roan.util.FileSelector.FileExtension;
import me.roan.util.FileTextField;
import me.roan.util.Util;

public class Main{
	private static final FileExtension PNG_EXTENSION = FileSelector.registerFileExtension("PNG", "png");
	
	public static void main(String[] args){
		Util.installUI();
		
		JFrame frame = new JFrame("Fringe Remover");
		Dialog.setDialogTitle("Fringe Remover");
		Dialog.setParentFrame(frame);
		//TODO set icon
		
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
		JCheckBox parseSubDir = new JCheckBox("Parse subdirectories", false);//TODO disable if input is file
		input.add(parseSubDir, BorderLayout.PAGE_END);
		
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
		JCheckBox overwrite = new JCheckBox("Overwrite existing files", false);//TODO default value
		output.add(overwrite, BorderLayout.PAGE_END);
		
		//TODO only sync if not edited yet?
		inputField.setListener(path->{
			outputField.setText(path);
		});
		
		saveLoc.addActionListener(e->{
			File selected = Dialog.showFolderOpenDialog();
			outputField.setText(selected == null ? null : selected.getAbsolutePath());
		});
		
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
