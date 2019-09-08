package org.voidspark.mpssse;

import static java.lang.String.format;
import static org.voidspark.mpssse.MpsseCommands.MC_CLK_N;
import static org.voidspark.mpssse.MpsseCommands.MC_CLK_N8;
import static org.voidspark.mpssse.MpsseCommands.MC_DATA_BITS;
import static org.voidspark.mpssse.MpsseCommands.MC_DATA_IN;
import static org.voidspark.mpssse.MpsseCommands.MC_DATA_OCN;
import static org.voidspark.mpssse.MpsseCommands.MC_DATA_OUT;
import static org.voidspark.mpssse.MpsseCommands.MC_READB_HIGH;
import static org.voidspark.mpssse.MpsseCommands.MC_READB_LOW;
import static org.voidspark.mpssse.MpsseCommands.MC_SETB_LOW;
import static org.voidspark.mpssse.MpsseCommands.MC_SET_CLK_DIV;
import static org.voidspark.mpssse.MpsseCommands.MC_TCK_D5;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.voidspark.ftd2xx.FtD2xx;
import org.voidspark.ftd2xx.FtD2xxException;
import org.voidspark.ftd2xx.lib.ftd2xx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jnr.ffi.Pointer;

public class Mpsse {
    private static final Logger LOG = LoggerFactory.getLogger(Mpsse.class);

    private final FtD2xx ftD2xx;
    private Pointer ftHandle;

    private boolean mpsse_ftdic_open;
    private boolean mpsse_ftdic_latency_set;
    private int mpsse_ftdi_latency;

    public Mpsse() {
        ftD2xx = new FtD2xx();
    }

    public void checkRx() {
        while (true) {
            byte[] buffer = new byte[1];
            int rc = ftD2xx.read(ftHandle, buffer, 1);
            if (rc <= 0) {
                break;
            }
            LOG.error(format("unexpected rx byte: %02X", buffer[0]));
        }
    }

    public void abort() {
        checkRx();
        LOG.error(format("ABORT."));
        if (mpsse_ftdic_open) {
            if (mpsse_ftdic_latency_set) {
                ftD2xx.setLatencyTimer(ftHandle, mpsse_ftdi_latency);
            }
            ftD2xx.close(ftHandle);
        }
//        ftdi_deinit(&mpsse_ftdic);
    }

    public int recvByte() throws MpssseException {
        byte[] buffer = new byte[1];
        while (true) {
            int rc = ftD2xx.read(ftHandle, buffer, 1);
            if (rc < 0) {
                throw new MpssseException(format("Read error."));
            }
            if (rc == 1) {
                break;
            }
            LockSupport.parkNanos(TimeUnit.MICROSECONDS.toNanos(100));
        }
        return buffer[0] & 0xff;
    }

    public void sendByte(int data) throws MpssseException {
        byte[] buffer = { (byte) data };
        int rc = ftD2xx.write(ftHandle, buffer, 1);
        if (rc != 1) {
            throw new MpssseException(format("Write error (single byte, rc=%d, expected %d).", rc, 1));
        }
    }

    public void sendSpi(byte[] data) throws MpssseException {
        sendSpi(data, data.length);
    }

    public void sendSpi(byte[] data, int len) throws MpssseException {
        if (len < 1) {
            return;
        }

        /* Output only, update data on negative clock edge. */
        sendByte(MC_DATA_OUT | MC_DATA_OCN);
        sendByte(len - 1);
        sendByte((len - 1) >> 8);

        int rc = ftD2xx.write(ftHandle, data, len);
        if (rc != len) {
            throw new MpssseException(format("Write error (chunk, rc=%d, expected %d).", rc, len));
        }
    }

    public void xferSpi(byte[] data) throws MpssseException {
        xferSpi(data, data.length);
    }

    public void xferSpi(byte[] data, int len)throws MpssseException {
        if (len < 1) {
            return;
        }

        /* Input and output, update data on negative edge read on positive. */
        sendByte(MC_DATA_IN | MC_DATA_OUT | MC_DATA_OCN);
        sendByte(len - 1);
        sendByte((len - 1) >> 8);

        int rc = ftD2xx.write(ftHandle, data, len);
        if (rc != len) {
            throw new MpssseException(format("Write error (chunk, rc=%d, expected %d).", rc, len));
        }

        for (int i = 0; i < len; i++) {
            data[i] = (byte) recvByte();
        }
    }

    public int xferSpiBits(int data, int len) throws MpssseException{
        if (len < 1) {
            return 0;
        }

        /* Input and output, update data on negative edge read on positive, bits. */
        sendByte(MC_DATA_IN | MC_DATA_OUT | MC_DATA_OCN | MC_DATA_BITS);
        sendByte(len - 1);
        sendByte(data);

        return recvByte();
    }

    public void setGpio(int gpio, int direction) throws MpssseException {
        sendByte(MC_SETB_LOW);
        sendByte(gpio); /* Value */
        sendByte(direction); /* Direction */
    }

    public int readbLow() throws MpssseException {
        int data;
        sendByte(MC_READB_LOW);
        data = recvByte();
        return data;
    }

    public int readbHigh() throws MpssseException {
        int data;
        sendByte(MC_READB_HIGH);
        data = recvByte();
        return data;
    }

    public void sendDummyBytes(int count) throws MpssseException {
        // add 8 x count dummy bits (aka count bytes)
        sendByte(MC_CLK_N8);
        sendByte(count - 1);
        sendByte(0x00);
    }

    public void sendDummyBit() throws MpssseException {
        // add 1  dummy bit
        sendByte(MC_CLK_N);
        sendByte(0x00);
    }

    public void init(/* int ifnum, const char *devstr, */ boolean slow_clock) throws MpssseException {
//        enum ftdi_interface ftdi_ifnum = INTERFACE_A;
//
//        switch (ifnum) {
//            case 0:
//                ftdi_ifnum = INTERFACE_A;
//                break;
//            case 1:
//                ftdi_ifnum = INTERFACE_B;
//                break;
//            case 2:
//                ftdi_ifnum = INTERFACE_C;
//                break;
//            case 3:
//                ftdi_ifnum = INTERFACE_D;
//                break;
//            default:
//                ftdi_ifnum = INTERFACE_A;
//                break;
//        }
//
//        ftdi_init(&mpsse_ftdic);
//        ftdi_set_interface(&mpsse_ftdic, ftdi_ifnum);
//
        ftHandle = ftD2xx.open(0);
//        if (devstr != NULL) {
//            if (ftdi_usb_open_string(&mpsse_ftdic, devstr)) {
//                fprintf(stderr, "Can't find iCE FTDI USB device (device string %s).\n", devstr);
//                mpsse_error(2);
//            }
//        } else {
//            if (ftdi_usb_open(&mpsse_ftdic, 0x0403, 0x6010) && ftdi_usb_open(&mpsse_ftdic, 0x0403, 0x6014)) {
//                fprintf(stderr, "Can't find iCE FTDI USB device (vendor_id 0x0403, device_id 0x6010 or 0x6014).\n");
//                mpsse_error(2);
//            }
//        }
//
        mpsse_ftdic_open = true;

        try {
            ftD2xx.resetDevice(ftHandle);
        } catch (FtD2xxException ex) {
            throw new MpssseException(format("Failed to reset iCE FTDI USB device: %s", ex.getMessage()), ex);
        }

        try {
            ftD2xx.purgeBuffers(ftHandle, true, true);
        } catch (FtD2xxException ex) {
            throw new MpssseException(format("Failed to purge buffers on iCE FTDI USB device: %s", ex.getMessage()), ex);
        }

        try {
            mpsse_ftdi_latency = ftD2xx.getLatencyTimer(ftHandle);
        } catch (FtD2xxException ex) {
            throw new MpssseException(format("Failed to get latency timer: %s", ex.getMessage()), ex);
        }

        /* 1 is the fastest polling, it means 1 kHz polling */
        try {
            ftD2xx.setLatencyTimer(ftHandle, 1);
        } catch (FtD2xxException ex) {
            throw new MpssseException(format("Failed to set latency timer: %s", ex.getMessage()), ex);
        }

        mpsse_ftdic_latency_set = true;

        /* Enter MPSSE (Multi-Protocol Synchronous Serial Engine) mode. Set all pins to output. */
        try {
            ftD2xx.setBitMode(ftHandle, 0xff, ftd2xx.FT_BITMODE_MPSSE);
        } catch (FtD2xxException ex) {
            throw new MpssseException(format("Failed to set BITMODE_MPSSE on iCE FTDI USB device: %s", ex.getMessage()), ex);
        }

        // enable clock divide by 5
        sendByte(MC_TCK_D5);

        if (slow_clock) {
            // set 50 kHz clock
            sendByte(MC_SET_CLK_DIV);
            sendByte(119);
            sendByte(0x00);
        } else {
            // set 6 MHz clock
            sendByte(MC_SET_CLK_DIV);
            sendByte(0x00);
            sendByte(0x00);
        }
    }

    public void close() {
        ftD2xx.setLatencyTimer(ftHandle, mpsse_ftdi_latency);
        ftD2xx.setBitMode(ftHandle, 0, ftd2xx.FT_BITMODE_RESET);

        ftD2xx.close(ftHandle);
//        ftD2xx.deinit(&mpsse_ftdic);
    }

}
