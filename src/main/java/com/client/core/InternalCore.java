package com.client.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;


@Slf4j
@RequiredArgsConstructor
public class InternalCore {
    private final int serverSocketChannelPort;
    private final String hostName;
    private SocketChannel socketChannel;


    public void initiateApplication(){
        log.info("initiateApplication() method execution started");
        try {

            log.info("Calling setUpChannelAndSelectorForCommunicationWithServer method()");
            Selector selector = setUpChannelAndSelectorForCommunicationWithServer();

            log.info("Calling createMessageWriterThread method()");
            createMessageWriterThread(socketChannel);

            log.info("Calling processSelectorReadEvent method()");
            processSelectorReadEvent(selector);

        } catch (Exception exception) {
            log.error("Cause of Error is ",exception);
            exception.printStackTrace();
        }
        log.info("initiateApplication method execution ended");
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
                    ChatReader.readingFromServer((SocketChannel) selectionKey.channel());
                }

                log.info("Removing the selection key");
                selector.selectedKeys().remove(selectionKey);
                log.info("Selection key removed");
            }
        }

    }

}