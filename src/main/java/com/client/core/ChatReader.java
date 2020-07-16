package com.client.core;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.DataInputStream;
import java.io.IOException;

@Slf4j
@AllArgsConstructor
public class ChatReader extends Thread {
    private DataInputStream in;



    @Override
    public void run() {
        log.info("Execution of thread started");

        String message = null;

        try {
            while (true) {
                log.info("Waiting for messages from server");
                message = in.readUTF();
                log.info("Got the message, now we are printing the message");
                System.out.println(message);
            }
        } catch (IOException exception) {
            log.error("Cause of Error is",exception);
            exception.printStackTrace();
        }
        log.info("Execution of sendMessageToOtherClient ended");



    }
}
