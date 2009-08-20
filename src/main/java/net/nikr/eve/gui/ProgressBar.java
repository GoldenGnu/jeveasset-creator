package net.nikr.eve.gui;

// <editor-fold defaultstate="collapsed" desc="imports">

import net.nikr.eve.io.ProgressMonitor;
import javax.swing.JProgressBar;

// </editor-fold>
/**
 * added the ProgressMonitor interface so that it can be passed to non-gui classes as a progress monitor.
 * @author Andrew Wheat
 */
public class ProgressBar extends JProgressBar implements ProgressMonitor {
  private static final long serialVersionUID = 1l;
}
