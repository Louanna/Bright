package com.movit.platform.mail.bean;

import android.content.Context;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.fsck.k9.mail.Address;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.mail.R;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by M on 2016/6/25.
 */
public class Recipient implements Serializable {

    public enum RecipientCryptoStatus {
        UNDEFINED,
        UNAVAILABLE,
        AVAILABLE_UNTRUSTED,
        AVAILABLE_TRUSTED;

        public boolean isAvailable() {
            return this == AVAILABLE_TRUSTED || this == AVAILABLE_UNTRUSTED;
        }
    }

    @Nullable // null means the address is not associated with a contact
    public final Long contactId;
    public final String contactLookupKey;

    @NonNull
    public Address address;
    public String addressLabel;

    @Nullable
    // null if the contact has no photo. transient because we serialize this manually, see below.
    public transient Uri photoThumbnailUri;

    @NonNull
    public RecipientCryptoStatus cryptoStatus;

    public String avatar;
    public String gender;

    public Recipient(@NonNull Address address) {
        this.address = address;
        this.contactId = null;
        this.cryptoStatus = RecipientCryptoStatus.UNDEFINED;
        this.contactLookupKey = null;
    }

    public Recipient(String name, String email, String addressLabel, long contactId, String lookupKey) {
        this.address = new Address(email, name);
        this.contactId = contactId;
        this.addressLabel = addressLabel;
        this.cryptoStatus = RecipientCryptoStatus.UNDEFINED;
        this.contactLookupKey = lookupKey;
    }

    public String getDisplayNameOrAddress() {
        String displayName = getDisplayName();
        if (displayName != null) {
            return displayName;
        }

        return address.getAddress().substring(0,address.getAddress().lastIndexOf("@"));
    }

    public String getDisplayNameOrUnknown(Context context) {
        String displayName = getDisplayName();
        if (displayName != null) {
            return displayName;
        }
        return context.getString(R.string.unknown_recipient);
    }

    public String getNameOrUnknown(Context context) {
        String name = address.getPersonal();
        if (name != null) {
            return name;
        }

        return context.getString(R.string.unknown_recipient);
    }

    private String getDisplayName() {
        if (TextUtils.isEmpty(address.getPersonal())) {
            return null;
        }

        String displayName = address.getPersonal();
        if (addressLabel != null) {
            displayName += " (" + addressLabel + ")";
        }

        return displayName;
    }

    @NonNull
    public RecipientCryptoStatus getCryptoStatus() {
        return cryptoStatus;
    }

    public void setCryptoStatus(@NonNull RecipientCryptoStatus cryptoStatus) {
        this.cryptoStatus = cryptoStatus;
    }

    public static Recipient fromUserInfo(UserInfo userInfo) {
        String personal = userInfo.getMail().substring(0,userInfo.getMail().lastIndexOf("@"));
        Address address = new Address(userInfo.getMail(), personal);
        Recipient recipient = new Recipient(address);
        recipient.avatar = userInfo.getAvatar();
        recipient.gender = userInfo.getGender();
        return recipient;
    }

    @Nullable
    public Uri getContactLookupUri() {
        if (contactId == null) {
            return null;
        }

        return ContactsContract.Contacts.getLookupUri(contactId, contactLookupKey);
    }

    @Override
    public boolean equals(Object o) {
        // Equality is entirely up to the address
        return o instanceof Recipient && address.equals(((Recipient) o).address);
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();

        // custom serialization, Android's Uri class is not serializable
        if (photoThumbnailUri != null) {
            oos.writeInt(1);
            oos.writeUTF(photoThumbnailUri.toString());
        } else {
            oos.writeInt(0);
        }
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        // custom deserialization, Android's Uri class is not serializable
        if (ois.readInt() != 0) {
            String uriString = ois.readUTF();
            photoThumbnailUri = Uri.parse(uriString);
        }
    }
}
