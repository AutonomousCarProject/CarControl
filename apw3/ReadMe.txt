TrakSim ReadMe (Copyright 2018 May 25 Itty Bitty Computers)...

TrakSim is an emulator program that pretends to be the steering and drive servos of a radio-controlled car with the radio receiver replaced by a LattePanda (LP) computer running Java on Winows 10 and connected to a PointGrey(Flir) Chameleon3 or FireFly camera pointed straight ahead, which TrakSim also simulates, using the same APIs in each case.

TrakSim is designed to work with the hardware complement of the LattePanda (LP) computer where it is used to replace the radio receiver in a standard R/C model car, with user-written software to drive the car. It can also be used in stand-alone mode on any Java-compatible computer to simulate the LP system in the absence of the LP hardware and attached car, which is its reason for existence. Develop on any computer, then deploy on an embedded LP in your car.

This program is released as Java source code which you can run in any standard Java development environment. It is designed to work with FakeFirmata (FF, included) to test your autonomous vehicle software apart from any controlled car, but also can be used in your car while controlling your car's servos. FF in turn works with the JavaSimpleSerialConnector serial port implementation (or any other compatible API) to send serial port commands to the Arduino daughter board included with the LattePanda computer. In a Java development environment other than LP, use package noJSSC (also included) to run the simulator as a stand-alone.

You can design your own tracks and add artifacts like stop signs, traffic lights, moving pedestrians and other vehicles for your simulated car to see and avoid, using the built-in API for that putpose.

For complete documentation, see:

  http://www.IttyBittyComputers.com/APW2/TrackSim/TrackSim.htm

License

All of my original source code in this implementation is licensed to the public. Like all software (whether you paid for it or not) there are no warranties, no promises. It worked for our project (so far), but if you have problems, it's not so complicated that you can't poke around and fix it yourself.

If you have questions, you can send me an email and I'll answer the best I can, but I may not have sufficient time or access to the necessary resources to test anything, so I cannot promise any particular improvements.

Tom Pittman
TPittman@IttyBittyComputers.com



