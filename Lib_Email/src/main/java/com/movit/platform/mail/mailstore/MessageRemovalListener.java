package com.movit.platform.mail.mailstore;

import com.fsck.k9.mail.Message;

public interface MessageRemovalListener {
    public void messageRemoved(Message message);
}
