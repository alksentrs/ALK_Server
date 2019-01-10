package com.alksentrs.network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by ksander on 18/05/17.
 */
public class TCPClient {

    private Socket socket;
    private int port;
    private String serverAddress;
    private BufferedInputStream bis;
    private BufferedOutputStream bos;

    public TCPClient(String serverAddress, int port) {
        this.port = port;
        this.serverAddress = serverAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public void connect() throws IOException {
        socket = new Socket(serverAddress, port);
    }

    public BufferedInputStream getBufferedInputStream() throws IOException {
        bis = new BufferedInputStream(socket.getInputStream());
        return bis;
    }

    public BufferedOutputStream getBufferedOutputStream() throws IOException {
        bos = new BufferedOutputStream(socket.getOutputStream());
        return bos;
    }

    public void close() throws IOException {
        if (null!=bis) bis.close();
        if (null!=bos) bos.close();
        if ((null!=socket)&&(!socket.isClosed())) socket.close();
    }
}
