package com.alksentrs.network;

import java.io.IOException;
import java.net.Socket;

public interface ConnectionObserver {
    void updateConnection(Socket socket) throws IOException;
}
