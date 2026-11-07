package org.igniterobotics.lib.preferences;

import edu.wpi.first.wpilibj.Preferences;

/**
 * Implements RobotPreference for preferences storing boolean values Having an explicity named class
 * helps with readability
 */
public class BooleanPreference extends RobotPreference<Boolean> {
  private boolean defaultValue;

  /** Default constructor storing a false boolean */
  public BooleanPreference(String key) {
    this(key, false);
  }

  /** Regular constructor requiring a boolean value that is to be stored by the preference */
  public BooleanPreference(String key, boolean defaultValue) {
    super(key);
    this.defaultValue = defaultValue;

    Preferences.initBoolean(key, defaultValue);
  }

  /**
   * Pulls the boolean value tied to the preference. The idea is so that the "key" is only relevant
   * for finding the preference in the UI while the code is "key" independent.
   */
  public boolean getValue() {
    return Preferences.getBoolean(super.key, defaultValue);
  }

  /**
   * Required implementation for using the Supplier interface This makes getValue() appear redunant,
   * but honestly "getValue" is a more intuitive name
   */
  @Override
  public Boolean get() {
    return getValue();
  }
}
