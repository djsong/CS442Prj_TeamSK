import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * [CS442] Mobile Computing, Spring 2015
 *
 * This simulates the sensor signal for testing the system.
 * */
public class ServerTestDummyMain
{
	private static Socket ServerCommSocket;
	
	/**
	 * In millisecond unit.
	 * Change or remove this variable if unnecessary due to some hardware conditions.
	 * */
	final static long mSendDataPeriod = 500;
		
	final static long mSampleRoomStateChangePeriod = 2000;
	
	
	public static void main(String[] args) 
	{
		/**
		 * This will be periodically switched.
		 * */
		boolean bSampleRoomInUse = false;
		long SampleRoomStateChangeTimer = 0;
		
		try {
			
			// The first argument is the IP address of the server, and the second argument is the port number.
			// However, we also allow default values.
			String FirstArgumentString = "127.0.0.1";
			String SecondArgumentString = "9101";
			if(args.length >= 1)
			{
				// Execution example: "java ServerTestDummyMain 127.0.0.1 9101"
				FirstArgumentString = args[0];
				if(args.length >= 2)
				{
					SecondArgumentString = args[1];
				}
			}
			
			System.out.println("Connecting to the server at " + FirstArgumentString + ":" + SecondArgumentString);
			
			String ServerAddr = FirstArgumentString;
			int ServerPort = Integer.parseInt(SecondArgumentString);
			
			ServerCommSocket = new Socket(ServerAddr, ServerPort);
			
			// Connection established. Let's do the communication.
			boolean bLoop = true;
			while(bLoop)
			{		
				// Just receive the information
				try {
					
					long StartTickTime = System.currentTimeMillis();
					
					////////////////////////////////////////////////////////////////////////////////
					// Read the data from USB port at this point, let's assume like below..
					
					// ITEM number starts from 0
					// INUSE 1 for occupied, 0 for empty
					
					//String SendString = "FLOOR=1 ROOM=105 ITEM=1 INUSE=1";
					String SendString = (bSampleRoomInUse == true) ? "1 105 1 1" : "1 105 1 0";
					
					
					////////////////////////////////////////////////////////////////////////////////
					
					// Send the information string to the server
					PrintWriter SensorDataWriter = new PrintWriter(ServerCommSocket.getOutputStream());
					SensorDataWriter.println(SendString);
					SensorDataWriter.flush();
					
					
					// Limit the data sending frequency.
					long EndTickTime = System.currentTimeMillis();
					// Use the abs value because I guess the currentTimeMillis might return reset value at some time..?
					long FrameDelta = Math.abs(EndTickTime - StartTickTime);
					if(FrameDelta < mSendDataPeriod)
					{
						// Here, sleep does not work in the main thread.
						// We just make some meaningless operations.
						while(Math.abs(System.currentTimeMillis() - StartTickTime) < mSendDataPeriod)
						{
							double Dummy = Math.cos((double)System.currentTimeMillis());
							Dummy += Math.sin((double)System.currentTimeMillis());
						}
					}
					
					// Get the time again for other periodic stuff..
					EndTickTime = System.currentTimeMillis();
					SampleRoomStateChangeTimer += Math.abs(EndTickTime - StartTickTime);
					if(SampleRoomStateChangeTimer >= mSampleRoomStateChangePeriod)
					{
						bSampleRoomInUse = !bSampleRoomInUse;
						SampleRoomStateChangeTimer = 0;
					}
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		} catch (Exception e) {
			System.err.println("Exception occurred in main");
		}

	}
}
