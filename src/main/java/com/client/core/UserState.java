package com.client.core;

import com.client.domain.Packet;
import com.client.utility.Adaptor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.exit;

/**
 * This class manages state of the user
 * and it contains state managing
 * functions
 *
 * @author umar.tahir@afiniti.com
 */

@Slf4j
public class UserState {

    @Getter
    private static final AtomicBoolean loggedInFlag = new AtomicBoolean(false);

    @Setter @Getter
    private static AtomicInteger magicNumberAssignedByServer= new AtomicInteger(0);

    @Setter @Getter
    private static AtomicInteger userIdOfClientAllocatedByServer = new AtomicInteger(0);

    /**
     * This class initiates login process for the
     * user.
     *
     */

    protected void initiateLoginProcess(SocketChannel socketChannel) {
        log.info("Execution of initiateLoginProcess started");

        log.info("Calling loginLogoutMenu method");
        loginLogoutMenu();

        log.info("Calling takeAndVerifyInput method");
        takeAndVerifyInput();

        log.info("Calling loggingInTheChatApplication method");
        loggingInTheChatApplication(socketChannel);

        log.info("Waiting for server to respond and mark us as logged in");
        waitingForServerLoginResponse(); //WILL UPDATE THIS LATER

        log.info("Execution of initiateLoginProcess ended");

    }

    /**
     *
     * This class initiates login and logout menu
     * and shows it to the user
     *
     */

    private void loginLogoutMenu() {
        System.out.println("=========== Menu for Chat Client ===========");
        System.out.println("=========== Login:        Press \"1\" to login ===========");
        System.out.println("=========== Logout:       To logout anytime please type logout ===========");
        System.out.println("=========== Close:        To close the program anytime please Press \"0\" ===========");
        System.out.println("=========== To Send Message after LOGIN please follow this protocol: ID | Type your message ===========");
    }


    /**
     * Here we will take input from user
     * specific to the menu and verify
     * it is correct or not
     *
     */
    private void takeAndVerifyInput() {
        log.info("Execution of takeAndVerifyInput method started");
        boolean correctInputFlag = false;
        Scanner scanner = new Scanner(System.in);

        while (!correctInputFlag) {
            System.out.println("Please Type: ");
            String input = scanner.nextLine();

            if (StringUtils.equals(input, "1")) {
                System.out.println("Logging you in. Please wait...");
                correctInputFlag = true;
            } else if (StringUtils.equals(input, "0")){
                System.out.println("Closing the program");
                exit(0);
            }
            else {
                System.out.println("Please give correct input. You are not logged in");
            }

        }

        log.info("Execution of takeAndVerifyInput method ended");
    }

    /**
     *
     * This method gets login packet and send it
     * to server
     *
     */

    private void loggingInTheChatApplication(SocketChannel socketChannel) {
        log.info("Execution of loggingInTheChatApplication started");

        log.info("Calling chatMessageBuilder method");
        byte [] messagePacketInBytes = Adaptor.getBytesArrayFromPacket(Adaptor.getLoginPacket());
        log.info("Sending message to server");
        int bytes = Writer.writingMessageToServer(messagePacketInBytes,socketChannel);
        log.info("Execution of loggingInTheChatApplication completed");

    }

    /**
     *
     * This method will keep thread busy until
     * user is logged in
     *
     * @apiNote after confirming you can use
     * wait() notify()
     *
     */
    private void waitingForServerLoginResponse() {
        while (!getLoggedInFlag().get());
    }

    /**
     *
     * This method take input from user, and returns
     * packet of that input
     *
     * @return packet
     *
     */

    public Packet takeAndAnalyzeUserInput(){
        Scanner scanner = new Scanner(System.in);
        boolean validInput = false;
        String message = null;

        while (!validInput) {
            System.out.println("Write Message (Destination ID | Your message: )");
            message = scanner.nextLine();
            validInput = isValidInput(message);
        }

        return !checkIsLogoutMessage(message)? Adaptor.getMessagePacket(message):Adaptor.getLogOutPacket();
    }

    /**
     * Check if user input is valid or not
     *
     * @param message user input
     * @return true or false
     *
     */

    private boolean isValidInput(String message) {
        log.info("Execution of isValidInput method started");
        String[] parts = message.split("\\|");
        return parts.length == 2 && parts[0].trim().matches("[0-9]+");
    }

    /**
     * Check if user wants to logout or not
     *
     * @param message user input
     *
     * @return true or false
     *
     */

    private boolean checkIsLogoutMessage(String message) {
        return StringUtils.equalsIgnoreCase(message.trim(), "logout");
    }


}
