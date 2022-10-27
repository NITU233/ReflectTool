package com.nitu.utils;

/**
 * @author NITU
 */
public class DebugUtils {

    public static String getException(Throwable t) {
        return getException(t, false);
    }
    public static String getException(Throwable t, boolean showMessage) {
        String mess;
        if (showMessage) {
            mess = t.getMessage();
        } else {
            mess = t.toString();
        }
        StringBuilder certificateEncodingException = new StringBuilder(mess);
        StackTraceElement[] stackTrace = t.getStackTrace();
        int length = stackTrace.length;
        for (int i2 = 0; i2 < length; i2++) {
            certificateEncodingException.append("\r\nat ").append(stackTrace[i2]);
        }
        return new String(certificateEncodingException);
    }

}
