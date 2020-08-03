package com.client.utility;

import com.client.core.UserState;
import com.client.domain.MessageType;
import com.client.domain.Packet;
import com.client.properties.Constants;
import com.google.common.primitives.Bytes;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Adaptor {

    /**
     * Method that converts byte array into packet class object
     *
     * @param message this is message in byte [] which needs to
     *                be converted
     * @return packet object
     */

    public static Packet getPacketFromByteArray(byte[] message) {
        log.info("Execution of convertByteArrayIntoPacket method started");

        int magicBytes = UtilityClass.getIntFromByteArray(message, Constants.START_OF_MAGIC_BYTES_INCLUSIVE, Constants.END_OF_MAGIC_BYTES_EXCLUSIVE);

        String messageTypeValue = UtilityClass.getStringFromByteArray(message, Constants.START_OF_MESSAGE_TYPE_INCLUSIVE, Constants.END_OF_MESSAGE_TYPE_EXCLUSIVE);
        MessageType messageType = MessageType.fromTextGetMessageType(messageTypeValue).get();

        int sourceId = UtilityClass.getIntFromByteArray(message, Constants.START_OF_SOURCE_ID_INCLUSIVE, Constants.END_OF_SOURCE_ID_EXCLUSIVE);
        int destId = UtilityClass.getIntFromByteArray(message, Constants.START_OF_DEST_ID_INCLUSIVE, Constants.END_OF_DEST_ID_EXCLUSIVE);

        int messageLength = UtilityClass.getIntFromByteArray(message, Constants.START_OF_MESSAGE_LENGTH_INCLUSIVE, Constants.END_OF_MESSAGE_LENGTH_EXCLUSIVE);
        String messageOfClient = UtilityClass.getStringFromByteArray(message, Constants.START_OF_MESSAGE_INCLUSIVE, message.length);

        log.info("Execution of convertByteArrayIntoPacket method ended");

        return Packet.builder().magicBytes(magicBytes).messageType(messageType).messageSourceId(sourceId).messageDestinationId(destId)
                .messageLength(messageLength).message(messageOfClient).build();

    }

    /**
     * This class returns login packet that is built
     * on specific login inputs
     *
     * @return Login Packet
     *
     */

    public static Packet getLoginPacket() {
        log.info("Execution of loggingInTheChatApplication completed");
        log.info("Returning login packet message");

        return Packet.builder().magicBytes(UserState.getMagicNumberAssignedByServer().get()).
                messageType(MessageType.LOGIN).messageSourceId(UserState.getUserIdOfClientAllocatedByServer().get()).
                messageDestinationId(Constants.SERVER_SOURCE_ID).
                messageLength(Constants.NO_MASSAGE_LENGTH_DEFINED).
                message(Constants.LOGIN_MESSAGE).build();
    }

    /**
     *
     * This method returns byte array from packet object
     *
     * @param packet input
     * @return byte []
     */

    public static byte[] getBytesArrayFromPacket(Packet packet) {
        log.info("Calling convertMessagePacketIntoTheByteArray method");
        log.info("Execution of convertMessagePacketIntoTheByteArray method started");

        List<Byte> byteArrayList = new ArrayList<>();

        byteArrayList.addAll(Bytes.asList(ByteBuffer.allocate(4).putInt(packet.getMagicBytes()).array()));
        byteArrayList.addAll(Bytes.asList(packet.getMessageType().getMessageCode().getBytes()));
        byteArrayList.addAll(Bytes.asList(ByteBuffer.allocate(4).putInt(packet.getMessageSourceId()).array()));
        byteArrayList.addAll(Bytes.asList(ByteBuffer.allocate(4).putInt(packet.getMessageDestinationId()).array()));
        byteArrayList.addAll(Bytes.asList(ByteBuffer.allocate(4).putInt(packet.getMessageLength()).array()));
        byteArrayList.addAll(Bytes.asList(packet.getMessage().getBytes()));

        log.info("returning bytes array");
        log.info("Execution of convertMessagePacketIntoTheByteArray method ended");

        return Bytes.toArray(byteArrayList);

    }

    /**
     * This method returns message packet which we need to send to user
     *
     * @param message input message
     * @return packet
     */

    public static Packet getMessagePacket(String message) {
        log.info("Execution of getMessagePacket started");
        String[] parts = message.split("\\|");

        return Packet.builder().magicBytes(UserState.getMagicNumberAssignedByServer().get()).messageType(MessageType.DATA).
                messageSourceId(UserState.getUserIdOfClientAllocatedByServer().get()).messageDestinationId(Integer.parseInt(parts[0].trim())).
                messageLength(parts[1].length()).message(parts[1]).build();

    }

    /**
     * This method returns logout packet to send to server
     *
     * @return packet
     *
     */

    public static Packet getLogOutPacket(){
        return Packet.builder().magicBytes(UserState.getMagicNumberAssignedByServer().get()).messageType(MessageType.LOGOUT).
                messageSourceId(UserState.getUserIdOfClientAllocatedByServer().get()).messageDestinationId(Constants.SERVER_SOURCE_ID).
                messageLength(Constants.NO_MASSAGE_LENGTH_DEFINED).message(Constants.LOGOUT_MESSAGE).build();

    }

}
