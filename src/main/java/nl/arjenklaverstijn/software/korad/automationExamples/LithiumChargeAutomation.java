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
public class LithiumChargeAutomation extends Automation {


  private float cutOffCurrent;
  private float chargeV;
  private float chargeA;

  public LithiumChargeAutomation(float chargeV, float chargeA) {
    this.chargeV = chargeV;
    this.chargeA = chargeA;
    this.cutOffCurrent = 0.1f;
  }

  public LithiumChargeAutomation(float chargeV, float chargeA, float cutOffCurrent) {
    this.chargeV = chargeV;
    this.chargeA = chargeA;
    this.cutOffCurrent = cutOffCurrent;
  }


  @Override
  public void job(LabPSU3005D device) {

    device.setOVP(true);
    device.setOCP(false);
    device.setVoltage(chargeV);
    device.setCurrent(chargeA);
    device.setEnabled(true);

    // Wait a bit, to let the battery voltage settle
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    long start = System.currentTimeMillis();
    long elapsed = 0;

    while (device.getOutputCurrent() > cutOffCurrent) {
      System.out.print("T: " + elapsed);
      System.out.print(" V: " + device.getOutputVoltage());
      System.out.println(" A: " + device.getOutputCurrent());
      elapsed = (System.currentTimeMillis() - start);
    }

    System.out.print("Cut off voltage reached with a current of: " + chargeA + " in "
        + elapsed / 1000 / 60 + " minutes");
  }

  public static void main(String[] args) {

    LabPSU3005D device = new LabPSU3005D("COM4");
    new LithiumChargeAutomation(4.2F, .5f).run(device);

  }

}
