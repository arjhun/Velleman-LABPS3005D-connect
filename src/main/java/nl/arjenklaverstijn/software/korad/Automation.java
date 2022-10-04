/**
 * 
 */
package nl.arjenklaverstijn.software.korad;

/**
 * @author arjen
 *
 */
public abstract class Automation {
  private LabPSU3005D device;

  public void before() {
    device.openPort();
    device.setCurrent(0);
    device.setVoltage(0);
    device.setEnabled(false);
  }

  public void beforeRun() {

  }

  public abstract void job(LabPSU3005D device);

  public void afterRun() {};

  public void after() {
    device.setEnabled(false);
    device.setCurrent(0);
    device.setVoltage(0);
    device.closePort();
  };

  public void cancel() {
    device.setEnabled(false);
    device.closePort();
  }

  public LabPSU3005D getDevice() {
    return device;
  }

  public final long run(LabPSU3005D device) {
    long start = System.currentTimeMillis();
    this.device = device;
    this.before();
    this.beforeRun();
    this.job(device);
    this.afterRun();
    this.after();
    return System.currentTimeMillis() - start;
  }


}
