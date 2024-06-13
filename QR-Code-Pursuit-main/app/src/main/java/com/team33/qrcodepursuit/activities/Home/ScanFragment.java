/*
https://github.com/yuriy-budiyev/code-scanner
MIT License

Copyright (c) 2017 Yuriy Budiyev [yuriy.budiyev@yandex.ru]

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

// also, perhaps make this multipurpose (use for login), and the qrreader dependency isn't needed anymore

package com.team33.qrcodepursuit.activities.Home;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.zxing.Result;
import com.team33.qrcodepursuit.R;
import com.team33.qrcodepursuit.models.GameQRCode;

/**
 * first fragment, automatically scans for QR code
 */
public class ScanFragment extends Fragment {

    public static String QRKEY = "com.team33.qrcodepursuit.NEWQR";
    public static String QRBMP = "com.team33.qrcodepursuit.NEWBMP";

    private CodeScanner qrScanner;
    private NavController navController;

    private ActivityResultLauncher<String> requestPermissionLauncher
      = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) {
            // granted, continue
        } else {
            // some msg i guess
        }
    });

    public ScanFragment() {
        super(R.layout.fragment_scan);
    }

    /**
     * on creation, setup scan result
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        navController = Navigation.findNavController(container);

        // not sure where else to put this
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // all good
        } else { // extra step should be here but meh
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }

        Activity activity = getActivity();
        View view = inflater.inflate(R.layout.fragment_scan, container, false);
        CodeScannerView scannerView = view.findViewById(R.id.scan_scannerview);
        qrScanner = new CodeScanner(activity, scannerView);

        qrScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull Result result) {
                // generate qr object
                Bundle args = new Bundle();
                GameQRCode newQR = new GameQRCode(result);

                Bitmap bitm = GameQRCode.getQRImage(result.getText());
                args.putParcelable(QRKEY, newQR);
                args.putParcelable(QRBMP, bitm);

                // give this to UI thread so it doesn't complain/mess up camera
                Handler handler = new Handler(getContext().getMainLooper());
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        navController.navigate(R.id.action_scanFragment_to_recieveQRFragment, args);
                    }
                };
                handler.post(runnable);

            }
        });

        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                qrScanner.startPreview();
            }
        });
        return view;
    }

    /**
     * restart qr scanner
     */
    @Override
    public void onResume() {
        super.onResume();
        qrScanner.startPreview();
    }

    /**
     * clean up qr scanner (free the camera)
     */
    @Override
    public void onPause() {
        qrScanner.releaseResources();
        super.onPause();
    }

}
