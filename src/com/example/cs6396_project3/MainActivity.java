package com.example.cs6396_project3;

import com.example.cs6396_project3.R;

import java.util.ArrayList;

//import android.R;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends ActionBarActivity {
    private BroadcastReceiver mReceiver;

    public void getBluetoothDevices(View v) {
            BluetoothAdapter BA = BluetoothAdapter.getDefaultAdapter();

            Log.i("getBluetoothDevices()", "Invoking...");

            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);

            ListView listView = (ListView)findViewById(R.id.listview);
            ArrayAdapter<String> adapter = (ArrayAdapter<String>)listView.getAdapter();

            adapter.clear();

            BA.startDiscovery();
            Log.i("getBluetoothDevices()", "Returning...");
            return;
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        Log.i("onCreate()", "Invoking...");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
        mReceiver = new BroadcastReceiver() {
            @Override
			public void onReceive(Context context, Intent intent ) {
                    Log.i("onReceive", "invoking...");
                String action = intent.getAction();
                Log.i("onReceive", "got action "+action );
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                    short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, (short)0);
                
                    name = name + " : " + rssi + "dB";
                    Log.i("onReceive", "Found device "+name );
                
                    ListView listView = (ListView)findViewById(R.id.listview);
                    ArrayAdapter<String> adapter = (ArrayAdapter<String>)listView.getAdapter();
                    adapter.add(name);

                }
                Log.i("onReceive", "exiting...");
            }
        };
        IntentFilter filter = new IntentFilter( BluetoothDevice.ACTION_FOUND );
        registerReceiver( mReceiver, filter );

        BluetoothAdapter BA;
        BA = BluetoothAdapter.getDefaultAdapter();
        if ( BA != null && !BA.isEnabled() ) {
            BA.enable();
        }

        final ListView listview = (ListView) findViewById(R.id.listview);
        Log.i("onCreate()", "listview = " + listview.toString() );
        ArrayList<String> list = new ArrayList<String>();

        ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>( this, android.R.layout.simple_list_item_1, list );
        listview.setAdapter(itemsAdapter);
        Log.i("onCreate()", "Exiting...");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

}
