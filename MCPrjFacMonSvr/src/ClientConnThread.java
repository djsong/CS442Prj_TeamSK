/**
 * [CS442] Mobile Computing, Spring 2015
 *
 * Server to client connection and communication thread
 *
 * @author: DJ Song
 * */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * ClientConnThread
 * Processing client accept and opening communication channel.
 * */
public class ClientConnThread extends Thread
{
	/**
	 * Client should use the same number.
	 * */
	int mServerPortNum = 9001;
	
	ServerSocket mAcceptSocket = null;
	
	ClientConnThread()
	{
		super();
		
		try{
			// Fixed port number.
			mAcceptSocket = new ServerSocket(mServerPortNum);
			
		}catch (Exception e){
			System.err.println("Exception creating ServerSocket");
		}
		
	}
	
	public void run() 
	{
		boolean bLoop = true;
		while(bLoop)
		{
			// Continuously waiting for a new client connection and generate a communication thread for each accepted client.
			
			System.out.println("Waiting for client connection at port " + mServerPortNum);

			try {
				// Will be blocked here..
				Socket ClientSocket = mAcceptSocket.accept();
				
				// Connected and create a new communication channel.
				Thread CliThreadObj = new ClientCommThread(ClientSocket);
				CliThreadObj.start();
			
				System.out.println("A client is connected @ " + ClientSocket.getLocalAddress() + ":" + ClientSocket.getLocalPort());
				
				// We probably need to manage all the connected client list, but not for this semester?
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}

/**
 * ClientCommThread
 * Doing some communication to connected client.
 * */
class ClientCommThread extends Thread 
{
	Socket mCommSocket;
	
	/** The minimum time duration of a single communication loop. (im millisec)
	 * If things are done within this time, the thread will sleep. */
	long mSingleLoopMinTime = 100;
	
	ClientCommThread(Socket InSocket) 
	{
		super();
		
		this.mCommSocket = InSocket;
	}
	
	public void run() 
	{
        //////////////////////////////////////////////////
        // As we don't have proper packet serialization scheme yet,
        // just send/recv the packet in pre-defined order.
        // Here, we send the facility initialization data to the client

		{
			ArrayList<TransmitFacilityCreateData> SendDataArray = DataManager.GetAllRestroomCreateData();
			CommPacketDef_Cli_FacInit SendPacket = new CommPacketDef_Cli_FacInit();
			SendPacket.mDataArray = SendDataArray;
			
			// Serialize for the network transmission. 
			DataOutputStream SendStream;
			try {
				SendStream = SendPacket.SerializeOut(mCommSocket.getOutputStream());
				SendStream.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
        //////////////////////////////////////////////////
		
		
		boolean bLoop = true;
		while(bLoop)
		{
			long StartTickTime = System.currentTimeMillis();
						
			try {
				
				//////////////////////////////////////////////////
				// Get the request first 
				
                DataInputStream RecvStream = new DataInputStream(mCommSocket.getInputStream());
                CommPacketDef_Cli_Req RecvPacket = new CommPacketDef_Cli_Req();
                if(RecvPacket.SerializeIn(RecvStream) == false)
                {
                	// When the communication fails for any reason, just finish this.
                	break;
                }
				
				
                //////////////////////////////////////////////////
				// Send requested information to the client according to the request.
                
                // I guess the send/recv timing is different from what I expected. Just checking the request this way doesn't work
                //if(RecvPacket.mRequestID == CommPacketDef_Cli_Req.REQ_ID_FAC_ITEM_USAGE_DATA)
                {
					// We probably need to provide some queueing interface for SendData?
                	
					ArrayList<TransmitFacilityItemData> SendDataArray = DataManager.GetRestroomItemDataInOnFloor(RecvPacket.mReqUsageFloorNum);
					CommPacketDef_Cli_UsageState SendPacket = new CommPacketDef_Cli_UsageState();
					if(RecvPacket.mRequestID == CommPacketDef_Cli_Req.REQ_ID_FAC_ITEM_USAGE_DATA)
					{
						SendPacket.mDataArray = SendDataArray;
					}
					else
					{
						// Just setting to null for non requested.. we send the packet anyway.
						SendPacket.mDataArray = null;
					}
					
					// Serialize for the network transmission. 
					DataOutputStream SendStream = SendPacket.SerializeOut(mCommSocket.getOutputStream());
					SendStream.flush();
                }
				
				//System.out.println(" Server One Tick Req " + RecvPacket.mRequestID);
				
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				
				// When the communication fails for any reason, just finish this.
				break;
			}
			
			long EndTickTime = System.currentTimeMillis();
			// Use the abs value because I guess the currentTimeMillis might return reset value at some time..?
			long FrameDelta = Math.abs(EndTickTime - StartTickTime);
			if(FrameDelta < mSingleLoopMinTime)
			{
				try {
					sleep(mSingleLoopMinTime - FrameDelta);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					
				}
			}
						
		}
	}
}