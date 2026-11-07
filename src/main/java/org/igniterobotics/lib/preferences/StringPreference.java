package org.igniterobotics.lib.preferences;

import edu.wpi.first.wpilibj.Preferences;

/**
 * Implements RobotPreference for preferences storing string values Having an explicity named class
 * helps with readability
 */
public class StringPreference extends RobotPreference<String> {
  private String defaultValue;

  /** Default constructor storing an empty string */
  public StringPreference(String key) {
    this(key, "");
  }

  /** Regular constructor requiring a string value that is to be stored by the preference */
  public StringPreference(String key, String defaultValue) {
    super(key);
    this.defaultValue = defaultValue;

    Preferences.initString(key, defaultValue);
  }

  /**
   * Pulls the string value tied to the preference. The idea is so that the "key" is only relevant
   * for finding the preference in the UI while the code is "key" independent.
   */
  public String getValue() {
    return Preferences.getString(super.key, defaultValue);
  }

  /**
   * Required implementation for using the Supplier interface This makes getValue() appear redunant,
   * but honestly "getValue" is a more intuitive name
   */
  @Override
  public String get() {
    return getValue();
  }
}
