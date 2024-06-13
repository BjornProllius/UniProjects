package com.team33.qrcodepursuit.activities.Scoreboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.team33.qrcodepursuit.R;
import com.team33.qrcodepursuit.models.Comment;

import java.util.ArrayList;

/**
 * adapt comments into a list
 */
public class CommentListAdapter  extends RecyclerView.Adapter<CommentListAdapter.ViewHolder> {

    private ArrayList<Comment> localData;

    /**
     * weirdness for recyclerview
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView commentView;
        private final TextView ownerView;
        private final TextView dateView;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            commentView = view.findViewById(R.id.comment_textview_comment);
            ownerView = view.findViewById(R.id.comment_textview_owner);
            dateView = view.findViewById(R.id.comment_textview_time);
        }

        public TextView getCommentView() { return commentView; }
        public TextView getOwnerView() { return ownerView; }
        public TextView getDateView() { return dateView; }
    }

    /**
     * make adapter with data
     * @param data to populate views with
     */
    public CommentListAdapter(ArrayList<Comment> data) {
        localData = data;
    }

    /**
     * create individual comment view
     * @param viewGroup
     * @param viewType
     * @return
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.view_comment, viewGroup, false);

        return new ViewHolder(view);
    }

    /**
     * connect view to comment info
     * @param viewHolder
     * @param position
     */
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.getCommentView().setText(localData.get(position).getContent());
        viewHolder.getOwnerView().setText(localData.get(position).getAuthor());
        viewHolder.getDateView().setText(localData.get(position).getTime().toString());
    }

    @Override
    public int getItemCount() {
        return localData.size();
    }

}
