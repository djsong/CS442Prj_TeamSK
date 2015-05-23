import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

/**
 * SensorConnThread
 * Process connection request of the sensor manager and open communication channel. (just like that with the client)
 * */
public class SensorConnThread extends Thread
{
	/**
	 * The sensor manager should use the same number.
	 * */
	int mServerPortNum = 9101;
	
	ServerSocket mAcceptSocket = null;
	
	SensorConnThread()
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
			// Continuously waiting for a new sensor manager connection and generate a communication thread for each accepted sensor manager (but actually just one?).
			
			System.out.println("Waiting for sensor manager connection at port " + mServerPortNum);

			try {
				// Will be blocked here..
				Socket SensorSocket = mAcceptSocket.accept();
				
				// Connected and create a new communication channel.
				Thread SensorThreadObj = new SensorCommThread(SensorSocket);
				SensorThreadObj.start();
			
				System.out.println("A sensor manager is connected @ " + SensorSocket.getLocalAddress() + ":" + SensorSocket.getLocalPort());
				
				// We probably need to manage all the connected client list, but not for this semester?
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}


/**
 * SensorCommThread
 * Communication to the sensor manager which contains all sensor's updated information,
 * being the port of getting the raw hardware information
 * */
class SensorCommThread extends Thread 
{
	Socket mCommSocket;
	
	SensorCommThread(Socket InSocket)
	{
		super();
		
		this.mCommSocket = InSocket;
	}
	
	public void run() 
	{
		// Initialize and do some communication to the sensor.
		
		boolean bLoop = true;
		while(bLoop)
		{		
			// Just receive the information
			try {

				// Getting the packet in string format.
				BufferedReader SensorDataReader = new BufferedReader(new InputStreamReader(mCommSocket.getInputStream())); 
				String RecvString = SensorDataReader.readLine();
				
				ProcessPacketFromSensor(RecvString);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Parse the received packet and also update the internal data according to it.
	 * */
	private void ProcessPacketFromSensor(String InRecvString)
	{
		System.out.println("Received Packet : " + InRecvString);
		
		// For now, this just handles restroom, no other type of facility.
		
		int ParsedFloorNum = 0;
		int ParsedRoomNum = 0;
		int ParsedItemNum = 0;
		boolean ParsedUsageState = false;
		
		// Parse the string assuming the format like "AA=BB CC=DD"
		
		// Use StringTokenizer for parsing, with default token " ".
		StringTokenizer RecvStringTokenizer = new StringTokenizer(InRecvString); 
		while(RecvStringTokenizer.hasMoreTokens())
		{
			// Further divide a single divided parameter string using the StringTokenizer again, this time with "="
			StringTokenizer SingleParamTokenizer = new StringTokenizer(RecvStringTokenizer.nextToken(), "=");
			if(SingleParamTokenizer.countTokens() >= 2)
			{
				String ParamKey = SingleParamTokenizer.nextToken();
				String ParamValue = SingleParamTokenizer.nextToken();
				
				// The parameter keys should match to the sensor side definitions.
				if(ParamKey.compareTo("FLOOR") == 0)
				{
					ParsedFloorNum = Integer.parseInt(ParamValue);
				}
				else if(ParamKey.compareTo("ROOM") == 0)
				{
					ParsedRoomNum = Integer.parseInt(ParamValue);
				}
				else if(ParamKey.compareTo("ITEM") == 0)
				{
					ParsedItemNum = Integer.parseInt(ParamValue);
				}
				else if(ParamKey.compareTo("INUSE") == 0)
				{
					ParsedUsageState = 
							(ParamValue.compareTo("0") == 0 || ParamValue.toUpperCase().compareTo("FALSE") == 0 || ParamValue.toUpperCase().compareTo("OFF") == 0) ?
									false : true;
				}
			}
		}
		
		// When you get the data from the sensor, set the data by calling DataManager.SetSingleRestroomItemOccupied();
		if( 
				DataManager.SetSingleRestroomItemOccupied(ParsedFloorNum, ParsedRoomNum, ParsedItemNum, ParsedUsageState) == true 
				)
		{
			System.out.println("Restroom " + ParsedFloorNum + " " +  ParsedRoomNum + " " + ParsedItemNum + " usage state changed to " + ParsedUsageState);		
		}
		else
		{
			System.out.println("Possibly wrong room or item number");
		}
	}
}
