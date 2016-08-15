package com.linute.linute.MainContent.Chat;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Arman on 1/16/16.
 */
public class ChatRoom implements Parcelable{

    public String roomId;
    public String roomName;
    public String roomImage;
    public String lastMessage; //last message
    public boolean hasUnread;
    public long time;
    public int roomType;
    public boolean isMuted;
    public long mutedUntil;

    public final ArrayList<User> users;


    public static final int ROOM_TYPE_DM = 0;
    public static final int ROOM_TYPE_GROUP = 1;


    public ChatRoom(String roomId,
                    int roomType,
                    String roomName,
                    String roomImage,
                    ArrayList<User> users,
                    String lastMessage,
                    boolean hasUnread,
                    long time,
                    boolean isMuted,
                    long mutedUntil
    ) {
        this.roomId = roomId;
        this.roomType = roomType;
        this.roomName = roomName;
        this.roomImage = roomImage;
        this.users = users;
        this.lastMessage = lastMessage;
        this.hasUnread = hasUnread;
        this.time = time;
        this.isMuted = isMuted;
        this.mutedUntil = mutedUntil;
    }

    public ChatRoom(String mRoomId, ArrayList<User> users, String mLastMessage, boolean mHasUnread, long mTime) {
        this.roomId = mRoomId;
        this.users = users;
        this.lastMessage = mLastMessage;
        this.hasUnread = mHasUnread;
        this.time = mTime;
    }

    public String getRoomName(){
        if("".equals(roomName) || roomName == null) {

            if (users.size() == 1){
                return users.get(0).firstName + " " +users.get(0).lastName;
            }

            String name = "";
            for (int i = 0; i < users.size(); i++) {
                name += users.get(i).firstName;
                if (i != users.size() - 1) {
                    name += ", ";
                }
            }
            return name;
        }else{
            return roomName;
        }
    }

    public void setMute(boolean isMute, long muteUntil){
        this.isMuted = isMute;
        mutedUntil = muteUntil;
    }

    public boolean isDM(){
        return roomType == ROOM_TYPE_DM;
    }

    public void merge(ChatRoom rooms){
        lastMessage = rooms.lastMessage;
        time = rooms.time;
        hasUnread = rooms.hasUnread;
    }





    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ChatRoom)) {
            return false;
        }

        return roomId.equals(((ChatRoom) o).roomId);
    }


    @Override
    public int hashCode() {
        return roomId.hashCode();
    }

    public static ChatRoom fromJSON(JSONObject json) throws JSONException {
        JSONArray usersJson = json.getJSONArray("users");
        ArrayList<User> usersList = new ArrayList<User>();
        for(int u = 0;u<usersJson.length();u++){
            JSONObject userJson = usersJson.getJSONObject(u);
            usersList.add(new User(
                    userJson.getString("id"),
                    userJson.getString("firstName"),
                    userJson.getString("lastName"),
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


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

         parcel.writeString(roomId);
         parcel.writeInt(roomType);
         parcel.writeString(roomName);
         parcel.writeString(roomImage);
         parcel.writeList(users);
         parcel.writeString(lastMessage);
         parcel.writeInt(hasUnread ? 1 : 0);
         parcel.writeLong(time);
         parcel.writeInt(isMuted ? 1 : 0);
         parcel.writeLong(mutedUntil);


    }

    public static final Parcelable.Creator<ChatRoom> CREATOR = new Parcelable.Creator<ChatRoom>(){
        @Override
        public ChatRoom createFromParcel(Parcel parcel) {
            return new ChatRoom(
                        parcel.readString()  ,
                        parcel.readInt()     ,
                        parcel.readString()  ,
                        parcel.readString()  ,
                        parcel.readArrayList(User.class.getClassLoader()),
                        parcel.readString()  ,
                        parcel.readInt() == 1,
                        parcel.readLong()    ,
                        parcel.readInt() == 1,
                        parcel.readLong()
                        );
        }

        @Override
        public ChatRoom[] newArray(int i) {
            return new ChatRoom[i];
        }
    };
}
