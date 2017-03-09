package com.movit.platform.mail.util;

import com.fsck.k9.mail.Address;
import com.fsck.k9.mail.Message;
import com.fsck.k9.mail.MessagingException;
import com.fsck.k9.mail.internet.ListHeaders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReplyToParser {

    public static Address[] getRecipientsToReplyTo(Message message) {
        Address[] replyToAddresses = message.getReplyTo();
        if (replyToAddresses.length > 0) {
            return replyToAddresses;
        }

        Address[] listPostAddresses = ListHeaders.getListPostAddresses(message);
        if (listPostAddresses.length > 0) {
            return listPostAddresses;
        }

        return message.getFrom();
    }

    public static Address[] getRecipientsToReplyAllTo(Address myAddress,Message message) {

//        Address[] replyToAddresses = message.getReplyTo();
        Address[] replyToAddresses = message.getFrom();

        try {
            Address[] receiveAddresses = message.getRecipients(Message.RecipientType.TO);

            List<Address> aL = Arrays.asList(replyToAddresses);
            List<Address> bL = Arrays.asList(receiveAddresses);

            List<Address> resultList = new ArrayList();
            resultList.addAll(aL);
            resultList.addAll(bL);

            resultList.remove(myAddress);

            Address[] addresses = new Address[resultList.size()];

            resultList.toArray(addresses);

            return addresses;

        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return message.getFrom();
    }
}
