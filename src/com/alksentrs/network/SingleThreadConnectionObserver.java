package com.alksentrs.network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

public abstract class SingleThreadConnectionObserver implements Runnable, ConnectionObserver {

    private int readBufferLength;
    private Vector<SocketAlk> sockets;
    private boolean running;
    private long timeout;

    public SingleThreadConnectionObserver() {
        readBufferLength = 4096;
        sockets = new Vector<>();
        timeout = 1000;
    }

    public void setReadBufferLength(int readBufferLength) {
        this.readBufferLength = readBufferLength;
    }

    protected abstract void updateRequest(Socket socket, byte [] request);
    protected abstract byte [] getAck();

    public void stop() {
        running = false;
        try {
            Iterator<SocketAlk> it = sockets.iterator();
            while (it.hasNext()) it.next().socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        running = true;
        try {
            while (running) {
                if (sockets.size() > 0) {
                    System.out.println("Connected: "+sockets.size());
                    Vector<SocketAlk> socketAlkVectorToRemove = new Vector<>();
                    Iterator<SocketAlk> it = sockets.iterator();
                    while (it.hasNext()) {
                        boolean done = true;
                        SocketAlk socketAlk = it.next();
                        BufferedInputStream bis = null;
                        try {
                            bis = new BufferedInputStream(socketAlk.socket.getInputStream());
                            int available = bis.available();
                            if ((null!=bis)&&( available > 0)) {
                                byte[] contents = new byte[readBufferLength];
                                int len = bis.read(contents);
                                if (len > 0) {
                                    byte[] contents_ = Arrays.copyOfRange(contents, 0, len);
                                    updateRequest(socketAlk.socket, contents_);
                                }
                                socketAlk.setLast(System.currentTimeMillis());
                            } else {
                                done = false;
                            }
                        } catch (IOException e) {
                            done = false;
                        }
                        if (!done) {
                            socketAlk.setIdle(System.currentTimeMillis()-socketAlk.getLast());
                            if (socketAlk.getIdle()>timeout) {
                                try {
                                    BufferedOutputStream bos = new BufferedOutputStream(socketAlk.socket.getOutputStream());
                                    bos.write(getAck());
                                    bos.flush();
                                    bos.close();
                                    socketAlk.setLast(System.currentTimeMillis());
                                } catch (IOException e) {
                                    socketAlkVectorToRemove.add(socketAlk);
                                }
                            }
                        }
                    }
                    if (socketAlkVectorToRemove.size()>0) {
                        Iterator<SocketAlk> it2 = socketAlkVectorToRemove.iterator();
                        while (it2.hasNext()) {
                            SocketAlk socketAlk = it2.next();
                            try {
                                socketAlk.socket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            sockets.remove(socketAlk);
                        }
                        socketAlkVectorToRemove.clear();
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                    }
                } else {
                    synchronized (this) {
                        try {
                            System.out.println("Sleeping");
                            this.wait();
                            System.out.println("Waked up");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        finally {
            Iterator<SocketAlk> it = sockets.iterator();
            while (it.hasNext()) {
                SocketAlk socketAlk = it.next();
                if ((null != socketAlk) && (null != socketAlk.socket) && (!socketAlk.socket.isClosed())) {
                    try {
                        socketAlk.socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            sockets.clear();
        }
        System.out.println("BYE");
    }

    public void updateConnection(Socket socket) throws IOException {
        SocketAlk socketAlk = new SocketAlk(socket);
        socketAlk.setLast(System.currentTimeMillis());
        sockets.add(socketAlk);
        synchronized(this) {
            notify();
        }
    }

    public boolean isClientConnected() {
        return (sockets.size()>0);
    }


    protected synchronized void sendData(Socket socket, String s) {
        byte [] b;
        try {
            b = s.getBytes("UTF-8");
            sendData(socket,b);
        } catch (UnsupportedEncodingException e) {

        } catch (IOException e) {
        }
    }

    protected synchronized void sendData(Socket socket, byte [] b) throws IOException {
        BufferedOutputStream bos = null;
            bos = new BufferedOutputStream(socket.getOutputStream());
            bos.write(b);
            bos.flush();
    }

    protected synchronized void sendData(Socket socket, byte b) throws IOException {
            BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
            bos.write(b);
            bos.flush();
    }

    protected synchronized void broadcastData(String s) {
        Vector<Socket> toRemove = new Vector<>();
        byte[] b = null;
        try {
            b = s.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if ( null!=b) {
            Iterator<SocketAlk> it = sockets.iterator();
            while (it.hasNext()) {
                Socket socket = it.next().socket;
                try {
                    sendData(socket, b);
                } catch (IOException e) {
                    toRemove.add(socket);
                }
            }
            if (toRemove.size()>0) remove(toRemove);
        }
    }

    protected synchronized void broadcastData(byte [] b) {
        Vector<Socket> toRemove = new Vector<>();
        Iterator<SocketAlk> it = sockets.iterator();
        while (it.hasNext()) {
            Socket socket = it.next().socket;
            try {
                BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
                bos.write(b);
                bos.flush();
            } catch (IOException e) {
                toRemove.add(socket);
            }
        }
        if (toRemove.size()>0) remove(toRemove);
    }

    protected synchronized void broadcastData(byte b) {
        Vector<Socket> toRemove = new Vector<>();
        Iterator<SocketAlk> it = sockets.iterator();
        while (it.hasNext()) {
            Socket socket = it.next().socket;
            try {
                BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
                bos.write(b);
                bos.flush();
            } catch (IOException e) {
                toRemove.add(socket);
            }
        }
        if (toRemove.size()>0) remove(toRemove);
    }

    public void close() {
        Iterator<SocketAlk> it = sockets.iterator();
        while (it.hasNext()) {
            try {
                Socket socket = it.next().socket;
                if (!socket.isClosed()) socket.close();
            } catch (IOException e) {
            }
        };
    }

    private void remove(Vector<Socket> socketsToRemove) {
        Iterator<Socket> it = socketsToRemove.iterator();
        while (it.hasNext()) {
            Socket socket = it.next();
            if (!socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
        sockets.removeAll(socketsToRemove);
    }

    public void remove(Socket socket) {
        if (sockets.contains(socket)) {
            if (!socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
            sockets.remove(socket);
        }
    }
}

