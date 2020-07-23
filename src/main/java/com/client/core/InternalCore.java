package com.client.core;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;


@Slf4j
@RequiredArgsConstructor
public class InternalCore {

    private final int serverPort;
    private final String hostName;
    private DataInputStream inputStreamFromServer;
    private DataOutputStream outputStreamToServer;
    private Socket clientSocketConnectedWithServer;

    private static Map<String,Long> durationConnectionTime = new TreeMap<String,Long>();


    public void initiateApplication(){
        log.info("initiateApplication method execution started");
        try {

            temporarySimulationMethod();

            /*log.info("Calling preProcessor method() to setUp connections");
            setUpInitialValuesForCommunicationWithServer();

            log.info("Calling getUserValuesByAskingForInput method() to take user input");
            getUserValuesByAskingForInput();

            log.info("Calling createMessageWriterThread method() to create writer message thread");
            createMessageWriterThread();

            log.info("Calling createMessageWriterThread method() to create reader message thread");
            createMessageReaderThread();*/

        } catch (Exception exception) {
            log.error("Cause of Error is {}",exception.getCause().toString());
            exception.printStackTrace();
        }
        log.info("initiateApplication method execution ended");
    }

    /**
     * Remove this method later
     * @throws IOException
     */

    private void temporarySimulationMethod() throws IOException {
        log.info("Creating a Socket that represents connection with socket at port {}",serverPort);
        Instant starts = Instant.now();
        clientSocketConnectedWithServer = new Socket(hostName, serverPort);
        Instant ends = Instant.now();
        durationConnectionTime.put(Thread.currentThread().getName(),Duration.between(starts, ends).toMillis());

        if(durationConnectionTime.size() == 100){
            double totalseconds = 0;
            for(Map.Entry entry:durationConnectionTime.entrySet()){

                String key = (String) entry.getKey();
                Long value = (Long) entry.getValue();

                System.out.println("\n" +
                        "=== Client Name (Thread) : "+key + " Time taken in milli seconds: "+ value.doubleValue() + " In seconds time is: "+value.doubleValue()/1000);


            }

        }
    }

    private void setUpInitialValuesForCommunicationWithServer() throws IOException {
        log.info("preProcessor method() execution started");

        try{

            log.info("Creating a Socket that represents connection with socket at port {}",serverPort);
            clientSocketConnectedWithServer = new Socket(hostName, serverPort);
            log.info("Connection established with socket");

            log.info("creating dataInputStream object from socket");
            inputStreamFromServer = new DataInputStream(clientSocketConnectedWithServer.getInputStream());

            log.info("creating dataOutPutStream object from socket");
            outputStreamToServer = new DataOutputStream(clientSocketConnectedWithServer.getOutputStream());

            log.info("creating and starting background thread");


        }catch(Exception exception) {
            log.error("Exception occurred" , exception);
            exception.printStackTrace();

            log.info("Check if socket is closed");
            if(!clientSocketConnectedWithServer.isClosed()){
                log.info("Closing the socket");
                clientSocketConnectedWithServer.close();
            }
        }
        log.info("Execution of preProcessor ended");
    }

    private void getUserValuesByAskingForInput(){

    }

    private void createMessageWriterThread(){
        log.info("createMessageWriterThread method execution started");
        Thread messageReaderThread = new ChatWriter(clientSocketConnectedWithServer,outputStreamToServer);
        log.info("Going to create writer thread");
        messageReaderThread.start();
    }

    private void createMessageReaderThread() {
        log.info("createMessageReaderThread method execution started");
        Thread messageReaderThread = new ChatReader(inputStreamFromServer);
        log.info("Making reader thread to be daemon");
        messageReaderThread.setDaemon(true);
        log.info("Goint to create a new reader thread");
        messageReaderThread.start();
    }
}