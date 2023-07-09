package com.example.finalprojectpartone;
import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import data.Comment;
import database.DBManager;
import database.DBManagerException;

public class ViewCommentsActivity extends Activity {
    private List<Comment> comments;
    private ListView listView;
    private EditText commentEditText;
    private Button addButton;
    private CheckBox filterCheckBox;
    private CommentAdapter commentAdapter;
    private Comment selectedComment;
    private boolean isEditMode = false;
    private boolean showOnlyMyComments = false;
    private DBManager dbManager;
    private int eventId;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_comments);

        // Initialize views
        listView = findViewById(R.id.listView);
        commentEditText = findViewById(R.id.commentEditText);
        addButton = findViewById(R.id.addButton);
        filterCheckBox = findViewById(R.id.filterCheckBox);

        dbManager = new DBManager(this);
        dbManager.open();
        Intent intent = this.getIntent();

        this.eventId = Integer.parseInt(intent.getStringExtra("eventId"));
        this.username = intent.getStringExtra("username");
        Log.d(TAG, "event id: " + eventId + " username: " + username);

        // Get comments from DB
        refreshCommentsFromDB();

        // Set up the adapter for the list view
        commentAdapter = new CommentAdapter(this, comments, username);
        listView.setAdapter(commentAdapter);

        // List view item click listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedComment = commentAdapter.getItem(position);
                Log.d(TAG, "edit bottom clicked by: " + " username: " + username);
                if (selectedComment != null && selectedComment.getUsername().equals(username)) {
                    Log.d(TAG, "entering edit mode: " + " username: " + username);
                    commentEditText.setText(selectedComment.getContent());
                    addButton.setText(R.string.save_button_label);
                    isEditMode = true;
                }
            }
        });

// edit button click listener added - move the text to the edit place
        commentAdapter.setOnEditButtonClickListener(new CommentAdapter.OnEditButtonClickListener() {
            @Override
            public void onEditButtonClick(int position) {
                selectedComment = commentAdapter.getItem(position);
                Log.d(TAG, "edit bottom clicked by: " + " username: " + username);
                if (selectedComment != null && selectedComment.getUsername().equals(username)) {
                    Log.d(TAG, "entering edit mode: " + " username: " + username);
                    commentEditText.setText(selectedComment.getContent());
                    addButton.setText(R.string.save_button_label);
                    isEditMode = true;
                }
            }
        });

        // Delete button click listener
        commentAdapter.setOnDeleteButtonClickListener(new CommentAdapter.OnDeleteButtonClickListener() {
            @Override
            public void onDeleteButtonClick(int position) {
                Comment comment = commentAdapter.getItem(position);
                if (comment != null) {
                    // delete comment
                    dbManager.deleteComment(comment.getId());
                    refreshCommentsFromDB();
                    //comments.remove(comment);
                    commentAdapter.notifyDataSetChanged();
                    Toast.makeText(ViewCommentsActivity.this, "Comment deleted", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Add button click listener
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String commentText = commentEditText.getText().toString().trim();

                if (commentText.isEmpty()) {
                    Toast.makeText(ViewCommentsActivity.this, "Please enter a comment", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isEditMode) {
                    Log.d(TAG, "save update in edit mode: " + " username: " + username);
                    // Save the edited comment
                    if (selectedComment != null) {
                        dbManager.updateComment(selectedComment.getId(), commentText);
                        refreshCommentsFromDB();
                        //selectedComment.setContent(commentText);
                        commentAdapter.notifyDataSetChanged();
                        resetEditMode();
                        Toast.makeText(ViewCommentsActivity.this, "Comment edited", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Add a new comment
                    try {
                        dbManager.addComment(username, commentText, eventId);
                    } catch (DBManagerException e) {
                        Toast.makeText(ViewCommentsActivity.this,
                                "Cannot comment on your own event", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    refreshCommentsFromDB();
                    commentAdapter.notifyDataSetChanged();
                    Toast.makeText(ViewCommentsActivity.this, "Comment added", Toast.LENGTH_SHORT).show();
                }

                commentEditText.setText("");
            }
        });

        // Filter checkbox click listener
        filterCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOnlyMyComments = filterCheckBox.isChecked();
                filterComments();
                commentAdapter.notifyDataSetChanged();
            }
        });
    }
    private void refreshCommentsFromDB(){
        // Get comments from DB
        try{
            if(comments != null)
            {
                comments.clear();
                comments.addAll(dbManager.getCommentsByEventId(eventId));
            } else {
                comments = dbManager.getCommentsByEventId(eventId);
            }

        } catch(DBManagerException dbex){
            //TODO: fix me
        }

    }

    private void resetEditMode() {
        commentEditText.setText("");
        addButton.setText(R.string.add_button_label);
        selectedComment = null;
        isEditMode = false;
    }

    private void filterComments() {
        if (showOnlyMyComments) {
            Log.d(TAG, "show only " + username + " comments, all comments: " + comments);
            Iterator<Comment> iterator = comments.iterator();
            while (iterator.hasNext()) {
                Comment comment = iterator.next();
                Log.d(TAG, "curr comment username:" + comment.getUsername());
                if (!comment.getUsername().equals(username)) {
                    iterator.remove();
                }
            }
        } else {
            refreshCommentsFromDB();
        }
    }
}
