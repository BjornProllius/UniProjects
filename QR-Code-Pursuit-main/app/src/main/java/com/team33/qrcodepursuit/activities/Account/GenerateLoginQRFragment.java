package com.team33.qrcodepursuit.activities.Account;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.team33.qrcodepursuit.R;

public class GenerateLoginQRFragment extends Fragment {

    private ImageView qrImage;
    private Button saveButton;
    private Button backButton;

    private FirebaseAuth auth;

    public GenerateLoginQRFragment() {
        super(R.layout.fragment_generateloginqr);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_generateloginqr, container, false);
        qrImage = view.findViewById(R.id.generateloginqr_imageview_qr);
        auth = FirebaseAuth.getInstance();
        FirebaseApp app = FirebaseApp.initializeApp(getContext());
        auth.getCurrentUser().getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<GetTokenResult> task) {
                if (task.isSuccessful()) {
                    String token = task.getResult().getToken();
                    qrImage.setImageBitmap(makeLoginQR(token));
                }
            }
        });

        return view;
    }

    private Bitmap makeLoginQR(String token) {
        int x = 300;
        int y = x;
        Bitmap bitm = Bitmap.createBitmap(x, y, Bitmap.Config.ARGB_8888);
        BitMatrix matrix = null;
        try {
            QRCodeWriter writer = new QRCodeWriter();
            matrix = writer.encode(token, BarcodeFormat.QR_CODE, x, y);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                bitm.setPixel(i, j, matrix.get(i, j) ? Color.BLACK : Color.WHITE);
            }
        }

        return bitm;
    }

}
