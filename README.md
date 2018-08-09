# Autonomous Vehicle Project
Speed, steering, and object detection for an autonomous RC car

[![Build Status](https://travis-ci.org/AutonomousCarProject/CarControl.svg?branch=master)](https://travis-ci.org/AutonomousCarProject/CarControl)

## Main Components

Stuff required for the car to drive, majority of the program.

* Image processing.
* Speed control.
* Pedestrian & object detection.
* Steering.
* PWM / Arduino Interfacing.

## Non-Essentials

Stuff we would like to complete.    

* GPU parallelization.
* Arduino driver rewrite.
* Raspberry Pi.
* Website.
* TrakSim replacement.

## Hardware
This project is mostly focused on the software side of the problem but we are running it on real hardware

* RC Car
* Onboard SBC - LattePanda (insert specific version here).
* Arduino integrated with LP to control servos.
* Frontmounted camera - FLIR Firefly Camera.

## Dependencies

* [Aparapi](http://aparapi.com/) - Open-source framework for executing native Java code on the GPU through OpenCL.
* [Fly2Cam](http://www.ittybittycomputers.com/APW2/TrackSim/Fly2cam.htm) - Tom explains it better [here](http://www.ittybittycomputers.com/APW2/TrackSim/Fly2cam.htm).
* [JSSC](https://code.google.com/archive/p/java-simple-serial-connector/) - Java Simple Serial Connector.
* [Firmata](https://www.arduino.cc/en/Reference/Firmata) - The Arduino Firmata library implements the Firmata protocol for communicating with software on the host computer.


# TrakSim
Standalone driving simulation to test code without the required hardware

## Components

* APW3
    * TrakSim and its supporting classes, as well as an exmple track to run it with.
* DriveDemo - **Deprecated since** [**eb8e01c**](https://github.com/AutonomousCarProject/CarControl/commit/eb8e01cc2d91feb26ebcebe2d798e27c0678d200)
    * A program designed to demonstrate how to use both TrakSim and the servo & camera interfaces simulated by TrakSim.
* Fly2Cam
    * A minor revision of the Java interface to the JNI (C-coded) DLL which accesses the Pt.Grey Chameleon3 or FireFly camera driver DLLs. FlyCamera.dll is included here.
* noJSSC
    * A non-functional (stub) plug-compatible substitute for the JSSCAPI, which may be used in its place when running TrakSim in stand-alone mode on any computer.
* FakeFirm
    * A Java clone of the C# API released by LP for driving digital outputs and servos that diverts a copy of the servo commands to TrakSim.


## Running
Setup the included Gradle project and make sure to include the [Aparapi](http://aparapi.com/) and [Raspivid-j](https://github.com/AutonomousCarProject/CarControl/commit/eb8e01cc2d91feb26ebcebe2d798e27c0678d200) libraries.

Compile and run MrModule.java in the `com.apw.carcontrol` package, by default its set to run on a car with the expected hardware and driver dlls but if
you are running it on your own computer with TrakSim or any equivalent use the cmd argument `sim`, this should launch a new TrakSim window and start driving the car.
If you wish to run the program with no visual display/window run with the argument `nosim`.

## Using TrakSim

* <span style="color:#FF4F69">**[1]**</span> - Clicking anywhere on the minimap in the top right will move the car to your click location.
* <span style="color:#FF4F69">**[2]**</span> - Clicking anywhere on the map in the bottom right will turn your car to the angle from the car to your click.
* <span style="color:#FF4F69">**[3]**</span> - Clicking on the bottom middle of the window on the small brownish bar will start driving.

![TrakSim](https://user-images.githubusercontent.com/3460531/43101980-af2225f8-8e7e-11e8-96f1-87fb08727a8e.png)

### Keybinds
* **UP** - Increase manual speed by 1.
* **DOWN** - Decrease manual speed by 1.
* **LEFT** - Steer left by 5.
* **RIGHT** - Steer right by 5.
* **P** - Tell the car that its stopped at a red light.
* **O** - Tell the car that its stopped at a yellow light.
* **I** - Tell the car that its stopped at a green light.
* **U** - Tell the car that its stopped at a stop sign.
* **Y** - Increments stop distance.
* **B** - Enable/disable blob rendering.
* **V** - Enable/disable overlay rendering.
* **F** - Begins camera calibration.
* **C** - Enable/disable writing blobs to console.
* **S** - Enable/disable writing speed to console.
* **M** - Increment blob color mode.
* **M** - Change image filter.
* **F11** - Toggle Fullscreen window (if using WindowModule).

### Track

TrakSim was originally designed to simulate the PatsAcres go-kart track, but now has a "Build Map" function so you can design your own track layouts using a (somewhat crude) text specification file described [here](http://www.ittybittycomputers.com/APW2/TrackSim/BuildMap.htm).

## IttyBittyComputers

Traksim was written by Tom Pittman and is extensively documented on his website, read it if you desire a better understanding of the program.

[TrakSim docs & explanations](http://www.ittybittycomputers.com/APW2/TrackSim/)

or contact Tom at TPittman@IttyBittyComputers.com

# About

This project was created and is maintained by a group of highschool students in Portland, Oregon.

You are welcome to submit issues and pull requests if you so desire (you probably dont).

# CI

[Travis](https://travis-ci.org).

# Style Guide

[Google Java Style Guide](https://google.github.io/styleguide/javaguide.html), enforced by [Google Java Formatter](https://github.com/google/google-java-format) with [google-java-format-gradle-plugin](https://github.com/sherter/google-java-format-gradle-plugin).

# License
See LICENSE
