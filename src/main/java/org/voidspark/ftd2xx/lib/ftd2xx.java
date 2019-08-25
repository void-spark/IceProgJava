package org.voidspark.ftd2xx.lib;

import jnr.ffi.Pointer;
import jnr.ffi.annotations.In;
import jnr.ffi.annotations.Out;
import jnr.ffi.byref.ByteByReference;
import jnr.ffi.byref.IntByReference;
import jnr.ffi.byref.PointerByReference;
import jnr.ffi.types.u_int32_t;
import jnr.ffi.types.u_int8_t;

public interface ftd2xx {
    // Purge rx and tx buffers
    public static final int FT_PURGE_RX = 1;
    public static final int FT_PURGE_TX = 2;

    // Bit Modes
    public static final int FT_BITMODE_RESET = 0x00;
    public static final int FT_BITMODE_ASYNC_BITBANG = 0x01;
    public static final int FT_BITMODE_MPSSE = 0x02;
    public static final int FT_BITMODE_SYNC_BITBANG = 0x04;
    public static final int FT_BITMODE_MCU_HOST = 0x08;
    public static final int FT_BITMODE_FAST_SERIAL = 0x10;
    public static final int FT_BITMODE_CBUS_BITBANG = 0x20;
    public static final int FT_BITMODE_SYNC_FIFO = 0x40;

    @u_int32_t long FT_Open(int deviceNumber, @Out PointerByReference pHandle);

    @u_int32_t long FT_OpenEx(@In Pointer pArg1, @u_int32_t long flags, @Out PointerByReference pHandle);

    @u_int32_t long FT_ListDevices(@In Pointer pArg1, @In Pointer pArg2, @u_int32_t long flags);

    @u_int32_t long FT_Close(@In Pointer ftHandle);

    @u_int32_t long FT_Read(@In Pointer ftHandle, @Out byte[] lpBuffer, @u_int32_t long dwBytesToRead, @Out IntByReference lpBytesReturned);
    
    @u_int32_t long FT_Write(@In Pointer ftHandle, @In byte[] lpBuffer, @u_int32_t long dwBytesToWrite, @Out IntByReference lpBytesWritten);

    @u_int32_t long FT_ResetDevice(@In Pointer ftHandle);
    
    @u_int32_t long FT_Purge(@In Pointer ftHandle, @u_int32_t long Mask);

    @u_int32_t long FT_SetLatencyTimer(@In Pointer ftHandle, @u_int8_t int ucLatency);

    @u_int32_t long FT_GetLatencyTimer(@In Pointer ftHandle, @Out ByteByReference pucLatency);

    @u_int32_t long FT_SetBitMode(@In Pointer ftHandle, @u_int8_t int ucMask, @u_int8_t int ucEnable);

    @u_int32_t long FT_GetBitMode(@In Pointer ftHandle, @Out ByteByReference pucMode);
    
    @u_int32_t long FT_CreateDeviceInfoList(@Out IntByReference lpdwNumDevs);

    @u_int32_t long FT_GetDeviceInfoList(FT_DEVICE_LIST_INFO_NODE[] pDest, @Out IntByReference lpdw);
    
    @u_int32_t long FT_GetDeviceInfoDetail(@u_int32_t long dwIndex, @Out IntByReference lpdwFlags, @Out IntByReference lpdwType, @Out IntByReference lpdwID, @Out IntByReference lpdwLocId,
            @Out PointerByReference lpSerialNumber, @Out PointerByReference lpDescription, @Out PointerByReference pftHandle);

    @u_int32_t long FT_GetDriverVersion(@In Pointer ftHandle, @Out IntByReference lpdwVersion);

}
