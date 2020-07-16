package com.client.core;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

@Slf4j
@RequiredArgsConstructor
public class ChatWriter extends Thread{
    private final Socket clientSocketConnectedWithServerSocket;
    private DataOutputStream messageWriterStream;

    @Override
    public void run() {
        try{

            log.info("Calling setInitialValuesForCommunication method");
            setInitialValuesForWritingToServer();
            writingMessageToServer();

        }catch (Exception exception){
            log.error("Error occurred while trying to send message to server ");
            log.error("Exception Occurred ",exception);
            exception.printStackTrace();
        }
    }

    private void setInitialValuesForWritingToServer() throws IOException {
        log.info("setInitialValuesForCommunication method execution started");

        log.info("creating messageWriterStream object from socket");
        messageWriterStream = new DataOutputStream(clientSocketConnectedWithServerSocket.getOutputStream());
        log.info("created messageWriterStream object from socket");

        log.info("setInitialValuesForCommunication method execution ended");

    }

    private void writingMessageToServer() throws IOException {
        log.info("Execution of sendMessageToServer started");
        String message = null;

        while (!clientSocketConnectedWithServerSocket.isClosed()){
            Scanner writeMessage = new Scanner(System.in);
            System.out.println("Write Message: ");
            message = writeMessage.nextLine();
            messageWriterStream.writeUTF(message);
        }
        log.info("Execution of sendMessageToServer ended");
    }
}
