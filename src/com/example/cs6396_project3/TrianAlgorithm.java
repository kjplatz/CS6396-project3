/*
import java.lang.*;
public class TrianAlgorithm {

	static class coordi{
		float x;
		float y;
	}

	static coordi calculateCoordinates(float xa, float ya, float xb, float yb, float xc, float yc, float ra, float rb, float rc)
	{
		coordi c = new coordi();
		//	Calculate XY with Trilateration (Beacons 1, 2, and 3 are Beacon subclasses with pre-set X and Y values for location and distance is calculated as above).
	
		float S = (float) ((Math.pow(xc, 2.) - Math.pow(xb, 2.) + Math.pow(yc, 2.) - Math.pow(yb, 2.) + Math.pow(rb, 2.) - Math.pow(rc, 2.)) / 2.0);
		float T = (float) ((Math.pow(xa, 2.) - Math.pow(xb, 2.) + Math.pow(ya, 2.) - Math.pow(yb, 2.) + Math.pow(rb, 2.) - Math.pow(ra, 2.)) / 2.0);
		c.y = ((T * (xb - xc)) - (S * (xb - xa))) / (((ya - yb) * (xb - xc)) - ((yc - yb) * (xb - xa)));
		c.x = ((c.y * (ya - yb)) - T) / (xb - xa);
	
		return c;
	}
	
	static double calculateAccuracyWithRSSI(double rssi)
	{
		if (rssi == 0) {
	        return -1.0; // if we cannot determine accuracy, return -1.
	    }

	    double power = -70;
	    double ratio = rssi*1.0/power;
	    if (ratio < 1.0) {
	        return Math.pow(ratio,10);
	    }
	    else {
	        double accuracy =  (0.89976) * Math.pow(ratio,7.7095) + 0.111;
	        return accuracy;
	    }
	}
	
	public static void main(String args[])
	{
		double rssi = -50;
		float xa=0; float ya=0; float xb=0; float yb=0; float xc=0; float yc=0; float ra=0; float rb=0; float rc=0;
		double accurateRssi = arulTest.calculateAccuracyWithRSSI(rssi);
		//Not sure of the kFilteringFactor, commented for now
		//rollingRssi = (beacon.rssi * kFilteringFactor) + (rollingRssi * (1.0 - kFilteringFactor));
		coordi final_coordi = arulTest.calculateCoordinates(xa, ya, xb, yb, xc, yc, ra, rb, rc);
	}
}
*/
