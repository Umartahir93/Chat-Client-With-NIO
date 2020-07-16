package com.client.core;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

@Slf4j
@AllArgsConstructor
public class ChatWriter extends Thread{
    private final Socket socketConnectedWithServer;
    private final DataOutputStream messageWriter;


    @Override
    public void run() {
        log.info("Calling sendMessageToServer method");
        try{

            sendMessageToServer();

        }catch (Exception exception){
            log.error("Error occurred while trying to send message to server ");
            log.error("Exception Occurred ",exception);
            exception.printStackTrace();
        }
    }

    private void sendMessageToServer() throws IOException {
        log.info("Execution of sendMessageToServer started");
        String message = null;

        while (!socketConnectedWithServer.isClosed()){
            Scanner writeMessage = new Scanner(System.in);
            System.out.println("Write Message: ");
            message = writeMessage.nextLine();
            messageWriter.writeUTF(message);
        }

        log.info("Execution of sendMessageToServer ended");
    }
}
