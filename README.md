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


## Manual

### iceprogjava
```
Usage: iceprogjava COMMAND
Simple programming tool for FTDI-based Lattice iCE programmers.
Commands:
  write  write file contents to flash, then verify
  check  Read bytes from flash and compare to file.
  read   Read bytes from flash and write to file
  erase  (partially) erase flash
  test   Just read the flash ID sequence
  dwp    Disable write protection. This can be useful if flash memory appears
           to be bricked and won't respond to erasing or programming.
  help   Displays help information about the specified command

Exit status:
  0   Success.
  1   Non-hardware error occurred (e.g., failure to read from or write to a
        file, or invoked with invalid options).
  2   Communication with the hardware failed (e.g., cannot find the iCE FTDI
        USB device).
  3   Verification of the data failed.
```
 
### iceprogjava write
```
Usage: iceprogjava write [[-w] | [-b]] [-svx] [-o=<offset in bytes>] <input
                         file>
write file contents to flash, then verify
Default: erase aligned chunks of 64kB in write mode. This means that some data
after the written data (or even before when -o is used) may be erased as well.
      <input file>        input file to read from
  -b                      bulk erase entire flash
  -o=<offset in bytes>    start address for read/write (append 'k' to the
                            argument for size in kilobytes, or 'M' for size in
                            megabytes)
                            Default: 0
  -w                      do not erase flash before writing
  -x                      do not read and verify against file after writing

Global options:
  -s                      slow SPI (50 kHz instead of 6 MHz)
  -v                      verbose output

Exit status:
  0   Success.
  1   Non-hardware error occurred (e.g., failure to read from or write to a
        file, or invoked with invalid options).
  2   Communication with the hardware failed (e.g., cannot find the iCE FTDI
        USB device).
  3   Verification of the data failed.
```
 
 
### iceprogjava check
```
Usage: iceprogjava check [-sv] [-o=<offset in bytes>] <input file>
Read bytes from flash and compare to file.
      <input file>        input file to read from
  -o=<offset in bytes>    start address for read/write (append 'k' to the
                            argument for size in kilobytes, or 'M' for size in
                            megabytes)
                            Default: 0

Global options:
  -s                      slow SPI (50 kHz instead of 6 MHz)
  -v                      verbose output

Exit status:
  0   Success.
  1   Non-hardware error occurred (e.g., failure to read from or write to a
        file, or invoked with invalid options).
  2   Communication with the hardware failed (e.g., cannot find the iCE FTDI
        USB device).
  3   Verification of the data failed.
```
 
 
### iceprogjava read
```
Usage: iceprogjava read [-sv] [-n=<size in bytes>] [-o=<offset in bytes>]
                        <output file>
Read bytes from flash and write to file
      <output file>
  -n=<size in bytes>      bytes to read from flash (append 'k' to the argument
                            for size in kilobytes, or 'M' for size in megabytes)
                            Default: 256k
  -o=<offset in bytes>    start address for read/write (append 'k' to the
                            argument for size in kilobytes, or 'M' for size in
                            megabytes)
                            Default: 0

Global options:
  -s                      slow SPI (50 kHz instead of 6 MHz)
  -v                      verbose output

Exit status:
  0   Success.
  1   Non-hardware error occurred (e.g., failure to read from or write to a
        file, or invoked with invalid options).
  2   Communication with the hardware failed (e.g., cannot find the iCE FTDI
        USB device).
  3   Verification of the data failed.
```
 
 
### iceprogjava erase
```
Usage: iceprogjava erase [-bsv] -n=<size in bytes> [-o=<offset in bytes>]
(partially) erase flash
Default: erase aligned chunks of 64kB in write mode. This means that some data
after the written data (or even before when -o is used) may be erased as well.
  -b                      bulk erase entire flash
  -n=<size in bytes>      number of bytes (append 'k' to the argument for size
                            in kilobytes, or 'M' for size in megabytes)
  -o=<offset in bytes>    start address for read/write (append 'k' to the
                            argument for size in kilobytes, or 'M' for size in
                            megabytes)
                            Default: 0

Global options:
  -s                      slow SPI (50 kHz instead of 6 MHz)
  -v                      verbose output

Exit status:
  0   Success.
  1   Non-hardware error occurred (e.g., failure to read from or write to a
        file, or invoked with invalid options).
  2   Communication with the hardware failed (e.g., cannot find the iCE FTDI
        USB device).
  3   Verification of the data failed.
```
 
 
### iceprogjava test
```
Usage: iceprogjava test [-sv]
Just read the flash ID sequence

Global options:
  -s     slow SPI (50 kHz instead of 6 MHz)
  -v     verbose output

Exit status:
  0   Success.
  1   Non-hardware error occurred (e.g., failure to read from or write to a
        file, or invoked with invalid options).
  2   Communication with the hardware failed (e.g., cannot find the iCE FTDI
        USB device).
  3   Verification of the data failed.
```
 
 
### iceprogjava dwp
```
Usage: iceprogjava dwp [-sv]
Disable write protection. This can be useful if flash memory appears to be
bricked and won't respond to erasing or programming.

Global options:
  -s     slow SPI (50 kHz instead of 6 MHz)
  -v     verbose output

Exit status:
  0   Success.
  1   Non-hardware error occurred (e.g., failure to read from or write to a
        file, or invoked with invalid options).
  2   Communication with the hardware failed (e.g., cannot find the iCE FTDI
        USB device).
  3   Verification of the data failed.
```
