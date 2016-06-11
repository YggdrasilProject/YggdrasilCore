package ru.linachan.rpc;

public interface RPCService {

    RPCMessage dispatch(RPCMessage message);
}
