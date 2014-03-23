package com.example.cs6396_project3;

import com.example.cs6396_project3.R;

import java.io.BufferedWriter;
import java.io.IOException;
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
                
                    name = name + " : " + rssi + "dB";
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

        final ListView listview = (ListView) findViewById(R.id.listview);
        Log.i("onCreate()", "listview = " + listview.toString() );
        ArrayList<String> list = new ArrayList<String>();

        ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>( this, android.R.layout.simple_list_item_1, list );
        listview.setAdapter(itemsAdapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener()  {

			@Override
			@TargetApi(15)
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				final long id_final = id;
				final View view_final = view;
				new Thread(new Runnable() {
				    public void run() {
				    	BluetoothDevice bt = btDevs.get((int)id_final);
				        Log.i( "listItemSelected()", "id = "+id_final);
				        Log.i( "listItemSelected()", ((TextView)view_final).getText().toString() );	
				        Log.i( "listItemSelected()", bt.getName());
				        Log.i( "listItemSelected()", ""+btDevs.get((int)id_final).getUuids().length );
				        Log.i( "listItemSelected()", btDevs.get((int)id_final).getUuids()[0].getUuid().toString());
				        UUID uuid =  bt.getUuids()[0].getUuid();
				        try {
				        	BluetoothSocket btSock = bt.createRfcommSocketToServiceRecord(uuid);
				        	btSock.connect();
				        	OutputStream os = btSock.getOutputStream();
				        	OutputStreamWriter ow = new OutputStreamWriter(os);
				        	BufferedWriter writer = new BufferedWriter(ow);
				        	
				        	writer.write("put 2 100\n");
				        	Thread.sleep(1000);
				        	writer.write("put 2 0\n" );
				        } catch (IOException e ) {
				        	Log.e("listItemsSelected()", "Error opening BluetoothSocket"+e.toString());
				        } catch (InterruptedException e ) {
				        	Log.e("listItemsSelected()", "Error sleeping"+e.toString() );
				        }
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
