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



import java.util.Calendar;
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
    private BluetoothAdapter BA = BluetoothAdapter.getDefaultAdapter();
        
    private static final int interval = 10;
    
    private class RssiEntry {
    	int dB;
    	long time;
    	
    	RssiEntry( int _dB ) {
    		time = System.currentTimeMillis();
    		dB = _dB;
    	}
    }
    
    private class LaunchPad {
    	String address;
    	int x;
    	int y;
    	float rssi;
    	// short rssi;
    	BluetoothDevice device;
    	BluetoothSerialService BlueSS;
    	boolean connected;
       	public LaunchPad( String a, int _x, int _y   ) {
    		address = a;
    		x = _x;
    		y = _y;
    		rssi = 0;
    		device = BA.getRemoteDevice( a );
    	    BlueSS = new BluetoothSerialService();
    	    connected = false;
    	}
    }
    
    LaunchPad[] launchPads;

    public void getBluetoothDevices(View v) {
            Log.i("getBluetoothDevices()", "Invoking...") ;

            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);

            ListView listView = (ListView)findViewById(R.id.listview);
            ArrayAdapter<String> adapter = (ArrayAdapter<String>)listView.getAdapter();

            BA.startDiscovery(); 
            Log.i("getBluetoothDevices()", "Returning..." );
            return;
    }
    
    private int calc_distance( int min ) {
    	int d1, d2, d3;
    	switch(min) {
    	//P3
    	case 0: 
    		d1 = (int)(-30 / (launchPads[0].rssi + 66));
    		d2 = (int)(52 - 22/(launchPads[1].rssi + 76));
    		return (d1+d2)/2;
    	//P4
    	case 1:
    		d1 = (int)(-52 - 22/(launchPads[1].rssi + 76));
    		d2 = (int)(77 -  25/ (launchPads[2].rssi + 70));
    		return ((d1+d2)/2);
    	//P6
    	case 2:
    		d1 = (int)(-77 - 25/(launchPads[2].rssi + 70));
    		d2 = (int)(103 -  26/ (launchPads[3].rssi + 71));
    		return ((d1+d2)/2);
    	}
    	return Integer.MAX_VALUE;
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        Log.i("onCreate()", "Invoking...");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	    BA = BluetoothAdapter.getDefaultAdapter();
	    launchPads = new LaunchPad[5];
	    launchPads[0] = new LaunchPad( "EC:FE:7E:11:04:5D", 0, 10 );   // BlueRadios11045D
	    launchPads[1] = new LaunchPad( "EC:FE:7E:11:03:7E", 30, 210 );  // BlueRadios11037E
	    launchPads[2] = new LaunchPad( "EC:FE:7E:11:03:31", 52, 208 );  // BlueRadios110331
	    launchPads[3] = new LaunchPad( "EC:FE:7E:11:02:DD", 77, 1 );     // BlueRadios1102DD
	    launchPads[4] = new LaunchPad( "EC:FE:7E:11:03:94", 103, 1 );     // BlueRadios1102DD

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		new Thread(new Runnable() {
		    public void run() {
                while(true) {
                	try { 
                		Thread.sleep(1000);
                	} catch( Exception e ) {
                		// Do nothing
                	}
                	BluetoothAdapter.getDefaultAdapter().startDiscovery();
                }    	
		    }
		  }).start();
        mReceiver = new BroadcastReceiver() {
            @Override
			public void onReceive(Context context, Intent intent ) {
                    Log.i("onReceive", "invoking...");
                String action = intent.getAction();
                // Log.i("onReceive", "got action "+action );
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                    Log.i( "onReceive",  name );
                    short rssi =  (short) Math.abs( intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, (short)0) );
                    
                    ListView listView = (ListView)findViewById(R.id.listview);
                    ArrayAdapter<String> adapter = (ArrayAdapter<String>)listView.getAdapter();
                    
                    adapter.clear();
                     
                    int i=0;
                    String s;
                    
                    int min = 0;
                    for( ; i<launchPads.length; ++i ) {
                    	// Log.i( "seeking", "["+i+"]"+device.getAddress()+"  Checking against "+launchPads[i].address );
                    	if ( launchPads[i].address.equals( device.getAddress() ) ) {
                    		if ( launchPads[i].connected == false ) {
                    		    launchPads[i].device = device;
                    		} 
                    		launchPads[i].rssi = rssi;
                    	}

                        Log.i("onReceive", "Found device "+name );
                    	BluetoothDevice bd = launchPads[i].device;
                    	s = bd.getName();
                        if (launchPads[i].rssi > 0 ) {
                        	Log.i("found", s + " min = " + min );
                        	s += " -" + launchPads[i].rssi + "dB";
                        	if ( launchPads[i].rssi < launchPads[min].rssi ) min = i;
                        } else {
                        	s += " not found";
                        }     		
                        adapter.add(s);
                	}
                    int x = calc_distance( min );
                    s = "Location: " + x + " units";
                    adapter.add(s);
                    
                    s = "Closest to device: " + launchPads[min].address;
                    adapter.add(s);
                    	
                    Log.i( "Closest", launchPads[min].address);

                }
            }
        };
        IntentFilter filter = new IntentFilter( BluetoothDevice.ACTION_FOUND );
        registerReceiver( mReceiver, filter );

        BA = BluetoothAdapter.getDefaultAdapter();
        if ( BA != null && !BA.isEnabled() ) {
            BA.enable();
        }
        BA.startDiscovery();

        final ListView listview = (ListView) findViewById(R.id.listview);
        Log.i("onCreate()", "listview = " + listview.toString() );
        ArrayList<String> list = new ArrayList<String>();
        for( int i=0; i<launchPads.length; i++ ) {
        	list.add("");
        }

        ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>( this, android.R.layout.simple_list_item_1, list );
        listview.setAdapter(itemsAdapter);
        
        for( int i=0; i<launchPads.length; i++ ) {
        	itemsAdapter.add("");
        }

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
	
	protected void onStop() {
		super.onStop();
		unregisterReceiver( mReceiver );
	}

}
