package org.voidspark.ftd2xx.lib;

import jnr.ffi.Runtime;
import jnr.ffi.Struct;

public final class FT_DEVICE_LIST_INFO_NODE extends Struct {
    public FT_DEVICE_LIST_INFO_NODE(Runtime runtime) {
        super(runtime);
    }

    public Unsigned32 Flags = new Unsigned32();
    public Unsigned32 Type = new Unsigned32();
    public Unsigned32 ID = new Unsigned32();
    public Unsigned32 LocId = new Unsigned32();

    public String SerialNumber = new AsciiString(16);
    public String Description = new AsciiString(64);
    public Pointer ftHandle = new Pointer();
}
