package com.apw.pwm.raspi;

import com.apw.pwm.PWMController;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.wiringpi.Gpio;
import java.util.HashSet;
import org.jetbrains.annotations.NotNull;

/**
 * WARNING: The Raspberry Pi only supports two hardware PWM signals at a time. Additional signals
 * are controlled by software.
 */
public class RaspiPWM implements PWMController {

  public static final int PWM_FREQUENCY = 50;
  public static final double BCLCK = 19.2e6;
  public static final double MAX_PULSE_WIDTH = 2.0 / 1000;
  private static final RaspiPWM theController = new RaspiPWM();
  private final GpioController gpioController = GpioFactory.getInstance();
  private int PWM_RANGE;
  private int provisionedHardwarePins = 0;
  private HashSet<Integer> provisionedPins = new HashSet<>(2);

  // TODO potentially implement software PWM generation
  private boolean softwarePWMGenerationEnabled = false;

  private RaspiPWM() {
    configureController();
  }

  // Basically an override
  public static RaspiPWM getInstance() {
    return theController;
  }

  private void configureController() {
    PWM_RANGE = softwarePWMGenerationEnabled ? 100 : 4095;
    Gpio.pwmSetMode(Gpio.PWM_MODE_MS);
    Gpio.pwmSetRange(PWM_RANGE);
    Gpio.pwmSetClock(Math.toIntExact(Math.round(BCLCK / (PWM_FREQUENCY * PWM_RANGE))));
  }

  public void provisionPin(int pin) {
    provisionPinPrivate(pin);
  }

  /**
   * Provisions hardware/software pins as appropriate, or if they have already been provisioned,
   * returns the existing object.
   *
   * @param pin The pin number to provision.
   * @return The appropriate object.
   */
  private @NotNull GpioPinPwmOutput provisionPinPrivate(int pin) {
    GpioPinPwmOutput provisionedPin;
    if (!provisionedPins.contains(pin)) {
      // TODO Check if the board actually supports PWM on 23, 24, 26
      // TODO Pin pairing safety
      if (provisionedHardwarePins < 2 && (pin == 1 || pin == 23 || pin == 24 || pin == 26)) {
        provisionedHardwarePins++;
        provisionedPin = gpioController.provisionPwmOutputPin(RaspiPin.getPinByAddress(pin));
      } else {
        if (softwarePWMGenerationEnabled) {
          provisionedPin = gpioController.provisionSoftPwmOutputPin(RaspiPin.getPinByAddress(pin));
        } else {
          throw new IllegalStateException("Software PWM generation is not enabled, but is "
              + "required for more than 2 PWM signals.");
        }
      }
      provisionedPins.add(pin);
      return provisionedPin;
    } else {
      return (GpioPinPwmOutput) gpioController.getProvisionedPin(RaspiPin.getPinByAddress(pin));
    }
  }

  // TODO possibly more dynamic switching between hardware and software?

  @Override
  public void setOutputPulseWidth(int pin, double ms) {
    provisionPinPrivate(pin).setPwmRange(
        Math.toIntExact(Math.round(MAX_PULSE_WIDTH * PWM_FREQUENCY * PWM_RANGE)));
  }

  @Override
  public void close() {
    gpioController.shutdown();
  }
}
