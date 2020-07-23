package com.client.core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatReader {

    protected static void readingFromServer(SocketChannel clientSocketConnectedWithServer) {
        try{
            log.info("Execution of readingFromServer started");
            log.info("Allocate a buffer for the message");

            ByteBuffer buffer = ByteBuffer.allocateDirect(2048);
            StringBuilder stringBuilder = new StringBuilder();

            while ((clientSocketConnectedWithServer.read(buffer))>0){
                log.info("Reading message from buffer");
                buffer.flip();

                //System.out.println("================= "+buffer+" ==================");

                byte[]bytes = new byte[buffer.limit()];
                buffer.get(bytes);
                stringBuilder.append(new String(bytes));
                if(!buffer.hasRemaining())
                    buffer.compact();
            }

            log.info("Message read from the buffer");
            System.out.println(stringBuilder);

        }catch (Exception exception){
            log.error("Exception occurred ",exception);
            exception.printStackTrace();
        }

        log.info("Execution of readingFromServer ended");
    }


}
