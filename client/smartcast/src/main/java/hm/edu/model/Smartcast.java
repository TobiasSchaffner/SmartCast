/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hm.edu.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

/**
 *
 * @author platypus
 */
public class Smartcast {

    /**
     * @param args the command line arguments
     */
    //public static void main(String[] args) throws IOException {
    //   Smartcast smartcast = new Smartcast();
    //  smartcast.client("192.168.0.13", 12345);
    //}
    private void client(String ip, int port) throws IOException {

        Socket smartCastSocket = new Socket(ip, port);
        BufferedReader reader = new BufferedReader(new InputStreamReader(smartCastSocket.getInputStream()));
        OutputStream writer = smartCastSocket.getOutputStream();

        String line;
        System.out.println("Enter");

        writer.write("JYJ2OuNz1zY\n".getBytes("UTF8"));
        writer.flush();
        System.out.println("sent url");

        System.out.println("reading");
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
            if (line.startsWith("duration", 1)) {
                break;
            }
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("Enter Command:\n");
            String command = br.readLine().replace("\n", "");
            if (command.equals("quit")) {
                break;
            }
            writer.write(command.getBytes("UTF8"));
            writer.flush();
        }
    }
}
