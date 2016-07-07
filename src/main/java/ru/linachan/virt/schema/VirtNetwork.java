package ru.linachan.virt.schema;

import java.io.Serializable;

public class VirtNetwork implements Serializable {
    public VirtNetworkType type;
    public String mac = null;
    public String source = null;
}
