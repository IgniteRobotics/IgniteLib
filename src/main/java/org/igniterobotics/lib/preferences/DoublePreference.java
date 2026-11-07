package org.igniterobotics.lib.preferences;

import edu.wpi.first.wpilibj.Preferences;

/**
 * Implements RobotPreference for preferences storing double-precision floating point numbers Having
 * an explicity named class helps with readability
 */
public class DoublePreference extends RobotPreference<Double> {
  private double defaultValue;

  /** Default constructor storing an approximately zero value */
  public DoublePreference(String key) {
    this(key, 0.0);
  }

  /**
   * Regular constructor requiring a floating point number value that is to be stored by the
   * preference
   */
  public DoublePreference(String key, double defaultValue) {
    super(key);
    this.defaultValue = defaultValue;

    Preferences.initDouble(key, defaultValue);
  }

  /**
   * Pulls the floating point value tied to the preference. The idea is so that the "key" is only
   * relevant for finding the preference in the UI while the code is "key" independent.
   */
  public double getValue() {
    return Preferences.getDouble(super.key, defaultValue);
  }

  /**
   * Required implementation for using the Supplier interface This makes getValue() appear redunant,
   * but honestly "getValue" is a more intuitive name
   */
  @Override
  public Double get() {
    return getValue();
  }
}
