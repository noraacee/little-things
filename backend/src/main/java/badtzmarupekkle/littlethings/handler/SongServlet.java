package badtzmarupekkle.littlethings.handler;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SongServlet extends HttpServlet {
    private static final String PARAMETER_SONG_KEY = "key";

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        BlobKey key = new BlobKey(req.getParameter(PARAMETER_SONG_KEY));
        BlobstoreServiceFactory.getBlobstoreService().serve(key, resp);
    }
}
