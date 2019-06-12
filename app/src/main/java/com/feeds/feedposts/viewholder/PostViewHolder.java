package com.feeds.feedposts.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.feeds.feedposts.R;

public class PostViewHolder extends RecyclerView.ViewHolder{

    private View mView;
    public Button btnDeletePost;
    public Button btnUpdatePost;

    public PostViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        btnDeletePost = mView.findViewById(R.id.btn_detele_post);
        btnUpdatePost = mView.findViewById(R.id.btn_update_post);
    }

    public void setTitle(String title) {
        TextView post_title = mView.findViewById(R.id.txt_post_title);
        post_title.setText(title);
    }

    public void setDesc(String desc) {
        TextView post_desc = mView.findViewById(R.id.txt_post_desc);
        post_desc.setText(desc);
    }

    public void setCreatedDate(String dateCreated) {
        TextView post_date_created = mView.findViewById(R.id.txt_date_created);
        post_date_created.setText(dateCreated);
    }

    public void setPostUpdatedDate(String postUpdate) {
        TextView post_update = mView.findViewById(R.id.txt_date_created);
        post_update.setText(postUpdate);
        TextView txtCreateUpdate = mView.findViewById(R.id.txt_create_update);
        txtCreateUpdate.setText(R.string.updated_at);
    }

    public void setPostCreatedBy(String postCreatedBy) {
        TextView post_created_By = mView.findViewById(R.id.txt_post_created_by);
        post_created_By.setText(postCreatedBy);
    }


}
