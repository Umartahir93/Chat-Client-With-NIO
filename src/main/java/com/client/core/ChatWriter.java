package com.client.core;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

@Slf4j
@RequiredArgsConstructor
public class ChatWriter extends Thread{
    private final SocketChannel clientSocketChannelConnectedWithServer;

    @Override
    public void run() {
        try{
            log.info("Execution of writer thread started");
            log.info("Calling writingMessageToServer() method");
            writingMessageToServer();

        }catch (Exception exception){
            log.error("Error occurred while trying to send message to server ");
            log.error("Exception Occurred ",exception);
            exception.printStackTrace();
        }
    }

    private void writingMessageToServer() throws IOException {
        log.info("Execution of writingMessageToServer started");
        byte [] message = null;

        while (clientSocketChannelConnectedWithServer.isOpen()){
            log.info("Take input from user");
            Scanner writeMessage = new Scanner(System.in);
            System.out.println("Write Message: ");
            message = writeMessage.nextLine().getBytes();
            log.info("Wrap that input into buffer");
            ByteBuffer buffer = ByteBuffer.wrap(message);

            try {
                while(buffer.hasRemaining()){
                    log.info("Sending message to the client");
                    clientSocketChannelConnectedWithServer.write(buffer);
                }

            } catch (Exception exception) {
                log.error("Exception occurred",exception);
                exception.printStackTrace();
            }

        }
        log.info("Execution of sendMessageToServer ended");
    }
}
