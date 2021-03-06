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
 * CommPacketDef_Cli_FacInit
 * Packet from server to client
 * The data to create a facility information. 
 * */
public class CommPacketDef_Cli_FacInit 
{
	/** Is this necessary for our scheme...? */
	private int mPacketID = CommPacketDef.PACKET_ID_CLI_FACINIT;
	public int GetPacketID() {return mPacketID;}
	
	public ArrayList<TransmitFacilityCreateData> mDataArray;
	
	public CommPacketDef_Cli_FacInit()
	{
		mDataArray = new ArrayList<TransmitFacilityCreateData>();
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
				// It might better to implement serialize interface in the TransmitFacilityCreateData class..
				TransmitFacilityCreateData NewElem = new TransmitFacilityCreateData(
						InStream.readInt(), InStream.readInt(), InStream.readInt(), InStream.readInt(),
						InStream.readInt(), InStream.readInt(), InStream.readInt(), InStream.readInt()
					);
				
				if(NewElem.mFacilityType == CommPacketDef.FACTYPE_RESTROOM)
				{
					NewElem.SetAdditionalDataForRestRoom(InStream.readBoolean());
				}
				
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
					// It might better to implement serialize interface in the TransmitFacilityCreateData class..
					TransmitFacilityCreateData CurrData = mDataArray.get(DI);
					ReturnStream.writeInt(CurrData.mFacilityType);
					ReturnStream.writeInt(CurrData.mFloorNumber);
					ReturnStream.writeInt(CurrData.mRoomNumber);
					ReturnStream.writeInt(CurrData.mTotalItemNumber);
					ReturnStream.writeInt(CurrData.mRelativeAreaLeft);
					ReturnStream.writeInt(CurrData.mRelativeAreaTop);
					ReturnStream.writeInt(CurrData.mRelativeAreaRight);
					ReturnStream.writeInt(CurrData.mRelativeAreaBottom);
					
					if(CurrData.mFacilityType == CommPacketDef.FACTYPE_RESTROOM)
					{
						ReturnStream.writeBoolean(CurrData.mbMaleRestRoom);
					}
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
 * TransmitFacilityCreateData
 * This contains all information to create any type of facility. (i.e. no subclass hierarchy)
 * Not a right choice, but just for this semester..
 * */
class TransmitFacilityCreateData
{
	// Members here are almost from the SingleFacilityDataBase
	
	/** 
	 * Use one of the CommPacketDef.FACTYPE_** variables..
	 *  */
	public int mFacilityType = CommPacketDef.FACTYPE_UNKNOWN;
	
	public int mFloorNumber;
	public int mRoomNumber;
	
	/**
	 * We not only need the total number of item, but also need more detailed item information to create them.
	 * However, doing all this is too much for this semester I guess. 
	 * So, just sending how many sub-items are in this facility. 
	 * */
	public int mTotalItemNumber;
	
	/**
	 * The coordinates of occupied area relative to the owning building.
	 * */
	public int mRelativeAreaLeft;
	public int mRelativeAreaTop;
	public int mRelativeAreaRight;
	public int mRelativeAreaBottom;
	
	/**  
	 * This is just needed for the restroom
	 * */
	public boolean mbMaleRestRoom; 
	
	/** Only common information is initialized by creator. */
	public TransmitFacilityCreateData(int InFacType, int InFloorNum, int InRoomNum, int InTotalItemNum,
			int InAreaLeft, int InAreaTop, int InAreaRight, int InAreaBottom)
	{
		mFacilityType = InFacType;
		mFloorNumber = InFloorNum;
		mRoomNumber = InRoomNum;
		mTotalItemNumber = InTotalItemNum;
		mRelativeAreaLeft = InAreaLeft;
		mRelativeAreaTop = InAreaTop;
		mRelativeAreaRight = InAreaRight;
		mRelativeAreaBottom = InAreaBottom;
	}

	/** Better not forget this for the restroom. */
	void SetAdditionalDataForRestRoom(boolean InbMale)
	{
		mbMaleRestRoom = InbMale;
	}
	
}