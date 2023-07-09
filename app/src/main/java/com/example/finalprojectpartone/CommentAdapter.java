package com.example.finalprojectpartone;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import data.Comment;

public class CommentAdapter extends BaseAdapter {
    private Context context;
    private List<Comment> comments;
    private OnDeleteButtonClickListener deleteButtonClickListener;
    private OnEditButtonClickListener editButtonClickListener;
    String username;
    public CommentAdapter(Context context, List<Comment> comments, String username) {
        this.context = context;
        this.comments = comments;
        this.username = username;
    }

    @Override
    public int getCount() {
        return comments.size();
    }

    @Override
    public Comment getItem(int position) {
        return comments.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
            holder = new ViewHolder();
            holder.authorTextView = convertView.findViewById(R.id.authorTextView);
            holder.contentTextView = convertView.findViewById(R.id.contentTextView);
            holder.deleteButton = convertView.findViewById(R.id.deleteButton);
            holder.editButton = convertView.findViewById(R.id.editButton);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Comment comment = getItem(position);

        holder.authorTextView.setText(comment.getUsername());
        holder.contentTextView.setText(comment.getContent());

        // Hide or show delete/edit buttons based on the author's username
        if (comment.getUsername().equals(username)) {
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.editButton.setVisibility(View.VISIBLE);
        } else {
            holder.deleteButton.setVisibility(View.GONE);
            holder.editButton.setVisibility(View.GONE);
        }

        // Delete button click listener
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (deleteButtonClickListener != null) {
                    deleteButtonClickListener.onDeleteButtonClick(position);
                }
            }
        });


        // Edit button click listener
        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editButtonClickListener != null) {
                    editButtonClickListener.onEditButtonClick(position);
                }
            }
        });

        return convertView;
    }

    public void setOnDeleteButtonClickListener(OnDeleteButtonClickListener listener) {
        this.deleteButtonClickListener = listener;
    }

    public interface OnDeleteButtonClickListener {
        void onDeleteButtonClick(int position);
    }
    public void setOnEditButtonClickListener(OnEditButtonClickListener listener) {
        this.editButtonClickListener = listener;
    }

    public interface OnEditButtonClickListener {
        void onEditButtonClick(int position);
    }
    private static class ViewHolder {
        TextView authorTextView;
        TextView contentTextView;
        Button deleteButton;
        Button editButton;
    }
}