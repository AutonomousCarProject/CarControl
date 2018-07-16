FlyCamera ReadMe...

Purpose & History

We are doing this summer program for high school students, in which they get to take the raw pixel data off a camera watching live action, then they learn to extract the significant image information from this data and display it in real time. Most of the students have learned Java in school, and (except for a few application areas that the Java designers didn't want to touch) it's a more robust language than C/C++. Furthermore (if you are willing to take the modest steps to avoid triggering a garbage collection time-out) the Java JIT compilers in most implementations produce code that runs essentially at machine speed, about as fast as C/C++.

Our application seemed likely to need something in the order of 256x256 pixels, and the Pt.Grey (now FLIR) FireFly camera series was specified at 640x480, which seemed like a sufficient margin of safety, and came with C/C++/C# APIs but not Java. The vendor mentioned a couple third-party Java wrapper class APIs, but upon examination, both seemed unnecessarily hard to use. These are high-school students, and part of the reason for using Java and not C is to minimize the difficulties not inherent in the project.

My task included (among other things) making sure the students had adequate access to the camera data from their Java code, and writing a new wrapper class seemed simpler than adapting existing code. After I got the wrapper working, I was able to discern that the specified "640x480" resolution of the FireFly camera was misleading, it refers to the sensor resolution, not the pixel resolution of the color camera, where each square cluster of four sensors (RG/GB) is one pixel, so the margin of safety evaporated, and I re-purposed the wrapper class to access the data for the next model up, the Chameleon. The code here now works with either camera.

The default camera settings for FireFly were suitable as-is, but Chameleon defaulted to the full sensor array (which is a few pixels larger than the standard 640x480 pixel size) and a frame rate too fast for the USB2 connection, so I added some code to strip off the extra pixels and to set a slower frame rate (if desired). The Camera2File.java program gives an example of how to use the FlyCamera class (and DLL).


The API

The FlyCapture2 DLL is designed to support several families of cameras, some with features not in the low-end cameras. The students don't need to worry about all that, all they want is to get things going, then pull off a frame every so often. So I collected all the relevant APIs into a single FlyCamera class with three essential methods, and three support methods:

  public boolean Connect(int frameRate);

This does all the necessary initialization, and starts the camera going at the designated frame rate (which is specified by a number defined in the C API (only 15fps and 30fps are supported; the USB3 hardware transfer cannot go faster, and if they cannot do the processing in the available time, the presentation will look bad). It returns true if successful, or false if there is no camera or something else goes wrong. This also queries the camera for the default image size and Bayer tiling, which can be retrieved by the Java program:

  public int Dimz();

This returns a pair of 16-bit integers packed into a single integer, the height in pixel rows in the upper half, and the width in the lower half.

  public int PixTile();

This returns a single number representing one of four Bayer tiling patterns, =1 for RG/GB (the FireFly default) and =3 for GB/RG (the Chameleon default). Each pair of bytes in the even rows is matched to the corresponding pair of bytes in the following odd rows to provide the three RGB components of a pixel (and a second green)...

  public boolean NextFrame(byte[] pixels);

This gets the next available image frame captured by the camera. The byte array should be pre-allocated to the size determined by the dimensions. The pixels will be Bayer-tiled as described by PixTile(). It returns true if it successfully got another frame.

  public String toString();

The toString() method is defined for all Java classes, usually to represent the current state, or at least the class name; here it returns a text description of the most recent error, if either Connect() or NextFrame() previously returned false.

  public void Finish();

The camera should be turned off using this call when the program terminates, so that temporary buffers can be released.


The Code

There are two parts to the implementation. The Java class FlyCamera source code includes headers (if you can call them that) for the three essential native code methods, and the full Java code for the support methods. The three essential methods are implemented in C (FlyCamera.cpp), which compiled successfully in VisualStudio 2015 (Community) to produce another DLL to be included in the Java runtime package with the two DLLs from the vendor. If you are compiling a DLL for the first time, see my Implementation Notes below.

You need these three DLL files in your Java project folder (the folder named by your project, which also contains "src" and "bin" folders), or if you are distributing your Java code in a jar file, these need to be in the same folder with the runtime file:

  FlyCamera.dll
  FlyCapture2_C.dll
  FlyCapture2.dll

You will need to download and install the FlyCapture2 APIs and drivers, and all this only works on their cameras. The Java code was tested in x64 Eclipse Neon.2 with JDK 8.0 on Windows 10. Nothing in the code depends on 64-bit-ness except for the fact that it's nominally x64; if you want to do it in 32-bit (x86) you need to rebuild everything in 32-bit, because there is no software compatibility between the two architectures.


Camera2File Code

A second copy of the FlyCamera class (in package fly0cam) is defined to read a binary file representing the captured data from the camera, which can be used for testing when no camera is available, or to test the program being developed on a consistent stream of data, or else at a time when the scene it is designed for is not available. Each frame in the file has a 4-byte header containing the frame size and tiling, followed by the required number of bytes of data. The last frame is followed by a null header (0 rows and 0 columns).

The source code for the program Camera2File to call the real FlyCamera class and write it to a file is included. It can also be used to monitor on the computer screen the scene seen by the camera. Click in the center of the window to record about five seconds; clicking the bottom left corner records some 20 seconds, the bottom-right corner records about a minute, and the top left and right corners record one and eight frames respectively. The file name for both reading and writing is hard-coded "FlyCapped.By8" but you can easily add code to ask for a file name.


Implementation Notes

Beware! "They" (everybody whose products you must use to make this happen) make it unnecessarily difficult for "noobs" (I suspect they think of it as job security to exclude newbies and thus drive market prices up). Here's how I did it:

The Win10 I got came with x64 Java already installed, so I was forced to do everything in x64. You must do everything in the same architecture, otherwise things break in mysterious ways.

Download the proper (32 or 64) version of the FlyCapture SDK and install it. There's a FlyCapture2GUI.exe program already compiled, which you can use to test the camera.

  http://www.ptgrey.com/flycapture-sdk

Go to the VisualStudio website and download either the (freebie) Community version, which you can use to make non-commercial projects like this, or else the (free trial, but eventually paid-for) Professional version. Both versions have a time-bomb and stop working after 30 days. I got VS 2015, but I see they now offer 2017, which is probably about the same. What I got is a stub, not the real thing. It wants to go online and you select what you want and then it downloads and installs what you selected. Be sure to choose the C/C++ package, it does not come unless you ask for it, and (nevermind what they say) you CANNOT repair the damage later. I backed up the system before doing this, so when I did it wrong a few times, it was easy to restore the whole system and start over. I was unable to recover from their time-bomb, so some of my remarks here are based on distant memories and may differ somewhat from your experience.

VisualStudio puts all your named projects in a folder in the Documents folder near the top of the folder hierarchy in the "File Explorer" view of your Win10 file system (right-click the windows button at the bottom left corner of the screen to get there). Eclipse puts all their projects in a "Workspace" folder under your UserName under "Users" under "DriveC" at the bottom of the same hierarchy. Scrolling back and forth gives you a sense of usefulness (if you crave that sort of thing ;-)

VS is the only software package in this whole ball of wax (wax is the "cere" in the Latin word "sincere" which means "without wax"), where the same code base does both 32-bit and 64-bit, but that's not particularly a benefit: the default is 32, and you must explicitly choose x64 in your project properties before you do anything else. Otherwise the linker will fail in obscure ways.

If you are already using Eclipse for Java, you can skip this paragraph. If you are using some other platform for Java development, you can probably figure out how to do it from what I say about Eclipse. If you are just getting started, Eclipse is probably about as good as anything, but it won't be easy. If you haven't used Java before, you need to learn it first. I suggest taking a class, but I wrote most of a quickie Java course here:

  http://www.IttyBittyComputers.com/Java/LearnProgJava.htm

Linked to this course are instructions for getting Eclipse up on Win10:

  http://www.IttyBittyComputers.com/Java/WinEclipse.htm

My experience with DLLs is that the include mechanism in Windows is quite brittle (easily broken). I learned most of this from the StackOverflow help website (they are rather hostile to C/C++ questions, so it's better to look for existing answers, of which there will be several, than to post your own). The best way to do this is to just copy everything into the proper folders where they are being used. There are pathname things that are supposed to do this correctly, but driving the mechanism is most likely to end you up in the ditch.

When you create a new C "console" project in VS, it makes a bunch of folders, two of them named by the project name you gave it (for example "ProjName"), one inside the other. I think the inner one is for the "solution". Using a 3rd-party DLL like FlyCapture2_C.dll, you need its headers copied into the inner "ProjName" folder where you will see other header files put there by VS. To create the FlyCamera DLL using the FlyCapture2 APIs you need these additional header files:

  fly2cam_FlyCamera.h
  JNI.H
  JNI_MD.H
  FlyCapture2_C.h
  FlyCapture2Defs_C.h
  FlyCapture2Platform_C.h

The first header is created by Eclipse, as explained shortly. It #includes the second, which #includes the third. Similarly, FlyCapture2_C.h the other two. These will not show up automatically in the headers list of the "Solution Explorer" panel in your VS project, you must right-click "Add" them for them to be seen by the compiler.

Before you can compile your DLL you must create the Java class that uses it. The "native" keyword in the three methods implemented in C tells the Java runtime to call the DLL when they are invoked. The Java class and its package will compile correctly without the DLL being present, it just won't run. After it is compiled and saved, there is an "External Tools" item at the bottom of the "Run" menu. The process for setting up an item to make your header file is explained here:

  http://omtlab.com/java-run-javah-from-eclipse/

The newly created header file will be in a new folder "JNI" created by javah.exe just inside the project folder. The new header file assumes that the Java runtime header files are accessible through the pathname mechanism, which as I said, is probably not the case. Go find the extra headers in "include" folder in the "jdk" folder in the Java installed in "Program Files" and copy them to your VS project. Open up your newly created "fly2cam_FlyCamera.h" (after you get it into your VS project is good enough) and change the "<jni.h>" on the second line to be quoted instead of angle-bracketed (to reflect that the fact that the headers are local to the project, not in some distant library somewhere). The copy included in this distribution has already been fixed.

To use the existing C DLL (FlyCapture2_C.dll) in your VS compile, you will also need its "FlyCapture2_C.lib" file in a "lib" folder you create in your outer "ProjName" folder, as explained here:

  http://stackoverflow.com/questions/495795/how-do-i-use-a-third-party-dll

They tell you to put the DLLs there too, but since you are making another DLL, that's not necessary; instead they need to be in the Java project folder where they are used.

After the C DLL is compiled, there should be a new "x64" folder (unless you did it in 32-bit) with the DLL buried in it somewhere. There will also be a corresponding ".lib" file which you only need if you are calling the DLL from C (Java does its own thing and doesn't need it). Move or copy the DLL to the Java project folder where FlyCamera class is used, and/or make it part of the jar file.

If something goes wrong (ever heard of "Murphey's Law"?) there is no way I know of to step through the DLL when it is called from Java, and I don't know what happened to console output from the C code. I built a corresponding .exe program to do the same things, and when that worked, built the DLL and Java called it successfully. Things still didn't work properly, so I added some extra Java variables in the class, and passed back numbers I could interpret as telling me what the C code thought it was doing. Of course I have a lot of experience debugging embedded systems with limited debugging capabilities, so I know what to look for. Some of the scaffolding is still there, but commented out or otherwise ineffectual; perhaps you can get some ideas from it.

The best way to see how to use this DLL is to look at the Camera2File.java. The important interface code is in the GetCameraImg() method, which accepts a single frame of video from the camera and converts it to RGB pixels. After I wrote this, we moved to a different (higher resolution) camera, and I was asked to decide which camera would be better for the students. So I added code to allow either camera to be used, and to return pixel data at either resolution independently of the actual camera data. The instance variable twosie=0 to use the data as-is, =1 to double it, and -1 to down-sample it. If the final static ImHi and ImWi instance constants match the camera image dimensions, twosie will be set =0. These dimensions can be set in the single constant BigCam=1 for 320x240 and =2 for 640x480.

Besides showing how to use the DLL, the Camera2File.java program has two purposes: (1) If you just run it with a camera connected, it will show in a window what the camera sees, and (2) You can use it to record a few frames or seconds of video in a raw image file which the fly0cam.FlyCamera class can read and return as if a camera were connected. I included an 8-frame "FlyCapped.By8" file for demonstation purposes. If you build the Camera2File.java program importing fly0cam.FlyCamera instead of fly2cam.FlyCamera, you can watch the video (put it in the project folder where the DLL would go).


License

All the source code in this implementation is released to the public domain. The camera APIs are of course licensed by the vendor with their own restrictions.

Like all software (whether you paid for it or not) there are no warranties, no promises. It worked for our project, but if you have problems, it's not so complicated that you cannot poke around and fix it yourself.

If you have questions, you can send me an email and I'll answer the best I can, but I may not have sufficient time or access to the necessary resources to test anything, so I cannot promise any particular improvements (for example, my copy of VisualStudio self-destructed, so I cannot recompile any C code).

Tom Pittman
TPittman@IttyBittyComputers.com



