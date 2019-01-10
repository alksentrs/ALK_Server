package com.alksentrs.test.server;

import com.alksentrs.network.TCPServer;

public class AppServer {

    public static void main(String[] args) {

        TCPServer tcpServer = new TCPServer(1234);
        MyApplication myApplication = new MyApplication();

        tcpServer.attachConnectionObserver(myApplication);
        Thread threadConnection = new Thread(tcpServer);
        Thread threadReceiver = new Thread(myApplication);

        threadConnection.start();
        threadReceiver.start();
    }

}
