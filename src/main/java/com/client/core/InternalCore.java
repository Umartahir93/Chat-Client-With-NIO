package com.client.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import com.client.properties.Constants;


@Slf4j
@RequiredArgsConstructor
public class InternalCore {
    private final int serverSocketChannelPort;
    private final String hostName;
    private SocketChannel socketChannel;
    private static int magicNumberUsedForClientIdentification;
    @Setter @Getter
    private static int userIdOfClientAllocatedByServer;



    public void initiateApplication(){
        log.info("initiateApplication() method execution started");
        Selector selector = null;
        try {

            log.info("Calling setUpChannelAndSelectorForCommunicationWithServer method()");
            selector = setUpChannelAndSelectorForCommunicationWithServer();

            log.info("Calling createMessageWriterThread method()");
            createMessageWriterThread(socketChannel);

            log.info("Calling processSelectorReadEvent method()");
            processSelectorReadEvent(selector);

        } catch (Exception exception) {
            log.error("Cause of Error is ",exception);
            exception.printStackTrace();
        }finally {
            log.info("Executing finally block");
            finallyBlockExecutionForGraceFulShutdown(selector);
            log.info("initiateApplication method execution ended");
        }



    }

    private Selector setUpChannelAndSelectorForCommunicationWithServer() throws IOException {

        log.info("setUpChannelAndSelectorForCommunicationWithServer method() execution started");
        log.info("Opening a Socket channel that represents connection with server at port {}",serverSocketChannelPort);

        InetSocketAddress hostAddress = new InetSocketAddress(hostName,serverSocketChannelPort);
        socketChannel = SocketChannel.open(hostAddress);
        socketChannel.configureBlocking(false);

        log.info("Socket channel opened that represents connection with server at port {}",serverSocketChannelPort);

        log.info("Opening Selector");
        Selector selector = Selector.open();
        log.info("Selector opened");

        log.info("Registering socket with selector");
        socketChannel.register(selector, SelectionKey.OP_READ);


        log.info("Server Socket Channel registered with Selector");
        log.info("Execution of setUpChannelAndSelectorForCommunicationWithServer ended");

        return selector;
    }

    private void createMessageWriterThread(SocketChannel socketChannel){
        log.info("createMessageWriterThread method execution started");
        Thread messageWriterThread = new ChatWriter(socketChannel);
        log.info("Going to create writer thread");
        messageWriterThread.setDaemon(true);
        messageWriterThread.start();
    }


    private void processSelectorReadEvent(Selector selector) throws IOException {

        log.info("Started channelEventsListenerViaSelector execution method");
        while (socketChannel.isOpen()) {
            log.info("Waiting for event to occur");
            selector.select();

            for (SelectionKey selectionKey : selector.selectedKeys()) {
                log.info("Retrieving key's ready-operation set");
                selectionKey.readyOps();

                if (selectionKey.isValid() && selectionKey.isReadable()) {
                    log.info("Read event has occurred");
                    log.info("Calling reading from server method");
                    ChatReader.getInstanceOfChatReader().readingFromServer((SocketChannel) selectionKey.channel());
                }

                log.info("Removing the selection key");
                selector.selectedKeys().remove(selectionKey);
                log.info("Selection key removed");
            }
        }

    }

    public static int getMagicNumberUsedForClientIdentification() {
        return magicNumberUsedForClientIdentification;
    }

    public static void initializeMagicNumberUsedForClientIdentification(){
        log.info("Execution of initializeMagicNumberUsedForClientIdentification started");
        log.info("Generating client identification number");
        magicNumberUsedForClientIdentification = new Random(System.nanoTime()).nextInt(Constants.UPPER_LIMIT_FOR_RANDOM_NUMBER);
        log.info("Execution of getMagicNumberUsedForClientIdentification ended");
    }

    private void finallyBlockExecutionForGraceFulShutdown(Selector selector) {
        log.info("Execution of selectorShutdown started");
        if(selector != null && selector.isOpen()){

            for (SelectionKey selectionKey : selector.keys()) {
                log.info("Selecting Channel from key");
                SocketChannel channel = (SocketChannel) selectionKey.channel();

                try {
                    channel.close();
                    log.info("Socket Channel closed");

                } catch (IOException e) {
                    log.error("Error occurred while closing socket");
                    e.printStackTrace();
                }

                log.info("Cancelling selection key");
                selectionKey.cancel();

            }

            log.info("Closing the selector");
            try {
                selector.close();
                log.info("Selector closed");
            } catch (IOException e) {
                log.error("Error occurred while closing selector");
                e.printStackTrace();
            }
        }
    }

}