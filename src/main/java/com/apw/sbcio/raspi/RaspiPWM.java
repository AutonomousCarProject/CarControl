package com.apw.sbcio.raspi;

import com.apw.sbcio.PWMController;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.wiringpi.Gpio;
import java.util.HashMap;
import org.jetbrains.annotations.NotNull;

/**
 * WARNING: The Raspberry Pi only supports two hardware PWM signals at a time. Additional signals
 * are controlled by software.
 */
public class RaspiPWM implements PWMController {

  private static final RaspiPWM theController = new RaspiPWM();

  public static final int PWM_FREQUENCY = 50, CLOCK_DIVIDER = 128;
  public static final double BCLCK = 19.2e6, MAX_PULSE_WIDTH = 2.0 / 1000;
  private int PWM_REAL_RANGE = Math.toIntExact(Math.round(BCLCK / CLOCK_DIVIDER / PWM_FREQUENCY)),
      PWM_ACTIONABLE_RANGE = Math.toIntExact(Math.round(
          (MAX_PULSE_WIDTH * PWM_FREQUENCY) * (BCLCK / CLOCK_DIVIDER / PWM_FREQUENCY)));
  private final GpioController gpioController = GpioFactory.getInstance();
  private int provisionedHardwarePins = 0;
  private HashMap<Integer, GpioPinPwmOutput> provisionedPins = new HashMap<>(2);

  // TODO potentially implement software PWM generation
  private boolean softwarePWMGenerationEnabled = false;

  private RaspiPWM() {
    // So as it turns out, configureController needs to be called *after* the pins are
    // provisioned, otherwise it outputs a garbage signal.
  }

  // Basically an override
  public static RaspiPWM getInstance() {
    return theController;
  }

  private void configureController() {
    // TODO software generation checks
    Gpio.pwmSetMode(Gpio.PWM_MODE_MS);
    Gpio.pwmSetRange(PWM_REAL_RANGE);
    Gpio.pwmSetClock(CLOCK_DIVIDER);
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
    if (!provisionedPins.containsKey(pin)) {
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
      configureController();
      provisionedPins.put(pin, provisionedPin);
      return provisionedPin;
    } else {
      return provisionedPins.get(pin);
    }
  }

  // TODO possibly more dynamic switching between hardware and software?

  @Override
  public void setOutputPulseWidth(int pin, double ms) {
    GpioPinPwmOutput realPin = provisionPinPrivate(pin);
    realPin
        .setPwm(Math.toIntExact(Math.round((ms / 1000) / MAX_PULSE_WIDTH * PWM_ACTIONABLE_RANGE)));
  }

  @Override
  public void close() {
    gpioController.shutdown();
  }
}
