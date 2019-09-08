package org.voidspark.mpssse;

public final class MpsseCommands {
    /* MPSSE engine command definitions */

    /* Mode commands */
    public static final int MC_SETB_LOW = 0x80; /* Set Data bits LowByte */
    public static final int MC_READB_LOW = 0x81; /* Read Data bits LowByte */
    public static final int MC_SETB_HIGH = 0x82; /* Set Data bits HighByte */
    public static final int MC_READB_HIGH = 0x83; /* Read data bits HighByte */
    public static final int MC_LOOPBACK_EN = 0x84; /* Enable loopback */
    public static final int MC_LOOPBACK_DIS = 0x85; /* Disable loopback */
    public static final int MC_SET_CLK_DIV = 0x86; /* Set clock divisor */
    public static final int MC_FLUSH = 0x87; /* Flush buffer fifos to the PC. */
    public static final int MC_WAIT_H = 0x88; /* Wait on GPIOL1 to go high. */
    public static final int MC_WAIT_L = 0x89; /* Wait on GPIOL1 to go low. */
    public static final int MC_TCK_X5 = 0x8A; /* Disable /5 div, enables 60MHz master clock */
    public static final int MC_TCK_D5 = 0x8B; /* Enable /5 div, backward compat to FT2232D */
    public static final int MC_EN_3PH_CLK = 0x8C; /* Enable 3 phase clk, DDR I2C */
    public static final int MC_DIS_3PH_CLK = 0x8D; /* Disable 3 phase clk */
    public static final int MC_CLK_N = 0x8E; /* Clock every bit, used for JTAG */
    public static final int MC_CLK_N8 = 0x8F; /* Clock every byte, used for JTAG */
    public static final int MC_CLK_TO_H = 0x94; /* Clock until GPIOL1 goes high */
    public static final int MC_CLK_TO_L = 0x95; /* Clock until GPIOL1 goes low */
    public static final int MC_EN_ADPT_CLK = 0x96; /* Enable adaptive clocking */
    public static final int MC_DIS_ADPT_CLK = 0x97; /* Disable adaptive clocking */
    public static final int MC_CLK8_TO_H = 0x9C; /* Clock until GPIOL1 goes high, count bytes */
    public static final int MC_CLK8_TO_L = 0x9D; /* Clock until GPIOL1 goes low, count bytes */
    public static final int MC_TRI = 0x9E; /* Set IO to only drive on 0 and tristate on 1 */

    /* CPU mode commands */
    public static final int MC_CPU_RS = 0x90; /* CPUMode read short address */
    public static final int MC_CPU_RE = 0x91; /* CPUMode read extended address */
    public static final int MC_CPU_WS = 0x92; /* CPUMode write short address */
    public static final int MC_CPU_WE = 0x93; /* CPUMode write extended address */

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

    public static final int MC_DATA_TMS = 0x40; /* When set use TMS mode */
    public static final int MC_DATA_IN = 0x20; /* When set read data (Data IN) */
    public static final int MC_DATA_OUT = 0x10; /* When set write data (Data OUT) */
    public static final int MC_DATA_LSB = 0x08; /* When set input/output data LSB first. */
    public static final int MC_DATA_ICN = 0x04; /* When set receive data on negative clock edge */
    public static final int MC_DATA_BITS = 0x02; /* When set count bits not bytes */
    public static final int MC_DATA_OCN = 0x01; /* When set update data on negative clock edge */

    private MpsseCommands() {
    }

}
