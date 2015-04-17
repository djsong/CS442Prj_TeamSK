import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * [CS442] Mobile Computing, Spring 2015
 *
 * Common packet definitions and functionalities shared by both server and client 
 *
 * @author: DJ Song
 * */

//Any changes in the packet definition MUST be applied both SERVER and CLIENT!

/**
 * CommPacketDef_Cli_UsageState
 * Packet from server to client
 * The main data which contains the usage states of all facilities in one floor
 * */
public class CommPacketDef_Cli_UsageState
{
	/** Is this necessary for our scheme...? */
	private int mPacketID = CommPacketDef.PACKET_ID_CLI_USAGESTATE;
	public int GetPacketID() {return mPacketID;}
	
	
	public ArrayList<TransmitFacilityItemData> mDataArray;

	
	public CommPacketDef_Cli_UsageState()
	{
		mDataArray = new ArrayList<TransmitFacilityItemData>();
	}
	
	/** Set the members from serialized data stream 
	 * Should be consistent with SerializeOut */
	public boolean SerializeIn(DataInputStream InStream)
	{
		try {
			mPacketID = InStream.readInt();
			
			mDataArray.clear();
			
			int ArraySize = InStream.readInt();
			for(int DI = 0; DI < ArraySize; ++DI)
			{
				TransmitFacilityItemData NewElem = new TransmitFacilityItemData(
					InStream.readInt(), InStream.readInt(), InStream.readInt(), InStream.readBoolean()
					);
				mDataArray.add(NewElem);
			}
			
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
			
			if(mDataArray != null)
			{
				ReturnStream.writeInt(mDataArray.size());
				
				for(int DI = 0; DI < mDataArray.size(); ++DI)
				{
					TransmitFacilityItemData CurrData = mDataArray.get(DI);
					ReturnStream.writeInt(CurrData.mFloorNumber);
					ReturnStream.writeInt(CurrData.mRoomNumber);
					ReturnStream.writeInt(CurrData.mItemNumber);
					ReturnStream.writeBoolean(CurrData.mbIsOccupied);
				}
			}
			else
			{
				ReturnStream.writeInt(0);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ReturnStream;
	}
}

/**
 * TransmitFacilityItemData
 * Almost the copy of SingleFacilityItemData
 * */
class TransmitFacilityItemData
{
	// Members here are almost from the SingleFacilityItemData
	public int mFloorNumber;
	public int mRoomNumber;
	public int mItemNumber;
	public boolean mbIsOccupied;
	
	public TransmitFacilityItemData(int FloorNum, int RoomNum, int ItemNum, boolean bOccupied)
	{
		mFloorNumber = FloorNum;
		mRoomNumber = RoomNum;
		mItemNumber = ItemNum;
		mbIsOccupied = bOccupied;
	}
}