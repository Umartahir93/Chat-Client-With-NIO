package com.client.simulator;

import com.client.core.InternalCore;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Simulator {
    public static void main(String[] args) {
        try {

            Runnable runnable =
                    () -> {
                        log.info("The name of user is: ");
                        String userName = "userName";
                        InternalCore core = new InternalCore(5000, "localhost");
                        core.initiateApplication();
                    };

            for(int i = 0 ;i < 100 ; i++){
                Thread thread = new Thread(runnable);
                thread.start();
            }

        }catch (Exception exception){
            log.error("Connection lost with server");
            log.error("Cause of error ",exception);
        }
    }
}

