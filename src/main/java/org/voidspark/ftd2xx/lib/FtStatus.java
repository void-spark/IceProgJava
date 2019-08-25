package org.voidspark.ftd2xx.lib;

import static java.lang.String.format;

public final class FtStatus {

    public static final int FT_OK = 0;
    public static final int FT_INVALID_HANDLE = 1;
    public static final int FT_DEVICE_NOT_FOUND = 2;
    public static final int FT_DEVICE_NOT_OPENED = 3;
    public static final int FT_IO_ERROR = 4;
    public static final int FT_INSUFFICIENT_RESOURCES = 5;
    public static final int FT_INVALID_PARAMETER = 6;
    public static final int FT_INVALID_BAUD_RATE = 7;
    public static final int FT_DEVICE_NOT_OPENED_FOR_ERASE = 8;
    public static final int FT_DEVICE_NOT_OPENED_FOR_WRITE = 9;
    public static final int FT_FAILED_TO_WRITE_DEVICE = 10;
    public static final int FT_EEPROM_READ_FAILED = 11;
    public static final int FT_EEPROM_WRITE_FAILED = 12;
    public static final int FT_EEPROM_ERASE_FAILED = 13;
    public static final int FT_EEPROM_NOT_PRESENT = 14;
    public static final int FT_EEPROM_NOT_PROGRAMMED = 15;
    public static final int FT_INVALID_ARGS = 16;
    public static final int FT_NOT_SUPPORTED = 17;
    public static final int FT_OTHER_ERROR = 18;
    public static final int FT_DEVICE_LIST_NOT_READY = 19;

    public static String getName(final long status) {
        if (status == 0) {
            return "FT_OK";
        } else if (status == 1) {
            return "FT_INVALID_HANDLE";
        } else if (status == 2) {
            return "FT_DEVICE_NOT_FOUND";
        } else if (status == 3) {
            return "FT_DEVICE_NOT_OPENED";
        } else if (status == 4) {
            return "FT_IO_ERROR";
        } else if (status == 5) {
            return "FT_INSUFFICIENT_RESOURCES";
        } else if (status == 6) {
            return "FT_INVALID_PARAMETER";
        } else if (status == 7) {
            return "FT_INVALID_BAUD_RATE";
        } else if (status == 8) {
            return "FT_DEVICE_NOT_OPENED_FOR_ERASE";
        } else if (status == 9) {
            return "FT_DEVICE_NOT_OPENED_FOR_WRITE";
        } else if (status == 10) {
            return "FT_FAILED_TO_WRITE_DEVICE";
        } else if (status == 11) {
            return "FT_EEPROM_READ_FAILED";
        } else if (status == 12) {
            return "FT_EEPROM_WRITE_FAILED";
        } else if (status == 13) {
            return "FT_EEPROM_ERASE_FAILED";
        } else if (status == 14) {
            return "FT_EEPROM_NOT_PRESENT";
        } else if (status == 15) {
            return "FT_EEPROM_NOT_PROGRAMMED";
        } else if (status == 16) {
            return "FT_INVALID_ARGS";
        } else if (status == 17) {
            return "FT_NOT_SUPPORTED";
        } else if (status == 18) {
            return "FT_OTHER_ERROR";
        } else if (status == 19) {
            return "FT_DEVICE_LIST_NOT_READY";
        }
        throw new IllegalStateException(format("Invalid FT_STATUS: %d", status));
    }
}
