package com.example.djsong.mcprjfacmon;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class MainActivity extends ActionBarActivity {

    Button mShowCurrBldgBtn;
    EditText mServerAddrEdit;

    public static final String INTENT_KEY_FacExpActivity_SERVERADDR = "ServerAddr";

    /**
     * Sort of activity identifier..?
     * */
    public static final int REQUEST_CODE_FACEXPACT = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mServerAddrEdit = (EditText)findViewById(R.id.server_addr_edit);

        mShowCurrBldgBtn = (Button)findViewById(R.id.show_curr_bldg);
        mShowCurrBldgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // For any data that need to be transmitted for facility exploration activity.
                Intent IntentForFacExpAct = new Intent(getApplicationContext(), FacExpActivity.class);

                // Any fallback in the case of empty address field?

                // A simple means to send some data. Should use the same key string when retrieve the data
                IntentForFacExpAct.putExtra(INTENT_KEY_FacExpActivity_SERVERADDR, mServerAddrEdit.getText().toString());

                startActivityForResult(IntentForFacExpAct, REQUEST_CODE_FACEXPACT);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    /**
     * When you return from other activity.
     * */
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        super.onActivityResult(requestCode, resultCode, intent);

        if(requestCode == REQUEST_CODE_FACEXPACT)
        {
            if(resultCode == RESULT_OK)
            {
                // Do some if we need to.. We can get some return info from the intent.
            }
        }

    }
}
