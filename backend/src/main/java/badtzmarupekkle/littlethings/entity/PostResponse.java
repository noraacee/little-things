package badtzmarupekkle.littlethings.entity;

import java.util.List;

public class PostResponse extends Response {
    private long id;
    private List<Post> posts;
    private Post post;
    private String url;

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public List<Post> getPosts() {
        return posts;
    }
    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }
    public Post getPost() {
        return post;
    }
    public void setPost(Post post) {
        this.post = post;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
}
