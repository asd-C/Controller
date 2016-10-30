package com.example.cc.controller.service;

import com.example.cc.controller.service.command.Command;

/**
 * Created by cc on 16-10-30.
 */

public class SMSParser {
    private static SMSParser instance;

    public synchronized static SMSParser getInstance() {
        if (instance == null) {
            instance = new SMSParser();
        }
        return instance;
    }

    private SMSParser() {}

    /**
     * TODO acomplish the function.
     */
    public synchronized Command parseSMS(String sms) {
        return new Command();
    }
}
