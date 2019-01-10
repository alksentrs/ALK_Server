package com.alksentrs.test;

import com.alksentrs.network.TCPClient;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

public class AppClient {

    public static void main(String[] args) {

        while (true) {

            TCPClient tcpClient = new TCPClient("127.0.0.1", 1234);
            try {
                System.out.println("Start");
                tcpClient.connect();
                BufferedOutputStream bos = tcpClient.getBufferedOutputStream();
                String request = "request 3";
                System.out.println("Sending: " + request);
                bos.write(request.getBytes());
                bos.flush();

                BufferedInputStream bis = tcpClient.getBufferedInputStream();
                byte[] contents = new byte[4096];
                int len = bis.read(contents);
                if (len > 0) {
                    String message = new String(contents, 0, len);
                    System.out.println(message);
                }

                request = "request 4";
                System.out.println("Sending: " + request);
                bos.write(request.getBytes());
                bos.flush();

                bis = tcpClient.getBufferedInputStream();
                contents = new byte[4096];
                len = bis.read(contents);
                if (len > 0) {
                    String message = new String(contents, 0, len);
                    System.out.println(message);
                }
                len = bis.read(contents);
                if (len > 0) {
                    String message = new String(contents, 0, len);
                    System.out.println(message);
                }

                tcpClient.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(2200);
            } catch (InterruptedException e) {
            }
        }
    }
}
