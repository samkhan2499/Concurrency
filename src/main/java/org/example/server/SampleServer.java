package org.example.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SampleServer {

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        try {
            ServerSocket serverSocket = new ServerSocket(1729);
            System.out.println("Server started on port 1729");

            while (true) {
                Socket conn = serverSocket.accept(); // Wait for a client to connect blocking method
                System.out.println("Client connected "+conn);
                executorService.execute(() ->  doWork(conn));
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            executorService.shutdown();
        }
    }

    private static void doWork(Socket conn) {
        try {
            InputStreamReader inputStream = new InputStreamReader(conn.getInputStream()) ;
            BufferedReader in = new BufferedReader(inputStream, 1024);
            Thread.sleep(10000);
            PrintWriter out = new PrintWriter(conn.getOutputStream(), true);
            String responseBody = "Hello from server\r\n";
            String response = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "Content-Length: " + responseBody.length() + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n" +
                    responseBody;
            out.print(response);
            out.flush();
            String clientMsg = in.readLine();
            System.out.println("Client says: "+clientMsg);
            in.close();
            out.close();
            conn.close();;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
