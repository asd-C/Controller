package com.example.cc.controller.service.command;

/**
 * Created by cc on 16-10-30.
 */
/**
 * Last used idx: 8
 * */
public class ServiceCommands {

    // ------------------------ Test Command ------------------------

    // Command for test
    public final static int DOSOMETHING = 2;

    // Command for test
    public final static int RETURN = 3;

    // ------------------------ Test Command ------------------------


    // ------------------------ Client Command ------------------------

    // Command for notifying the service that client is on
    public final static int MSG_CONNECT = 0;

    // Command for notifying the service that client is off
    public final static int MSG_DISCONNECT = 1;

    // Command for notifying the service that client is on
    public final static int MSG_START_RECEIVING_SMS = 5;

    // Command for notifying the service that client is off
    public final static int MSG_STOP_RECEIVING_SMS = 6;

    // ------------------------ Client Command ------------------------


    // ------------------------ Service Command ------------------------

    // Command for notifying the client that
    // there is a new sms, if the client is on
    public final static int MSG_NEW_SMS = 4;

    // Command for sending location to requester
    public final static int MSG_START_SENDING_LOCATION = 7;

    // // Command for stopping to send location to requester
    public final static int MSG_STOP_SENDING_LOCATION = 8;

    // ------------------------ Service Command ------------------------
}
