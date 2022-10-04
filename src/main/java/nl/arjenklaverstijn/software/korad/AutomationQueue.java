package nl.arjenklaverstijn.software.korad;

import java.util.LinkedList;

public class AutomationQueue extends LinkedList<Automation> {

  private LinkedList<Automation> queue;

  public long run(LabPSU3005D device) {
    long start = System.currentTimeMillis();
    while (!queue.isEmpty()) {
      Automation automation = queue.pop();
      automation.run(device);
    }
    return System.currentTimeMillis() - start;

  }

}
