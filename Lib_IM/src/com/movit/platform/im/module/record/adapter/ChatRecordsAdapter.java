package com.movit.platform.im.module.record.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.BitmapAjaxCallback;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.entities.MessageBean;
import com.movit.platform.common.module.user.db.UserDao;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.utils.DateUtils;
import com.movit.platform.framework.utils.PicUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.framework.widget.swipeLayout.SwipeLayout;
import com.movit.platform.framework.widget.swipeLayout.SwipeLayout.DragEdge;
import com.movit.platform.framework.widget.swipeLayout.SwipeLayout.ShowMode;
import com.movit.platform.framework.widget.swipeLayout.adapter.BaseSwipeAdapter;
import com.movit.platform.im.R;
import com.movit.platform.im.constants.IMConstants;
import com.movit.platform.im.module.group.entities.Group;
import com.movit.platform.im.module.record.activity.ChatRecordsActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

public class ChatRecordsAdapter extends BaseSwipeAdapter {
    private Context context;
    private List<MessageBean> mData;
    private Handler handler;
    AQuery aq;

    public ChatRecordsAdapter(Context context, List<MessageBean> mData, Handler handler) {
        super();
        this.context = context;
        this.mData = mData;
        this.handler = handler;
        aq = new AQuery(context);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int arg0) {
        return mData.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }

    @Override
    public View generateView(final int position, ViewGroup parent) {
        View v = LayoutInflater.from(context).inflate(R.layout.im_item_chat_recent, null);
        SwipeLayout swipeLayout = (SwipeLayout) v.findViewById(getSwipeLayoutResourceId(position));
        swipeLayout.setShowMode(ShowMode.LayDown);
        swipeLayout.setDragEdge(DragEdge.Right);
        return v;
    }

    @Override
    public void fillValues(final int position, View converView) {
        ViewHolder holder = new ViewHolder();

        holder.name = (TextView) converView.findViewById(R.id.recent_item_name);
        holder.time = (TextView) converView.findViewById(R.id.recent_item_time);
        holder.photo = (ImageView) converView.findViewById(R.id.recent_item_icon);
        holder.content = (TextView) converView.findViewById(R.id.recent_item_content);
        holder.msgUnReadNum = (TextView) converView.findViewById(R.id.recent_item_msgUnReadNum);
        holder.pending = (ImageView) converView.findViewById(R.id.recent_item_pending);
        holder.markBtn = (Button) converView.findViewById(R.id.mark_read_status_btn);
        holder.delBtn = (Button) converView.findViewById(R.id.recent_del_btn);

        final MessageBean itemObject = (MessageBean) getItem(position);
        UserInfo userInfo = itemObject.getUserInfo();
        if (userInfo != null) {
            UserDao dao = UserDao.getInstance(context);
            userInfo = dao.getUserInfoById(userInfo.getId());
        }

        String mtype = itemObject.getMtype();

        if (mtype.equals(CommConstants.MSG_TYPE_TEXT)) {
            String json = itemObject.getContent();
            try {
                JSONObject jsonObject = new JSONObject(json);
                JSONObject content = jsonObject.getJSONObject("content");
                String text = content.getString("text");
                if (itemObject.getCtype() == CommConstants.CHAT_TYPE_GROUP) {
                    if (itemObject.getRsflag() == CommConstants.MSG_RECEIVE) {
                        if (CommConstants.GROUP_ADMIN.equals(itemObject
                                .getFriendId())) {
                            text = itemObject.getFriendId() + ":" + text;
                        } else if (itemObject.isFromWechatUser()) {
                            text = itemObject.getFriendId() + ":" + text;
                        } else {
                            switch (null != IMConstants.groupsMap.get(itemObject.getRoomId()) ?
                                    IMConstants.groupsMap.get(itemObject.getRoomId()).getType() : 0) {
                                case CommConstants.CHAT_TYPE_GROUP_PERSON:
                                    text = userInfo.getEmpCname() + ":" + text;
                                    break;
                                case CommConstants.CHAT_TYPE_GROUP_ANS:
                                    text = IMConstants.ansGroupMembers.get(itemObject.getRoomId() + "," + userInfo.getId()) + ":" + text;
                                    break;
                                default:
                                    break;
                            }
                        }

                    } else if (itemObject.getRsflag() == CommConstants.MSG_SEND) {
                        text = context.getString(R.string.me)+ text;
                    }
                }
                CharSequence charSeq = StringUtils
                        .convertNormalStringToSpannableString(context, text,
                                true, (int) holder.content.getTextSize() + 5);
                holder.content.setText(charSeq);
            } catch (JSONException e) {
                e.printStackTrace();
                holder.content.setText(context.getString(R.string.content_error));
            }
        } else if (mtype.equals(CommConstants.MSG_TYPE_PIC)) {
            if (itemObject.getCtype() == CommConstants.CHAT_TYPE_SINGLE) {
                holder.content.setText(CommConstants.PIC_TEXT);
            } else if (itemObject.getCtype() == CommConstants.CHAT_TYPE_GROUP) {
                if (itemObject.getRsflag() == CommConstants.MSG_RECEIVE) {
                    if (CommConstants.GROUP_ADMIN.equals(itemObject.getFriendId())) {
                        holder.content.setText(itemObject.getFriendId() + ":"
                                + CommConstants.PIC_TEXT);
                    } else {

                        String text = userInfo.getEmpCname() + ":" + CommConstants.PIC_TEXT;

                        switch (null != IMConstants.groupsMap.get(itemObject.getRoomId()) ? IMConstants.groupsMap.get(itemObject.getRoomId()).getType() : 0) {
                            case CommConstants.CHAT_TYPE_GROUP_PERSON:
                                text = userInfo.getEmpCname() + ":" + CommConstants.PIC_TEXT;
                                break;
                            case CommConstants.CHAT_TYPE_GROUP_ANS:
                                text = IMConstants.ansGroupMembers.get(itemObject.getRoomId() + "," + userInfo.getId()) + ":" + CommConstants.PIC_TEXT;
                                break;
                            default:
                                break;
                        }

                        holder.content.setText(text);
                    }
                } else if (itemObject.getRsflag() == CommConstants.MSG_SEND) {
                    holder.content.setText(context.getString(R.string.me) + CommConstants.PIC_TEXT);
                }
            }
        } else if (mtype.equals(CommConstants.MSG_TYPE_AUDIO)) {
            if (itemObject.getCtype() == CommConstants.CHAT_TYPE_SINGLE) {
                holder.content.setText(CommConstants.VOICE_TEXT);
            } else if (itemObject.getCtype() == CommConstants.CHAT_TYPE_GROUP) {
                if (itemObject.getRsflag() == CommConstants.MSG_RECEIVE) {
                    if (CommConstants.GROUP_ADMIN.equals(itemObject.getFriendId())) {
                        holder.content.setText(itemObject.getFriendId() + ":"
                                + CommConstants.VOICE_TEXT);
                    } else {
                        String text = userInfo.getEmpCname() + ":" + CommConstants.VOICE_TEXT;
                        switch (null != IMConstants.groupsMap.get(itemObject.getRoomId()) ? IMConstants.groupsMap.get(itemObject.getRoomId()).getType() : 0) {
                            case CommConstants.CHAT_TYPE_GROUP_PERSON:
                                text = userInfo.getEmpCname() + ":" + CommConstants.VOICE_TEXT;
                                break;
                            case CommConstants.CHAT_TYPE_GROUP_ANS:
                                text = IMConstants.ansGroupMembers.get(itemObject.getRoomId() + "," + userInfo.getId()) + ":" + CommConstants.VOICE_TEXT;
                                break;
                            default:
                                break;
                        }

                        holder.content.setText(text);
                    }
                } else if (itemObject.getRsflag() == CommConstants.MSG_SEND) {
                    holder.content.setText(context.getString(R.string.me) + CommConstants.VOICE_TEXT);
                }
            }
        } else if (mtype.equals(CommConstants.MSG_TYPE_METTING)) {
            if (itemObject.getCtype() == CommConstants.CHAT_TYPE_SINGLE) {
                holder.content.setText(CommConstants.MEETING_TEXT);
            } else if (itemObject.getCtype() == CommConstants.CHAT_TYPE_GROUP) {
                if (itemObject.getRsflag() == CommConstants.MSG_RECEIVE) {
                    if (CommConstants.GROUP_ADMIN.equals(itemObject.getFriendId())) {
                        holder.content.setText(itemObject.getFriendId() + ":"
                                + CommConstants.MEETING_TEXT);
                    } else {
                        String text = userInfo.getEmpCname() + ":" + CommConstants.MEETING_TEXT;
                        switch (null != IMConstants.groupsMap.get(itemObject.getRoomId()) ? IMConstants.groupsMap.get(itemObject.getRoomId()).getType() : 0) {
                            case CommConstants.CHAT_TYPE_GROUP_PERSON:
                                text = userInfo.getEmpCname() + ":" + CommConstants.MEETING_TEXT;
                                break;
                            case CommConstants.CHAT_TYPE_GROUP_ANS:
                                text = IMConstants.ansGroupMembers.get(itemObject.getRoomId() + "," + userInfo.getId()) + ":" + CommConstants.MEETING_TEXT;
                                break;
                            default:
                                break;
                        }

                        holder.content.setText(text);
                    }
                } else if (itemObject.getRsflag() == CommConstants.MSG_SEND) {
                    holder.content.setText(context.getString(R.string.me) + CommConstants.MEETING_TEXT);
                }
            }
        } else if (mtype.equals(CommConstants.MSG_TYPE_VIDEO)) {
            if (itemObject.getCtype() == CommConstants.CHAT_TYPE_SINGLE) {
                holder.content.setText(CommConstants.VIDEO_TEXT);
            } else if (itemObject.getCtype() == CommConstants.CHAT_TYPE_GROUP) {
                if (itemObject.getRsflag() == CommConstants.MSG_RECEIVE) {
                    if (CommConstants.GROUP_ADMIN.equals(itemObject.getFriendId())) {
                        holder.content.setText(itemObject.getFriendId() + ":"
                                + CommConstants.VIDEO_TEXT);
                    } else {
                        String text = userInfo.getEmpCname() + ":" + CommConstants.VIDEO_TEXT;
                        switch (null != IMConstants.groupsMap.get(itemObject.getRoomId()) ? IMConstants.groupsMap.get(itemObject.getRoomId()).getType() : 0) {
                            case CommConstants.CHAT_TYPE_GROUP_PERSON:
                                text = userInfo.getEmpCname() + ":" + CommConstants.VIDEO_TEXT;
                                break;
                            case CommConstants.CHAT_TYPE_GROUP_ANS:
                                text = IMConstants.ansGroupMembers.get(itemObject.getRoomId() + "," + userInfo.getId()) + ":" + CommConstants.VIDEO_TEXT;
                                break;
                            default:
                                break;
                        }
                        holder.content.setText(text);
                    }
                } else if (itemObject.getRsflag() == CommConstants.MSG_SEND) {
                    holder.content.setText(context.getString(R.string.me) + CommConstants.VIDEO_TEXT);
                }
            }
        } else if (mtype.equals(CommConstants.MSG_TYPE_FILE_1) || mtype.equals(CommConstants.MSG_TYPE_FILE_2)) {
            if (itemObject.getCtype() == CommConstants.CHAT_TYPE_SINGLE) {
                holder.content.setText(CommConstants.FILE_TEXT);
            } else if (itemObject.getCtype() == CommConstants.CHAT_TYPE_GROUP) {
                if (itemObject.getRsflag() == CommConstants.MSG_RECEIVE) {
                    if (CommConstants.GROUP_ADMIN.equals(itemObject.getFriendId())) {
                        holder.content.setText(itemObject.getFriendId() + ":"
                                + CommConstants.FILE_TEXT);
                    } else {
                        String text = userInfo.getEmpCname() + ":" + CommConstants.FILE_TEXT;
                        switch (null != IMConstants.groupsMap.get(itemObject.getRoomId()) ? IMConstants.groupsMap.get(itemObject.getRoomId()).getType() : 0) {
                            case CommConstants.CHAT_TYPE_GROUP_PERSON:
                                text = userInfo.getEmpCname() + ":" + CommConstants.FILE_TEXT;
                                break;
                            case CommConstants.CHAT_TYPE_GROUP_ANS:
                                text = IMConstants.ansGroupMembers.get(itemObject.getRoomId() + "," + userInfo.getId()) + ":" + CommConstants.FILE_TEXT;
                                break;
                            default:
                                break;
                        }
                        holder.content.setText(text);
                    }
                } else if (itemObject.getRsflag() == CommConstants.MSG_SEND) {
                    holder.content.setText(context.getString(R.string.me) + CommConstants.FILE_TEXT);
                }
            }
        } else if (mtype.equals(CommConstants.MSG_TYPE_LOCATION)) {
            if (itemObject.getCtype() == CommConstants.CHAT_TYPE_SINGLE) {
                holder.content.setText(CommConstants.LOCATION_TEXT);
            } else if (itemObject.getCtype() == CommConstants.CHAT_TYPE_GROUP) {
                if (itemObject.getRsflag() == CommConstants.MSG_RECEIVE) {
                    if (CommConstants.GROUP_ADMIN.equals(itemObject.getFriendId())) {
                        holder.content.setText(itemObject.getFriendId() + ":"
                                + CommConstants.LOCATION_TEXT);
                    } else {
                        String text = userInfo.getEmpCname() + ":" + CommConstants.LOCATION_TEXT;
                        switch (null != IMConstants.groupsMap.get(itemObject.getRoomId()) ? IMConstants.groupsMap.get(itemObject.getRoomId()).getType() : 0) {
                            case CommConstants.CHAT_TYPE_GROUP_PERSON:
                                text = userInfo.getEmpCname() + ":" + CommConstants.LOCATION_TEXT;
                                break;
                            case CommConstants.CHAT_TYPE_GROUP_ANS:
                                text = IMConstants.ansGroupMembers.get(itemObject.getRoomId() + "," + userInfo.getId()) + ":" + CommConstants.LOCATION_TEXT;
                                break;
                            default:
                                break;
                        }
                        holder.content.setText(text);
                    }
                } else if (itemObject.getRsflag() == CommConstants.MSG_SEND) {
                    holder.content.setText(context.getString(R.string.me) + CommConstants.LOCATION_TEXT);
                }
            }
        } else if (mtype.equals(CommConstants.MSG_TYPE_KICK)) {
            holder.content.setText(itemObject.getContent());
        } else if (mtype.equals(CommConstants.MSG_TYPE_INVITE)) {
            holder.content.setText(itemObject.getContent());
        } else if (mtype.equals(CommConstants.MSG_TYPE_MEMBERS_CHANGE)) {
            holder.content.setText(itemObject.getContent());
        } else if (mtype.equals(CommConstants.MSG_TYPE_DISSOLVE)) {
            holder.content.setText(itemObject.getContent());
        } else if (mtype.equals(CommConstants.MSG_TYPE_ADMIN)) {
            holder.content.setText(itemObject.getFriendId() + ":" + "[报表]");
        }

        holder.delBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                closeItem(position);
                handler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        handler.obtainMessage(
                                ChatRecordsActivity.POP_DEL_BTN,
                                position).sendToTarget();
                    }
                }, 200);
            }
        });

        if (itemObject.getCtype() != CommConstants.CHAT_TYPE_SYSTEM) {
            holder.markBtn.setVisibility(View.VISIBLE);
            holder.markBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    closeItem(position);
                    handler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            handler.obtainMessage(
                                    ChatRecordsActivity.MARK_READ_STATUS,
                                    position).sendToTarget();
                        }
                    }, 200);
                }
            });
            if (itemObject.getMarkReadStatus() == -1) {
                holder.msgUnReadNum.setText(itemObject.getUnReadCount() + "");
                if (itemObject.getUnReadCount() == 0) {
                    holder.msgUnReadNum.setVisibility(View.GONE);
                    holder.markBtn.setText(context.getString(R.string.mark_unread));
                } else {
                    holder.msgUnReadNum.setVisibility(View.VISIBLE);
                    holder.markBtn.setText(context.getString(R.string.mark_read));
                }
            } else if (itemObject.getMarkReadStatus() == 1) {
                holder.msgUnReadNum.setText(itemObject.getMarkReadStatus() + "");
                holder.msgUnReadNum.setVisibility(View.VISIBLE);
                holder.markBtn.setText(context.getString(R.string.mark_read));
            } else {
                holder.msgUnReadNum.setVisibility(View.GONE);
                holder.markBtn.setText(context.getString(R.string.mark_unread));
            }
        } else {
            holder.markBtn.setVisibility(View.GONE);
            if (itemObject.getUnReadCount() != 0) {
                holder.msgUnReadNum.setText(itemObject.getUnReadCount() + "");
                holder.msgUnReadNum.setVisibility(View.VISIBLE);
            } else {
                holder.msgUnReadNum.setVisibility(View.GONE);
            }
        }

        holder.time.setText(DateUtils.getFormateDateWithTime(itemObject
                .getFormateTime()));
        AQuery aQuery = aq.recycle(holder.photo);
        holder.photo.setTag("photo" + position);
        int picId = R.drawable.avatar_male;
        if (itemObject.getCtype() == CommConstants.CHAT_TYPE_GROUP) {
            String room = itemObject.getRoomId();
            Group group = IMConstants.groupsMap.get(room);
            int groupIcon = null!=group?group.getType():-1;
            switch (groupIcon) {
                case 0:
                    aQuery.id(holder.photo).image(R.drawable.group_admin);
                    break;
                case 1:
                    aQuery.id(holder.photo).image(R.drawable.group_org);
                    break;
                case 2:
                    aQuery.id(holder.photo).image(R.drawable.group_task);
                    break;
                case 3:
                    File file = new File(CommConstants.SD_DATA_PIC+group.getId()+"_temp.jpg");
                    if(null!=file && file.exists()){
                        aQuery.id(holder.photo).image(file,256);
                    }else{
                        aQuery.id(holder.photo).image(R.drawable.group_personal);
                    }
                    break;
                case 4:
                    aQuery.id(holder.photo).image(R.drawable.group_ans);
                    break;
                default:
                    break;
            }
        } else if (itemObject.getCtype() == CommConstants.CHAT_TYPE_SINGLE) {
            picId = R.drawable.avatar_male;
            if (context.getString(R.string.boy).equals(userInfo.getGender())) {
                picId = R.drawable.avatar_male;
            } else if (context.getString(R.string.girl).equals(userInfo.getGender())) {
                picId = R.drawable.avatar_female;
            }
            String avatarName = userInfo.getAvatar();
            String avatarUrl = "";
            if (StringUtils.notEmpty(avatarName)) {
                avatarUrl = avatarName;
            }
            if (StringUtils.notEmpty(avatarUrl)) {
                BitmapAjaxCallback callback = new BitmapAjaxCallback();

                //为了适配其他项目
                if (avatarUrl.startsWith("http")) {
                    callback.animation(AQuery.FADE_IN_NETWORK).round(10)
                            .fallback(picId).url(avatarUrl)
                            .memCache(true).fileCache(true).targetWidth(128);
                } else {
                    callback.animation(AQuery.FADE_IN_NETWORK).round(10)
                            .fallback(picId).url(CommConstants.URL_DOWN + avatarUrl)
                            .memCache(true).fileCache(true).targetWidth(128);
                }

                aQuery.id(holder.photo).image(callback);
            } else {
                Bitmap bitmap = PicUtils.getRoundedCornerBitmap(context, picId,
                        10);
                holder.photo.setImageBitmap(bitmap);
            }
        } else if (itemObject.getCtype() == CommConstants.CHAT_TYPE_SYSTEM) {
            aQuery.id(holder.photo).image(R.drawable.group_task);
        }

        if (itemObject.getCtype() == CommConstants.CHAT_TYPE_SINGLE) {
            holder.name.setText(userInfo.getEmpCname());
        } else if (itemObject.getCtype() == CommConstants.CHAT_TYPE_GROUP) {
            holder.name.setText(itemObject.getSubject());
        } else if (itemObject.getCtype() == CommConstants.CHAT_TYPE_SYSTEM) {
            holder.name.setText(context.getString(R.string.notification));
//			if (mtype.equals(CommConstants.MSG_TYPE_KICK)) {
//				holder.name.setText("移除通知");
//			} else if (mtype.equals(CommConstants.MSG_TYPE_INVITE)) {
//				holder.name.setText("邀请加入");
//			} else if (mtype.equals(CommConstants.MSG_TYPE_MEMBERS_CHANGE)) {
//				holder.name.setText("消息通知");
//			} else if (mtype.equals(CommConstants.MSG_TYPE_DISSOLVE)) {
//				holder.name.setText("解散通知");
//			}
        }
    }

    public final class ViewHolder {
        public ImageView photo;
        public TextView name;
        public TextView time;
        public TextView content;
        public TextView msgUnReadNum;
        public ImageView pending;
        public Button markBtn;
        public Button delBtn;
    }
}
