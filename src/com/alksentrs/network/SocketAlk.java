package com.alksentrs.network;

import java.net.Socket;

public class SocketAlk {

    private long idle;
    private long last;
    public Socket socket;

    public SocketAlk(Socket socket) {
        this.socket = socket;
        idle = 0;
    }

    public long getIdle() {
        return idle;
    }

    public void setIdle(long idle) {
        this.idle = idle;
    }

    public long getLast() {
        return last;
    }

    public void setLast(long last) {
        this.last = last;
    }
}
