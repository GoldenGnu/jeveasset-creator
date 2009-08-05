/*
 * Copyright 2009, Niklas Kyster Rasmussen
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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.Connection;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import net.nikr.eve.Program;
import net.nikr.eve.io.DataWriter;


public class Frame extends JFrame implements WindowListener, ActionListener  {

	public static final int WINDOW_WIDTH = 300;
	public static final int WINDOW_HEIGHT = 120;
	public final static String ACTION_RUN = "ACTION_RUN";

	//GUI
	private JPanel jMainPanel;
	private JCheckBox jLocations;
	private JCheckBox jItems;
	private JButton jRun;
	private JLabel jLocationLabel;
	private JLabel jItemsLabel;
	private JProgressBar jProgressBar;

	



	//Data
	private Connection con;
	
	public Frame(Connection con){
		this.con = con;


		//Main Panel
		jMainPanel = new JPanel();
		GroupLayout layout = new GroupLayout(jMainPanel);
		jMainPanel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		
		jLocations = new JCheckBox("locations.xml");
		jMainPanel.add(jLocations);

		jItems = new JCheckBox("items.xml");
		jMainPanel.add(jItems);

		jRun = new JButton("Run");
		jRun.setActionCommand(ACTION_RUN);
		jRun.addActionListener(this);
		jMainPanel.add(jRun);
		
		jLocationLabel = new JLabel();
		jMainPanel.add(jLocationLabel);

		jItemsLabel = new JLabel();
		jMainPanel.add(jItemsLabel);

		jProgressBar = new JProgressBar();
		jProgressBar.setEnabled(false);
		jMainPanel.add(jProgressBar);

		layout.setHorizontalGroup(
			layout.createParallelGroup()
				.addGroup(layout.createSequentialGroup()
					.addComponent(jLocations)
					.addComponent(jLocationLabel, 50, 50, 50)
					.addComponent(jItems)
					.addComponent(jItemsLabel, 50, 50, 50)
				)
				.addGroup(layout.createSequentialGroup()
					.addComponent(jProgressBar)
					.addComponent(jRun)
				)
		);
		layout.setVerticalGroup(
			layout.createSequentialGroup()

				.addGroup(layout.createParallelGroup()
					.addComponent(jLocations, 22, 22, 22)
					.addComponent(jLocationLabel, 22, 22, 22)
					.addComponent(jItems, 22, 22, 22)
					.addComponent(jItemsLabel, 22, 22, 22)
				)
				.addGroup(layout.createParallelGroup()
					.addComponent(jProgressBar, 22, 22, 22)
					.addComponent(jRun, 22, 22, 22)
				)
		);

		//Frame
		this.setTitle(Program.PROGRAM_NAME);
		this.setSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT)); //800, 600
		this.addWindowListener(this);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.getContentPane().add(jMainPanel);
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

	public void actionPerformed(ActionEvent e) {
		if (ACTION_RUN.equals(e.getActionCommand())){
			DataWriter dataWriter = new DataWriter(this, jLocations.isSelected(), jItems.isSelected(), con);
			dataWriter.start();
		}
	}
	public void startRun(){
		setAllEnabled(false);
		jProgressBar.setValue(0);
		jProgressBar.setIndeterminate(true);
		jLocationLabel.setText("");
		jItemsLabel.setText("");
	}
	public void endRun(String locationsText, String itemsText){
		setAllEnabled(true);
		jProgressBar.setIndeterminate(false);
		jProgressBar.setValue(0);
		jLocationLabel.setText(locationsText);
		jItemsLabel.setText(itemsText);
	}
	private void setAllEnabled(boolean b){
		jLocations.setEnabled(b);
		jItems.setEnabled(b);
		jRun.setEnabled(b);
		jProgressBar.setEnabled(!b);
	}

}