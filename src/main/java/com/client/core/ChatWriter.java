package com.client.core;


import com.client.domain.MessagePacket;
import com.client.domain.MessageType;
import com.client.properties.Constants;
import com.google.common.primitives.Bytes;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
public class ChatWriter extends Thread{
    private final SocketChannel clientSocketChannelConnectedWithServer;
    private byte [] messagePacketInBytes;

    @Override
    public void run() {
        try{
            log.info("Execution of writer thread started");
            log.info("Calling initiateLoginProcess()");
            initiateLoginProcess();

            while (clientSocketChannelConnectedWithServer.isOpen()){
                if(ChatReader.getInstanceOfChatReader().getLoggedInFlag().get()) startSendingMessagesToServer();
                else initiateLoginProcess();
            }

        }catch (BufferOverflowException bufferOverflowException){
            log.error("Exception occured while data into buffer");
            handleBufferOverflowException();


        }catch (Exception exception){
            log.error("Error occurred while trying to send message to server ");
            log.error("Exception Occurred ",exception);
            exception.printStackTrace();
        }
    }



    private void initiateLoginProcess() throws IOException {
        log.info("Execution of initiateLoginProcess started");

        log.info("Calling loginLogoutMenu method");
        loginLogoutMenu();

        log.info("Calling takeAndVerifyInput method");
        takeAndVerifyInput();

        log.info("Calling loggingInTheChatApplication method");
        loggingInTheChatApplication();

        log.info("Waiting for server to respond and mark us as logged in");
        waitingForServerLoginResponse(); //WILL UPDATE THIS LATER


    }

    private void loginLogoutMenu() {
        System.out.println("=========== Menu for Chat Client ===========");
        System.out.println("=========== Login:        Press \"1\" to login ===========");
        System.out.println("=========== Logout:       To logout anytime please type logout ===========");
        System.out.println("=========== Send Message: To Send Message please follow this protocol: ID | Type your message ===========");
    }

    private void takeAndVerifyInput() {
        log.info("Execution of takeAndVerifyInput method started");
        boolean correctInputFlag = false;
        Scanner scanner = new Scanner(System.in);


        while (!correctInputFlag){
            System.out.println("Please Type: ");
            String input = scanner.nextLine();

            if(StringUtils.equals(input,"1")){
                System.out.println("Logging you in. Please wait...");
                correctInputFlag=true;
            }else{
                System.out.println("Please give correct input. You are not logged in");
            }

        }

        log.info("Execution of takeAndVerifyInput method ended");
    }


    private void loggingInTheChatApplication() throws IOException {
        log.info("Execution of loggingInTheChatApplication started");

        log.info("Calling chatMessageBuilder method");
        messagePacketInBytes = convertMessagePacketIntoTheByteArray(loginMessageBuilder());
        log.info("Sending message to server");
        writingMessageToServer();

        log.info("Execution of loggingInTheChatApplication completed");

    }

    private MessagePacket loginMessageBuilder() {
        log.info("Execution of loggingInTheChatApplication completed");
        log.info("Returning login packet message");

        log.info("Calling initializeMagicNumberUsedForClientIdentification");
        InternalCore.initializeMagicNumberUsedForClientIdentification();

        return MessagePacket.builder().magicBytes(InternalCore.getMagicNumberUsedForClientIdentification()).
                messageType(MessageType.LOGIN).messageSourceId(Constants.NO_SOURCE_DEFINED).
                messageDestinationId(Constants.NO_DESTINATION_DEFINED).
                messageLength(Constants.NO_MASSAGE_LENGTH_DEFINED).
                message(Constants.LOGIN_MESSAGE).build();
    }

    private byte [] convertMessagePacketIntoTheByteArray(MessagePacket messagePacket) {
        log.info("Calling convertMessagePacketIntoTheByteArray method");
        log.info("Execution of convertMessagePacketIntoTheByteArray method started");

        List<Byte> byteArrayList = new ArrayList<>();

        byteArrayList.addAll(Bytes.asList(ByteBuffer.allocate(4).putInt(messagePacket.getMagicBytes()).array()));
        byteArrayList.addAll(Bytes.asList(messagePacket.getMessageType().getMessageCode()));
        byteArrayList.addAll(Bytes.asList(ByteBuffer.allocate(4).putInt(messagePacket.getMessageSourceId()).array()));
        byteArrayList.addAll(Bytes.asList(ByteBuffer.allocate(4).putInt(messagePacket.getMessageDestinationId()).array()));
        byteArrayList.addAll(Bytes.asList(ByteBuffer.allocate(4).putInt(messagePacket.getMessageLength()).array()));
        byteArrayList.addAll(Bytes.asList(messagePacket.getMessage().getBytes()));

        log.info("returning bytes array");
        log.info("Execution of convertMessagePacketIntoTheByteArray method ended");

        return Bytes.toArray(byteArrayList);

    }


    private void writingMessageToServer() throws IOException {
        log.info("Execution of writingMessageToServer started");
        log.info("Creating buffer with allocation of private backend space with size {}",messagePacketInBytes.length);
        ByteBuffer messageToServerBuffer = ByteBuffer.allocate(messagePacketInBytes.length);

        log.info("Putting data in bulk into buffer.");
        messageToServerBuffer.put(messagePacketInBytes);

        while(messageToServerBuffer.hasRemaining()){
            log.info("Sending message to the server");
            clientSocketChannelConnectedWithServer.write(messageToServerBuffer);
        }

        log.info("Message sent to server");
        log.info("Clearing the buffer");
        messageToServerBuffer.clear();
        log.info("Execution of sendMessageToServer ended");
    }

    //will change it later
    private void waitingForServerLoginResponse() {
        while (!ChatReader.getInstanceOfChatReader().getLoggedInFlag().get()){

        }
    }


    private void startSendingMessagesToServer() throws IOException {
        log.info("Execution of startSendingMessagesToServer method started");

        MessagePacket packet = createMessagePacket();
        messagePacketInBytes = convertMessagePacketIntoTheByteArray(packet);
        writingMessageToServer();
    }

    private MessagePacket createMessagePacket() {
        log.info("Execution of createMessagePacket method started");
        String message = getUserInputForMessage();

        MessageType messageType = analyzeUserInputToDecideMessageType(message);

        return messageType.equals(MessageType.LOGOUT)?
                createLogoutMessagePacket() : createDataMessagePacket();

    }

    private String getUserInputForMessage() {
        Scanner scanner = new Scanner(System.in);
        boolean validInput = false;
        String message = null;

        while (!validInput){
            System.out.println("Write Message (Destination ID | Your message: )");
            message = scanner.nextLine();
            String [] parts = message.split("|");
            if(parts[0].trim().length()>0) validInput = true;
        }

        return message;
    }

    private MessageType analyzeUserInputToDecideMessageType(String message) {
        if(StringUtils.equalsIgnoreCase(message.trim(),"logout")){
            return MessageType.LOGOUT;
        }
        return MessageType.DATA;
    }

    private MessagePacket createLogoutMessagePacket() {
        return MessagePacket.builder().magicBytes(InternalCore.getMagicNumberUsedForClientIdentification()).messageType(MessageType.LOGOUT).
                messageSourceId(InternalCore.getUserIdOfClientAllocatedByServer()).messageDestinationId(Constants.NO_DESTINATION_DEFINED).
                messageLength(Constants.NO_MASSAGE_LENGTH_DEFINED).message(Constants.LOGOUT_MESSAGE).build();
    }

    private MessagePacket createDataMessagePacket() {
        return MessagePacket.builder().magicBytes(InternalCore.getMagicNumberUsedForClientIdentification()).messageType(MessageType.DATA).
                messageSourceId(InternalCore.getUserIdOfClientAllocatedByServer()).messageDestinationId(Constants.NO_DESTINATION_DEFINED).
                messageLength(Constants.NO_MASSAGE_LENGTH_DEFINED).message(Constants.LOGOUT_MESSAGE).build();
    }



    private void handleBufferOverflowException() {
        log.info("Execution of handleBufferOverflowException started");
        log.info("Try again writing into buffer");

        try {
            writingMessageToServer();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (BufferOverflowException bufferOverflowException){
            log.error("We tried again writing into buffer but error persisted");
        }
    }



}
