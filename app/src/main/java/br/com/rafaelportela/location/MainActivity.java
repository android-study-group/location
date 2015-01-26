package br.com.rafaelportela.location;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;

import static com.google.android.gms.common.GooglePlayServicesUtil.isGooglePlayServicesAvailable;


public class MainActivity extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public static final String TAG = "MainActivity";
    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buildGoogleApiClient();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            ((TextView) findViewById(R.id.hello_label)).setText("Google Play Services Available!");
        }
        else {
            ((TextView) findViewById(R.id.hello_label)).setText("Google Play Services NOT Available.");
        }
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Requesting location");

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,
                locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "Failed to connect to Play Services.");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "onLocationChanged: " + location.getLatitude() + ", " + location.getLongitude());
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);

        new GeocoderTask().execute(location);
    }

    protected void updateUI(String country) {
        ((TextView) findViewById(R.id.country)).setText("You are in " + country);
    }

    class GeocoderTask extends AsyncTask<Location, Void, List<Address>> {

        @Override
        protected List<Address> doInBackground(Location... params) {

            Location location = params[0];
            List<Address> addresses = null;

            try {
                 addresses = new Geocoder(MainActivity.this).getFromLocation(
                        location.getLatitude(),
                        location.getLongitude(), 1);

            } catch (IOException e) {
                e.printStackTrace();
            }

            return addresses;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {
            updateUI(addresses.get(0).getCountryName());
        }
    }
}