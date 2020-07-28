package com.client.core;

import com.client.domain.MessageType;
import com.client.properties.Constants;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatReader {
    private static ChatReader reader = null;
    private static final ByteBuffer readByteBuffer = ByteBuffer.allocate(256*256);
    private byte [] messageReceivedFromServer;
    @Getter
    private final AtomicBoolean loggedInFlag = new AtomicBoolean(false);


    public static ChatReader getInstanceOfChatReader(){
        if(reader == null) reader = new ChatReader();
        return reader;
    }

    protected void readingFromServer(SocketChannel clientSocketConnectedWithServer) {
        try{
            log.info("Execution of readingFromServer started");

            log.info("Calling checkConnectionWithServer method ()");
            if (!checkConnectionWithServer(clientSocketConnectedWithServer)) return;

            log.info("Calling readingMessageFromBufferIntoByteArray method ()");
            messageReceivedFromServer = readingMessageFromBufferIntoByteArray();

            log.info("Calling extractMessageTypeFromPacketAndProcessIt method");
            extractMessageTypeFromPacketAndProcessIt();

        }catch (Exception exception){
            log.error("Exception occurred ",exception);
            exception.printStackTrace();
        }

        log.info("Execution of readingFromServer ended");
    }

    private boolean checkConnectionWithServer(SocketChannel clientSocketConnectedWithServer) throws IOException {
        log.info("Execution of checkConnectionIsStillOnWithServer() method started");

        if((clientSocketConnectedWithServer.read(readByteBuffer)) == -1){
            log.info("Connection is not on with server");
            log.info("Closing channel from client side");
            clientSocketConnectedWithServer.close();
            return false;
        }

        log.info("Connection is ON with Server");
        log.info("Execution of checkConnectionIsStillOnWithServer() method ended");
        return true;
    }

    private byte[] readingMessageFromBufferIntoByteArray() {
        log.info("Execution of readingMessageFromBufferIntoByteArray() method started");
        log.info("Flipping the buffer");
        readByteBuffer.flip();

        byte[]messageInBytes = new byte[readByteBuffer.limit()];
        log.info("Reading message from buffer");
        while (readByteBuffer.hasRemaining()) readByteBuffer.get(messageInBytes);

        log.info("Clearing the buffer");
        readByteBuffer.clear();

        log.info("Execution of readingMessageFromBuffer() method ended");
        return messageInBytes;
    }

    private void extractMessageTypeFromPacketAndProcessIt() {
        log.info("Execution of extractMessageTypeInformationFromPacketAndProcessIt method started");

        takeActionOnIdentifiedMessageType(
                identifyMessageType(
                        getStringFromByteArray(Constants.START_POSITION_OF_MESSAGE_TYPE_BYTES_IN_BYTES_ARRAY_INCLUSIVE,
                                Constants.END_POSITION_OF_MESSAGE_TYPE_BYTES_IN_BYTES_ARRAY_EXCLUSIVE
                        )
                )
        );

        log.info("Execution of extractMessageInformationFromPacketAndProcessIt method ended");

    }

    private String getStringFromByteArray(int start ,int end){
        log.info("Calling helper function getStringFromByteArray()");
        log.info("returning string from byte array");
        return Arrays.toString(Arrays.copyOfRange(messageReceivedFromServer ,start , end));
    }

    private MessageType identifyMessageType(String messageTypeValue){
        log.info("Calling identifyMessageType");
        log.info("Execution of identifyMessageType method started");
        return Enum.valueOf(MessageType.class,messageTypeValue);
    }

    private void takeActionOnIdentifiedMessageType(MessageType type) {
        log.info(" Execution of takeActionOnIdentifiedMessageType started");

        if(type.equals(MessageType.LOGIN)) loginAction();
        else if (type.equals(MessageType.LOGOUT)) logoutAction();
        else if (type.equals(MessageType.DATA)) messageDisplayingAction();

        log.info(" Execution of takeActionOnIdentifiedMessageType ended");
    }

    private void loginAction() {
        log.info("Calling loginAction");
        log.info("Execution of loginAction started");
        loggedInFlag.set(true);

        InternalCore.setUserIdOfClientAllocatedByServer(getIntFromByteArray(Constants.START_POSITION_OF_MESSAGE_DEST_INCLUSIVE,
                Constants.END_POSITION_OF_MESSAGE_DEST_EXCLUSIVE));

        System.out.println("You have logged in. Your id is "+InternalCore.getUserIdOfClientAllocatedByServer());
    }

    private void logoutAction() {
        log.info("Calling logoutAction");
        log.info("Execution logoutAction started");
        loggedInFlag.set(false);

        System.out.println("User with Id has logged out "+getIntFromByteArray(Constants.START_POSITION_OF_MESSAGE_DEST_INCLUSIVE,
                Constants.END_POSITION_OF_MESSAGE_DEST_EXCLUSIVE));
        log.info("Execution logoutAction ended");
    }

    private void messageDisplayingAction() {
        log.info("Execution of messageDisplayingAction started");

        log.info("Getting message length");
        int messageLength = getIntFromByteArray(Constants.START_POSITION_OF_MESSAGE_LENGTH_INCLUSIVE,
                Constants.END_POSITION_OF_MESSAGE_LENGTH_EXCLUSIVE);

        log.info("Getting message");
        String message = getStringFromByteArray(Constants.START_POSITION_OF_MESSAGE_INCLUSIVE,
                messageReceivedFromServer.length);

        log.info("Getting message source id");
        int messageSourceId = getIntFromByteArray(Constants.START_POSITION_OF_MESSAGE_SOURCE_ID_INCLUSIVE,
                Constants.END_POSITION_OF_MESSAGE_SOURCE_ID_EXCLUSIVE);

        if(messageLength != message.length()) System.out.println("Message which we received is corrupted");

        System.out.println(messageSourceId+" says: "+message);
        log.info("Execution of messageDisplayingAction ended");

    }

    private int getIntFromByteArray(int start ,int end){
        log.info("Calling getIntFromByteArray method");
        log.info("Execution of getIntFromByteArray method started");
        log.info("Execution of getIntFromByteArray method ended");
        return ByteBuffer.wrap(Arrays.copyOfRange(messageReceivedFromServer ,start , end)).getInt();
    }

}
