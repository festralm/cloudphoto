package ru.itis.vvot.cloudphoto.other;

public class Utils {
    public static int printErrorAndReturnExitCode(String error) {
        System.err.println(error);
        return 1;
    }

}
