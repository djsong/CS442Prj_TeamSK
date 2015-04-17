/**
 * [CS442] Mobile Computing, Spring 2015
 *
 * Common packet definitions and functionalities shared by both server and client
 *
 * @author: DJ Song
 * */

 package com.example.djsong.mcprjfacmon;

/**
 * Created by DJSong on 2015-03-31.
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

// Any changes in the packet definition MUST be applied both SERVER and CLIENT!

/**
 * CommPacketDef_Cli_Req
 * Packet from client to server
 * Server determines which data to be sent based on this information.
 * */
public class CommPacketDef_Cli_Req
{
    /** Is this necessary for our scheme...? */
    private int mPacketID = CommPacketDef.PACKET_ID_CLI_REQ;
    public int GetPacketID() {return mPacketID;}

    public final static int REQ_ID_NONE = 0;
    public final static int REQ_ID_FAC_ITEM_USAGE_DATA = 1;
    // And else..?

    // This single class supports various request.. I would not implement in this way for the real application.

    public int mRequestID;

    //////////////////////////////////////////////////
    // Data for REQ_ID_FAC_ITEM_USAGE_DATA

    public int mReqUsageFloorNum;

    //////////////////////////////////////////////////

    /** Use this when you are sending */
    public CommPacketDef_Cli_Req(int InRequestID)
    {
        mRequestID = InRequestID;
    }

    /** Use this when you are receiving */
    public CommPacketDef_Cli_Req()
    {
        mRequestID = REQ_ID_NONE;
    }

    /** Set the members from serialized data stream
     * Should be consistent with SerializeOut */
    public boolean SerializeIn(DataInputStream InStream)
    {
        try {
            mPacketID = InStream.readInt();
            mRequestID = InStream.readInt();
            mReqUsageFloorNum = InStream.readInt();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /** Serialize the members to data stream
     * Should be consistent with SerializeIn */
    public DataOutputStream SerializeOut(OutputStream OutStreamInterface)
    {
        DataOutputStream ReturnStream = new DataOutputStream(OutStreamInterface);

        try {
            ReturnStream.writeInt(mPacketID);
            ReturnStream.writeInt(mRequestID);
            ReturnStream.writeInt(mReqUsageFloorNum);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return ReturnStream;
    }
}