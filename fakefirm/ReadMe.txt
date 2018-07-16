FakeFirmata ReadMe (2018 May 4)...

Important Notice

The JSSC (Java Simple Serial Connector) driver we used for this implementation has been known to not properly close out its serial port unless you call its closePort() method when you are done. Failing to do this (like if you terminate your program in the debugger) can leave the port inaccessible, and Win10 could allocate a different "COM" identifier when you try again. Because FakeFirmata uses a constant "COM3" port identifier, it may fail unless you reboot the computer after every abnormal termination.

Purpose & History

We are doing this summer program for high school students, in which they get to drive a radio-controlled car modified to be driven autonomously from an on-board computer with attached camera. Last year they learned how to extract objects ("pedestrians") from the video feed, this year they will be controlling the steering and speed by means of servos driven from an Arduino connected to the main computer.

Most of the students have learned Java in school, and (except for a few application areas that the Java designers didn't want to touch) it's a more robust language than C/C++ and more standard than C#. Furthermore (if you are willing to take the reasonable and modest steps to avoid triggering a garbage collection time-out) the Java JIT compilers in most implementations produce code that runs essentially at machine speed, about as fast as C/C++.

The computer we chose, LattePanda (LP) runs Win10, and Win10 runs Java, but the LP interface to the Arduino is specified and written in C#. The Arduino itself is programmed with (open-source) Firmata, and the purpose of this package is to avoid re-inventing that wheel, but rather to replace the supplied LP driver with a Java equivalent.

We originally were only using the Arduino to drive two servos, so most of the Firmata interface is not implemented, but only the two APIs to blink the LED and to drive servos. The LP code is well-commented, and with reference to the Firmata C++ source code, the Firmata documentation on GitHub, adding the additional APIs should be straight-forward knowing only Java and neither C nor C#. I tried to preserve the LP text as much as possible, changing only the spelling as appropriate to Java syntax.

Unfortunately, there is no standard way in Java to access the serial port that Arduino uses for communication to the host computer. There are several serial port implementations, every one I looked at is huge and hard to understand and overkill for this application, but I did not want to re-invent that wheel either, so we chose the Java-Simple-Serial-Connector (JSSC) implementation as being slightly more transparent. After a hiccup or two (readily identified in the Java debugger and fixed by making sure their DLL is in the correct folder), I was able to make my test program blink the blue Arduino LED and thereafter to drive servos.

Everything tends to grow by "feature creep," and our application is no exception. So when I tried to allow for digital and analog input, I had to make some substantial revisions. The LattePanda examples for analog input do not require it, but I found it helpful to require the client to specify the pin mode for analog input as well as the other modes, so my glue code can initialize exactly that one pin as analog input instead of a whole bunch of them. You can do them all by default if you want, with a single boolean switch, AutoStartInputs = true (instead of false), and then it should work according to the example code. My efforts at making input work failed, so what you see in this release are the original two output modes only, not much different from the initial version uploaded to the LP user forum. It does, however, have a code hook (SimHookBase) to give the TrackSim simulator look-only access to the signals being sent to the servo controls.

For more documentation, see:

  http://www.IttyBittyComputers.com/APW2/TrackSim/FakeFirm.htm

License

All of my original source code in this implementation is released to the public domain. Some of the code is copied from the LattePanda implementation of Firmata, but that is probably small enough to qualify as "fair use." If you do a full implementation, it would be a "derivative copy" and therefore subject to the much more restrictive GPL license, which as one industry giant remarked, "is a cancer that infects everything it touches."

Like all software (whether you paid for it or not) there are no warranties, no promises. It worked for our project, but if you have problems, it's not so complicated that you can't poke around and fix it yourself.

If you have questions, you can send me an email and I'll answer the best I can, but I may not have sufficient time or access to the necessary resources to test anything, so I cannot promise any particular improvements.

Tom Pittman
TPittman@IttyBittyComputers.com



