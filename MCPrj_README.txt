==================================================
[CS442] Mobile Computing Project Team Smoothie King
==================================================

--------------------------------------------------
MCPrjFacMon (Mobile Client)
--------------------------------------------------

* Development environment

	- Android Studio 1.1.0 

	- JDK 1.7.0_40 

	- Target Android SDK Version: 21 / Minimum Android SDK Version: 16

* Execution

	- Import to the Android Studio as a project, and run it on the emulator or a device.

	- Run the server prior to the client.

	- If you don't enter the IP address at the main menu, it will try connecting to 143.248.139.34 (which won't be running in most cases..)

	- If you cannot see any restroom at the first time, try changing the floor by clicking 'NEXT FLOOR' button. It will start working.

--------------------------------------------------
MCPrjFacMonSvr (Server)
--------------------------------------------------

* Development environment

	- Eclipse Luna 4.4.1

	- JDK 1.7.0_40

* Execution

	- In the bin folder, open the command window and enter "java ServerMain" (No argument)


--------------------------------------------------
MCPrjServerTestDummy
--------------------------------------------------

This is provided to test the server and client without the sensor device. (substituting sensor and sensorSerial.py)
It will send a continuously switching usage state signal of 1st toilet at restroom #105.

* Development environment

	- Same as the server

* Execution
	
	- In the bin folder, open the command window and enter "java ServerTestDummyMain [ServerIPAddr]"

	- e.g. "java ServerTestDummyMain 143.248.139.34"


--------------------------------------------------
sensorSerial.py
--------------------------------------------------

This is the code for the data transmission from the sensor relay to the server.
This will NOT work without the relay device. 
To make a test without the hardware, use MCPrjServerTestDummy instead.

* Development environment

	- Python 2.7
	
	- pySerial 2.7

* Execution

	- "python sensorSerial.py [COM#] [ServerIPAddr]"

	- e.g. "python sensorSerial.py COM5 143.248.139.34"