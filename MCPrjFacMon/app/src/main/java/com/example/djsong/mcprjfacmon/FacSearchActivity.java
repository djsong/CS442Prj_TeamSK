/**
 * [CS442] Mobile Computing, Spring 2015
 *
 * The activity activated when you click the Search button in the exploration screen.
 *
 * The server communication functionalities are implemented here though.
 *
 * @author: DJ Song
 * */

package com.example.djsong.mcprjfacmon;

import android.app.ListActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;


public class FacSearchActivity extends ListActivity {

    // Just for the test.. to be removed..
    String[] DummyYet = { "Let's", "Blow", "Shit", "Up" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Not for this..
        //setContentView(R.layout.activity_fac_search);

        setListAdapter(new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                DummyYet));

    }

    protected void onListItemClick(ListView list, View v, int position, long id)
    {
        super.onListItemClick(list, v, position, id);

        // Still a test..

        String text = " position:" + position;
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_fac_search, menu);
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
}
