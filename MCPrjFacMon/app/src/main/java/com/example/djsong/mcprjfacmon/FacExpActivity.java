/**
 * [CS442] Mobile Computing, Spring 2015
 *
 * A building's facility exploration interface
 * Probably no direct operation on the exploration map. Just like the outer shell?
 *
 * The server communication functionalities are implemented here though.
 *
 * @author: DJ Song
 * */

package com.example.djsong.mcprjfacmon;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.net.Socket;

public class FacExpActivity extends ActionBarActivity {

    Button mBackToMainMenuBtn;

    FacExpMapView mBuildingMapView;

    ServerCommThread mCommThreadObj = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fac_exp);

        // Upper menu layer
        LinearLayout MenuLayout = (LinearLayout) findViewById(R.id.MenuLayout);
        mBackToMainMenuBtn = (Button)findViewById(R.id.return_to_main_menu_btn);
        mBackToMainMenuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ReturnIntent = new Intent();
                // If we need to send some data back to the main activity..
                //ReturnIntent.putExtra("SomeInfoKey", "SomeInfoContent");
                setResult(RESULT_OK, ReturnIntent);
                mCommThreadObj.SetTerminate();
                finish();
            }
        });

        // Map exploration layer
        LinearLayout FacExpMapLayout = (LinearLayout) findViewById(R.id.FacExpMapLayout);
        mBuildingMapView = new FacExpMapView(this);

        //MapView.setPadding(0,0,0,0);
        FacExpMapLayout.addView(mBuildingMapView);

        LoadProjectSpecificData();

        // Get some data from calling activity..
        RetrieveDataFromIntent();
    }

    private void RetrieveDataFromIntent()
    {
        // See the part to insert ServerAddr string to the Intent in MainActivity.
        // The key string should be consistent.

        Intent IntentFromMain = getIntent();
        Bundle ExtraBundle = IntentFromMain.getExtras();

        String ServerAddrString = ExtraBundle.getString(MainActivity.INTENT_KEY_FacExpActivity_SERVERADDR);

        if(ServerAddrString.isEmpty())
        {
            // Just for the convenience of the development.
            // This is the main development PC's IP address.
            ServerAddrString = "143.248.139.34";
        }

        // Just temporary
        Toast.makeText(getApplicationContext(), "Connect to server @" + ServerAddrString, Toast.LENGTH_LONG).show();

        // Create and kick off the server communication thread from the address string.
        mCommThreadObj = new ServerCommThread(ServerAddrString, mBuildingMapView);
        mCommThreadObj.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_fac_exp, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /** This is just like a hack for the project demonstration. */
    private void LoadProjectSpecificData()
    {
        // If we have any data to be loaded for the demonstration..

    }
}

//////////////////////////////////////////////////////////////////////

/**
 * ServerCommThread
 * Connect to the server and getting the facility usage info
 * */
class ServerCommThread extends Thread
{
    /**
     * Should be the same as what's being used in the server side.
     * */
    int mServerPortNum = 9001;

    String mSvrAddr; // IP Address of the server

    FacExpMapView mFacMapView;

    /** in millisec */
    final static long mRequestUsageDataPeriod = 2000;
    long mLastUsageDataRequestTime = 0;

    /**
     * Signal from the main thread (FacExpActivity)
     * */
    private boolean mTerminateSignal = false;

    public ServerCommThread(String InSvrAddr, FacExpMapView InFacMapView)
    {
        super();

        mSvrAddr = new String(InSvrAddr);

        mFacMapView = InFacMapView;
    }

    public void run()
    {
        try{

            Socket CommSocket = new Socket(mSvrAddr, mServerPortNum);

            // Well, I guess that if CommSocket failed, then the flow won't even come to here..
            if(CommSocket != null)
            {
                // Doesn't work..
                //Toast.makeText(mFacMapView.getContext(), "Connected to the server @ " + mSvrAddr + ":" + mServerPortNum, Toast.LENGTH_LONG).show();
            }

            //////////////////////////////////////////////////
            // As we don't have proper packet serialization scheme yet,
            // just send/recv the packet in pre-defined order.
            // Here, we get the facility initialization data from the server

            {
                DataInputStream RecvStream = new DataInputStream(CommSocket.getInputStream());
                CommPacketDef_Cli_FacInit RecvPacket = new CommPacketDef_Cli_FacInit();
                RecvPacket.SerializeIn(RecvStream);
                mFacMapView.OnRecvPacket_Cli_FacInit(RecvPacket);
            }

            //////////////////////////////////////////////////

            boolean bLoop = true;
            while(bLoop)
            {
                int RequestThisTime = CommPacketDef_Cli_Req.REQ_ID_NONE;

                // See if we need to make a periodical usage request.
                long CurrTickTime = System.currentTimeMillis();
                // Do the abs because I guess the currentTimeMillis might return reset value at some time..?
                long TimeSinceLastUsageDataRequest = Math.abs( CurrTickTime - mLastUsageDataRequestTime );
                if(TimeSinceLastUsageDataRequest >= mRequestUsageDataPeriod)
                {
                    RequestThisTime = CommPacketDef_Cli_Req.REQ_ID_FAC_ITEM_USAGE_DATA;
                    mLastUsageDataRequestTime = CurrTickTime;
                }

                //////////////////////////////////////////////////
                // Send a request first.

                CommPacketDef_Cli_Req SendPacket = new CommPacketDef_Cli_Req( RequestThisTime );
                // Data for the request should be set manually..
                SendPacket.mReqUsageFloorNum = mFacMapView.GetCurrentFloor();
                DataOutputStream SendStream = SendPacket.SerializeOut(CommSocket.getOutputStream());
                SendStream.flush();


                //////////////////////////////////////////////////
                // Get the data according to the sent request

                // I guess the send/recv timing is different from what I expected. Just checking the request this way doesn't work
                //if(RequestThisTime == CommPacketDef_Cli_Req.REQ_ID_FAC_ITEM_USAGE_DATA)
                {
                    DataInputStream RecvStream = new DataInputStream(CommSocket.getInputStream());

                    // We need an interface to serialize the packet header and identify the ID first,
                    // then do the proper serialization, but guess not that needed for this semester.

                    CommPacketDef_Cli_UsageState RecvPacket = new CommPacketDef_Cli_UsageState();
                    RecvPacket.SerializeIn(RecvStream);
                    // At the very first moment, mFacMapView might not ready to draw something yet.
                    // It won't do any in that case.
                    mFacMapView.OnRecvPacket_Cli_UsageState(RecvPacket);
                }

                // It doesn't really work.. How to end the thread from the outside?
                if(mTerminateSignal == true)
                {
                    // No disconnection packet yet.
                    break;
                }
            }

        } catch(Exception ex){
            ex.printStackTrace();
        }

    }

    /**
     * After you call this, the thread will be terminated after current communication is done.
     * It doesn't really work.. How to end the thread from the outside?
     * */
    public void SetTerminate() {mTerminateSignal = true;}
}