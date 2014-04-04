public class  TrianAlgorithm{
	(double)calculateAccuracyWithRSSI:(double)rssi {
	    //formula adapted from David Young's Radius Networks Android iBeacon Code
	    if (rssi == 0) {
	        return -1.0; // if we cannot determine accuracy, return -1.
	    }


	    double txPower = -70;
	    double ratio = rssi*1.0/txPower;
	    if (ratio < 1.0) {
	        return pow(ratio,10);
	    }
	    else {
	        double accuracy =  (0.89976) * pow(ratio,7.7095) + 0.111;
	        return accuracy;
	    }
	}

//	Calculate XY with Trilateration (Beacons 1, 2, and 3 are Beacon subclasses with pre-set X and Y values for location and distance is calculated as above).

	float xa = beacon1.locationX;
	float ya = beacon1.locationY;
	float xb = beacon2.locationX;
	float yb = beacon2.locationY;
	float xc = beacon3.locationX;
	float yc = beacon3.locationY;
	float ra = beacon1.filteredDistance;
	float rb = beacon2.filteredDistance;
	float rc = beacon3.filteredDistance;

	float S = (pow(xc, 2.) - pow(xb, 2.) + pow(yc, 2.) - pow(yb, 2.) + pow(rb, 2.) - pow(rc, 2.)) / 2.0;
	float T = (pow(xa, 2.) - pow(xb, 2.) + pow(ya, 2.) - pow(yb, 2.) + pow(rb, 2.) - pow(ra, 2.)) / 2.0;
	float y = ((T * (xb - xc)) - (S * (xb - xa))) / (((ya - yb) * (xb - xc)) - ((yc - yb) * (xb - xa)));
	float x = ((y * (ya - yb)) - T) / (xb - xa);

	CGPoint point = CGPointMake(x, y);
	return point;
}
