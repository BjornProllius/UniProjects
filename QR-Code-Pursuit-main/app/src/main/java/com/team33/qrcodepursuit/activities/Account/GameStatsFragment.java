package com.team33.qrcodepursuit.activities.Account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.team33.qrcodepursuit.R;
import com.team33.qrcodepursuit.models.GameStats;

import java.util.ArrayList;

public class GameStatsFragment extends Fragment {

    ListView statsList;
    ArrayAdapter<String> statsAdapter;
    ArrayList<String> dataList;
    GameStats userStats;
    Button closeButton;

    public GameStatsFragment(){
        super(R.layout.fragment_game_stats);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        Bundle args = getArguments();
        GameStats userStats = new GameStats(args.getString("username"));
        View view = inflater.inflate(R.layout.fragment_game_stats,container,false);

        statsList = (ListView) view.findViewById(R.id.stats_ListView);

        ArrayList<String> dataList = new ArrayList<String>();
        dataList.add(userStats.getAssociatedUser() + "'s Stats");
        dataList.add("Total Score: " + userStats.getTotalScore());
        dataList.add("Highest QR Code: " + userStats.getHighestQRAttained());
        dataList.add("Lowest QR Code: " + userStats.getLowestQRAttained());
        dataList.add("Total # of QR Codes Collected" + userStats.getTotalQRCodes());
        ArrayAdapter<String> statsAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,dataList);

        statsList.setAdapter(statsAdapter);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        closeButton = (Button) view.findViewById(R.id.close_button);
        closeButton.setOnClickListener(v ->
                getActivity().getFragmentManager().popBackStack());
    }





}
