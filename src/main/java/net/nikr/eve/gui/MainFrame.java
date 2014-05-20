/*
 * Copyright 2009, Niklas Kyster Rasmussen, Flaming Candle
 *
 * This file is part of XML Creator for jEveAssets
 *
 * XML Creator for jEveAssets is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * XML Creator for jEveAssets is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with XML Creator for jEveAssets; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */

package net.nikr.eve.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import net.nikr.eve.io.sql.ConnectionData;
import net.nikr.eve.Program;
import net.nikr.eve.io.DataWriter;
import net.nikr.eve.io.creator.Creator;
import net.nikr.eve.io.creator.Creators;
import net.nikr.eve.io.ftp.FtpData;
import net.nikr.eve.io.ftp.FtpWriter;


public class MainFrame extends JFrame implements WindowListener, ActionListener	{
	private static final long serialVersionUID = 1l;

	public final static String ACTION_RUN = "ACTION_RUN";
	public final static String CHECK_ALL = "CHECK_ALL";
	public final static String CHECK = "CHECK";

	//GUI
	private final JPanel jPanel;
	private final JButton jRun;
	private final JCheckBox jAll;
	private final ProgressBar jProgressBar;
	List<CreatorSection> creatorSections = new ArrayList<CreatorSection>();

	//Data
	private final ConnectionData connectionData;
	private final FtpData ftpData;
	
	public MainFrame(ConnectionData connectionData, FtpData ftpData){
		this.connectionData = connectionData;
		this.ftpData = ftpData;
		setLayout(new BorderLayout(1, 4));
		
		//Main Panel
		jPanel = new JPanel();
		jPanel.setLayout(new GridLayout(Creators.values().length+1, 2, 5, 5));

		GroupLayout layout = new GroupLayout(jPanel);
		jPanel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		ParallelGroup horizontalGroup = layout.createParallelGroup();
		SequentialGroup verticalGroup = layout.createSequentialGroup();

		jAll = new JCheckBox("All");
		jAll.setActionCommand(CHECK_ALL);
		jAll.addActionListener(this);

		horizontalGroup.addComponent(jAll);
		verticalGroup.addComponent(jAll, 30, 30, 30);

		JSeparator jSeparator1 = new JSeparator(JSeparator.HORIZONTAL);
		horizontalGroup.addComponent(jSeparator1);
		verticalGroup.addComponent(jSeparator1, 5, 5, 5);

		for (Creators creator : Creators.values()) {
			CreatorSection cs = new CreatorSection(creator.getCreator());
			creatorSections.add(cs);
			cs.getCheckBox().setActionCommand(CHECK);
			cs.getCheckBox().addActionListener(this);
			horizontalGroup.addComponent(cs.getCheckBox());
			verticalGroup.addComponent(cs.getCheckBox(), 30, 30, 30);
		}

		JSeparator jSeparator2 = new JSeparator(JSeparator.HORIZONTAL);
		horizontalGroup.addComponent(jSeparator2);
		verticalGroup.addComponent(jSeparator2, 5, 5, 5);

		jProgressBar = new ProgressBar();
		jProgressBar.setEnabled(false);


		jRun = new JButton("Run");
		jRun.setActionCommand(ACTION_RUN);
		jRun.addActionListener(this);

		horizontalGroup.addGroup(layout.createSequentialGroup()
				.addComponent(jProgressBar, 250, 250, 250)
				.addComponent(jRun)
		);
		verticalGroup.addGroup(layout.createParallelGroup()
				.addComponent(jProgressBar, 25, 25, 25)
				.addComponent(jRun, 25, 25, 25)
		);

		layout.setHorizontalGroup(horizontalGroup);
		layout.setVerticalGroup(verticalGroup);

		//Frame
		this.getContentPane().add(jPanel);
		this.setTitle(Program.PROGRAM_NAME);
		this.addWindowListener(this);
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();
	}
	
	@Override
	public void windowOpened(WindowEvent e) {}

	@Override
	public void windowClosing(WindowEvent e) {}

	@Override
	public void windowClosed(WindowEvent e) {}

	@Override
	public void windowIconified(WindowEvent e) {}

	@Override
	public void windowDeiconified(WindowEvent e) {}

	@Override
	public void windowActivated(WindowEvent e) {}

	@Override
	public void windowDeactivated(WindowEvent e) {}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (ACTION_RUN.equals(e.getActionCommand())){
			List<Creator> creatorList = new ArrayList<Creator>();
			List<CreatorSection> sections = new ArrayList<CreatorSection>();
			for (CreatorSection cs : creatorSections) {
				if (cs.isSelected()) {
					creatorList.add(cs.getCreator());
					sections.add(cs);
				}
			}
			DataWriter dataWriter = new DataWriter(this, creatorList, sections, connectionData);
			dataWriter.addProgressMonitor(jProgressBar);
			dataWriter.start();
		}
		if (CHECK.equals(e.getActionCommand())){
			boolean selected = true;
			for (CreatorSection cs : creatorSections) {
				if (!cs.getCheckBox().isSelected()){
					selected = false;
					break;
				}
			}
			jAll.setSelected(selected);
		}
		if (CHECK_ALL.equals(e.getActionCommand())){
			for (CreatorSection cs : creatorSections) {
				cs.getCheckBox().setSelected(jAll.isSelected());
			}
		}
	}
	public void startRun(){
		setAllEnabled(false);
	}
	public void endRun(){
		setAllEnabled(true);
		StringBuilder builder = new StringBuilder();
		builder.append("<html>");
		for (CreatorSection cs : creatorSections) {
			if (cs.isSelected()) {
				String rgb = Integer.toHexString(cs.getCheckBox().getForeground().getRGB());
				rgb = rgb.substring(2, rgb.length());
				builder.append("<font color=\"");
				builder.append(rgb);
				builder.append("\">");
				builder.append(cs.getCreator().getName());
				builder.append("</font>");
				builder.append("<br>");
			}
		}
		JOptionPane.showMessageDialog(this, builder.toString(), "Done", JOptionPane.INFORMATION_MESSAGE);
		for (CreatorSection cs : creatorSections) {
			cs.getCheckBox().setForeground(Color.BLACK);
		}
		int value = JOptionPane.showConfirmDialog(this, "Upload via FTP?", "Update", JOptionPane.OK_CANCEL_OPTION);
		if (value == JOptionPane.OK_OPTION) {
			setAllEnabled(false);
			FtpWriter.upload(ftpData, jProgressBar);
			setAllEnabled(true);
		}
	}
	private void setAllEnabled(boolean b){
		for (CreatorSection cs : creatorSections) {
			cs.getCheckBox().setEnabled(b);
		}
		jAll.setEnabled(b);
		jRun.setEnabled(b);
		jProgressBar.setEnabled(!b);
	}

	public static class CreatorSection {
		private static final long serialVersionUID = 1l;
		private final Creator creator;
		private final JCheckBox jCheckbox;

		public Creator getCreator() {
			return creator;
		}

		public boolean isSelected() {
			return jCheckbox.isSelected();
		}

		public JCheckBox getCheckBox() {
			return jCheckbox;
		}

		public CreatorSection(Creator creator) {
			this.creator = creator;
			jCheckbox = new JCheckBox(creator.getName());
		}
	}

}