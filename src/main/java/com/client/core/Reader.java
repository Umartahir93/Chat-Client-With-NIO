package com.client.core;

import com.client.domain.MessageType;
import com.client.domain.Packet;
import com.client.utility.Adaptor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;


/**
 * This class is a reader class and it
 * reads all the messages sent from client
 * and display them on console
 *
 * @author umar.tahir@afiniti.com
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Reader {
    private static final ByteBuffer readByteBuffer = ByteBuffer.allocate(256 * 256);
    private static Reader reader = null;

    /**
     * This method returns instance of the chatReader class
     *
     * @return chatReader
     */
    public static Reader getInstanceOfChatReader() {
        if (reader == null)
            reader = new Reader();

        return reader;
    }

    /**
     * In this method we read all the messages that our socket channel has
     * received from server. After identifying message we will decide its
     * correct course of processing. Here we also catching all the exceptions
     * from below methods and dealing with it.
     * <p>
     * Message Types can be
     * 1. LOGIN
     * 2. LOGOUT
     * 3. DATA
     * 4. ID_GENERATED
     *
     * @param clientSocketConnectedWithServer channel on which read events have occurred
     * @apiNote read partial problem deal with this later*
     */

    protected void readingFromServer(SocketChannel clientSocketConnectedWithServer) {
        log.info("Execution of readingFromServer started");

        try {

            log.info("Calling checkConnectionWithServer method ()");
            if (!checkConnectionWithServer(clientSocketConnectedWithServer))
                return;

            log.info("Connection with server is still up");

            log.info("Calling readingMessageFromBuffer method ()");
            byte[] messageReceivedFromServer = readingMessageFromBuffer();

            /*
             * Deal here with partial read and write problem
             */

            log.info("Calling convertByteArrayIntoPacket() method");
            Packet packet = Adaptor.getPacketFromByteArray(messageReceivedFromServer);

            log.info("Calling takePacketAndPerformAction on single message packet");
            takePacketAndPerformAction(packet);

        } catch (Exception exception) {
            log.error("Exception occurred ", exception);
            exception.printStackTrace();
        }

        log.info("Execution of readingFromServer ended");
    }

    /**
     * Here we check connection with server and is there any data to read
     * in case of no connection and zero data to read we close our connection
     * with server.
     *
     * @param clientSocketConnectedWithServer channel
     * @return true if connection is on else false
     * @throws IOException through above
     */

    private boolean checkConnectionWithServer(SocketChannel clientSocketConnectedWithServer) throws IOException {
        log.info("Execution of checkConnectionIsStillOnWithServer() method started");

        if ((clientSocketConnectedWithServer.read(readByteBuffer)) == -1) {
            log.info("Closing channel from client side");
            clientSocketConnectedWithServer.close();
            return false;
        }

        log.info("Connection is ON with Server");
        log.info("Execution of checkConnectionIsStillOnWithServer() method ended");

        return true;
    }

    /**
     * This method is reading message from buffer and returning
     * us byte [] arrays.
     *
     * @return byte array which contains message
     */

    private byte[] readingMessageFromBuffer() {
        log.info("Execution of readingMessageFromBuffer() method started");

        log.info("Flipping the buffer");
        readByteBuffer.flip();

        byte[] messageInBytes = new byte[readByteBuffer.limit()];
        log.info("Reading message from buffer");
        readByteBuffer.get(messageInBytes);

        log.info("Clearing the buffer");
        readByteBuffer.clear();

        log.info("Execution of readingMessageFromBuffer() method ended");
        return messageInBytes;
    }

    /**
     * This method take packet and analyze the packed to identify
     * correct course of action. Action can be based on login,
     * logout and Data message type
     *
     * @param packet contains all the information dervied from
     *               bytes array
     * @throws IOException exception occurred while sending message
     *                     through socket channel
     */

    private void takePacketAndPerformAction(Packet packet) throws IOException {
        log.info("Execution of takePacketAndPerformAction method started");

        if (packet.getMessageType().equals(MessageType.DATA)) {
            log.info("Message type is Data. Call its course of action to send message to desired client");
            acceptMessageFromServerAndDisplay(packet);

        } else if (packet.getMessageType().equals(MessageType.LOGIN)) {
            log.info("Message type is login. Calling its course of action");
            acceptMagicNumberFromServer(packet);

        } else if (packet.getMessageType().equals(MessageType.LOGOUT)) {
            log.info("Message type is logout. Calling its course of action");
            removeMagicNumberFromClient();

        } else if (packet.getMessageType().equals(MessageType.GENERATED_ID)) {
            log.info("Message type is GENERATED_ID. Accept ID from the server");
            acceptSourceIDFromServer(packet);
        }

        log.info("Execution of takePacketAndPerformAction method ended");
    }

    /**
     * This method displays message received from the client
     *
     * @param packet contains messages
     */

    private void acceptMessageFromServerAndDisplay(Packet packet) {
        log.info("Execution of acceptMessageFromServerAndDisplay started");
        System.out.println("Client with ID says: " + packet.getMessageSourceId() + " :" +
                packet.getMessage());
    }

    /**
     * This method accepts Magic number on logging in
     * and mark user as logged in.
     *
     * @param packet
     */

    private void acceptMagicNumberFromServer(Packet packet) {
        log.info("Execution of acceptMagicNumberFromServer started");
        log.info("Assigning magic numbers");

        UserState.getMagicNumberAssignedByServer().set(packet.getMagicBytes());
        UserState.getLoggedInFlag().set(true);

        System.out.println("========= You have been logged in ============");
        log.info("Execution of acceptMagicNumberFromServer ended");
    }


    /**
     * This method removes the magic number from client
     * on logging out and mark user has logged out
     */

    private void removeMagicNumberFromClient() {
        log.info("Execution of removeMagicNumberFromClient started");
        log.info("removing magic numbers");
        UserState.getMagicNumberAssignedByServer().set(0);
        UserState.getLoggedInFlag().set(false);

        System.out.println("========= You have been logged out ==========");

        log.info("Execution of removeMagicNumberFromClient ended");
    }


    /**
     * This action is taken when message type is generated id.
     * Here we accept that id from the server
     *
     * @param packet data received from server
     */

    private void acceptSourceIDFromServer(Packet packet) {
        log.info("Execution of acceptSourceIDFromServer started");
        UserState.getUserIdOfClientAllocatedByServer().set(packet.getMessageDestinationId());
        log.info("Allocated client id is: " + UserState.getUserIdOfClientAllocatedByServer());

        System.out.println("=======> YOU HAVE BEEN ASSIGNED THIS ID: " + UserState.getUserIdOfClientAllocatedByServer());

        log.info("Execution of acceptSourceIDFromServer ended");
    }


}
