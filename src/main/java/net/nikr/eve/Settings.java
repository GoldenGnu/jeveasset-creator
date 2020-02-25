/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.nikr.eve;

/**
 *
 * @author nkr
 */
public class Settings {
	private static boolean failOnOutdated = false;
	private static boolean failOnCurrent = false;
	private static boolean auto = false;

	public static boolean isFailOnOutdated() {
		return failOnOutdated;
	}

	public static void setFailOnOutdated(boolean failOnOutdated) {
		Settings.failOnOutdated = failOnOutdated;
	}

	public static boolean isFailOnCurrent() {
		return failOnCurrent;
	}

	public static void setFailOnCurrent(boolean failOnCurrent) {
		Settings.failOnCurrent = failOnCurrent;
	}

	public static boolean isAuto() {
		return auto;
	}

	public static void setAuto(boolean auto) {
		Settings.auto = auto;
	}
	
}
