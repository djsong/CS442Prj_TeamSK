
/**
 * SensorCommThread
 * This is just an example of communication to the sensor device.
 * I temporarily put this just to set a demonstrative situation.
 * This can be modified or totally replaced for the actual sensor communication.
 * */
public class SensorCommThread extends Thread 
{
	SensorCommThread()
	{
		super();
	}

	
	public void run() 
	{
		// Initialize and do some communication to the sensor.

		long CachedPrevTickTime = System.currentTimeMillis();
		long TempExampleUpdateFrequency = 10000; // In millisec.
		
		boolean bLoop = true;
		while(bLoop)
		{		
			//////////////////////////////////////////////////////////////////////
			// Remove or modify this block when the real sensor communication is implemented here.
			
			long CurrTickTime = System.currentTimeMillis();
			
			// Use the abs value because I guess the currentTimeMillis might return reset value at some time..? 
			if( Math.abs(CurrTickTime - CachedPrevTickTime) > TempExampleUpdateFrequency )
			{
				boolean bNewState = !DataManager.GetSingleRestroomItemOccupied(ServerMain.EXAMPLE_RESTROOM_FLOORNUM, ServerMain.EXAMPLE_MALE_RESTROOM_ROOMNUM, 1); 
				// Just for an example, periodically switch the state.
				DataManager.SetSingleRestroomItemOccupied(ServerMain.EXAMPLE_RESTROOM_FLOORNUM, ServerMain.EXAMPLE_MALE_RESTROOM_ROOMNUM, 1, 
						bNewState);
				
				System.out.println("Restroom " + ServerMain.EXAMPLE_RESTROOM_FLOORNUM + " " +  ServerMain.EXAMPLE_MALE_RESTROOM_ROOMNUM
						+ " usage state changed to " + bNewState);
				
				CachedPrevTickTime = CurrTickTime;
			}
			
			//////////////////////////////////////////////////////////////////////
			
			
			
			// When you get the data from the sensor, set the data by calling DataManager.SetSingleRestroomItemOccupied();
			
		}
		
	}
}
