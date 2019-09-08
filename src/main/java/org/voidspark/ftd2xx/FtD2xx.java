package org.voidspark.ftd2xx;

import static java.lang.String.format;

import org.voidspark.ftd2xx.lib.FT_DEVICE_LIST_INFO_NODE;
import org.voidspark.ftd2xx.lib.FtStatus;
import org.voidspark.ftd2xx.lib.ftd2xx;

import jnr.ffi.LibraryLoader;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.annotations.In;
import jnr.ffi.byref.ByteByReference;
import jnr.ffi.byref.IntByReference;
import jnr.ffi.byref.PointerByReference;

public class FtD2xx {

    private final ftd2xx lib;
    private final Runtime runtime;

    public FtD2xx() {

        final String libraryName = Runtime.getSystemRuntime().addressSize() == 64 ? "ftd2xx64" : "ftd2xx";
        lib = LibraryLoader.create(ftd2xx.class).search("native").search("src/main/native").stdcall().load(libraryName);
        runtime = Runtime.getRuntime(lib);
    }

    public long createDeviceInfoList() {
        final IntByReference numDevs = new IntByReference();
        doChecked(lib.FT_CreateDeviceInfoList(numDevs));
        return numDevs.getValue() & 0x00000000ffffffffL;
    }

    public FT_DEVICE_LIST_INFO_NODE[] getDeviceInfoList(long devCount) {
        final IntByReference numDevs = new IntByReference();

        final FT_DEVICE_LIST_INFO_NODE[] nodes = FT_DEVICE_LIST_INFO_NODE.arrayOf(runtime, FT_DEVICE_LIST_INFO_NODE.class, (int) devCount);
        doChecked(lib.FT_GetDeviceInfoList(nodes, numDevs));
        long devs = numDevs.getValue() & 0x00000000ffffffffL;
        if (devs != nodes.length) {
            throw new IllegalStateException(format("Unexpected device count, expected %d, got %d", nodes.length, devs));
        }
        return nodes;
    }

    public Pointer open(final int deviceNumber) {
        final PointerByReference pHandle = new PointerByReference();
        doChecked(lib.FT_Open(deviceNumber, pHandle));
        return pHandle.getValue();
    }

    public long getDriverVersion(final Pointer ftHandle) {
        final IntByReference driverVersion = new IntByReference();
        doChecked(lib.FT_GetDriverVersion(ftHandle, driverVersion));
        return driverVersion.getValue() & 0x00000000ffffffffL;
    }

    public void close(final Pointer ftHandle) {
        doChecked(lib.FT_Close(ftHandle));
    }

    public int read(final Pointer ftHandle, byte[] buffer, int count) {
        final IntByReference bytesReturned = new IntByReference();
        doChecked(lib.FT_Read(ftHandle, buffer, count, bytesReturned));
        return bytesReturned.getValue();
    }

    public int write(final Pointer ftHandle, byte[] buffer, int count) {
        final IntByReference bytesWritten = new IntByReference();
        doChecked(lib.FT_Write(ftHandle, buffer, count, bytesWritten));
        return bytesWritten.getValue();
    }

    public void resetDevice(final Pointer ftHandle) {
        doChecked(lib.FT_ResetDevice(ftHandle));
    }

    public void purgeBuffers(final Pointer ftHandle, boolean rx, boolean tx) {
        long mask = 0;
        if (rx) {
            mask |= ftd2xx.FT_PURGE_RX;
        }
        if (tx) {
            mask |= ftd2xx.FT_PURGE_TX;
        }
        doChecked(lib.FT_Purge(ftHandle, mask));
    }

    public void setLatencyTimer(@In Pointer ftHandle, int latency) {
        doChecked(lib.FT_SetLatencyTimer(ftHandle, latency));
    }

    public int getLatencyTimer(@In Pointer ftHandle) {
        final ByteByReference latency = new ByteByReference();
        doChecked(lib.FT_GetLatencyTimer(ftHandle, latency));
        return latency.getValue() & 0xff;
    }

    public void setBitMode(@In Pointer ftHandle, int mask, int enable) {
        doChecked(lib.FT_SetBitMode(ftHandle, mask, enable));
    }

    public int getBitMode(@In Pointer ftHandle) {
        final ByteByReference mode = new ByteByReference();
        doChecked(lib.FT_GetBitMode(ftHandle, mode));
        return mode.getValue() & 0xff;
    }

    private void doChecked(long ftStatus) {
        if (ftStatus != FtStatus.FT_OK) {
            throw new FtD2xxException(format("FT Status not OK: %s", FtStatus.getName(ftStatus)));
        }
    }
}
