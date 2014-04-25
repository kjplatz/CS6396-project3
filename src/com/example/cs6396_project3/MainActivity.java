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
import java.util.Vector;

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
        
    private static final int maxTime = 60 * 1000;
    
    private class RssiEntry {
    	int dB;
    	long time;
    	
    	RssiEntry( int _dB ) {
    		time = System.currentTimeMillis();
    		dB = _dB;
    	}
    	RssiEntry( int _dB, long _time ) {
    		time = _time;
    		dB = _dB;
    	}
    }
    
    private class LaunchPad {
    	String address;
    	int x;
    	int y;
    	float rssi;
    	Vector<RssiEntry> rssiVec;
    	// short rssi;
    	BluetoothDevice device;
    	BluetoothSerialService BlueSS;
    	boolean connected;
       	public LaunchPad( String a, int _x, int _y   ) {
    		address = a;
    		x = _x;
    		y = _y;
    		rssi = 0;
    		rssiVec = new Vector<RssiEntry>();
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
    
    private class FingerPrint {
    	int x;
    	float[] rssi;

    	FingerPrint( int _x, float _r1, float _r2, float _r3, float _r4, float _r5 ) {
    		x=_x;
            rssi = new float[5];
            rssi[0] = _r1;
            rssi[1] = _r2;
            rssi[2] = _r3;
            rssi[3] = _r4;
            rssi[4] = _r5;
    	};
    };
    
    private float find_distance( int item, float[] rssi ) {
    	float distance = 0;
    	float r=0;
    	for( int i=0; i<fingerprints[item].rssi.length; i++ ) {
    		if ( rssi[i] == 0 ) continue;
    		
    		r = fingerprints[item].rssi[i] - rssi[i];
    		distance += (r * r);
    	}
    	Log.i("Distance", ""+item+" = "+distance );
    	return distance;
    }
    private int find_closest( float[] rssi ) {
    	int closest=0;
    	float closest_dist=find_distance( 0, rssi );
    	for( int i=1; i<fingerprints.length; i++ ) {
    		float dist = find_distance( i, rssi );
    		if ( dist < closest_dist ) {
    			closest_dist = dist;
    			closest = i;
    		}
    	}
        return closest;	
    }
    
    private FingerPrint[] fingerprints;
	
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
	    launchPads[4] = new LaunchPad( "EC:FE:7E:11:03:2C", 103, 12 );     // BlueRadios11032C
      
      
      //For some reason when we added fingerprints[6] the app crashed. Maybe because of some index problem?? Please check.
	    fingerprints = new FingerPrint[5];
	    fingerprints[0] = new FingerPrint( 12, 77.4f, 84.4f, 0.0f, 74.7f, 58.0f);
	    fingerprints[1] = new FingerPrint( 16, 76.56338f, 81.1125f, 0.0f, 68.61628f, 57.2637f);
	    fingerprints[2] = new FingerPrint( 22, 77.85f, 80.3158f, 0.0f, 66.38461f, 66.21f);
	    fingerprints[3] = new FingerPrint( 84, 64.0f, 62.0f, 0.0f, 76.0f, 77.0f);
	    fingerprints[4] = new FingerPrint( 108, 49.3f, 74.36364f, 0.0f, 85.6f, 83.2f);
	    //fingerprints[5] = new FingerPrint( 114, 53.0f, 75.0f, 0.0f, 83.0f, 83.0f);
	    //fingerprints[6] = new FingerPrint( 120, 54.8571f, 76.75f, 0.0f, 81.14286f, 83.1f);
	 //   fingerprints[7] = new FingerPrint( 126, 55.0f, 75.39286f, 0.0f, 82.36f, 84.6263f);
	    //fingerprints[7] = new FingerPrint( 135, 58.625f, 76.0625f, 0.0f, 85.28571f, 85.66664f);
	    
	    
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
                    if ( name == null ) name = "(null)";
                    Log.i( "onReceive",  name );
                    short rssi =  (short) Math.abs( intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, (short)0) );
                    float[] r = new float[launchPads.length];
                    
                    ListView listView = (ListView)findViewById(R.id.listview);
                    ArrayAdapter<String> adapter = (ArrayAdapter<String>)listView.getAdapter();
                    
                    adapter.clear();
                     
                    int i=0;
                    String s;
                    
                    int min = 0;
                    long time = System.currentTimeMillis();
                    for( ; i<launchPads.length; ++i ) {
                    	float dB=0;
                    	int count=0;
                    	for( RssiEntry ent : launchPads[i].rssiVec ) {
                    		if ( (ent.time - time) > maxTime ) {
                    			launchPads[i].rssiVec.remove( ent );
                    		} else {
                    			dB += ent.dB;
                    			count++;
                    		}
                    	}

                    	// Log.i( "seeking", "["+i+"]"+device.getAddress()+"  Checking against "+launchPads[i].address );
                    	if ( launchPads[i].address.equals( device.getAddress() ) ) {
                    		RssiEntry ent = new RssiEntry( rssi, time );
                    		dB += rssi;
                    		count++;
                    		launchPads[i].rssiVec.add( ent );
                    		
                    		if ( launchPads[i].connected == false ) {
                    		    launchPads[i].device = device;
                    		    launchPads[i].connected = true;
                    		} 
                    		launchPads[i].rssi = rssi;
                    	}
                    	if (count > 0) {
                    	    launchPads[i].rssi = dB / count;
                    	    r[i] = launchPads[i].rssi;
                    	} else {
                            launchPads[i].rssi = 0;
                    	}

                        Log.i("onReceive", "Found device "+name );
                    	BluetoothDevice bd = launchPads[i].device;
                    	s = bd.getName();
                        if (launchPads[i].rssi > 0 ) {
                        	s += " -" + launchPads[i].rssi + "dB";
                        } else {
                        	s += " not found";
                        }     		
                        adapter.add(s);
                	}
                    min = find_closest(r);
                    s = "Location: " + fingerprints[min].x + " units";
                    adapter.add(s);
                    
                    s = "Closest to fingerprint: " +fingerprints[min].rssi[min];
                    adapter.add(s);
                    	
                    //Log.i( "Closest", fingerprints[min].rssi);

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
