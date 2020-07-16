package com.client.core;


import com.client.domain.PeerConnectionStatus;
import com.client.domain.User;
import com.client.properties.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;


@Slf4j
@RequiredArgsConstructor
public class InternalCore {

    private final int serverPort;
    private final String hostName;
    private Socket clientSocketConnectedWithServerSocket;
    private ObjectOutputStream userObjectWriter;
    private ObjectInputStream userObjectReader;
    private PeerConnectionStatus status;
    private User user;


    public void initiateApplication(){
        log.info("initiateApplication method execution started");
        try {

            log.info("Calling setUpInitialValuesForCommunicationWithServer method() to setUp connections");
            setUpInitialValuesForCommunicationWithServer();

            log.info("Calling getUserValuesByAskingForInput method() to take user input");
            getUserValuesByAskingForInput();

            log.info("Calling attemptConnectingUsers method() to take user input");
            while(!attemptConnectingUsers()){
                log.info("Calling giveNewValuesToReattemptConnectingUsers method() to take user input");
                giveNewValuesToReattemptConnectingUsers();
            }

            log.info("Calling createMessageWriterThread method() to create writer message thread");
            createMessageWriterThread();

            log.info("Calling createMessageWriterThread method() to create reader message thread");
            createMessageReaderThread();

        } catch (Exception exception) {
            log.error("Cause of Error is {}",exception.getCause().toString());
            exception.printStackTrace();
        }
        log.info("initiateApplication method execution ended");
    }

    private void setUpInitialValuesForCommunicationWithServer() throws IOException {
        log.info("setUpInitialValuesForCommunicationWithServer method() execution started");
        try{

            log.info("Creating a Socket that represents connection with socket at port {}",serverPort);
            clientSocketConnectedWithServerSocket = new Socket(hostName, serverPort);
            log.info("Connection established with socket");

            log.info("creating userObjectWriter object from socket");
            userObjectWriter = new ObjectOutputStream(clientSocketConnectedWithServerSocket.getOutputStream());
            log.info("created userObjectWriter object from socket");

            log.info("creating userObjectReader object from socket");
            userObjectReader = new ObjectInputStream(clientSocketConnectedWithServerSocket.getInputStream());
            log.info("created userObjectReader object from socket");

        }catch(Exception exception) {
            log.info("Check if socket is closed");
            if(!clientSocketConnectedWithServerSocket.isClosed()){
                log.info("Closing the socket");
                clientSocketConnectedWithServerSocket.close();
            }
        }
        log.info("Execution of setUpInitialValuesForCommunicationWithServer ended");
    }

    private void getUserValuesByAskingForInput(){

        Scanner input = new Scanner(System.in);

        System.out.println("\n" +
                "Please provide your full name: ");
        user.setFullName(input.nextLine());

        System.out.println("\n" +
                "Please provide your userName: ");
        user.setUserName(input.nextLine());

        System.out.println("\n" +
                "Please enter username to whom you want to chat with ");
        user.setChatWith(input.nextLine());

    }

    private boolean attemptConnectingUsers() throws IOException ,ClassNotFoundException {
        log.info("attemptConnectingUsers method execution started");

        userObjectWriter.writeObject(user);
        status = (PeerConnectionStatus) userObjectReader.readObject();

        if(StringUtils.equals(status.getStatus(),Constants.NOT_CONNECTED_STATUS)){
            log.info("User not found");
            log.info("attemptConnectingUsers method execution finished");
            return false;
        }

        log.info("User Found");
        log.info("attemptConnectingUsers method execution finished");
        return true;

    }

    public void giveNewValuesToReattemptConnectingUsers() {
        log.info("giveNewValuesToReattemptConnectingUsers method execution started");
        Scanner input = new Scanner(System.in);

        if(status.getErrorCode() ==  Constants.ERROR_CODE_USERNAME_ALREADY_TAKEN){
            log.info("Username given by user already taken by someone else");
            log.info("Asking for new username");
            System.out.println("Please provide new username");
            user.setUserName(input.nextLine());

        }else if(status.getErrorCode() == Constants.ERROR_CODE_CHATTER_USERNAME_NOT_FOUND){
            log.info("Cannot find user to whom you want to");
            log.info("Asking for new username to whom you want to connect");
            System.out.println("Please provide new username to whom you want to connect");
            user.setChatWith(input.nextLine());
        }

        log.info("giveNewValuesToReattemptConnectingUsers method execution finished");
    }

    private void createMessageWriterThread(){
        log.info("createMessageWriterThread method execution started");
        Thread messageReaderThread = new ChatWriter(clientSocketConnectedWithServerSocket);
        log.info("Going to create writer thread");
        messageReaderThread.start();
    }

    private void createMessageReaderThread() {
        log.info("createMessageReaderThread method execution started");
        Thread messageReaderThread = new ChatReader(clientSocketConnectedWithServerSocket);
        log.info("Making reader thread to be daemon");
        messageReaderThread.setDaemon(true);
        log.info("Goint to create a new reader thread");
        messageReaderThread.start();
    }
}