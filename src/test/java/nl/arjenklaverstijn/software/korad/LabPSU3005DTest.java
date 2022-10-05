/**
 * 
 */
package nl.arjenklaverstijn.software.korad;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import nl.arjenklaverstijn.software.korad.LabPSU3005D.MODE;

/**
 * @author Arjen Klaverstijn
 *
 */

public class LabPSU3005DTest {

  // current test settings
  private static final float TEST_RESISTOR = 6.8F; // I used a 6.8Ω 5W power resistor from my desk.
                                                   // You can use any low value resistor if it's
                                                   // within the right range.
  private static final float TEST_VOLTAGE = 3.3F;
  private static final float TEST_CURRENTLIMIT = .2F;

  private LabPSU3005D device;

  @Before
  public void setUp() throws Exception {

    device = new LabPSU3005D("COM4");
    device.setDebug(true);
    device.openPort();
  }


  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    device.setVoltage(0);
    device.setCurrent(0);
    device.setEnabled(false);
    device.closePort();
  }

  @Test
  public void connectionOpen() {
    assertTrue("We should have a connection", device.isOpen());
  }

  @Test
  public void connectionClosed() {
    device.closePort();
    assertTrue("We should not have a connection", !device.isOpen());
  }


  @Test
  public void testDeviceID() {
    assertEquals("The device id should be brand[model] VELLEMAN[LABPS3005DV2.0]",
        "VELLEMANLABPS3005DV2.0", device.getId());
  }

  @Test
  public void testSettingVoltage() {
    device.setVoltage(12.34f);
    assertEquals("The output should be the same as the setting", 12.34f, device.getSetVoltage(), 0);
  }

  @Test
  public void testSettingCurrent() {
    device.setCurrent(1.234f);
    assertEquals("The output should be the same as the setting", 1.234F, device.getSetCurrent(), 0);
  }

  @Test
  public void testReadingTargetOutput() {
    device.setVoltage(10.00F);
    device.setCurrent(6F);
    device.setEnabled(false);
    assertEquals(
        "The output should return a voltage that corresponds to resistor used and limit set", 10F,
        device.getSetVoltage(), 0);
    assertEquals("The output should return a current that corresponds to resistor used", 5.1F,
        device.getSetCurrent(), 0);
  }

  @Test
  public void testReadingTrueOutput() {
    device.setVoltage(TEST_VOLTAGE);
    device.setCurrent(TEST_CURRENTLIMIT);
    device.setEnabled(true);
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    // A = V/R
    assertEquals("The output should return a current that corresponds to resistor used",
        TEST_CURRENTLIMIT, device.getOutputCurrent(), TEST_CURRENTLIMIT * .1);
    // V = I*R
    assertEquals(
        "The output should return a voltage that corresponds to resistor used and limit set",
        TEST_CURRENTLIMIT * TEST_RESISTOR, device.getOutputVoltage(),
        TEST_CURRENTLIMIT * TEST_RESISTOR * .1);
  }

  @Test
  public void testSettingVoltageLowerThanMaxVoltage() {
    device.setVoltage(50f);
    assertEquals("The output should be 31 because that is the max!", 31.00F, device.getSetVoltage(),
        0);
  }

  @Test
  public void testSettingVoltageHigherThanMinVoltage() {
    device.setVoltage(-1f);
    assertEquals("The output should be 0 because that is the min!", 0F, device.getSetVoltage(), 0);
  }


  @Test
  public void testSettingCurrentLowerThanMinCurrent() {
    device.setCurrent(-1f);
    assertEquals("The output should be 0 because that is the min!", 0F, device.getSetCurrent(), 0);
  }

  @Test
  public void testSettingCurrentHigherThanMaxCurrent() {
    device.setCurrent(10f);
    assertEquals("The output should be 5.1A because that is the max!", 5.100F,
        device.getSetCurrent(), 0);
  }

  @Test
  public void testOCP() throws Exception {
    device.setOCP(false);
    assertFalse("OCP should be off", device.getStatus().getOCP());
    device.setOCP(true);
    assertTrue("OCP should be on", device.getStatus().getOCP());
    device.setOCP(false);
    assertFalse("OCP should be off", device.getStatus().getOCP());

  }

  @Test
  public void testOut() throws Exception {
    device.setCurrent(0);
    device.setVoltage(0);
    device.setEnabled(false);
    assertFalse("Out should be off", device.getStatus().getOutput());
    device.setEnabled(true);
    assertTrue("Out should be on", device.getStatus().getOutput());
    device.setEnabled(false);
    assertFalse("Out should be off", device.getStatus().getOutput());
    device.setEnabled(true);
    assertTrue("Out should be on", device.getStatus().getOutput());
  }

  @Test
  public void SetRecalMemoryTest() throws Exception {
    String message = "Recalled values should corespond to value stored";
    for (int i = 1; i <= 5; i++) {
      device.savePreset(i, 11.23F, 1.100F);
    }
    for (int i = 0; i <= 5; i++) {
      device.recalPreset(i);
      assertEquals(message, 11.23F, device.getSetVoltage(), 0);
      assertEquals(message, 1.100F, device.getSetCurrent(), 0);
    }
  }

  @Test
  public void getChannelMode() throws Exception {
    device.setVoltage(3.3F);
    device.setCurrent(.1F);
    device.setEnabled(true);
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    assertEquals("device should be in CC mode", device.getStatus(1).getMode(), MODE.CC);
    device.setVoltage(3.3F);
    device.setCurrent(1F);
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    assertEquals("device should be in CC mode", device.getStatus(1).getMode(), MODE.CV);
  }

  @Rule()
  public TestWatcher watchman = new TestWatcher() {
    @Override
    protected void failed(Throwable e, Description description) {
      System.out.println(description.toString());
      device.setVoltage(0);
      device.setCurrent(0);
      device.closePort();
    }

    @Override
    protected void succeeded(Description description) {

    }
  };



}
