package com.team33.qrcodepursuit.activities.Scoreboard;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.team33.qrcodepursuit.R;
import com.team33.qrcodepursuit.models.Account;

import java.util.ArrayList;

public class ScoreBoardList extends ArrayAdapter<Account> {

    private ArrayList<Account> players;
    private Context context;

    public ScoreBoardList(Context context, ArrayList<Account> players){
        super(context,0, players);
        this.players = players;
        this.context = context;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//        return super.getView(position, convertView, parent);
        View view = convertView;
        Context context = getContext();

        if(view == null){
            view = LayoutInflater.from(context).inflate(R.layout.scoreboard_element, parent,false);
        }



        Account player = players.get(position);

       //these bottom 4 lines were in the lab, but idk if they are needed?

        TextView ranking = view.findViewById(R.id.scoreboard_ranking);
        TextView username = view.findViewById(R.id.scoreboard_username);
        TextView totalScore = view.findViewById(R.id.scoreboard_totalscore);

        ranking.setText(String.valueOf(position + 1));
        username.setText("Username: " + player.getUsername());
        totalScore.setText("Total Score: " + String.valueOf(player.getTotalScore()));
        //TextView playerRegion = view.findViewById(R.id.province_text);

        //playerName.setText(player.getPlayerName());
        //playerRegion.setText(player.getPlayerRegion());

        return view;

    }
}
