package org.igniterobotics.lib.preferences;

import edu.wpi.first.wpilibj.Preferences;
import java.util.Collection;
import java.util.function.Supplier;

public abstract class RobotPreference<T> implements Supplier<T> {
  protected String key;

  public RobotPreference(String key) {
    this.key = key;
  }

  public void remove() {
    Preferences.remove(key);
  }

  public static void removeAll() {
    Preferences.removeAll();
  }

  public static Collection<String> getKeys() {
    return Preferences.getKeys();
  }
}
