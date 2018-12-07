package com.alksentrs.test.server;

import com.alksentrs.network.ConnectionObserver;

import java.net.Socket;

public class MyApplication extends ConnectionObserver {

    @Override
    protected void updateRequest(Socket socket, byte[] request) {


        String message = new String(request, 0, request.length);
        System.out.println(message);
        sendData(socket,"response");
        broadcastData("broadcast");
    }

}
