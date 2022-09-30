package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Serialises requests
 * Yes passwords are sent in the clear but I don't care enough to do Kerberos
 * Authentication based on port/ip means local testing will be harder
 */
public class MessageFactory {

    public enum MessageType {
        JOIN_REQUEST,
        APPROVE_JOIN_REQUEST,
        APPROVE_JOIN_REPLY,
        KICK_REQUEST,
        RESET_REQUEST,
        SAVE_REQUEST,
        OPEN_REQUEST,
        QUIT_REQUEST,

        SUCCESS_REPLY,
        FAILURE_REPLY
    }
    public static String createMessage(MessageType t, String username, String password, String arg) {
        return t.toString() + ":" + username + ":" + password + ":" + arg;
    }

    public static String createReply(MessageType t, String arg) {
        return t.toString() + arg;
    }

    public static void writeMsg(BufferedWriter bufferedWriter, String msg) throws IOException {
        bufferedWriter.write(msg);
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }

    public static String readMsg(BufferedReader bufferedReader) throws IOException {
        String str = bufferedReader.readLine();
        if(str!=null) {
            return str;
        } else {
            throw new IOException();
        }
    }

}
