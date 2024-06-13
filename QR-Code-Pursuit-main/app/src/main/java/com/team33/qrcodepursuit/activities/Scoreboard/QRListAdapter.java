package com.team33.qrcodepursuit.activities.Scoreboard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.team33.qrcodepursuit.R;
import com.team33.qrcodepursuit.models.Comment;

import java.util.ArrayList;

/**
 * adapt qr data into a ListView
 */
public class QRListAdapter extends BaseAdapter {

    private ArrayList<String> localData;

    /**
     * simply construct an adapter
     * @param data to be displayed
     */
    public QRListAdapter(ArrayList<String> data) {
        localData = data;
    }

    /**
     * get size of list
     * @return
     */
    @Override
    public int getCount() {
        return localData.size();
    }

    /**
     * get item of a certain index
     * @param i index
     * @return
     */
    @Override
    public Object getItem(int i) {
        return localData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.view_qr, viewGroup, false);
        View finalView = view;
        System.out.println(localData.get(i));
        System.out.println(i);
        StorageReference qrref = FirebaseStorage.getInstance().getReference("qrImages/"+localData.get(i)+".jpg");
        qrref.getBytes(1024*1024*10).addOnCompleteListener(new OnCompleteListener<byte[]>() {
            @Override
            public void onComplete(@NonNull Task<byte[]> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().length >= 0) {
                        byte[] bytes = task.getResult();
                        Bitmap bitm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        ((ImageView) finalView.findViewById(R.id.qr_image)).setImageBitmap(bitm);
                    }
                }
            }
        });
        ((TextView) (view.findViewById(R.id.qr_id))).setText(localData.get(i));

        return view;
    }
}
