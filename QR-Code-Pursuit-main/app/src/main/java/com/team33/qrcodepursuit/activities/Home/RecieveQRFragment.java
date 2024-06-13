package com.team33.qrcodepursuit.activities.Home;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.team33.qrcodepursuit.R;
import com.team33.qrcodepursuit.models.GameQRCode;

import java.io.ByteArrayOutputStream;

/**
 * handle adding identifying photo and location to GameQRCode
 */
public class RecieveQRFragment extends Fragment {

    private Button addPhotoButton;
    private Button addLocationButton;
    private Button submitButton;
    private Button cancelButton;
    private ImageView qrImage;
    private TextView qrScore;
    private GameQRCode qr;

    private NavController navController;
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;

    private boolean imageAdded;

    private ActivityResultLauncher<String> requestPermissionLauncher
            = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) {
            // granted, continue
        } else {
            // lol no location
        }
    });

    public RecieveQRFragment() {
        super(R.layout.fragment_scan);
    }

    /**
     * on creation, set up all elements defined in layout
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // all good
        } else { // extra step should be here but meh
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        imageAdded = false;
        Activity activity = getActivity();
        View view = inflater.inflate(R.layout.fragment_recieveqr, container, false);
        Bundle b = getArguments();
        db = FirebaseFirestore.getInstance();
        qr = (GameQRCode) b.getParcelable(ScanFragment.QRKEY);

        navController = Navigation.findNavController(container);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        addPhotoButton = view.findViewById(R.id.recieveqr_button_addphoto);
        addLocationButton = view.findViewById(R.id.recieveqr_button_addlocation);
        submitButton = view.findViewById(R.id.recieveqr_button_submit);
        cancelButton = view.findViewById(R.id.recieveqr_button_cancel);
        qrImage = view.findViewById(R.id.recieveqr_imageview_qrimage);
        qrScore = view.findViewById(R.id.recieveqr_textview_qrscore);

        qrImage.setImageBitmap(b.getParcelable(ScanFragment.QRBMP));
        qrScore.setText("Score: " + qr.getScore());

        addPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent imageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                try {
                    startActivityForResult(imageIntent, 1);
                } catch (ActivityNotFoundException e) {
                    // cant find cam app
                }
            }
        });

        addLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addLocation();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submit();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goHome();
            }
        });

        return view;
    }

    /**
     * retrieve compressed image (bitmap) from camera app
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == -1) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            qrImage.setImageBitmap(imageBitmap);
            if (qrImage.getDrawable() != null) {
                addPhotoButton.setText("Retake photo");
            }
            imageAdded = true;
        }
    }

    private void submit() {
        // update owner logic
        // if qr already exists
        CollectionReference qrcol = db.collection("GameQRs");
        final boolean[] addNewQR = {true};
        Task<QuerySnapshot> findMatches = qrcol.whereEqualTo("qrHash", qr.getQrHash()).get();
        findMatches
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                // todo: set user as another scanner of the scanned qr
                                // also let user know that their location and photo just got tossed lol
                                addNewQR[0] = false;
                            }
                            if (addNewQR[0]) {
                                addQRCode();
                            } else {
                                goHome();
                            }
                        }
                    }
                });
    }

    private void addQRCode() {
        StorageReference storage = FirebaseStorage.getInstance().getReference();
        CollectionReference qrcol = db.collection("GameQRs");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        qr.setOwner(user.getUid());

        qrcol.add(qr).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                if (imageAdded) {
                    String id = documentReference.getId();

                    StorageReference ref = storage.child("qrImages/" + id + ".jpg");
                    Bitmap bitm = ((BitmapDrawable) qrImage.getDrawable()).getBitmap();
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitm.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                    byte[] data = byteArrayOutputStream.toByteArray();

                    UploadTask uploadTask = ref.putBytes(data);

                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String url = uri.toString();
                                    qrcol.document(id).update("imageURL", url);
                                }
                            });
                        }
                    });
                }
            }
        });

        goHome();
    }

    private void addLocation() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                qr.setLocation(location);
                                addLocationButton.setEnabled(false);
                            }
                        }
                    });
        } else if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            addLocationButton.setText("Permission Denied");
            addLocationButton.setEnabled(false);
        }
        else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION);
            addLocation();
        }
    }

    private void goHome() {
        navController.popBackStack();
    }

}
