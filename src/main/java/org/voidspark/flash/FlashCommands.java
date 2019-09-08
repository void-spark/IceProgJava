package org.voidspark.flash;

/**
 * SPI Flash command bytes<br>
 * Source: Winbond W25Q80, W25Q16, W25Q32 datasheet.
 */
public final class FlashCommands {

    private FlashCommands() {
    }

    /**
     * Write Enable
     */
    public static final int WRITE_ENABLE = 0x06;

    /**
     * Write Disable
     */
    public static final int WRITE_DISABLE = 0x04;

    /**
     * Read Status Register-1
     */
    public static final int READ_STATUS_REGISTER_1 = 0x05;

    /**
     * Read Status Register-2
     */
    public static final int READ_STATUS_REGISTER_2 = 0x35;

    /**
     * Write Status Register
     */
    public static final int WRITE_STATUS_REGISTER = 0x01;

    /**
     * Page Program
     */
    public static final int PAGE_PROGRAM = 0x02;

    /**
     * Quad Page Program
     */
    public static final int QUAD_PAGE_PROGRAM = 0x32;

    /**
     * Block Erase (64KB)
     */
    public static final int BLOCK_ERASE_64KB = 0xD8;

    /**
     * Block Erase (32KB)
     */
    public static final int BLOCK_ERASE_32KB = 0x52;

    /**
     * Sector Erase (4KB)
     */
    public static final int SECTOR_ERASE_4KB = 0x20;

    /**
     * Chip Erase
     */
    public static final int CHIP_ERASE = 0xC7;

    /**
     * Erase Suspend
     */
    public static final int ERASE_SUSPEND = 0x75;

    /**
     * Erase Resume
     */
    public static final int ERASE_RESUME = 0x7A;

    /**
     * Power-down
     */
    public static final int POWER_DOWN = 0xB9;

    /**
     * High Performance Mode
     */
    public static final int HIGH_PERFORMANCE_MODE = 0xA3;

    /**
     * Mode Bit Reset
     */
    public static final int MODE_BIT_RESET = 0xFF;

    /**
     * Release Power down
     */
    public static final int RELEASE_POWER_DOWN = 0xAB;
    /**
     * Release HPM
     */
    public static final int RELEASE_HIGH_PERFORMANCE_MODE = 0xAB;
    /**
     * Device ID
     */
    public static final int DEVICE_ID = 0xAB;

    /**
     * Manufacturer/ Device ID
     */
    public static final int FC_MFGID = 0x90;

    /**
     * Read Unique ID
     */
    public static final int READ_UNIQUE_ID = 0x4B;

    /**
     * JEDEC ID
     */
    public static final int JEDEC_ID = 0x9F;

    /**
     * Read Data
     */
    public static final int READ_DATA = 0x03;

    /**
     * Fast Read
     */
    public static final int FAST_READ = 0x0B;

    /**
     * Fast Read Dual Output
     */
    public static final int FAST_READ_DUAL_OUTPUT = 0x3B;

    /**
     * Fast Read Dual I/O
     */
    public static final int FAST_READ_DUAL_IO = 0xBB;

    /**
     * Fast Read Quad Output
     */
    public static final int FAST_READ_QUAD_OUTPUT = 0x6B;

    /**
     * Fast Read Quad I/O
     */
    public static final int FAST_READ_QUAD_IO = 0xEB;
}
