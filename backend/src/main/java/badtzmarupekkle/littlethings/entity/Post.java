package badtzmarupekkle.littlethings.entity;

public class Post extends EntityModel {
    private int comments;
    private String photo;
    private String post;
    private String video;

    public int getComments() {
        return comments;
    }
    public void setComments(int comments) {
        this.comments = comments;
    }
    public String getPhoto() {
        return photo;
    }
    public void setPhoto(String photo) {
        this.photo = photo;
    }
    public String getPost() {
        return post;
    }
    public void setPost(String post) {
        this.post = post;
    }
    public String getVideo() {
        return video;
    }
    public void setVideo(String video) {
        this.video = video;
    }
}
