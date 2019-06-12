package com.feeds.feedposts.model;

public class Post {

    private String timeStamp;
    private String UserId;
    private String PostTitle;
    private String PostDescription;
    private String CreatedDate;
    private String UpdatedDate;
    private String PostCreatedBy;

    public Post(String UserId, String postTitle, String postDescription, String createdDate, String updatedDate, String PostCreatedBy) {
        this.UserId = UserId;
        this.PostTitle = postTitle;
        this.PostDescription = postDescription;
        this.CreatedDate = createdDate;
        this.UpdatedDate = updatedDate;
        this.PostCreatedBy = PostCreatedBy;
    }

    public Post() {
    }


    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        this.UserId = userId;
    }

    public String getPostTitle() {
        return PostTitle;
    }

    public void setPostTitle(String postTitle) {
        PostTitle = postTitle;
    }

    public String getPostDescription() {
        return PostDescription;
    }

    public void setPostDescription(String postDescription) {
        PostDescription = postDescription;
    }

    public String getCreatedDate() {
        return CreatedDate;
    }

    public void setCreatedDate(String createdDate) {
        CreatedDate = createdDate;
    }

    public String getUpdatedDate() {
        return UpdatedDate;
    }

    public void setUpdatedDate(String updatedDate) {
        UpdatedDate = updatedDate;
    }

    public String getPostCreatedBy() {
        return PostCreatedBy;
    }

    public void setPostCreatedBy(String postCreatedBy) {
        PostCreatedBy = postCreatedBy;
    }
}
