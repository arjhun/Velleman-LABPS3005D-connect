/**
 * 
 */
package nl.arjenklaverstijn.software.korad.automationExamples;

import nl.arjenklaverstijn.software.korad.AutomationQueue;
import nl.arjenklaverstijn.software.korad.LabPSU3005D;

/**
 * @author arjen
 *
 */
public class AutomationQueueExample {

  public static void main(String[] args) {

    LabPSU3005D labPSU = new LabPSU3005D("COM4");
    AutomationQueue automationQueue = new AutomationQueue();

    automationQueue.add(new TimedOutputAutomation(5.0F, 2F, 2000));
    automationQueue.add(new TimedOutputAutomation(7.0F, 4F, 4000));
    automationQueue.add(new TimedOutputAutomation(9.0F, 6F, 6000));
    automationQueue.add(new TimedOutputAutomation(11.0F, 7F, 7000));
    automationQueue.add(new TimedOutputAutomation(13.0F, 10F, 10000));
    automationQueue.add(new LiniairSweepAutomation(1, 5, .2F, 5000));

    long time = automationQueue.run(labPSU);

    System.out.println("It took " + time / 1000 + "s to finish all experiments!");
  }

}
