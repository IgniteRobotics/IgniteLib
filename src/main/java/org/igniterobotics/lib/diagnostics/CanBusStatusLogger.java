package org.igniterobotics.lib.diagnostics;

import com.ctre.phoenix6.CANBus;
import edu.wpi.first.util.datalog.DataLog;
import edu.wpi.first.util.datalog.DoubleLogEntry;
import edu.wpi.first.util.datalog.IntegerLogEntry;
import edu.wpi.first.wpilibj.DataLogManager;
import edu.wpi.first.wpilibj.Notifier;
import edu.wpi.first.wpilibj.RobotController;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Logs CAN bus health for the roboRIO bus and each CANivore to the wpilog, under {@code
 * /Flashpoint/CANBus/<bus>/}: {@code Utilization} (0 to 1), {@code BusOffCount}, {@code
 * TxFullCount}, {@code ReceiveErrorCount}, {@code TransmitErrorCount}, and {@code Status} (the
 * Phoenix status of the read; CANivores only). Flashpoint uses these to tell a noisy or
 * badly-terminated bus (rising error counts) from a single device dropping out.
 *
 * <p>Samples at 10 Hz on a {@link Notifier} thread, so it never runs in the robot loop. Entries are
 * written only when a value changes. Call {@link #start} once after {@code DataLogManager.start()}.
 * It never throws into robot code.
 */
public final class CanBusStatusLogger {
  public static final String ENTRY_PREFIX = "/Flashpoint/CANBus/";
  public static final double PERIOD_S = 0.1;

  private static final List<BusEntries> buses = new ArrayList<>();
  private static Notifier notifier;

  private CanBusStatusLogger() {}

  /** Starts logging the roboRIO bus and the given CANivores. Safe to call more than once. */
  public static synchronized void start(CANBus... canivores) {
    if (notifier != null) {
      return;
    }
    DataLog log = DataLogManager.getLog();
    buses.add(
        new BusEntries(
            log, "rio", () -> CanBusSample.fromRio(RobotController.getCANStatus()), false));
    for (CANBus bus : canivores) {
      buses.add(
          new BusEntries(
              log, bus.getName(), () -> CanBusSample.fromCanivore(bus.getStatus()), true));
    }
    notifier = new Notifier(CanBusStatusLogger::sample);
    notifier.setName("CanBusStatusLogger");
    notifier.startPeriodic(PERIOD_S);
  }

  private static synchronized void sample() {
    long now = RobotController.getFPGATime();
    for (BusEntries bus : buses) {
      try {
        bus.record(bus.reader.get(), now);
      } catch (RuntimeException e) {
        // A failed read is skipped; the next period tries again.
      }
    }
  }

  private static final class BusEntries {
    private final Supplier<CanBusSample> reader;
    private final DoubleLogEntry utilization;
    private final IntegerLogEntry busOff;
    private final IntegerLogEntry txFull;
    private final IntegerLogEntry receiveErrors;
    private final IntegerLogEntry transmitErrors;
    private final IntegerLogEntry status;

    BusEntries(DataLog log, String name, Supplier<CanBusSample> reader, boolean hasStatus) {
      String prefix = ENTRY_PREFIX + name + "/";
      this.reader = reader;
      utilization = new DoubleLogEntry(log, prefix + "Utilization");
      busOff = new IntegerLogEntry(log, prefix + "BusOffCount");
      txFull = new IntegerLogEntry(log, prefix + "TxFullCount");
      receiveErrors = new IntegerLogEntry(log, prefix + "ReceiveErrorCount");
      transmitErrors = new IntegerLogEntry(log, prefix + "TransmitErrorCount");
      status = hasStatus ? new IntegerLogEntry(log, prefix + "Status") : null;
    }

    void record(CanBusSample sample, long timestamp) {
      utilization.update(sample.utilization(), timestamp);
      busOff.update(sample.busOffCount(), timestamp);
      txFull.update(sample.txFullCount(), timestamp);
      receiveErrors.update(sample.receiveErrorCount(), timestamp);
      transmitErrors.update(sample.transmitErrorCount(), timestamp);
      if (status != null) {
        status.update(sample.statusCode(), timestamp);
      }
    }
  }
}
