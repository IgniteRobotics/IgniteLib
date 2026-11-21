package org.igniterobotics.lib.diagnostics;

import com.ctre.phoenix6.CANBus;
import edu.wpi.first.hal.can.CANStatus;

/**
 * One CAN bus health reading. Utilization is a fraction (0 to 1); the counts are the controller's
 * own counters. {@code statusCode} is the Phoenix status of the read (0 is OK; always 0 for the
 * roboRIO bus).
 */
public record CanBusSample(
    double utilization,
    long busOffCount,
    long txFullCount,
    long receiveErrorCount,
    long transmitErrorCount,
    long statusCode) {

  /** The roboRIO bus, from {@code RobotController.getCANStatus()}. */
  public static CanBusSample fromRio(CANStatus status) {
    return new CanBusSample(
        status.percentBusUtilization,
        status.busOffCount,
        status.txFullCount,
        status.receiveErrorCount,
        status.transmitErrorCount,
        0);
  }

  /** A CANivore, from {@code CANBus.getStatus()}. */
  public static CanBusSample fromCanivore(CANBus.CANBusStatus status) {
    return new CanBusSample(
        status.BusUtilization,
        status.BusOffCount,
        status.TxFullCount,
        status.REC,
        status.TEC,
        status.Status == null ? -1 : status.Status.value);
  }
}
