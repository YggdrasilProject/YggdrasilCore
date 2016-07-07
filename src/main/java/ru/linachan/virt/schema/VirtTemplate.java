package ru.linachan.virt.schema;

import java.io.Serializable;
import java.util.List;

public class VirtTemplate implements Serializable {

    public Long memory;
    public Short vcpu;
    public Long disk_size;
    public String image;
    public List<VirtNetwork> network;

}
