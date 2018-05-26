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

public class BlobUploadHandler extends HttpServlet {
    private static final long serialVersionUID = 3695219428874622842L;

    private static final String ENTITY_BLOB = "Blob";
    private static final String ENTITY_WRITER = "Writer";
    private static final String PARAMETER_BLOB_KEY = "blobKey";
    private static final String PARAMETER_IMAGE = "image";
    private static final String PARAMETER_WRITER = "writer";
    private static final String PROPERTY_TIMESTAMP = "Timestamp";
    private static final String WRITER_AARON = "Aaron";
    private static final String WRITER_DIANE = "Diane";

    private BlobstoreService blobstore;
    private DatastoreService datastore;

    public BlobUploadHandler() {
        blobstore = BlobstoreServiceFactory.getBlobstoreService();
        datastore = DatastoreServiceFactory.getDatastoreService();
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        BlobKey blobKey = blobstore.getUploads(request).get(PARAMETER_IMAGE).get(0);
        JSONObject json = new JSONObject();
        json.put(PARAMETER_BLOB_KEY, blobKey.getKeyString());
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");

        Key writerKey = getWriterKey(Boolean.parseBoolean(request.getParameter(PARAMETER_WRITER)));
        Entity blobEntity = new Entity(ENTITY_BLOB, blobKey.getKeyString(), writerKey);
        blobEntity.setProperty(PROPERTY_TIMESTAMP, System.currentTimeMillis());
        datastore.put(blobEntity);

        PrintWriter out = response.getWriter();
        out.print(json.toString());
        out.flush();
        out.close();
    }

    private Key getWriterKey(boolean writer) {
        if (writer)
            return KeyFactory.createKey(ENTITY_WRITER, WRITER_DIANE);
        return KeyFactory.createKey(ENTITY_WRITER, WRITER_AARON);
    }
}
