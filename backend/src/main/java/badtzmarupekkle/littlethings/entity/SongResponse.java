package badtzmarupekkle.littlethings.entity;

import java.util.List;

public class SongResponse extends Response {
    public List<Song> songs;
    public String url;

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }
    public List<Song> getSongs() {
        return songs;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    public String getUrl() {
        return url;
    }
}
