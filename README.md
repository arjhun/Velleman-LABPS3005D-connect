# Korad/ Velleman LABPS3005D Library
 A Java library to connect to your Velleman or other Korad style 3005D power supplies over the serial port.
 
 This project started out as a way for me to learn how to interface with hardware, serial protocols (although this is a real shitty one), and finish creating a Java Library. When using this library, keep in mind that this project is a work in progress.

## Table of Contents

- [Installation](#installation)
- [Usage](#usage)
- [Support](#support)
- [Contributing](#contributing)

## Installation

For now you have to download the source and build/ install yourself using maven.
You can also include the project via maven into your own project.

```xml
<dependency>
	<groupId>nl.arjenklaverstijn</groupId>
	<artifactId>KoradConnect</artifactId>
	<version>0.0.1-SNAPSHOT</version>
</dependency>
```


## Usage

You can find experiments and examples [included in this repository](https://github.com/arjhun/Velleman-LABPS3005D-connect/tree/main/src/main/java/nl/arjenklaverstijn/software/korad/automationExamples "Check out the examples!") .

### Connect to your PSU with the `LabPSU3005D.class`

```java
// setup and open port
LabPSU3005D device = new LabPSU3005D("COM4");
device.openPort();
try {
  // do stuff
  device.setVoltage(4.2F);
  device.setCurrent(0.2F);
  device.setEnabled(true);
  int samples = 1000;
  long start = System.currentTimeMillis();
  System.out.println("Timestamp, Voltage, Current ");
  for (int i = 0; i < samples; i++) {
    System.out.print((System.currentTimeMillis() - start) + ", ");
    System.out.print(device.getOutputVoltage() + ", ");
    System.out.println(device.getOutputCurrent());
    Thread.sleep(1000);
  }
} catch (InterruptedException e) {
  e.printStackTrace();
} finally {
  device.closePort();
}
```

### Create automated experiments by extending `Automation.class`

Automation class sets everything up, you only need to implement the logic of your experiment. The abstract method run exposes your device of `LabPS3005D.class`.


```java

public class LithiumChargeAutomation extends Automation {


  private float cutOffCurrent;
  private float chargeV;
  private float chargeA;
  
  public LithiumChargeAutomation(float chargeV, float chargeA, float cutOffCurrent) {
    this.chargeV = chargeV;
    this.chargeA = chargeA;
    this.cutOffCurrent = cutOffCurrent;
  }


  @Override
  public void job(LabPSU3005D device) {
    try {
    device.setOVP(true);
    device.setOCP(false);
    device.setVoltage(chargeV);
    device.setCurrent(chargeA);
    device.setEnabled(true);

    // Wait a bit, to let the battery voltage settle
      Thread.sleep(1000);
    while (device.getOutputCurrent() > cutOffCurrent) {
    }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }finaly{
      device.closePort();
    }
  }
}
```

Run it like this:

```java
LabPSU3005D device = new LabPSU3005D("COM4");
Long time = new LithiumChargeAutomation(4.2F, .1f).run(device);
System.out.print("Cut off voltage reached with a current of: " + chargeA + " in "
        + time / 1000 / 60 + " minutes");
```

### Run multiple experiments in succession with `AutomationQueue.class`

```java
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
```

## Support
This is a fun project for me to work on. But I'm just getting started with Java. Help, tips or advice are welcome!

Please [open an issue](https://github.com/arjhun/Velleman-LABPS3005D-connect/issues/new) for support, or remarks.

## Contributing

Please contribute using [Github Flow](https://guides.github.com/introduction/flow/). Create a branch, add commits, and [open a pull request](https://github.com/arjhun/Velleman-LABPS3005D-connect/compare).
