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
public class LiniairSweepAutomation extends Automation {


  private int durationMillis;
  private float currentLimit;
  private float toVoltage;
  private float fromVoltage;

  public LiniairSweepAutomation(float fromVoltage, float toVoltage, float currentLimit,
      int durationMillis) {
    this.fromVoltage = fromVoltage;
    this.toVoltage = toVoltage;
    this.currentLimit = currentLimit;
    this.durationMillis = durationMillis;
  }

  @Override
  public void job(LabPSU3005D device) {

    device.setVoltage(fromVoltage);
    device.setCurrent(currentLimit);
    device.setEnabled(true);

    float currentV = fromVoltage;
    int minStepDuration = 150;
    int totalSteps = durationMillis / minStepDuration;
    int stepsTaken = 0;
    long startExp = System.currentTimeMillis();
    long elapsed = 0;

    while (stepsTaken <= totalSteps) {
      long start = System.currentTimeMillis();
      device.setVoltage(currentV);
      stepsTaken++;
      System.out.print("T: " + elapsed);
      System.out.print(" V: " + device.getOutputVoltage());
      System.out.println(" A: " + device.getOutputCurrent());
      elapsed = (System.currentTimeMillis() - startExp);
      long took = ((System.currentTimeMillis() - start));
      long timeLeft = minStepDuration - took;
      if (timeLeft > 0 && timeLeft < minStepDuration && stepsTaken <= totalSteps) {
        try {
          Thread.sleep(timeLeft);
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
      currentV += ((toVoltage - fromVoltage) / totalSteps);
    }
    System.out.println("Experiment done in: " + elapsed / 1000 + " Seconds");

  }

  public static void main(String[] args) {
    LabPSU3005D labPSU = new LabPSU3005D("COM4");
    new LiniairSweepAutomation(1, 5, .6F, 20000).run(labPSU);

  }

}
