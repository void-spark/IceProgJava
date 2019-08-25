package org.voidspark.mpssse;
import static java.lang.String.format;

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

    /* MPSSE engine command definitions */

    /* Mode commands */
    private static final int MC_SETB_LOW = 0x80; /* Set Data bits LowByte */
    private static final int MC_READB_LOW = 0x81; /* Read Data bits LowByte */
    private static final int MC_SETB_HIGH = 0x82; /* Set Data bits HighByte */
    private static final int MC_READB_HIGH = 0x83; /* Read data bits HighByte */
    private static final int MC_LOOPBACK_EN = 0x84; /* Enable loopback */
    private static final int MC_LOOPBACK_DIS = 0x85; /* Disable loopback */
    private static final int MC_SET_CLK_DIV = 0x86; /* Set clock divisor */
    private static final int MC_FLUSH = 0x87; /* Flush buffer fifos to the PC. */
    private static final int MC_WAIT_H = 0x88; /* Wait on GPIOL1 to go high. */
    private static final int MC_WAIT_L = 0x89; /* Wait on GPIOL1 to go low. */
    private static final int MC_TCK_X5 = 0x8A; /* Disable /5 div, enables 60MHz master clock */
    private static final int MC_TCK_D5 = 0x8B; /* Enable /5 div, backward compat to FT2232D */
    private static final int MC_EN_3PH_CLK = 0x8C; /* Enable 3 phase clk, DDR I2C */
    private static final int MC_DIS_3PH_CLK = 0x8D; /* Disable 3 phase clk */
    private static final int MC_CLK_N = 0x8E; /* Clock every bit, used for JTAG */
    private static final int MC_CLK_N8 = 0x8F; /* Clock every byte, used for JTAG */
    private static final int MC_CLK_TO_H = 0x94; /* Clock until GPIOL1 goes high */
    private static final int MC_CLK_TO_L = 0x95; /* Clock until GPIOL1 goes low */
    private static final int MC_EN_ADPT_CLK = 0x96; /* Enable adaptive clocking */
    private static final int MC_DIS_ADPT_CLK = 0x97; /* Disable adaptive clocking */
    private static final int MC_CLK8_TO_H = 0x9C; /* Clock until GPIOL1 goes high, count bytes */
    private static final int MC_CLK8_TO_L = 0x9D; /* Clock until GPIOL1 goes low, count bytes */
    private static final int MC_TRI = 0x9E; /* Set IO to only drive on 0 and tristate on 1 */

    /* CPU mode commands */
    private static final int MC_CPU_RS = 0x90; /* CPUMode read short address */
    private static final int MC_CPU_RE = 0x91; /* CPUMode read extended address */
    private static final int MC_CPU_WS = 0x92; /* CPUMode write short address */
    private static final int MC_CPU_WE = 0x93; /* CPUMode write extended address */

    /* Transfer Command bits */

    /* All byte based commands consist of:
     * - Command byte
     * - Length lsb
     * - Length msb
     *
     * If data out is enabled the data follows after the above command bytes,
     * otherwise no additional data is needed.
     * - Data * n
     *
     * All bit based commands consist of:
     * - Command byte
     * - Length
     *
     * If data out is enabled a byte containing bitst to transfer follows.
     * Otherwise no additional data is needed. Only up to 8 bits can be transferred
     * per transaction when in bit mode.
     */

    /* b 0000 0000
     *   |||| |||`- Data out negative enable. Update DO on negative clock edge.
     *   |||| ||`-- Bit count enable. When reset count represents bytes.
     *   |||| |`--- Data in negative enable. Latch DI on negative clock edge.
     *   |||| `---- LSB enable. When set clock data out LSB first.
     *   ||||
     *   |||`------ Data out enable
     *   ||`------- Data in enable
     *   |`-------- TMS mode enable
     *   `--------- Special command mode enable. See mpsse_cmd enum.
     */

    private static final int MC_DATA_TMS = 0x40; /* When set use TMS mode */
    private static final int MC_DATA_IN = 0x20; /* When set read data (Data IN) */
    private static final int MC_DATA_OUT = 0x10; /* When set write data (Data OUT) */
    private static final int MC_DATA_LSB = 0x08; /* When set input/output data LSB first. */
    private static final int MC_DATA_ICN = 0x04; /* When set receive data on negative clock edge */
    private static final int MC_DATA_BITS = 0x02; /* When set count bits not bytes */
    private static final int MC_DATA_OCN = 0x01; /* When set update data on negative clock edge */

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

    public void error(int status) {
        checkRx();
        LOG.error(format("ABORT."));
        if (mpsse_ftdic_open) {
            if (mpsse_ftdic_latency_set) {
                ftD2xx.setLatencyTimer(ftHandle, mpsse_ftdi_latency);
            }
            ftD2xx.close(ftHandle);
        }
//        ftdi_deinit(&mpsse_ftdic);
        System.exit(status);
    }

    public int recvByte() {
        byte[] buffer = new byte[1];
        while (true) {
            int rc = ftD2xx.read(ftHandle, buffer, 1);
            if (rc < 0) {
                LOG.error(format("Read error."));
                error(2);
            }
            if (rc == 1) {
                break;
            }
            LockSupport.parkNanos(TimeUnit.MICROSECONDS.toNanos(100));
        }
        return buffer[0] & 0xff;
    }

    public void sendByte(int data) {
        byte[] buffer = { (byte) data };
        int rc = ftD2xx.write(ftHandle, buffer, 1);
        if (rc != 1) {
            LOG.error(format("Write error (single byte, rc=%d, expected %d).", rc, 1));
            error(2);
        }
    }

    public void sendSpi(byte[] data) {
        sendSpi(data, data.length);
    }
    
    public void sendSpi(byte[] data, int len) {
        if (len < 1) {
            return;
        }

        /* Output only, update data on negative clock edge. */
        sendByte(MC_DATA_OUT | MC_DATA_OCN);
        sendByte(len - 1);
        sendByte((len - 1) >> 8);

        int rc = ftD2xx.write(ftHandle, data, len);
        if (rc != len) {
            LOG.error(format("Write error (chunk, rc=%d, expected %d).", rc, len));
            error(2);
        }
    }

    public void xferSpi(byte[] data) {
        xferSpi(data, data.length);
    }

    public void xferSpi(byte[] data, int len) {
        if (len < 1) {
            return;
        }

        /* Input and output, update data on negative edge read on positive. */
        sendByte(MC_DATA_IN | MC_DATA_OUT | MC_DATA_OCN);
        sendByte(len - 1);
        sendByte((len - 1) >> 8);

        int rc = ftD2xx.write(ftHandle, data, len);
        if (rc != len) {
            LOG.error(format("Write error (chunk, rc=%d, expected %d).", rc, len));
            error(2);
        }

        for (int i = 0; i < len; i++) {
            data[i] = (byte) recvByte();
        }
    }

    public int xferSpiBits(int data, int len) {
        if (len < 1) {
            return 0;
        }

        /* Input and output, update data on negative edge read on positive, bits. */
        sendByte(MC_DATA_IN | MC_DATA_OUT | MC_DATA_OCN | MC_DATA_BITS);
        sendByte(len - 1);
        sendByte(data);

        return recvByte();
    }

    public void setGpio(int gpio, int direction) {
        sendByte(MC_SETB_LOW);
        sendByte(gpio); /* Value */
        sendByte(direction); /* Direction */
    }

    public int readbLow() {
        int data;
        sendByte(MC_READB_LOW);
        data = recvByte();
        return data;
    }

    public int readbHigh() {
        int data;
        sendByte(MC_READB_HIGH);
        data = recvByte();
        return data;
    }

    public void sendDummyBytes(int count) {
        // add 8 x count dummy bits (aka count bytes)
        sendByte(MC_CLK_N8);
        sendByte(count - 1);
        sendByte(0x00);

    }

    public void sendDummyBit() {
        // add 1  dummy bit
        sendByte(MC_CLK_N);
        sendByte(0x00);
    }

    public void init(/* int ifnum, const char *devstr, */ boolean slow_clock) {
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
            LOG.error(format("Failed to reset iCE FTDI USB device: %s", ex.getMessage()), ex);
            error(2);
        }

        try {
            ftD2xx.purgeBuffers(ftHandle, true, true);
        } catch (FtD2xxException ex) {
            LOG.error(format("Failed to purge buffers on iCE FTDI USB device: %s", ex.getMessage()), ex);
            error(2);
        }

        try {
            mpsse_ftdi_latency = ftD2xx.getLatencyTimer(ftHandle);
        } catch (FtD2xxException ex) {
            LOG.error(format("Failed to get latency timer: %s", ex.getMessage()), ex);
            error(2);
        }

        /* 1 is the fastest polling, it means 1 kHz polling */
        try {
            ftD2xx.setLatencyTimer(ftHandle, 1);
        } catch (FtD2xxException ex) {
            LOG.error(format("Failed to set latency timer: %s", ex.getMessage()), ex);
            error(2);
        }

        mpsse_ftdic_latency_set = true;

        /* Enter MPSSE (Multi-Protocol Synchronous Serial Engine) mode. Set all pins to output. */
        try {
            ftD2xx.setBitMode(ftHandle, 0xff, ftd2xx.FT_BITMODE_MPSSE);
        } catch (FtD2xxException ex) {
            LOG.error(format("Failed to set BITMODE_MPSSE on iCE FTDI USB device: %s", ex.getMessage()), ex);
            error(2);
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
