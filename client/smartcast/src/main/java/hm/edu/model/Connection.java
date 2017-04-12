/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hm.edu.model;

import hm.edu.controller.SmartCastFXMLController;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

/**
 *
 * @author platypus
 */
public class Connection {

    Socket smartCastSocket;
    BufferedReader reader;
    OutputStream writer;
    String ip;
    int port;
    public boolean up = false;
    String lastMessage = "Welcome to smartcast!";

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void initiate() {
        try {
            smartCastSocket = new Socket(ip, port);
            reader = new BufferedReader(new InputStreamReader(smartCastSocket.getInputStream()));
            writer = smartCastSocket.getOutputStream();
            up = true;
        } catch (IOException ex) {
            up = false;
        }
    }
    
    public void disconnect(){
        try {
            smartCastSocket.close();
            up = false;
        } catch (IOException ex) {
            up = false;
        }
    }

    public void write(String output) {
        try {
            writer.write(output.getBytes("UTF8"));
            writer.flush();

        } catch (IOException ex) {
            up = false;
        }
    }

    public long getDuration() {

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                lastMessage = line;
                if (line.startsWith("Error")) {
                    break;
                }
                if (line.startsWith("duration", 1)) {
                    return calcDuration(line.substring(11));
                }
            }
        } catch (IOException ex) {
            up = false;
        }
        return -1;
    }

    private long calcDuration(String string) {
        String timestampStr = string.replace('.', ':');
        String[] tokens = timestampStr.split(":");
        int hours = Integer.parseInt(tokens[0]);
        int minutes = Integer.parseInt(tokens[1]);
        int seconds = Integer.parseInt(tokens[2]);
        int miliseconds = Integer.parseInt(tokens[3]);
        return (3600 * hours + 60 * minutes + seconds + 2) * 1000 + miliseconds;
    }
    
    public String getLastMessage(){
        return lastMessage;
    }
    
}
