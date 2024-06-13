package com.team33.qrcodepursuit.activities.Map;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.team33.qrcodepursuit.R;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

// Fragment which represents the map screen
public class MapFragment extends Fragment {
    private final String TAG = "MapFragment";

    private MapView map = null;
    FirebaseFirestore db;
    CollectionReference qrs;
    ArrayList<QueryDocumentSnapshot> nearbyScans;
    ArrayList<OverlayItem> overlayItemArray;
    ItemizedIconOverlay<OverlayItem> itemizedIconOverlay;

    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;

    public MapFragment() {
        // Required empty public constructor
    }

    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = this.getContext();
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));

        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();

        // Create a reference to the qrs collection
        qrs = db.collection("GameQRs");

        overlayItemArray = new ArrayList<OverlayItem>();

        qrs.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Thread thread = new Thread(new Runnable() {

                            @Override
                            public void run() {
                                try  {
                                    Map<String, Object> location = (Map<String, Object>)document.get("location");
                                    String imageURL = (String)document.get("imageURL");
                                    Drawable image = null;
                                    if (imageURL != null) {

                                        try {
                                            Bitmap bitmap = BitmapFactory.decodeStream((InputStream)new URL(imageURL).getContent());
                                            image = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 150, 150, false));
                                        } catch (MalformedURLException e) {
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    if (location != null) {
                                        GeoPoint QRlocation = new GeoPoint((double)location.get("latitude"), (double)location.get("longitude"));
                                        OverlayItem newOverlay = new OverlayItem(document.getId(), "", QRlocation);
                                        if (image != null)
                                            newOverlay.setMarker(image);

                                        itemizedIconOverlay.addItem(newOverlay);
                                    }

                                    map.invalidate();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        thread.start();


                    }
                } else {
                    Log.e(TAG, "Error while querying for QRs");
                }
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        requestPermissionsIfNecessary(new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.INTERNET
        });

        // Inflate the layout for this fragment
        map = rootView.findViewById(R.id.mapview);
        map.getController().setZoom(10.0);
        map.setMinZoomLevel(4.0);

        MyLocationNewOverlay mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(getContext()), map);
        mLocationOverlay.enableMyLocation();
        GeoPoint myLocation = mLocationOverlay.getMyLocation();

        if (myLocation != null) {
            map.getOverlays().add(mLocationOverlay);
            map.getController().animateTo(new GeoPoint(myLocation.getLatitude(), myLocation.getLongitude()));
        } else {
            map.getController().animateTo(new GeoPoint(53.0, 113.0));
        }

        itemizedIconOverlay = new ItemizedIconOverlay<OverlayItem>(
                this.getActivity(), overlayItemArray, null);
        map.getOverlays().add(itemizedIconOverlay);

        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.ALWAYS);
        map.setMultiTouchControls(true);

        return rootView;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            permissionsToRequest.add(permissions[i]);
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(this.getActivity(), permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this.getActivity(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this.getActivity(), permissionsToRequest.toArray(new String[0]), REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }
}