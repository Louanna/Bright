package com.movit.platform.mail.util;

import com.fsck.k9.mail.ServerSettings;

/**
 * Created by Jamison on 2016/6/12.
 */
public class PortUtil {
    private static final int POP3_port_with_SSL = 995;
    private static final int POP3_port_without_SSL = 110;
    private static final int IMAP_port_with_SSL = 993;
    private static final int IMAP_port_without_SSL = 143;
    private static final int SMTP_port_with_SSL = 994;
    private static final int SMTP_port_without_SSL = 25;
    // private static final int Exchange_SMTP_port_with_SSL = 587;

    public static int getDefaultReceivePort(ServerSettings.Type type, boolean isSSL) {
        if (ServerSettings.Type.POP3 == type) {
            return isSSL ? POP3_port_with_SSL : POP3_port_without_SSL;
        } else {
            return isSSL ? IMAP_port_with_SSL : IMAP_port_without_SSL;
        }
    }

    public static int getDefaultSendPort(boolean isSSL) {
        return isSSL ? SMTP_port_with_SSL : SMTP_port_without_SSL;
    }


}
