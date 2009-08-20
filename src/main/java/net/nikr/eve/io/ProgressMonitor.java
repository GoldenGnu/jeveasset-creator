package net.nikr.eve.io;

// <editor-fold defaultstate="collapsed" desc="imports">
// </editor-fold>
/**
 *
 * @author Andrew Wheat
 */
public interface ProgressMonitor {
  void setMaximum(int max);
  void setValue(int current);
  void setMinimum(int min);
  void setIndeterminate(boolean indeterminate);
}
