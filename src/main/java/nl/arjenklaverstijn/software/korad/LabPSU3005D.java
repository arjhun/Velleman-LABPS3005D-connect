package nl.arjenklaverstijn.software.korad;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortTimeoutException;

public class LabPSU3005D {

  private SerialPort serialPort;
  private boolean debug = false;
  private long lastSend;

  /**
   * This constructor sets up the {@link SerialPort} object for you with the right settings. Opening
   * and closing is left up to the user.
   * 
   * @param port The system port name to connect to.
   */
  public LabPSU3005D(String port) {
    serialPort = SerialPort.getCommPort(port);
    // Options
    serialPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
    serialPort.setParity(SerialPort.NO_PARITY);
    serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 80, 0);
  }

  public void setCurrent(float current) {
    // ISET<X>:<NR2>
    // Description: Sets the output current.
    // Example:ISET1:2.225
    // Sets the CH1 output current to 2.225A
    if (current > 5.1)
      current = 5.1F;
    else if (current < 0)
      current = 0;
    String command = "ISET1:" + current;
    send(command);
  }

  public float getSetCurrent() {

    String command = "ISET1?";
    // ISET<X>?
    // Description: Returns the output current setting.
    // Example ISET1?
    // Returns the CH1 voltage setting
    String result = sendRead(command, 6);
    return Float.valueOf(result.substring(0, 5));

  }

  //
  /**
   * Sends VSET&lt;X&gt;:<NR2></br>
   * Description:Sets the output voltage.</br>
   * Example VSET1:20.50</br>
   * Sets the CH1 voltage to 20.50V
   * 
   * @param voltage The Voltage to set
   */
  public void setVoltage(float voltage) {
    if (voltage > 31)
      voltage = 31F;
    else if (voltage < 0)
      voltage = 0;
    double roundOff = (double) Math.round(voltage * 100) / 100;
    String command = "VSET1:" + roundOff;
    send(command);
  }

  public float getSetVoltage() {
    String command = "VSET1?";
    // VSET<X>?
    // Description: Returns the output voltage setting.
    // Example VSET1?
    // Returns the CH1 voltage setting
    String result = sendRead(command, 5);
    return Float.valueOf(result);

  }

  public float getOutputCurrent() {
    // IOUT<X>?
    // Description:Returns the actual output current.
    // Example IOUT1?
    // Returns the CH1 output current
    String command = "IOUT1?";
    String result = sendRead(command, 5);
    System.out.println(result);
    return Float.valueOf(new String(result));

  }

  public float getOutputVoltage() {
    // VOUT<X>?
    // Description:Returns the actual output voltage.
    // Example VOUT1?
    // Returns the CH1 output voltage
    String command = "VOUT1?";
    String result = sendRead(command, 5);
    return Float.valueOf(new String(result));

  }

  // public void setBeep(boolean onOff) {

  // BEEP doesn't work (yet?)
  //
  // BEEP<Boolean>
  // Description:Turns on or off the beep. Boolean: boolean logic.
  // Example BEEP1 Turns on the beep.
  // send("BEEP" + booleanToInt(onOff));
  // }


  public void setEnabled(boolean onOff) {
    setEnabled(onOff, 100);
  }

  public void setEnabled(boolean onOff, int settleTime) {
    // OUT<Boolean>
    // Description:Turns on or off the output.
    // Boolean:0 OFF,1 ON
    // Example: OUT1 Turns on the output

    send("OUT" + booleanToInt(onOff));
    try {
      Thread.sleep(settleTime);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  public Status getStatus() {
    return getStatus(1);
  }

  public Status getStatus(int channel) {
    if (channel != 1 || channel != 2)
      channel = 1;
    // status byte
    String statusByte = sendRead("STATUS?", 1);
    return new Status(channel, ((byte) statusByte.charAt(0)));
  }

  public String getId() {

    // *IDN?
    // Description:Returns the KA3005P identification.
    // Example *IDN?
    // Contents TENMA 72-2535 V2.0 (Manufacturer, model name,).
    String result = sendRead("*IDN?");
    return new String(result);

  }

  public void recalPreset(int num) {
    // RCL<NR1>
    // Description:Recalls a panel setting.
    // NR1 1 to 5: Memory number 1 to 5
    // Example RCL1 Recalls the panel setting stored in memory number 1
    if (num > 0 && num <= 5) {
      send("RCL" + num);
    }
  }

  public void savePreset(int num, float voltage, float current) {
    // SAV<NR1>
    // Description:Stores the panel setting.
    // NR1 1-5: Memory number 1 to 5
    // Example :SAV1 Stores the panel setting in memory number 1

    if (num > 0 && num <= 5) {
      recalPreset(num);
      setVoltage(voltage);
      setCurrent(current);
      send("SAV" + num);
    }

  }

  public void setOCP(boolean onOff) {
    // OCP< Boolean >
    // Description:Stores the panel setting.
    // Boolean:0 OFF,1 ON
    // Example: OCP1 Turns on the OCP
    send("OCP" + booleanToInt(onOff));
  }

  public void setOVP(boolean onOff) {
    // OVP< Boolean >
    // Description:Turns on the OVP.
    // Boolean:0 OFF,1 ON
    // Example: OVP1 Turns on the OVP
    send("OVP" + booleanToInt(onOff));

  }

  private String sendRead(String command) {
    return sendRead(command, -1);
  }

  private String sendRead(String command, int numBytes) {
    send(command);
    return read(numBytes);
  }

  public String read() {
    return read(-1);
  }


  private int send(String command) {

    long now = System.currentTimeMillis();
    long timePassed = now - lastSend;
    long minTime = 50; // ms
    // Optimized sleep between sends.
    // So we don't always have to sleep the Thread.
    if (timePassed <= minTime) {
      try {
        Thread.sleep(minTime - timePassed);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    byte[] buffer;
    int bytes = -1;
    try {
      buffer = command.getBytes("UTF-8");
      long bytesToWrite = Long.valueOf(buffer.length);
      bytes = serialPort.writeBytes(buffer, bytesToWrite);
      lastSend = System.currentTimeMillis();
      if (bytes == buffer.length) {
        log("SEND " + command + " (" + bytes + " bytes)");
      } else {
        log("ERROR only send: " + buffer.length + " bytes");
      }
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return bytes;
  }

  /**
   * @param numBytes
   * @return
   */
  private String read(int numBytes) {
    String value = "";
    InputStream in = serialPort.getInputStream();
    try {
      while (true) {
        int currentByte = in.read();
        value += (char) currentByte;
        if (numBytes >= 0 && value.length() == numBytes) {
          break;
        }
      }
      System.out.println("closing");
      in.close();
      log("READ " + value + " (" + value.length() + " bytes)");
    } catch (SerialPortTimeoutException e) {
      log("ERROR during read... No more data comming in");
    } catch (IOException e) {

    }

    return value;
  }


  private void log(String string) {
    if (debug)
      System.out.println(string);
  }

  private int booleanToInt(boolean value) {
    int result = (value) ? 1 : 0;
    return result;
  }

  enum MODE {
    CC,
    CV,
    NONE
  }

  public class Status {

    private byte raw;
    private int modeCh1;
    private int modeCh2;
    private boolean beep;
    private boolean ocp;
    private boolean output;
    private boolean ovp;
    private int channel;

    public Status(int channel, byte statusByte) {
      this.channel = channel;
      this.raw = statusByte;
      this.modeCh1 = statusByte & 1;
      this.modeCh2 = (statusByte >> 1) & 1;
      this.beep = intToBool((statusByte >> 4) & 1);
      this.ocp = intToBool((statusByte >> 5) & 1);
      this.output = intToBool((statusByte >> 6) & 1);

    }

    private boolean intToBool(int i) {
      if (i == 1)
        return true;
      return false;
    }

    /*
     * source: https://sigrok.org/wiki/Velleman_LABPS3005D this stuff is buggy AF and rather
     * important korad/ velleman others, FIX THIS! STATUS? Description:Returns the POWER SUPPLY
     * status. Contents 8 bits in the following format Bit Item Description 0 CH1 0=CC mode, 1=CV
     * mode 1 nothing for 1ch device 2 nothing for 1ch device 4 Beeper enabled? 5 OCP enabled? 6
     * Output is enabled? 7 N/A N/A (this one is sketchy it shows true when output is enabed and OCP
     * and/or OVP is enabled... weird
     */
    public boolean beep() {
      return beep;
    }

    public MODE getMode() {
      if (channel >= 1 && channel <= 2) {
        int modeValue = (channel == 1) ? modeCh1 : modeCh2;
        if (modeValue == 0) {
          return MODE.CC;
        } else {
          return MODE.CV;
        }
      }
      return MODE.NONE;
    }

    public boolean getOCP() {
      return ocp;
    }

    public boolean getOutput() {
      return output;
    }

    public byte getRaw() {
      return raw;
    }


    @Override
    public String toString() {
      return "Status [raw=" + Integer.toBinaryString(Byte.toUnsignedInt(raw)) + ", modeCh1="
          + modeCh1 + ", modeCh2=" + modeCh2 + ", beep=" + beep + ", ocp=" + ocp + ", output="
          + output + ", ovp=" + ovp + "]";
    }

  }

  public boolean openPort() {

    boolean success = serialPort.openPort();
    log(success
        ? "Device with id: " + getId() + " connected on port: " + serialPort.getSystemPortName()
        : "No device on port detected");
    return success;
  }

  public boolean isOpen() {
    ;
    return serialPort.isOpen();
  }

  public boolean closePort() {
    return serialPort.closePort();
  }

  public void setDebug(boolean debug) {
    this.debug = debug;
  }

}
