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

package net.nikr.eve;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import net.nikr.log.Log;


public class Main {
	static String[] inputString;
	Program program;
	//DataLoader dataLoader;
	/** Creates a new instance of Main */
	public Main(String[] argumentsString) {
		//Force error here, if any libraries are missing
		program = new Program();
		
	}
	
	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
			inputString = args;
			javax.swing.SwingUtilities.invokeLater(
				new Runnable() {
					@Override
					public void run() {
						createAndShowGUI();
							}
					}
			);
	}
	
	private static void createAndShowGUI() {
		try {
			//Force error here, if the log library is missing
			Class.forName(Log.class.getName());
		} catch (ClassNotFoundException e){
			String s = "The NiKR Log library is missing (lib\\nikr_log.jar)\nPlease see jeveassets_faq.txt for more information.";
			System.err.println(s);
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, s, "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
		Log.init(Main.class, "Please email the latest error.txt in the logs directory to niklaskr@gmail.com");

		initLookAndFeel();

		//Make sure we have nice window decorations.
		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);

		Main main = new Main(inputString);
	}
	
	private static void initLookAndFeel() {
		String lookAndFeel = null;
		//lookAndFeel = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
		lookAndFeel = UIManager.getSystemLookAndFeelClassName(); //System
		//lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName(); //Java
		//lookAndFeel = "javax.swing.plaf.metal.MetalLookAndFeel";
		//lookAndFeel = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel"; //GTK
		//lookAndFeel = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
		try {
			UIManager.setLookAndFeel(lookAndFeel);
		} catch (Exception e) {
			Log.error("failed to set LookAndFeel: "+lookAndFeel, e);
			e.printStackTrace();
		}
	}


}
