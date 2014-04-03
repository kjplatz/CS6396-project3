//
// CS 6396, Spring 2014
// Group 1
//   Gaurav Dey
//   Annush Krishnamurthy
//   Kenneth Platz
//   Vinoth Ravady
//   Arulmani Sennimali
//
// Phase 1
// This application provides two major pieces of functionality
// - A "Scan" button will invoke a Bluetooth startDiscovery()
//   to populate the ListView with all Bluetooth devices in range
// - A ListView object which, when selected, will attempt to
//   open a serial connection to the bluetooth device selected,
//   and will send two commands to the device.
//
package com.example.cs6396_project3;

import com.example.cs6396_project3.R;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;



import java.util.UUID;

import android.annotation.TargetApi;
//import android.R;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {
	private BluetoothSerialService BlueSS;
    private BroadcastReceiver mReceiver;
    private ArrayList<BluetoothDevice> btDevs;

    public void getBluetoothDevices(View v) {
            BluetoothAdapter BA = BluetoothAdapter.getDefaultAdapter();

            Log.i("getBluetoothDevices()", "Invoking...");

            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);

            ListView listView = (ListView)findViewById(R.id.listview);
            ArrayAdapter<String> adapter = (ArrayAdapter<String>)listView.getAdapter();

            adapter.clear();
            btDevs = new ArrayList<BluetoothDevice>();

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
                
                    name = name + " : " + rssi + "dB" +  " (" + device.getAddress() + ")";
                    Log.i("onReceive", "Found device "+name );
                
                    ListView listView = (ListView)findViewById(R.id.listview);
                    ArrayAdapter<String> adapter = (ArrayAdapter<String>)listView.getAdapter();
                    adapter.add(name);
                    btDevs.add(device);

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

        BlueSS = new BluetoothSerialService( );
        final ListView listview = (ListView) findViewById(R.id.listview);
        Log.i("onCreate()", "listview = " + listview.toString() );
        ArrayList<String> list = new ArrayList<String>();

        ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>( this, android.R.layout.simple_list_item_1, list );
        listview.setAdapter(itemsAdapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener()  {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				final long id_final = id;
				final View view_final = view;
		    	BluetoothDevice bt = btDevs.get((int)id_final);
		        Log.i( "listItemSelected()", "id = "+id_final);
		        Log.i( "listItemSelected()", ((TextView)view_final).getText().toString() );	
		        Log.i( "listItemSelected()", bt.getName());
				
				BlueSS.connect( bt );
				new Thread(new Runnable() {
				    public void run() {
				    	do {
				    		try {
								Thread.sleep(1);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								Log.e( "Thread", "Exception", e );
							}
				    	} while( BlueSS.getState() != BluetoothSerialService.STATE_CONNECTED );
//				    	BlueSS.write("?\r\n".getBytes());
				    	BlueSS.write( "put 2 100\n".getBytes() );
				    	try {
							Thread.sleep(1000);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							Log.e( "Thread", "Exception", e1 );
						}
				    	BlueSS.write( "put 2 0\n".getBytes() );
//				    	BlueSS.write("?\r\n".getBytes());
				    	
				    }
				  }).start();		
			}
        	
        } );
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
