/*
 * Copyright 2011 Niklas Kyster Rasmussen
 * 
 * This file is part of WotXP.
 *
 * WotXP is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * WotXP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with WotXP; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */

package net.nikr.eve;

import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ExceptionHandler implements Thread.UncaughtExceptionHandler {

	private final static Logger LOG = LoggerFactory.getLogger(ExceptionHandler.class);
	private final static String NEW_LINE = System.getProperty("line.separator");
	
	private static String message = "";
	private static boolean error = false;
	private static boolean gui = false;
	
	public static void install(String message, boolean gui){
		ExceptionHandler.message = message;
		ExceptionHandler.gui = gui;
		System.setProperty("sun.awt.exception.handler", ExceptionHandler.class.getName());
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
	}
	
	private void log(String msg, Throwable e){
		LOG.info("Program "+Program.PROGRAM_NAME+" "+Program.PROGRAM_VERSION);
		LOG.info("OS: "+System.getProperty("os.name")+" "+System.getProperty("os.version"));
		LOG.info("Java: "+System.getProperty("java.vendor")+" "+System.getProperty("java.version"));
		LOG.error(msg, e);
		System.out.println("");
		System.out.println("");
		System.out.println(message+"\r\n"+e.getMessage());
		System.out.println("");
		if (gui) JOptionPane.showMessageDialog(null, message+"\r\n"+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
	}

	private ExceptionHandler() { }

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		if (!error){
			error = true;
			log("Uncaught Exception (Thread):", e);
			System.exit(-1);
		}
	}
	
	public void handle(Throwable e){
		if (!error){
			error = true;
			log("Uncaught Exception (sun.awt.exception.handler):", e);
			System.exit(-1);
		}
	}
}
