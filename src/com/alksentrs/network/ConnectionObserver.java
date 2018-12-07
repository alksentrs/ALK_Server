package com.alksentrs.network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

public abstract class ConnectionObserver {

    private int readBufferLength;
    protected Vector<SocketManager> socketManageVector;

    public ConnectionObserver() {
        socketManageVector = new Vector();
        readBufferLength = 4096;
    }

    public void setReadBufferLength(int readBufferLength) {
        this.readBufferLength = readBufferLength;
    }

    protected abstract void updateRequest(Socket socket, byte [] request);

    private class SocketManager implements Runnable {

        private boolean running;
        private Socket socket;

        public SocketManager(Socket socket) {
            this.socket = socket;
        }

        public Socket getSocket() {
            return socket;
        }

        public void stop() {
            running = false;
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            running = true;
            try {

                while (running) {
                    try {
                        BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
                        byte[] contents = new byte[readBufferLength];
                        int len = bis.read(contents);
                        if (len>0) {
                            byte [] contents_ = Arrays.copyOfRange(contents,0,len);
                            updateRequest(socket, contents_);
                        }
                    } catch (IOException e) {
                    }
                }
            }
            finally {
                if ((null!=socket)&&(!socket.isClosed())) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                remove(this);
            }
        }
    }

    private void remove(SocketManager socketManager) {
        socketManager.stop();
        socketManageVector.remove(socketManager);
    }

    private void remove(Vector<SocketManager> socketManageVector) {
        Iterator<SocketManager> it = socketManageVector.iterator();
        while (it.hasNext()) {
            SocketManager socketManager = it.next();
            socketManager.stop();
            socketManageVector.remove(socketManager);
        }
    }

    public void updateConnection(Socket socket) throws IOException {
        SocketManager socketManager = new SocketManager(socket);
        socketManageVector.add(socketManager);
        Thread threadRequest = new Thread(socketManager);
        threadRequest.start();
    }

    public boolean isClientConnected() {
        return (socketManageVector.size()>0);
    }


    protected void sendData(Socket socket, String s) {
        byte [] b;
        try {
            b = s.getBytes("UTF-8");
            sendData(socket,b);
        } catch (UnsupportedEncodingException e) {

        }
    }

    protected void sendData(Socket socket, byte [] b) {
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(socket.getOutputStream());
            bos.write(b);
            bos.flush();
        } catch (IOException e1) {

        }
    }

    protected void sendData(Socket socket, byte b) {
        try {
            BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
            bos.write(b);
            bos.flush();
        } catch (IOException e) {

        }
    }

    protected void broadcastData(String s) {
        Iterator<SocketManager> it = socketManageVector.iterator();
        byte[] b = null;
        try {
            b = s.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if ( null!=b) {
            while (it.hasNext()) {
                SocketManager socketManager = it.next();
                Socket socket = socketManager.getSocket();
                sendData(socket, b);
            }
        }
    }

    protected void broadcastData(byte [] b) {
        Vector<SocketManager> toRemove = new Vector<>();
        Iterator<SocketManager> it = socketManageVector.iterator();
        while (it.hasNext()) {
            SocketManager socketManager = it.next();
            Socket socket = socketManager.getSocket();
            try {
                BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
                bos.write(b);
                bos.flush();
            } catch (IOException e) {
                toRemove.add(socketManager);
            }
        }
        if (toRemove.size()>0) remove(toRemove);
    }

    protected void broadcastData(byte b) {
        Vector<SocketManager> toRemove = new Vector<>();
        Iterator<SocketManager> it = socketManageVector.iterator();
        while (it.hasNext()) {
            SocketManager socketManager = it.next();
            Socket socket = socketManager.getSocket();
            try {
                BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
                bos.write(b);
                bos.flush();
            } catch (IOException e) {
                toRemove.add(socketManager);
            }
        }
        if (toRemove.size()>0) remove(toRemove);
    }
    
    public void close() {
        Iterator<SocketManager> it = socketManageVector.iterator();
        while (it.hasNext()) it.next().stop();
    }
}
