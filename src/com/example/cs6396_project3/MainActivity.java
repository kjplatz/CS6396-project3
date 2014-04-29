//
// CS 6396, Spring 2014
// Group 1
//   Gaurav Dey
//   Anush Krishnamurthy
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
import android.app.Activity;
//import android.R;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
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

public class MainActivity extends Activity {
//	public class MainActivity extends ActionBarActivity {

	//private BluetoothSerialService BlueSS;
    private BroadcastReceiver mReceiver;
    private BluetoothAdapter BA = BluetoothAdapter.getDefaultAdapter();
        
    private static final int maxTime = 30 * 1000;
    
    private MyView myview;
    
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
    	//BluetoothSerialService BlueSS;
    	boolean connected;
       	public LaunchPad( String a, int _x, int _y   ) {
    		address = a;
    		x = _x;
    		y = _y;
    		rssi = 0;
    		rssiVec = new Vector<RssiEntry>();
    		device = BA.getRemoteDevice( a );
    	    //BlueSS = new BluetoothSerialService();
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
//    	Log.i("Distance", ""+item+" = "+distance );
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

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        
		super.onCreate(savedInstanceState);
		setContentView( myview = new MyView(this, dm) );
		myview.setYPos( 126 );
		
		Log.i("Dimensions", "Height = " + dm.widthPixels + " Width = "+dm.heightPixels);
		// setContentView(R.layout.activity_main);
	    BA = BluetoothAdapter.getDefaultAdapter();
	    launchPads = new LaunchPad[5];
	    launchPads[0] = new LaunchPad( "EC:FE:7E:11:04:5D", 0, 10 );   // BlueRadios11045D
	    launchPads[1] = new LaunchPad( "EC:FE:7E:11:03:7E", 30, 210 );  // BlueRadios11037E
	    launchPads[2] = new LaunchPad( "EC:FE:7E:11:03:31", 52, 208 );  // BlueRadios110331
	    launchPads[3] = new LaunchPad( "EC:FE:7E:11:02:DD", 77, 1 );     // BlueRadios1102DD
	    launchPads[4] = new LaunchPad( "EC:FE:7E:11:03:2C", 103, 12 );     // BlueRadios11032C
      
      
      //For some reason when we added fingerprints[6] the app crashed. Maybe because of some index problem?? Please check.
	    fingerprints = new FingerPrint[22];
	    fingerprints[0] = new FingerPrint( 0, 76.4f, 81.222f, 73.0833f, 71.25f, 56.66f );
	    fingerprints[1] = new FingerPrint( 12, 75f, 83.13f, 73.32f, 70.608f, 55.2f);
	    fingerprints[2] = new FingerPrint( 16, 69.637f, 68.74286f, 63.9838f, 70.5169f, 74.36638f);
	    fingerprints[3] = new FingerPrint( 22, 69.395f, 68.36441f, 63.60759f, 70.75111f, 74.7668f);
	    fingerprints[4] = new FingerPrint( 28, 68.75728f, 67.614f, 63.02232f, 71.31731f, 75.72596f );
        fingerprints[5] = new FingerPrint( 35, 68.3281f, 66.9282f, 62.4928f, 71.78974f, 76.55498f );
        fingerprints[6] = new FingerPrint( 41, 67.96722f, 66.061f, 62.2551f, 72.86339f, 77.24f );
        fingerprints[7] = new FingerPrint( 47, 67.431f, 65.22034f, 61.887f, 74.1636f, 77.81481f );
        fingerprints[8] = new FingerPrint( 53, 66.85621f, 64.31875f, 62.0188f, 75.58108f, 78.34028f );
        fingerprints[9] = new FingerPrint( 59, 66.0365f, 63.9261f, 62.546764f, 76.81955f, 78.92969f );
        fingerprints[10] = new FingerPrint( 65, 65.352f, 63.5275f, 63.6774f, 77.83761f, 79.60361f );
        fingerprints[11] = new FingerPrint( 71, 64.60909f, 63.47368f, 64.71171f, 78.54717f, 80.06061f );
        fingerprints[12] = new FingerPrint( 78, 62.9777f, 64.39130f, 66.56322f, 79.69879f, 80.8533f );
	    fingerprints[13] = new FingerPrint( 84, 62.25f, 65.73684f, 67.666f, 80.169014f, 81.08064f);
	    fingerprints[14] = new FingerPrint( 90, 61.6181f, 67.6272f, 68.40351f, 80.92453f, 81.05882f );
        fingerprints[15] = new FingerPrint( 96, 60.75f, 67.8880f, 68.878f, 81.216f, 81.694f );
        fingerprints[16] = new FingerPrint( 102, 60.2222f, 67.2871f, 69.1f, 81.56f, 81.33336f );
	    fingerprints[17] = new FingerPrint( 108, 55.705883f, 75.4127f, 73.93846f, 84.339f, 83.9403f);
	    fingerprints[18] = new FingerPrint( 114, 56.7451f, 76.270836f, 73.791664f, 84.9025f, 83.72727f);
	    fingerprints[19] = new FingerPrint( 120, 57.85714f, 77.40625f, 74.90625f, 84.344f, 83.72973f);
	    fingerprints[20] = new FingerPrint( 126, 58.0689f, 77.8846f, 74.12f, 84.3333f, 83.9677f);
	    fingerprints[21] = new FingerPrint( 135, 59.7f, 78.1111f, 73.36364f, 85.90909f, 84f);
	    
	    
//		if (savedInstanceState == null) {
//			getSupportFragmentManager().beginTransaction()
//					.add(R.id.container, new PlaceholderFragment()).commit();
//		}
		
	    final Handler updateHandler = new Handler();
	    Runnable updateRunnable = new Runnable() {
	    	@Override
	    	public void run() {
	    		BluetoothAdapter.getDefaultAdapter().startDiscovery();
	    		updateHandler.postDelayed( this, 500 );
	    		long time = System.currentTimeMillis();
	    		float[] r = new float[launchPads.length];
	    		
	    		// Update the RSSI values of all launchpads.
	    		for( int i=0; i<launchPads.length; i++ ) {
	    			float dB = 0.0f;
	    			int count=0;
	    			for( RssiEntry re : launchPads[i].rssiVec ) {
	    				if ( (time - re.time) > maxTime ) continue;
	    				dB += re.dB;
	    				count++;
	    			}
	    			
	    			if ( count > 0 ) launchPads[i].rssi = dB / count;
	    			else             launchPads[i].rssi = 0f;
	    			
	    			r[i] = launchPads[i].rssi;
	    		}
	    		
	    		
	    		Log.i("PositionUpdate", ""+r );
	    		int min = find_closest( r );
	    		myview.setYPos( min );
	    	}
	    };
	    
	    updateHandler.postDelayed(updateRunnable, 500);
//		new Thread(new Runnable() {
//		    public void run() {
//                while(true) {
//                	try { 
//                		Thread.sleep(1000);
//                	} catch( Exception e ) {
//                		// Do nothing
//                	}
//                	BluetoothAdapter.getDefaultAdapter().startDiscovery();
//                }    	
//		    }
//		  }).start();
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
//                    
//                    ListView listView = (ListView)findViewById(R.id.listview);
//                    ArrayAdapter<String> adapter = (ArrayAdapter<String>)listView.getAdapter();
//                    
//                    adapter.clear();
                     
                    int i=0;
                    String s;
                    
                    int min = 0;
                    long time = System.currentTimeMillis();
                    for( ; i<launchPads.length; ++i ) {
//                    	float dB=0;
//                    	int count=0;
//                    	for( RssiEntry ent : launchPads[i].rssiVec ) {
//                    		if ( (ent.time - time) > maxTime ) {
//  //                  			launchPads[i].rssiVec.remove( ent );
//                    		} else {
//                    			dB += ent.dB;
//                    			count++;
//                    		}
//                    	}

                    	// Log.i( "seeking", "["+i+"]"+device.getAddress()+"  Checking against "+launchPads[i].address );
                    	if ( launchPads[i].address.equals( device.getAddress() ) ) {
                    		RssiEntry ent = new RssiEntry( rssi, time );
//                    		dB += rssi;
//                    		count++;
                    		launchPads[i].rssiVec.add( ent );
                    		
//                    		if ( launchPads[i].connected == false ) {
//                    		    launchPads[i].device = device;
//                    		    launchPads[i].connected = true;
//                    		} 
//                    		launchPads[i].rssi = rssi;
                    	}
//                    	if (count > 0) {
//                    	    launchPads[i].rssi = dB / count;
//                    	    r[i] = launchPads[i].rssi;
//                    	} else {
//                            launchPads[i].rssi = 0;
//                    	}

                        //Log.i("onReceive", "Found device "+name );
//                    	BluetoothDevice bd = launchPads[i].device;
//                    	s = bd.getName();
//                        if (launchPads[i].rssi > 0 ) {
//                        	s += " -" + launchPads[i].rssi + "dB";
//                        } else {
//                        	s += " not found";
//                        }     		
//                        adapter.add(s);
                	}
//                    min = find_closest(r);
//                    myview.setYPos(fingerprints[min].x);
//                    s = "Location: " + fingerprints[min].x + " units";
//                    adapter.add(s);
//                    
//                    s = "Closest to fingerprint: " +min;
//                    adapter.add(s);
                    	
                    Log.i( "onReceive", "Location is " + fingerprints[min].x );

                }
            }
        };
        IntentFilter filter = new IntentFilter( BluetoothDevice.ACTION_FOUND );
        registerReceiver( mReceiver, filter );
        Log.i( "onCreate",  "Registered receiver..." );

        BA = BluetoothAdapter.getDefaultAdapter();
        if ( BA != null && !BA.isEnabled() ) {
            BA.enable();
        }
        BA.startDiscovery();

//        final ListView listview = (ListView) findViewById(R.id.listview);
//        Log.i("onCreate()", "listview = " + listview.toString() );
//        ArrayList<String> list = new ArrayList<String>();
//        for( int i=0; i<launchPads.length; i++ ) {
//        	list.add("");
//        }
//
//        ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>( this, android.R.layout.simple_list_item_1, list );
//        listview.setAdapter(itemsAdapter);
//        
//        for( int i=0; i<launchPads.length; i++ ) {
//        	itemsAdapter.add("");
//        }

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
