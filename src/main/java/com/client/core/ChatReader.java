package com.client.core;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

@Slf4j
@RequiredArgsConstructor
public class ChatReader extends Thread {
    private final Socket clientSocketConnectedWithServerSocket;
    private DataInputStream readingFromServerStream;

    @Override
    public void run() {
        log.info("Reading thread execution started");

        try{
            log.info("Calling setInitialValuesForReadingFromServer method");
            setInitialValuesForReadingFromServer();

            log.info("Calling readingFromServer method");
            readingFromServer();
        }catch (Exception exception){
            log.error("Cause of Error is",exception);
            exception.printStackTrace();
        }

        log.info("Reading thread execution ended");

    }

    private void setInitialValuesForReadingFromServer() throws IOException {
        log.info("Execution of setInitialValuesForReadingFromServer method started");
        log.info("creating dataInputStream object from socket");
        readingFromServerStream = new DataInputStream(clientSocketConnectedWithServerSocket.getInputStream());
        log.info("created dataInputStream object from socket");
    }

    private void readingFromServer() throws IOException {
        log.info("Execution of readingFromServer started");

        while (!clientSocketConnectedWithServerSocket.isClosed()) {
            log.info("Waiting for messages from server");
            log.info("Got the message, now we are printing the message");
            System.out.println(readingFromServerStream.readUTF());
        }

        log.info("Execution of readingFromServer ended");
    }


}
