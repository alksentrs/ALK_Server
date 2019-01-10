package com.alksentrs.test.server;

import com.alksentrs.network.SingleThreadConnectionObserver;

import java.net.Socket;

public class MyApplication extends SingleThreadConnectionObserver {

    @Override
    protected void updateRequest(Socket socket, byte[] request) {

        String message = new String(request, 0, request.length);
        System.out.println(message);
        sendData(socket,"response("+message+")");
        broadcastData("broadcast");
    }

    @Override
    protected byte[] getAck() {
        return new byte[0];
    }

}
