package com.example.plugin;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LOG;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BeaconPlugin extends CordovaPlugin {

    private static final int REQUEST_ENABLE_BT = 1234;
    private static final Region ALL_ESTIMOTE_BEACONS_REGION = new Region("rid", null, null, null);
    private static final String TAG="BeaconPlugin";
    private ArrayList<Beacon> beacons=new ArrayList<Beacon>();

    private BeaconManager beaconManager;
    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

        if(action.equals("startScanning")){

            // Configure BeaconManager.
            beaconManager = new BeaconManager(cordova.getActivity());
            beaconManager.setRangingListener(new BeaconManager.RangingListener() {
                @Override
                public void onBeaconsDiscovered(Region region, final List<Beacon> beacons) {
                    // Note that results are not delivered on UI thread.
                    cordova.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Note that beacons reported here are already sorted by estimated
                            // distance between device and beacon.
                            replaceWith(beacons);
                            Log.d(TAG,"Beacons Found ,"+beacons.size());
                        }
                    });
                }
            });

            // Check if device supports Bluetooth Low Energy.
            if (!beaconManager.hasBluetooth()) {
//                Toast.makeText(this, "Device does not have Bluetooth Low Energy", Toast.LENGTH_LONG).show();
                LOG.e(TAG,"Device does not have Bluetooth Low Energy");
                return false;
            }

            // If Bluetooth is not enabled, let user enable it.
            if (!beaconManager.isBluetoothEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                cordova.getActivity().startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                connectToService();
            }

            callbackContext.success("");

            return true;
        }
        else {
            
            return false;

        }
    }
    public void replaceWith(Collection<Beacon> newBeacons) {
        this.beacons.clear();
        this.beacons.addAll(newBeacons);
        for (int i = 0; i <beacons.size() ; i++) {
            Beacon beacon = beacons.get(i);
            Log.d(TAG,"Beacon Object "+beacon.toString());
        }
    }

    private void connectToService() {
        LOG.d(TAG,"Scanning");
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                try {
                    beaconManager.startRanging(ALL_ESTIMOTE_BEACONS_REGION);
                } catch (RemoteException e) {
                    LOG.e(TAG, "Cannot start ranging", e);
                }
            }
        });
    }
}
