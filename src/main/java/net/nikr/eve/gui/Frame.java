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
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import net.nikr.eve.Program;
import net.nikr.eve.io.DataWriter;
import net.nikr.eve.io.creator.Creator;
import net.nikr.eve.io.creator.Creators;


public class Frame extends JFrame implements WindowListener, ActionListener  {
  private static final long serialVersionUID = 1l;

	public final static String ACTION_RUN = "ACTION_RUN";

	//GUI
	private JPanel jMainPanel;
	private JButton jRun;
	private ProgressBar jProgressBar;
  List<CreatorSection> creatorSections = new ArrayList<CreatorSection>();

	//Data
	private Connection con;
	
	public Frame(Connection con){
		this.con = con;
    
    setLayout(new BorderLayout(4, 4));

		//Main Panel
    jMainPanel = new JPanel();
    jMainPanel.setLayout(new GridLayout(Creators.values().length, 2, 5, 5));

    for (Creators creator : Creators.values()) {
      CreatorSection cs = new CreatorSection(creator.getCreator());
      creatorSections.add(cs);
      jMainPanel.add(cs.getLeft());
      jMainPanel.add(cs.getRight());
    }

    Box bottom = Box.createHorizontalBox();
		jProgressBar = new ProgressBar();
		jProgressBar.setEnabled(false);
		bottom.add(jProgressBar);
    
    bottom.add(Box.createHorizontalStrut(5));

		jRun = new JButton("Run");
		jRun.setActionCommand(ACTION_RUN);
		jRun.addActionListener(this);
		bottom.add(jRun);

    jMainPanel.setAlignmentX(CENTER_ALIGNMENT);
    jMainPanel.setAlignmentY(CENTER_ALIGNMENT);
    add(jMainPanel, BorderLayout.CENTER);
    add(bottom, BorderLayout.SOUTH);

		//Frame
		this.setTitle(Program.PROGRAM_NAME);
		this.addWindowListener(this);
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
      for (CreatorSection cs : creatorSections) {
        if (cs.isSelected()) {
          creatorList.add(cs.getCreator());
        }
      }
			DataWriter dataWriter = new DataWriter(this, creatorList, con);
      dataWriter.addProgressMonitor(jProgressBar);
			dataWriter.start();
		}
	}
	public void startRun(){
		setAllEnabled(false);
	}
	public void endRun(){
		setAllEnabled(true);
	}
	private void setAllEnabled(boolean b){
    for (CreatorSection cs : creatorSections) {
      cs.getRight().setEnabled(b);
    }
		jRun.setEnabled(b);
		jProgressBar.setEnabled(!b);
	}

  static class CreatorSection {
    private static final long serialVersionUID = 1l;
    Creator creator;
    JLabel label;
    JCheckBox checkbox;

    public Creator getCreator() {
      return creator;
    }

    public boolean isSelected() {
      return checkbox.isSelected();
    }

    public JComponent getLeft() {
      return label;
    }

    public JComponent getRight() {
      return checkbox;
    }

    public CreatorSection(Creator creator) {
      this.creator = creator;

      label = new JLabel(creator.getName());
      label.setHorizontalAlignment(JLabel.TRAILING);
      checkbox = new JCheckBox();
      checkbox.setAlignmentX(CENTER_ALIGNMENT);
    }
  }

}