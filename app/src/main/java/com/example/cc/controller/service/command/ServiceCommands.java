package com.example.cc.controller.service.command;

/**
 * Created by cc on 16-10-30.
 */

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

    // ------------------------ Client Command ------------------------


    // ------------------------ Service Command ------------------------

    // Command for notifying the client that
    // there is a new sms, if the client is on
    public final static int MSG_NEW_SMS = 4;

    // ------------------------ Service Command ------------------------
}
