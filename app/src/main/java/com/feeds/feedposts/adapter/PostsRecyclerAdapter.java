package com.feeds.feedposts.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.feeds.feedposts.R;
import com.feeds.feedposts.interfacesss.PostActionInterface;
import com.feeds.feedposts.model.Post;
import com.feeds.feedposts.viewholder.PostViewHolder;
import com.feeds.feedposts.views.MainActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class PostsRecyclerAdapter extends FirebaseRecyclerAdapter<Post, PostViewHolder> {

    Context context;
    private PostActionInterface mPostActionInterface;
    private List<Post> postList;

    public PostsRecyclerAdapter(Class<Post> modelClass, int modelLayout, Class<PostViewHolder> viewHolderClass, DatabaseReference dbRef, Context context) {
        super(modelClass, modelLayout, viewHolderClass, dbRef);
        mPostActionInterface = (PostActionInterface) context;
        this.context = context;
        postList = new ArrayList<>();
    }

    public void addAll(List<Post> newPost) {
        int initSize = postList.size();
        postList.addAll(newPost);
        notifyItemChanged(initSize, newPost.size());
        notifyDataSetChanged();
    }

    public String getLastPostUserId() {
        return postList.get(postList.size() - 1).getUserId();
    }

    public void removeLastItem() {
        postList.remove(postList.size() - 1);
    }

    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_create_post, parent, false);
        return new PostViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PostViewHolder viewHolder, int position) {
        super.onBindViewHolder(viewHolder, position);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    @Override
    protected void populateViewHolder(PostViewHolder viewHolder, final Post model, int position) {
        final String postKey = getRef(position).getKey();
        viewHolder.setTitle(model.getPostTitle());
        viewHolder.setDesc(model.getPostDescription());
        viewHolder.setPostCreatedBy(model.getPostCreatedBy());

        if (model.getUpdatedDate() == null) {
            viewHolder.setCreatedDate(model.getCreatedDate());
        } else {
            viewHolder.setPostUpdatedDate(model.getUpdatedDate());
        }

        if (model.getUserId() != null && model.getUserId().equalsIgnoreCase(MainActivity.loggedInUserId)) {
            viewHolder.btnDeletePost.setVisibility(View.VISIBLE);
            viewHolder.btnUpdatePost.setVisibility(View.VISIBLE);
        } else {
            viewHolder.btnDeletePost.setVisibility(View.GONE);
            viewHolder.btnUpdatePost.setVisibility(View.GONE);
        }

        viewHolder.btnDeletePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPostActionInterface.deletePostDialog(postKey, model.getPostTitle());
            }
        });

        viewHolder.btnUpdatePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPostActionInterface.showUpdatePostDialog(postKey, model.getPostTitle(), model.getPostDescription());
            }
        });
    }


}
