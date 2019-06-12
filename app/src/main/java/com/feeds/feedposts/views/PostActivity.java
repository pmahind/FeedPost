package com.feeds.feedposts.views;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.feeds.feedposts.R;
import com.feeds.feedposts.adapter.PostsRecyclerAdapter;
import com.feeds.feedposts.interfacesss.PostActionInterface;
import com.feeds.feedposts.model.Post;
import com.feeds.feedposts.viewholder.PostViewHolder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import dmax.dialog.SpotsDialog;

public class PostActivity extends AppCompatActivity implements View.OnClickListener, PostActionInterface {

    private static DatabaseReference mDatabase;
    private EditText etPostTitle;
    private EditText etPostDescription;
    private RecyclerView recyclerView;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;
    private FirebaseUser mCurrentUser;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private RelativeLayout layoutRoot;
    private SimpleDateFormat simpleDateFormat;
    private SpotsDialog waitingDialog;
    private int ITEM_LOAD_COUNT = 10;
    private int total_item = 0;
    private int last_visible_item;
    private boolean isLoading = false;
    private boolean isMaxData = false;
    private String last_node = "";
    private String last_key = "";
    private PostsRecyclerAdapter postsRecyclerAdapter;
    private List<Post> tempPosts = new ArrayList<>();
    private boolean isNewPostAdded = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        initViews();

        initialiseVariables();

        setUpAuthListener();

        waitingDialog.show();

        getLastKeyFromFirebase();

        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        postsRecyclerAdapter = new PostsRecyclerAdapter(Post.class, R.layout.layout_create_post, PostViewHolder.class, mDatabase, this);
        recyclerView.setAdapter(postsRecyclerAdapter);

        getUsers();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                total_item = layoutManager.getItemCount();
                last_visible_item = layoutManager.findLastVisibleItemPosition();

                if (!isLoading && total_item <= ((last_visible_item + ITEM_LOAD_COUNT))) {
                    getUsers();
                    isLoading = true;
                }

            }
        });

    }

    private void getUsers() {
        if (!isMaxData) {
            Query query;
            if (TextUtils.isEmpty(last_node)) {
                query = FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.post))
                        .orderByChild(getString(R.string.timeStamp))
                        .limitToFirst(ITEM_LOAD_COUNT);
            } else {
                query = FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.post))
                        .orderByChild(getString(R.string.timeStamp))
                        .startAt(last_node)
                        .limitToFirst(ITEM_LOAD_COUNT);
            }

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChildren()) {
                        List<Post> newPosts = new ArrayList<>();
                        for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                            newPosts.add(postSnapShot.getValue(Post.class));
                        }

                        last_node = newPosts.get(newPosts.size() - 1).getTimeStamp();

                        if (!last_node.equals(last_key)) {
                            newPosts.remove(newPosts.size() - 1);
                        } else if (isNewPostAdded && !newPosts.isEmpty()) {
                            newPosts.remove(0);
                            isNewPostAdded = false;
                            last_node = last_key;
                            isMaxData = true;
                        } else {
                            last_node = getString(R.string.end);
                        }

                        postsRecyclerAdapter.addAll(newPosts);
                        isLoading = false;

                    } else {

                        isLoading = false;
                        isMaxData = true;

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    isLoading = false;
                }
            });

        }
    }

    private void getLastKeyFromFirebase() {
        // Query get Last Key
        Query getLastKey = FirebaseDatabase.getInstance().getReference()
                .child("Post")
                .orderByKey()
                .limitToLast(1);

        getLastKey.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot lastKey : dataSnapshot.getChildren()) {
                    // last_key = lastKey.getKey();
                    tempPosts.add(lastKey.getValue(Post.class));
                    if (tempPosts.size() >= 2) {
                        last_key = tempPosts.get(tempPosts.size() - 1).getTimeStamp();
                    } else {
                        last_key = tempPosts.get(0).getTimeStamp();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PostActivity.this, "Cannot get last key", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void initViews() {
        layoutRoot = findViewById(R.id.rootMain);
        etPostTitle = findViewById(R.id.et_post_title);
        etPostDescription = findViewById(R.id.et_post_description);
        Button btnPost = findViewById(R.id.btn_post);
        btnPost.setOnClickListener(this);

        recyclerView = findViewById(R.id.post_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

    }

    private void initialiseVariables() {
        waitingDialog = new SpotsDialog(PostActivity.this, R.string.loading_post);
        simpleDateFormat = new SimpleDateFormat(getString(R.string.date_format), Locale.getDefault());

        mDatabase = FirebaseDatabase.getInstance().getReference().child(getString(R.string.post));
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child(getString(R.string.Users)).child(mCurrentUser.getUid());
    }

    private void setUpAuthListener() {
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (mAuth.getCurrentUser() == null) {
                    Intent loginIntent = new Intent(PostActivity.this, MainActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(loginIntent);
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);

        waitingDialog.dismiss();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_post) {
            if (!etPostTitle.getText().toString().trim().isEmpty()
                    && !etPostDescription.getText().toString().trim().isEmpty()) {
                addPost();
            } else {
                Snackbar.make(layoutRoot, getString(R.string.post_title_desc_not_empty), Snackbar.LENGTH_LONG).show();
            }
        }
    }


    private void addPost() {

        final SpotsDialog waitingDialog = new SpotsDialog(PostActivity.this, getString(R.string.creating_posting));
        waitingDialog.show();

        final String PostTitle = etPostTitle.getText().toString().trim();
        final String PostDesc = etPostDescription.getText().toString().trim();
        // do a check for empty fields
        if (!TextUtils.isEmpty(PostDesc) && !TextUtils.isEmpty(PostTitle)) {

            final DatabaseReference newPost = mDatabase.push();
            //adding post contents to database reference
            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                    newPost.child(getString(R.string.PostTitle)).setValue(PostTitle);
                    newPost.child(getString(R.string.PostDescription)).setValue(PostDesc);
                    newPost.child(getString(R.string.UserId)).setValue(mCurrentUser.getUid());
                    newPost.child(getString(R.string.CreatedDate)).setValue(simpleDateFormat.format(new Date()));
                    newPost.child("timeStamp").setValue(System.currentTimeMillis() + "");
                    newPost.child(getString(R.string.PostCreatedBy)).setValue(dataSnapshot.child(getString(R.string.name)).getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            waitingDialog.dismiss();
                            if (task.isSuccessful()) {
                                etPostTitle.getText().clear();
                                etPostDescription.getText().clear();
                                Snackbar.make(layoutRoot, getString(R.string.post_create_success), Snackbar.LENGTH_LONG).show();
                                isNewPostAdded = true;
                                getLastKeyFromFirebase();
                                isMaxData = false;
                                if (last_node.equals(getString(R.string.end))) {
                                    last_node = last_key;
                                }
                                getUsers();
                            } else {
                                isNewPostAdded = false;
                                Snackbar.make(layoutRoot, getString(R.string.post_failed_to_create), Snackbar.LENGTH_LONG).show();
                            }
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    waitingDialog.dismiss();
                    Snackbar.make(layoutRoot, getString(R.string.post_cancelled), Snackbar.LENGTH_LONG).show();
                }
            });
        }
    }


    @Override
    public void showUpdatePostDialog(final String postKey, String postTitle, String postDesc) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.update_post));
        //dialog.setMessage("");

        LayoutInflater inflater = LayoutInflater.from(this);
        View login_layout = inflater.inflate(R.layout.layout_update_post, null);

        final EditText editUpdatePostTitle = login_layout.findViewById(R.id.editUpdatePostTitle);
        final EditText editUpdatePostDesc = login_layout.findViewById(R.id.editUpdatePostDesc);

        editUpdatePostTitle.setText(postTitle);
        editUpdatePostDesc.setText(postDesc);

        dialog.setView(login_layout);

        // set button
        dialog.setPositiveButton(getString(R.string.update), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                HashMap<String, Object> updatePostMap = new HashMap<>();
                if (TextUtils.isEmpty(editUpdatePostTitle.getText().toString())) {
                    Snackbar.make(layoutRoot, getString(R.string.title_not_empty), Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(editUpdatePostDesc.getText().toString())) {
                    Snackbar.make(layoutRoot, getString(R.string.desc_not_empty), Snackbar.LENGTH_SHORT).show();
                    return;
                }

                updatePostMap.put(getString(R.string.PostTitle), editUpdatePostTitle.getText().toString().trim());
                updatePostMap.put(getString(R.string.PostDescription), editUpdatePostDesc.getText().toString().trim());
                updatePostMap.put(getString(R.string.UpdatedDate), simpleDateFormat.format(new Date()));
                mDatabase.child(postKey).updateChildren(updatePostMap);

                dialog.dismiss();
                Snackbar.make(layoutRoot, getString(R.string.post_update_success), Snackbar.LENGTH_LONG).show();
            }
        });


        dialog.setNegativeButton(getString(R.string.cancel_dia_msg), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }


    @Override
    public void deletePostDialog(final String postKey, String postTitle) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.delete_post));
        dialog.setMessage(getString(R.string.want_to_delete_post) + "\"" + postTitle + "\" ?");

        // set button
        dialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                mDatabase.child(postKey).removeValue();

                dialog.dismiss();

                Snackbar.make(layoutRoot, getString(R.string.post_deleted_success), Snackbar.LENGTH_LONG).show();

            }
        });


        dialog.setNegativeButton(getString(R.string.cancel_dia_msg), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_logout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menuLogout) {
            showLogoutDialog();
        }
        return true;
    }

    private void showLogoutDialog() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.logout))
                .setMessage(getString(R.string.logout_message))
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        mAuth.signOut();
                        startActivity(new Intent(PostActivity.this, MainActivity.class));
                        finish();
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        paramDialogInterface.dismiss();
                    }
                });
        dialog.show();
    }

}
