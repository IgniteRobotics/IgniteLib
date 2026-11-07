package org.igniterobotics.lib.preferences;

import edu.wpi.first.wpilibj.Preferences;
import java.util.function.Supplier;

/**
 * Superclass of all preference types in IgniteLib Because of its supplier status, the stored value
 * can be changed during runtime by a NetworkTables UI without restarting the code. Effectively, it
 * is a bridge between the running code and the UI.
 */
public abstract class RobotPreference<T> implements Supplier<T> {
  protected String key;

  /** Constructor requires a string that will be used to identify the preference in the UI. */
  public RobotPreference(String key) {
    this.key = key;
  }

  /**
   * Deletes the preference in Network Tables. Usually never utilized as when a preference needs to
   * be replaced, it is just removed from the code.
   */
  public void remove() {
    Preferences.remove(key);
  }
}
