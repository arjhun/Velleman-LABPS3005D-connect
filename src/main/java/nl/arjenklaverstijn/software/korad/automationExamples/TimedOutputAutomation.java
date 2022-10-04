/**
 * 
 */
package nl.arjenklaverstijn.software.korad.automationExamples;

import nl.arjenklaverstijn.software.korad.Automation;
import nl.arjenklaverstijn.software.korad.LabPSU3005D;

/**
 * @author arjen
 *
 */
public class TimedOutputAutomation extends Automation {

  private float voltage;
  private float currentLimit;
  private int durationMillis;

  /**
   * 
   */
  public TimedOutputAutomation(float voltage, float currentLimit, int durationMillis) {
    this.voltage = voltage;
    this.currentLimit = currentLimit;
    this.durationMillis = durationMillis;
  }

  public void job(LabPSU3005D device) {

    device.setVoltage(voltage);
    device.setCurrent(currentLimit);
    device.setEnabled(true);

    long start = System.currentTimeMillis();
    while ((System.currentTimeMillis() - start) < durationMillis);

  }

  public static void main(String[] args) {

  }

}
