__author__ = 'koo'

import serial
import time
from socket import *
import sys

portName = 'COM5'
serverHost = '143.248.139.34'
# serverHost = 'localhost'
serverPort = '9101'

if(len(sys.argv) == 2):
    portName = sys.argv[1]
elif(len(sys.argv) == 3):
    portName = sys.argv[1]
    serverHost = sys.argv[2]
elif(len(sys.argv) == 4):
    portName = sys.argv[1]
    serverHost = sys.argv[2]
    serverPort = sys.argv[3]

clientSocket = socket(AF_INET, SOCK_STREAM)
clientSocket.connect((serverHost, int(serverPort)))

ser = serial.Serial('COM5', 9600, timeout=0)
# var = raw_input("Enter something: ")
# ser.write(var)
# print 'start...'
while 1:
    # try:
    msg = ser.readline()
    # print type(msg)
    # print msg
    msgList = msg.split()

    # There should be 4 separate items
    if(len(msgList) == 4):
    	print len(msgList)

	# Some problem regarding this packet format string.
	#msgString = 'FLOOR=%s ROOM=%s ITEM=%s INUSE=%s' % (msgList[0], msgList[1], msgList[2], msgList[3])
	print msg
	clientSocket.send(msg)
    	
    time.sleep(1)
    # except ser.SerialTimeoutException:
    #     print('Data could not be read')
