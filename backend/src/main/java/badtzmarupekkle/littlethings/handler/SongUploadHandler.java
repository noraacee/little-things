package badtzmarupekkle.littlethings.handler;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import org.json.simple.JSONObject;

import badtzmarupekkle.littlethings.util.Validation;

public class SongUploadHandler extends HttpServlet {
    private static final String ENTITY_SONG = "Song";
    private static final String ENTITY_WRITER = "Writer";
    private static final String PARAMETER_MESSAGE = "message";
    private static final String PARAMETER_SECRET = "secret";
    private static final String PARAMETER_SONG = "song";
    private static final String PROPERTY_MESSAGE = "message";
    private static final String PROPERTY_TIMESTAMP = "timestamp";
    private static final String WRITER_AARON = "Aaron";

    private BlobstoreService blobstore;
    private DatastoreService datastore;

    public SongUploadHandler() {
        blobstore = BlobstoreServiceFactory.getBlobstoreService();
        datastore = DatastoreServiceFactory.getDatastoreService();
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!Validation.validateUser(req.getParameter(PARAMETER_SECRET))) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        BlobKey blobKey = blobstore.getUploads(req).get(PARAMETER_SONG).get(0);

        Key writerKey = KeyFactory.createKey(ENTITY_WRITER, WRITER_AARON);
        Entity blobEntity = new Entity(ENTITY_SONG, blobKey.getKeyString(), writerKey);
        blobEntity.setUnindexedProperty(PROPERTY_MESSAGE, req.getParameter(PARAMETER_MESSAGE));
        blobEntity.setProperty(PROPERTY_TIMESTAMP, System.currentTimeMillis());
        datastore.put(blobEntity);
    }
}
