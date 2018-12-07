package com.alksentrs.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by ksander on 18/05/17.
 */
public class TCPServer implements Runnable {

    private int port;
    private ConnectionObserver connectionObserver;
    private ServerSocket serverSocket;
    private boolean running;

    public TCPServer(int port) {
        this.port = port;
        running = false;
    }

    public void attachConnectionObserver(ConnectionObserver connectionObserver) {
        this.connectionObserver = connectionObserver;
    }

    public String getAddress() {
        if (null!=serverSocket) {
            return serverSocket.getInetAddress().getHostAddress();
        }
        return "";
    }

    public void close() {
        try {
            if (null!=serverSocket) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        running = true;
        try {
            while (running) {
                try {
                    if (null == serverSocket) serverSocket = new ServerSocket(port);
                    if ((null != serverSocket) && (!serverSocket.isClosed())) {
                        Socket socket = serverSocket.accept();
                        if (null != connectionObserver) connectionObserver.updateConnection(socket);
                    } else {
                        running = false;
                    }
                } catch (IOException e) {
                    running = false;
                }
            }
        } finally {
            if (null!=serverSocket) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
