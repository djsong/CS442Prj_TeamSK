/**
 * [CS442] Mobile Computing, Spring 2015
 *
 * Common packet definitions and functionalities shared by both server and client
 *
 * @author: DJ Song
 * */

// Any changes in the packet definition MUST be applied both SERVER and CLIENT!

public class CommPacketDef 
{
	/** Packet IDs. I am not sure if this is needed.. */
	public final static int PACKET_ID_CLI_REQ = 0;
	public final static int PACKET_ID_CLI_FACINIT = 1;
	public final static int PACKET_ID_CLI_USAGESTATE = 2;
	
	/**
	 * For the mFacilityType member in SingleFacilityItemData
	 * Instead of using enum type.. Enum in Java looks like somewhat different from that of C++.  
	 * */
	public final static int FACTYPE_UNKNOWN = 0;
	public final static int FACTYPE_RESTROOM = 1;
	public final static int FACTYPE_PARKINGLOT = 2;
	public final static int FACTYPE_MAX = 3;
	
}
