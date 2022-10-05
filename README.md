# Velleman LABPS3005D Library
 A Java library to connect to your Velleman or other Korad style 3005D power supplies

## Table of Contents

- [Installation](#installation)
- [Usage](#usage)
- [Support](#support)
- [Contributing](#contributing)

## Installation

For now you have to download the source and build yourself using maven.
You can also include the porject via maven into your own project:

```xml
<dependency>
	<groupId>nl.arjenklaverstijn</groupId>
	<artifactId>KoradConnect</artifactId>
	<version>0.0.1-SNAPSHOT</version>
</dependency>
```


## Usage

You can find experiments and examples [included in this repository](https://github.com/arjhun/Velleman-LABPS3005D-connect/tree/main/src/main/java/nl/arjenklaverstijn/software/korad/automationExamples "Check out the examples!") .

### Connect to your PSU

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

    while (device.getOutputCurrent() > cutOffCurrent) {
    }
  }
}
```

Run it like this:

```java
LabPSU3005D device = new LabPSU3005D("COM4");
Long time = new LithiumChargeAutomation(4.2F, .5f).run(device);
System.out.print("Cut off voltage reached with a current of: " + chargeA + " in "
        + time / 1000 / 60 + " minutes");
```

## Support
This is a fun project for me to work on. But I'm just getting started with Java. Help, tips or advice are welcome!

Please [open an issue](https://github.com/arjhun/Velleman-LABPS3005D-connect/issues/new) for support, or remarks.

## Contributing

Please contribute using [Github Flow](https://guides.github.com/introduction/flow/). Create a branch, add commits, and [open a pull request](https://github.com/arjhun/Velleman-LABPS3005D-connect/compare).