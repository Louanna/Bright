package com.movit.platform.common.entities;

import java.io.Serializable;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.user.entities.UserInfo;

@SuppressWarnings("serial")
public class MessageBean implements Serializable {

    private boolean isATMessage;
    private String msgId;//message uuid
    private String cuserId; // 自己的id
    private String friendId; // 对方的id
    private String content; // 包含了 time mtype的完整内容
    private String formateTime;
    private String groupType;//匿名  实名
    private String mtype;// T,P,A
    private int ctype; // 单人，多人，公众
    private int rsflag;// 1代表发送，0 接收
    int unReadCount = 0;
    int markReadStatus = -1;
    private int isread;
    private int isSend;// 判断是否当前正在上传，显示progress,显示发送失败等，1成功，0失败，2发送中

    private UserInfo userInfo;// 对方的用户信息

    private String roomId = "";// 房间的id
    private String subject;// 房间的主题
    private String tag;
    private String timestamp = "";
    private int insertFlag = 0;

    private boolean fromWechatUser = false;

    public MessageBean() {
        super();
    }

    @Override
    public String toString() {
        return "MessageBean [msgId=" + msgId + ",cuserId=" + cuserId + ", friendId=" + friendId
                + ", content=" + content + ", formateTime=" + formateTime
                + ", mtype=" + mtype + ", ctype=" + ctype + ", rsflag="
                + rsflag + ", unReadCount=" + unReadCount + ", isread="
                + isread + ", isSend=" + isSend + ", userInfo=" + userInfo
                + ", roomId=" + roomId + ", subject=" + subject + ", tag="
                + tag + ", timestamp=" + timestamp + ", fromWechatUser="
                + fromWechatUser + ", groupType=" + groupType + ", insertFlag=" + insertFlag + "]";
    }

    public MessageBean(String msgId, String cuserId, String friendId, String content,
                       String formateTime, String mtype, int ctype, int rsflag,
                       int unReadCount, int isread, int isSend, UserInfo userInfo,
                       String roomId, String subject, String tag, String timestamp) {
        super();
        this.msgId = msgId;
        this.cuserId = cuserId;
        this.friendId = friendId;
        this.content = content;
        this.formateTime = formateTime;
        this.mtype = mtype;
        this.ctype = ctype;
        this.rsflag = rsflag;
        this.unReadCount = unReadCount;
        this.isread = isread;
        this.isSend = isSend;
        this.userInfo = userInfo;
        this.roomId = roomId;
        this.subject = subject;
        this.tag = tag;
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        MessageBean bean = (MessageBean) o;
        if (bean.getCtype() == CommConstants.CHAT_TYPE_SINGLE
                && bean.getCtype() == this.ctype) {
            if (this.cuserId.equalsIgnoreCase(bean.getCuserId())) {
                if (this.friendId.equalsIgnoreCase(bean.getFriendId())
                        && bean.getCtype() == this.ctype
                        && this.content.equals(bean.getContent())
                        && this.msgId.equals(bean.getMsgId())) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else if (bean.getCtype() == CommConstants.CHAT_TYPE_GROUP
                && bean.getCtype() == this.ctype) {
            if (this.cuserId.equalsIgnoreCase(bean.getCuserId())) {
                if (this.roomId.equals(bean.getRoomId())
                        && this.content.equals(bean.getContent())
                        && this.msgId.equals(bean.getMsgId())
                        && this.rsflag == bean.getRsflag()
                        && bean.getCtype() == this.ctype) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else if (bean.getCtype() == CommConstants.CHAT_TYPE_SYSTEM
                && bean.getCtype() == this.ctype) {
            if (bean.getCtype() == this.ctype) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public int getMarkReadStatus() {
        return markReadStatus;
    }

    public void setMarkReadStatus(int markReadStatus) {
        this.markReadStatus = markReadStatus;
    }

    public boolean isATMessage() {
        return isATMessage;
    }

    public void setIsATMessage(boolean isATMessage) {
        this.isATMessage = isATMessage;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getCuserId() {
        return cuserId;
    }

    public void setCuserId(String cuserId) {
        this.cuserId = cuserId;
    }

    public String getFriendId() {
        return friendId;
    }

    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFormateTime() {
        return formateTime;
    }

    public void setFormateTime(String formateTime) {
        this.formateTime = formateTime;
    }

    public String getMtype() {
        return mtype;
    }

    public void setMtype(String mtype) {
        this.mtype = mtype;
    }

    public int getCtype() {
        return ctype;
    }

    public void setCtype(int ctype) {
        this.ctype = ctype;
    }

    public int getRsflag() {
        return rsflag;
    }

    public void setRsflag(int rsflag) {
        this.rsflag = rsflag;
    }

    public int getUnReadCount() {
        return unReadCount;
    }

    public void setUnReadCount(int unReadCount) {
        this.unReadCount = unReadCount;
    }

    public int getIsread() {
        return isread;
    }

    public void setIsread(int isread) {
        this.isread = isread;
    }

    public int getIsSend() {
        return isSend;
    }

    public void setIsSend(int isSend) {
        this.isSend = isSend;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isFromWechatUser() {
        return fromWechatUser;
    }

    public void setFromWechatUser(boolean fromWechatUser) {
        this.fromWechatUser = fromWechatUser;
    }

    public int getInsertFlag() {
        return insertFlag;
    }

    public void setInsertFlag(int insertFlag) {
        this.insertFlag = insertFlag;
    }
}
