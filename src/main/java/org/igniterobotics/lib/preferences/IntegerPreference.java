package org.igniterobotics.lib.preferences;

import edu.wpi.first.wpilibj.Preferences;

/**
 * Implements RobotPreference for preferences storing 32-bit integer values Having an explicity
 * named class helps with readability
 */
public class IntegerPreference extends RobotPreference<Integer> {
  private int defaultValue;

  /** Default constructor storing the zero integer */
  public IntegerPreference(String key) {
    this(key, 0);
  }

  /** Regular constructor requiring an integer value that is to be stored by the preference */
  public IntegerPreference(String key, int defaultValue) {
    super(key);
    this.defaultValue = defaultValue;

    Preferences.initInt(key, defaultValue);
  }

  /**
   * Pulls the integer value tied to the preference. The idea is so that the "key" is only relevant
   * for finding the preference in the UI while the code is "key" independent.
   */
  public int getValue() {
    return Preferences.getInt(super.key, defaultValue);
  }

  /**
   * Required implementation for using the Supplier interface This makes getValue() appear redunant,
   * but honestly "getValue" is a more intuitive name
   */
  @Override
  public Integer get() {
    return getValue();
  }
}
