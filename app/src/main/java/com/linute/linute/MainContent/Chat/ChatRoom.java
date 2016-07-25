package com.linute.linute.MainContent.Chat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Arman on 1/16/16.
 */
public class ChatRoom {

    private String mRoomId;

    public final ArrayList<User> users;

    private String mLastMessage; //last message

    private boolean mHasUnread;
    //private int mUsersCount;
    //private ArrayList<ChatHead> mChatHeadList;

    private long mTime;

    private boolean mIsMuted;
    private long mMutedUntil;


/*public ChatRoom() {
        mRoomId = "";
        mUserId = "";
        mUserName = "";
        mLastMessage = "";
        mUserImage = "";
        mHasUnread = false;
        mTime = 0;
        //mChatHeadList = new ArrayList<>();
    }*/


    //, int usersCount, ArrayList<ChatHead> chatHeadList

    public ChatRoom(String roomId,
                    String userId,
                    String userName,
                    String lastMessage,
                    String userImage,
                    boolean hasUnread,
                    long time
    ) {

        mRoomId = roomId;
        users = new ArrayList<>();
        users.add(new User(userId, userName, userImage));
        mLastMessage = lastMessage;
        mHasUnread = hasUnread;
        mTime = time;

        //mUsersCount = usersCount;
        //mChatHeadList = chatHeadList;
    }



    public ChatRoom(String roomId,
                    String userId,
                    String userName,
                    String lastMessage,
                    String userImage,
                    boolean hasUnread,
                    long time,
                    boolean isMuted,
                    long mutedUntil
    ) {

        mRoomId = roomId;
        users = new ArrayList<>();
        users.add(new User(userId, userName, userImage));
        mLastMessage = lastMessage;
        mHasUnread = hasUnread;
        mTime = time;
        mIsMuted = isMuted;
        mMutedUntil = mutedUntil;
        //mUsersCount = usersCount;
        //mChatHeadList = chatHeadList;
    }

    public ChatRoom(String mRoomId, ArrayList<User> users, String mLastMessage, boolean mHasUnread, long mTime) {
        this.mRoomId = mRoomId;
        this.users = users;
        this.mLastMessage = mLastMessage;
        this.mHasUnread = mHasUnread;
        this.mTime = mTime;
    }


    public String getRoomName(){
        String name = "";
        for(int i = 0; i<users.size();i++){
            name += users.get(i).userName;
            if(i != users.size()-1){
                name += ", ";
            }
        }
        return name;
    }

    public String getRoomId() {
        return mRoomId;
    }



    public String getLastMessage() {
        return mLastMessage;
    }

    public void setLastMessage(String message)
    {
        mLastMessage = message;
    }

    public boolean hasUnread() {
        return mHasUnread;
    }

    public void setHasUnread(boolean hasUnread) {
        mHasUnread = hasUnread;
    }

    public long getTime() {
        return mTime;
    }

    public void setTime(long time) {
        mTime = time;
    }

    public void merge(ChatRoom rooms){
        mLastMessage = rooms.getLastMessage();
        mTime = rooms.getTime();
        mHasUnread = rooms.hasUnread();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ChatRoom)) {
            return false;
        }

        return mRoomId.equals(((ChatRoom) o).mRoomId);
    }


    @Override
    public int hashCode() {
        return mRoomId.hashCode();
    }

    public static ChatRoom fromJSON(JSONObject json) throws JSONException {
        JSONArray usersJson = json.getJSONArray("users");
        ArrayList<User> usersList = new ArrayList<User>();
        for(int u = 0;u<usersJson.length();u++){
            JSONObject userJson = usersJson.getJSONObject(u);
            usersList.add(new User(
                    userJson.getString("id"),
                    userJson.getString("fullName"),
                    userJson.getString("profileImage")
            ));
        }


        return new ChatRoom(
                json.getString("id"),
                usersList,
                "",
                false,
                0
        );
    }
}
