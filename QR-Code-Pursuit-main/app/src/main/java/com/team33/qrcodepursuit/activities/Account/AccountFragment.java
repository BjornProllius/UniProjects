package com.team33.qrcodepursuit.activities.Account;

import static java.lang.String.format;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.firestore.FirebaseFirestore;
import com.team33.qrcodepursuit.R;
import com.team33.qrcodepursuit.models.Account;
import com.team33.qrcodepursuit.models.GameQRCode;
import com.team33.qrcodepursuit.models.Scoring;

import java.util.Map;

/**
 * base class for MyAccountFragment and OtherAccountFragment
 */
public abstract class AccountFragment extends Fragment {

    // ui elements
    protected ImageView pfp;
    protected TextView username_text;
    protected Button editpf_button;
    protected TextView contactinfo_text;
    protected TextView bio_text;
    protected TextView ownedCount_text;
    protected TextView scannedCount_text;
    protected TextView scannedCountRank_text;
    protected TextView lowscore_text;
    protected TextView hiscore_text;
    protected TextView hiscoreRank_text;
    protected TextView totalscoreRank_text;
    protected TextView totalscore_text;
    protected Button collection_button;
    protected Button settings_button;
    protected NavController navctrl;
    // account
    protected Account acc;
    protected String uid;
    // scoring
    protected Scoring sco = Scoring.getInstance();
    // abstract methods
    protected abstract boolean setAccount();
    protected abstract void settings_button_onclick(View v);
    protected abstract void editpf_button_onclick(View v);
    protected abstract void collection_button_onclick(View v);


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
        { return inflater.inflate(R.layout.fragment_account, container, false); }

    @SuppressLint("DefaultLocale")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // link ui
        // pfp = view.findViewById(R.id.account_pfp_imgview);
        username_text = view.findViewById(R.id.account_username_text);
        editpf_button = view.findViewById(R.id.account_editprofile_button);
        contactinfo_text = view.findViewById(R.id.account_info_contactinfo_content);
        bio_text = view.findViewById(R.id.account_info_bio_content);
        ownedCount_text = view.findViewById(R.id.account_gamestats_ownedqrs);
        scannedCount_text = view.findViewById(R.id.account_gamestats_scannedqrs);
        scannedCountRank_text = view.findViewById(R.id.account_gamestats_scannedqr_ranking_text);
        lowscore_text = view.findViewById(R.id.account_gamestats_lowscore);
        hiscore_text = view.findViewById(R.id.account_gamestats_hiscore);
        hiscoreRank_text = view.findViewById(R.id.account_gamestats_hiscore_ranking_text);
        totalscore_text = view.findViewById(R.id.account_gamestats_totalscore);
        totalscoreRank_text = view.findViewById(R.id.account_gamestats_totalscore_ranking_text);
        collection_button = view.findViewById(R.id.account_collection_button);
        settings_button = view.findViewById(R.id.account_settings_button);
        navctrl = Navigation.findNavController(view);
        // set account
        if (!this.setAccount()) return;
        uid = acc.getUid();
        int total = sco.getTotalPlayers();
        // fill content
        username_text.setText(acc.getUsername());
        contactinfo_text.setText(acc.getContactinfo());
        bio_text.setText(acc.getBio());
        ownedCount_text.setText(format("%d", acc.getOwnedQRsCount()));
        scannedCount_text.setText(format("%d", acc.getScannedQRsCount()));
        scannedCountRank_text.setText(
                format("%d / %d", sco.getRank(uid, Scoring.SortBy.TOTALSCANS), total));
        Map.Entry<String, Integer> lowScore = sco.getLowScoreQR(uid);
        lowscore_text.setText(format("%d", (lowScore != null) ? lowScore.getValue() : -1));
        Map.Entry<String, Integer> hiScore = sco.getHiScoreQR(uid);
        hiscore_text.setText(format("%d pts", (hiScore != null) ? hiScore.getValue() : -1));
        hiscoreRank_text.setText(
                format("%d / %d", sco.getRank(uid, Scoring.SortBy.HISCORE), total));
        totalscore_text.setText(format("%d pts", sco.getTotalScore(uid)));
        totalscoreRank_text.setText(
                format("%d / %d", sco.getRank(uid, Scoring.SortBy.TOTALSCORE), total));

        // set listeners
        settings_button.setOnClickListener(this::settings_button_onclick);
        editpf_button.setOnClickListener(this::editpf_button_onclick);
        collection_button.setOnClickListener(this::collection_button_onclick);
    }
}
