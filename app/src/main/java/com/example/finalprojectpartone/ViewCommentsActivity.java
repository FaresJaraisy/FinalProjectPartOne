package com.example.finalprojectpartone;
import android.app.Activity;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_comments);

        // Initialize views
        listView = findViewById(R.id.listView);
        commentEditText = findViewById(R.id.commentEditText);
        addButton = findViewById(R.id.addButton);
        filterCheckBox = findViewById(R.id.filterCheckBox);

        // Create dummy comments
        comments = new ArrayList<>();
        comments.add(new Comment(1, "Comment 1", "User1", 1));
        comments.add(new Comment(2, "Comment 2", "User2", 1));
        comments.add(new Comment(3, "Comment 3", "User1", 2));
        // ... add more comments

        // Set up the adapter for the list view
        commentAdapter = new CommentAdapter(this, comments);
        listView.setAdapter(commentAdapter);

        // List view item click listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedComment = commentAdapter.getItem(position);
                if (selectedComment != null && selectedComment.getUsername().equals("connected_user")) {
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
                    comments.remove(comment);
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
                    // Save the edited comment
                    if (selectedComment != null) {
                        selectedComment.setContent(commentText);
                        commentAdapter.notifyDataSetChanged();
                        resetEditMode();
                        Toast.makeText(ViewCommentsActivity.this, "Comment edited", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Add a new comment
                    Comment newComment = new Comment(comments.size() + 1, commentText, "connected_user", 1);
                    comments.add(newComment);
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

    private void resetEditMode() {
        commentEditText.setText("");
        addButton.setText(R.string.add_button_label);
        selectedComment = null;
        isEditMode = false;
    }

    private void filterComments() {
        if (showOnlyMyComments) {
            Iterator<Comment> iterator = comments.iterator();
            while (iterator.hasNext()) {
                Comment comment = iterator.next();
                if (!comment.getUsername().equals("connected_user")) {
                    iterator.remove();
                }
            }
        }
        // TODO: Implement any other desired filters
    }
}
