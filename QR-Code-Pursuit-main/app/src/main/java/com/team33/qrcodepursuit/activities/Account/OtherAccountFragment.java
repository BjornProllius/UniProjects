package com.team33.qrcodepursuit.activities.Account;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.team33.qrcodepursuit.R;
import com.team33.qrcodepursuit.models.Account;

public class OtherAccountFragment extends AccountFragment {

    public static String ACCKEY = "com.team33.qrcodepursuit.OTHERACC";

    /**
     * sets this.acc to Account passed in with Bundle
     * @return true if Account was passed in, false if not
     */
    @Override
    protected boolean setAccount() {
        Bundle args = getArguments();
        if (args == null) return false;
        this.acc = args.getParcelable("account");
        return true;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        settings_button.setVisibility(View.GONE);
        editpf_button.setVisibility(View.GONE);
    }

    @Override
    protected void settings_button_onclick(View v) { }

    @Override
    protected void editpf_button_onclick(View v) { }

    @Override
    protected void collection_button_onclick(View v) {
        Bundle a = new Bundle();
        a.putParcelable(ACCKEY, this.acc);
//        navctrl.navigate(R.id.action_OtherAccountFragment_to_CollectionFragment, a);
    }


}
