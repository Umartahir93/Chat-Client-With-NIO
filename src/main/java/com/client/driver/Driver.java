package com.client.driver;

import com.client.core.InternalCore;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class Driver {

    public static void main(String[] args) {
       try {

           log.info("Driver Execution Started");
           InternalCore client = new InternalCore(5000, "localhost");
           log.info("Initiating Client Application");
           client.initiateApplication();

       }catch (Exception exception){
           log.error("Connection lost with server");
           log.error("Cause of error ",exception);
       }
    }
}