package org.voidspark.ftd2xx;

import static java.lang.String.format;

import org.voidspark.ftd2xx.lib.FT_DEVICE_LIST_INFO_NODE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jnr.ffi.Pointer;

public class TestFtd2xx {
    private static final Logger LOG = LoggerFactory.getLogger(TestFtd2xx.class);

    public static void main(String[] args) {
        final FtD2xx ftD2xx = new FtD2xx();

        long numDevs = ftD2xx.createDeviceInfoList();
        LOG.info(format("Number of devices is %d", numDevs));

        if (numDevs < 0) {
            LOG.info(format("No devices found"));
            return;
        }

        final FT_DEVICE_LIST_INFO_NODE[] nodes = ftD2xx.getDeviceInfoList(numDevs);

        for (int index = 0; index < numDevs; index++) {
            LOG.info(format("Dev %d:", index));
            LOG.info(format(" Flags=0x%x", nodes[index].Flags.intValue()));
            LOG.info(format(" Type=0x%x", nodes[index].Type.intValue()));
            LOG.info(format(" ID=0x%x", nodes[index].ID.intValue()));
            LOG.info(format(" LocId=0x%x", nodes[index].LocId.intValue()));
            LOG.info(format(" SerialNumber=%s", nodes[index].SerialNumber));
            LOG.info(format(" Description=%s", nodes[index].Description));
            LOG.info(format(" ftHandle=0x%x", nodes[index].ftHandle.intValue()));
        }

        final Pointer ftHandle = ftD2xx.open(0);
        final long driverVersion = ftD2xx.getDriverVersion(ftHandle);
        LOG.info(format("Driver version = %x.%02x.%02x", (driverVersion & (0xffL << 16)) >> 16, (driverVersion & (0xffL << 8)) >> 8, driverVersion & 0xffL));
        ftD2xx.close(ftHandle);
    }

}
