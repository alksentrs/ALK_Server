package com.alksentrs.test.server;

import com.alksentrs.network.TCPServer;

public class AppServer {

    public static void main(String[] args) {

        TCPServer tcpServer = new TCPServer(1234);

        MyApplication myApplication = new MyApplication();

        tcpServer.attachConnectionObserver(myApplication);
        tcpServer.run();
    }

}
