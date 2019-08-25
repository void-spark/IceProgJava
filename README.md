# IceProgJava
A Java 'port' of iceprog, which uses FTDI D2XX

I have a upduino v2.0, which uses a FTDI FT232H for programming.
I'm using the amazing IceStorm toolchain to program it, on Windows 10 from a Docker container.

Only, when you do that iceprog won't work. And a Windows build of iceprog won't work unless
you start replacing the standard FTDI driver with WinUSB drivers using Zadig, which I don't like :)

There's also Diamond Programmer, which works fine too, and even comes with a commandline tool which can be used if
you create a project (xcf) file to go with it. But who wants to install closed source software :)

So instead I 'ported' iceprog to Java (I don't like visual studio or cygwin/msys2 either :) ),
and made it work with the D2XX API provided by the standard Windows driver for FTDI.

With this the only dependencies to program a .bin file are the (automatically installed) default Windows drivers,
the ftd2xx dll, and a jre :)

And yes I'm crazy, but I did learn a lot about how FPGA's are programmed :)
