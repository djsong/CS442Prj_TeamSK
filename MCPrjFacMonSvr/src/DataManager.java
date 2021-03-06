import java.util.ArrayList;

/**
 * [CS442] Mobile Computing, Spring 2015
 *
 * Global facility data management
 *
 * @author: DJ Song
 * */

/**
 * DataManager
 * Contains all facility data and provide interfaces for accessing them.
 * This is intended to be a static class which can be accessed globally.
 * */
public class DataManager 
{

	/**
	 * Array of facilities, which is supposed to contain facility items within it.
	 * However for now, we manage facility array and item array separately.
	 * */
	private static ArrayList<SingleFacilityDataBase> mFacilities = new ArrayList<SingleFacilityDataBase>();
	
	/**
	 * The actual facility item data array.
	 * Ideally, should be a part of mFacilities array.
	 * */
	private static ArrayList<SingleFacilityItemData> mFacItems = new ArrayList<SingleFacilityItemData>();
	
	public DataManager()
	{
		
	}
	
	
	//////////////////////////////////////////////////////////////////////
	// REMARK!
	// Each method that provides interfaces to the facility item data is not thread safe.
	// Each of them is just supposed to be used by a specific thread.
	// For example, client communication thread is not supposed to change the values of item, it just get the item data from here.
	//////////////////////////////////////////////////////////////////////
	
	
	private static boolean AddAnItem(int InFacType, int InFloorNum, int InRoomNum, int InItemNum)
	{
		if(InFacType <= CommPacketDef.FACTYPE_UNKNOWN || InFacType > CommPacketDef.FACTYPE_MAX)
		{
			return false;
		}
		
		SingleFacilityItemData NewData = new SingleFacilityItemData(InFacType, InFloorNum, InRoomNum, InItemNum);
		mFacItems.add(NewData);
		return true;
	}
	
	/**
	 * Add a restroom, which is composed of one or more sub-items (e.g. a restroom facility compose of 3 toilet spaces)
	 * @param InFloorNum, InRoomNum : They are like the identifying numbers for the new facility
	 * @param InTotalItemNum : The number of items (toilet seat space) belong to this restroom 
	 * @param InAreaLeft/Top/Right/Bottom : The facilitie's area coordinate information relative to its owning building.
	 * @return : It will return false if facility creation failed for any reason, like wrong parameter?
	 * */
	public static boolean AddARestroom(boolean bInMale, int InFloorNum, int InRoomNum, int InTotalItemNum,
			int InAreaLeft, int InAreaTop, int InAreaRight, int InAreaBottom)
	{				
		// Add a facility first.
		SingleFacilityDataBase NewFacInfo = new SingleRestroomFacilityData(bInMale, InFloorNum, InRoomNum, InTotalItemNum, InAreaLeft, InAreaTop, InAreaRight, InAreaBottom);
		mFacilities.add(NewFacInfo);
		
		// Then items..
		for(int ItemIndex = 0; ItemIndex < InTotalItemNum; ++ItemIndex)
		{
			if( AddAnItem(NewFacInfo.GetFacilityType(), InFloorNum, InRoomNum, ItemIndex) == false )
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Add a parking lot, which is composed of one or more sub-items (e.g. a restroom facility compose of 3 toilet spaces)
	 * @param InFloorNum, InRoomNum : They are like the identifying numbers for the new facility
	 * @param InTotalItemNum : The number of items (single parking lot space) belong to this whole parking lot 
	 * @param InAreaLeft/Top/Right/Bottom : The facilitie's area coordinate information relative to its owning building.
	 * @return : It will return false if facility creation failed for any reason, like wrong parameter?
	 * */
	public static boolean AddAParkingLot(int InFloorNum, int InRoomNum, int InTotalItemNum,
			int InAreaLeft, int InAreaTop, int InAreaRight, int InAreaBottom)
	{				
		// Add a facility first.
		SingleFacilityDataBase NewFacInfo = new SingleParkingLotFacilityData(InFloorNum, InRoomNum, InTotalItemNum, InAreaLeft, InAreaTop, InAreaRight, InAreaBottom);
		mFacilities.add(NewFacInfo);
		
		// Then items..
		for(int ItemIndex = 0; ItemIndex < InTotalItemNum; ++ItemIndex)
		{
			if( AddAnItem(NewFacInfo.GetFacilityType(), InFloorNum, InRoomNum, ItemIndex) == false )
			{
				return false;
			}
		}
		return true;
	}
	
	/** 
	 * Use this when you want to retrieve a facility item's reference with unique room and item number, etc.
	 * Returns null if found none.
	 * */
	public static SingleFacilityItemData GetSpecificItem(int InFacType, int InFloorNum, int InRoomNum, int InItemNum)
	{
		for(int ItemIndex = 0; ItemIndex < mFacItems.size(); ++ItemIndex)
		{
			SingleFacilityItemData CurrItem = mFacItems.get(ItemIndex);
			// We have a little problem regarding the floor number transmission from the sensor.
			// Room number is already regarded as unique, so just ignore the floor number for now.
			if(CurrItem.GetFacilityType() == InFacType && //CurrItem.GetFloorNumber() == InFloorNum &&
				CurrItem.GetRoomNumber() == InRoomNum && CurrItem.GetItemNumber() == InItemNum)
			{
				return CurrItem;
			}	
		}
		
		return null;
	}
	
	/** 
	 * When you are going to set the usage state from the incoming sensor signal.. (of restroom) 
	 * @param InFloorNum, InRoomNum, InItemNum : They are like the identifying numbers for the facility item
	 * @param bInOccupied : The data to be set by this.
	 * @return : Returns false if no matching room or item is found.
	 * */
	public static boolean SetSingleRestroomItemOccupied(int InFloorNum, int InRoomNum, int InItemNum, boolean bInOccupied)
	{
		SingleFacilityItemData SpecifiedItem = GetSpecificItem(CommPacketDef.FACTYPE_RESTROOM, InFloorNum, InRoomNum, InItemNum);
		if(SpecifiedItem != null)
		{
			SpecifiedItem.SetOccupied(bInOccupied);
			return true;
		}
		return false;
	}
	
	public static boolean GetSingleRestroomItemOccupied(int InFloorNum, int InRoomNum, int InItemNum)
	{
		SingleFacilityItemData SpecifiedItem = GetSpecificItem(CommPacketDef.FACTYPE_RESTROOM, InFloorNum, InRoomNum, InItemNum);
		if(SpecifiedItem != null)
		{
			return SpecifiedItem.IsOccupied();
		}
		
		return false;
	}
	
	/** 
	 * When you are going to set the usage state from the incoming sensor signal.. (of parking lot) 
	 * @param InFloorNum, InRoomNum, InItemNum : They are like the identifying numbers for the facility item
	 * @param bInOccupied : The data to be set by this.
	 * @return : Returns false if no matching room or item is found.
	 * */
	public static boolean SetSingleParkingLotItemOccupied(int InFloorNum, int InRoomNum, int InItemNum, boolean bInOccupied)
	{
		SingleFacilityItemData SpecifiedItem = GetSpecificItem(CommPacketDef.FACTYPE_PARKINGLOT, InFloorNum, InRoomNum, InItemNum);
		if(SpecifiedItem != null)
		{
			SpecifiedItem.SetOccupied(bInOccupied);
			return true;
		}
		return false;
	}
	
	public static boolean GetSingleParkingLotItemOccupied(int InFloorNum, int InRoomNum, int InItemNum)
	{
		SingleFacilityItemData SpecifiedItem = GetSpecificItem(CommPacketDef.FACTYPE_PARKINGLOT, InFloorNum, InRoomNum, InItemNum);
		if(SpecifiedItem != null)
		{
			return SpecifiedItem.IsOccupied();
		}
		
		return false;
	}
	
	/** Get all the restroom information within a specified floor. Possibly for the packet transmission to the client. */
	public static ArrayList<TransmitFacilityItemData> GetRestroomItemDataInOnFloor(int InFloorNum)
	{
		ArrayList<TransmitFacilityItemData> NewList = new ArrayList<TransmitFacilityItemData>();
		
		for(int ItemIndex = 0; ItemIndex < mFacItems.size(); ++ItemIndex)
		{
			SingleFacilityItemData CurrItem = mFacItems.get(ItemIndex);
			if(CurrItem.GetFacilityType() == CommPacketDef.FACTYPE_RESTROOM && CurrItem.GetFloorNumber() == InFloorNum)
			{
				NewList.add( new TransmitFacilityItemData(CurrItem.GetFloorNumber(), CurrItem.GetRoomNumber(), CurrItem.GetItemNumber(), CurrItem.IsOccupied()) );
			}
		}
		
		return NewList;
	}
	
	/** Get all the parking lot information within a specified floor. Possibly for the packet transmission to the client. */
	public static ArrayList<TransmitFacilityItemData> GetParkingLotItemDataInOnFloor(int InFloorNum)
	{
		ArrayList<TransmitFacilityItemData> NewList = new ArrayList<TransmitFacilityItemData>();
		
		for(int ItemIndex = 0; ItemIndex < mFacItems.size(); ++ItemIndex)
		{
			SingleFacilityItemData CurrItem = mFacItems.get(ItemIndex);
			if(CurrItem.GetFacilityType() == CommPacketDef.FACTYPE_PARKINGLOT && CurrItem.GetFloorNumber() == InFloorNum)
			{
				NewList.add( new TransmitFacilityItemData(CurrItem.GetFloorNumber(), CurrItem.GetRoomNumber(), CurrItem.GetItemNumber(), CurrItem.IsOccupied()) );
			}
		}
		
		return NewList;
	}
	
	/** Get all the information needed to create restrooms in the building, for the initial packet transmission. */
	public static ArrayList<TransmitFacilityCreateData> GetAllRestroomCreateData()
	{
		ArrayList<TransmitFacilityCreateData> NewList = new ArrayList<TransmitFacilityCreateData>();
		
		for(int FacIndex = 0; FacIndex < mFacilities.size(); ++FacIndex)
		{
			SingleFacilityDataBase CurrFac = mFacilities.get(FacIndex);
			
			if(CurrFac.GetFacilityType() == CommPacketDef.FACTYPE_RESTROOM)
			{
				SingleRestroomFacilityData CastedFac = (SingleRestroomFacilityData)CurrFac;
				
				TransmitFacilityCreateData NewTransmitData = new TransmitFacilityCreateData(
						CastedFac.GetFacilityType(), CastedFac.GetFloorNumber(), CastedFac.GetRoomNumber(), CastedFac.GetTotalItemNumber(),
						CastedFac.GetRelativeAreaLeft(), CastedFac.GetRelativeAreaTop(), CastedFac.GetRelativeAreaRight(), CastedFac.GetRelativeAreaBottom());
				
				NewTransmitData.SetAdditionalDataForRestRoom(CastedFac.IsMaleRestroom());
				
				NewList.add( NewTransmitData );
			}
		}
		
		return NewList;
	}
	
	/** Get all the information needed to create parking lots in the building, for the initial packet transmission. */
	public static ArrayList<TransmitFacilityCreateData> GetAllParkingLotCreateData()
	{
		ArrayList<TransmitFacilityCreateData> NewList = new ArrayList<TransmitFacilityCreateData>();
		
		for(int FacIndex = 0; FacIndex < mFacilities.size(); ++FacIndex)
		{
			SingleFacilityDataBase CurrFac = mFacilities.get(FacIndex);
			
			if(CurrFac.GetFacilityType() == CommPacketDef.FACTYPE_PARKINGLOT)
			{
				SingleParkingLotFacilityData CastedFac = (SingleParkingLotFacilityData)CurrFac;
				
				TransmitFacilityCreateData NewTransmitData = new TransmitFacilityCreateData(
						CastedFac.GetFacilityType(), CastedFac.GetFloorNumber(), CastedFac.GetRoomNumber(), CastedFac.GetTotalItemNumber(),
						CastedFac.GetRelativeAreaLeft(), CastedFac.GetRelativeAreaTop(), CastedFac.GetRelativeAreaRight(), CastedFac.GetRelativeAreaBottom());
				
				// Set any additional data if needed..
				
				NewList.add( NewTransmitData );
			}
		}
		
		return NewList;
	}
}

/**
 * SingleFacilityDataBase
 * A facility is suppose to be composed of one or more facility item
 * */
class SingleFacilityDataBase
{
	/** 
	 * Use one of the CommPacketDef.FACTYPE_** variables..
	 * */
	protected int mFacilityType = CommPacketDef.FACTYPE_UNKNOWN;
	
	/** -1 is B1, no 0 */
	private int mFloorNumber = 1;
	
	/** This might also has floor information.. This could be a unique identifier of a facility within a building. */
	private int mRoomNumber = 0;
	
	/** Gotta be a way to go.. but not now.. */
	//ArrayList<SingleFacilityItemData> mItemArray;
	protected int mTotalItemNum;
	
	/**
	 * The coordinates of occupied area relative to the owning building.
	 * */
	private int mRelativeAreaLeft;
	private int mRelativeAreaTop;
	private int mRelativeAreaRight;
	private int mRelativeAreaBottom;
	
	/** Only common information is initialized by creator. */
	public SingleFacilityDataBase(int InFloorNum, int InRoomNum, int InTotalItemNum,
			int InAreaLeft, int InAreaTop, int InAreaRight, int InAreaBottom)
	{
		// mFacilityType will be set at the sub-class side.
		
		mFloorNumber = InFloorNum;
		mRoomNumber = InRoomNum;
		mTotalItemNum = InTotalItemNum;
		mRelativeAreaLeft = InAreaLeft;
		mRelativeAreaTop = InAreaTop;
		mRelativeAreaRight = InAreaRight;
		mRelativeAreaBottom = InAreaBottom;
	}

	
	public int GetFacilityType() {return mFacilityType;}
	public int GetFloorNumber() {return mFloorNumber;}
	public int GetRoomNumber() {return mRoomNumber;}
	public int GetTotalItemNumber() {return mTotalItemNum;}
	
	public int GetRelativeAreaLeft() {return mRelativeAreaLeft;}
	public int GetRelativeAreaTop() {return mRelativeAreaTop;}
	public int GetRelativeAreaRight() {return mRelativeAreaRight;}
	public int GetRelativeAreaBottom() {return mRelativeAreaBottom;}
}

/**
 * SingleRestroomFacilityData
 * */
class SingleRestroomFacilityData extends SingleFacilityDataBase
{
	/**  
	 * This is just needed for the restroom facility
	 * */
	private boolean mbMaleRestRoom; 
	
	SingleRestroomFacilityData(boolean bInMale, int InFloorNum, int InRoomNum, int InTotalItemNum,
			int InAreaLeft, int InAreaTop, int InAreaRight, int InAreaBottom)
	{
		super(InFloorNum, InRoomNum, InTotalItemNum, InAreaLeft, InAreaTop, InAreaRight, InAreaBottom);
		
		mFacilityType = CommPacketDef.FACTYPE_RESTROOM;
		mbMaleRestRoom = bInMale;
	}
	
	public boolean IsMaleRestroom() {return mbMaleRestRoom;}
}

/**
 * SingleParkingLotFacilityData
 * */
class SingleParkingLotFacilityData extends SingleFacilityDataBase
{
	
	SingleParkingLotFacilityData(int InFloorNum, int InRoomNum, int InTotalItemNum,
			int InAreaLeft, int InAreaTop, int InAreaRight, int InAreaBottom)
	{
		super(InFloorNum, InRoomNum, InTotalItemNum, InAreaLeft, InAreaTop, InAreaRight, InAreaBottom);
		
		mFacilityType = CommPacketDef.FACTYPE_PARKINGLOT;	
	}
	
}

/**
 * SingleFacilityItemData
 * An element data that represents a facility item which can accommodate one person (e.g. a toilet space) 
 * */
class SingleFacilityItemData
{
	// This better be a part of SingleFacilityData (i.e. SingleFacilityDataBase should contain the array of this)
	// For now, things are little complicated.
	
	
	/** 
	 * Use one of the CommPacketDef.FACTYPE_** variables..
	 * */
	private int mFacilityType = CommPacketDef.FACTYPE_UNKNOWN;
	
	/** -1 is B1, no 0 */
	private int mFloorNumber = 1;
	
	/** This might also has floor information.. This could be a unique identifier of a facility within a building. */
	private int mRoomNumber = 0;
	
	/** A facility is composed of one or more items, and an item can accommodate one person. */
	private int mItemNumber = 0;
	
	/** The usage flag. True if somebody is using this item. */
	private boolean mbIsOccupied = false;
	
	
	public SingleFacilityItemData(int InFacType, int InFloorNum, int InRoomNum, int InItemNum)
	{
		mFacilityType = InFacType;
		mFloorNumber = InFloorNum;
		mRoomNumber = InRoomNum;
		mItemNumber = InItemNum;
		
		// This is supposed to be changed in runtime, so just set as default value..
		mbIsOccupied = false;
	}
	
	public int GetFacilityType() {return mFacilityType;}
	public int GetFloorNumber() {return mFloorNumber;}
	public int GetRoomNumber() {return mRoomNumber;}
	public int GetItemNumber() {return mItemNumber;}
	public boolean IsOccupied() {return mbIsOccupied;}
	
	public void SetOccupied(boolean bInOccupied) {mbIsOccupied = bInOccupied;}
}
