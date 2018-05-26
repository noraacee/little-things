package badtzmarupekkle.littlethings.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.validator.routines.UrlValidator;

import badtzmarupekkle.littlethings.util.Validation;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;

public class RawrHandler extends HttpServlet {
    private static final long serialVersionUID = -5543472829913932774L;

    private static final String ENTITY_RAWR = "Rawr";
    private static final String ENTITY_WRITER = "Writer";
    private static final String PARAMETER_RAWR_URL = "rawrurl";
    private static final String PARAMETER_TITLE = "title";
    private static final String PARAMETER_WRITER = "writer";
    private static final String PROPERTY_TIMESTAMP = "Timestamp";
    private static final String PROPERTY_TITLE = "Title";
    private static final String PROPERTY_TYPE = "Type";
    private static final String PROPERTY_URL = "Url";
    private static final String PROPERTY_WRITER = "Writer";
    private static final String TYPE_GIF = "gif";
    private static final String TYPE_JPG = "jpg";
    private static final String WRITER_AARON = "Aaron";
    private static final String WRITER_DIANE = "Diane";

    private DatastoreService datastore;
    private UrlValidator urlValidator;

    public RawrHandler() {
        datastore = DatastoreServiceFactory.getDatastoreService();
        urlValidator = new UrlValidator();
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String rawrUrl = request.getParameter(PARAMETER_RAWR_URL);
        if (!urlValidator.isValid(rawrUrl)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (!rawrUrl.contains(TYPE_GIF) && !rawrUrl.contains(TYPE_JPG)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        boolean writer = Boolean.parseBoolean(request.getParameter(PARAMETER_WRITER));
        Key writerKey = getWriterKey(writer);

        Entity rawrEntity = new Entity(ENTITY_RAWR, writerKey);
        rawrEntity.setProperty(PROPERTY_TIMESTAMP, System.currentTimeMillis());
        rawrEntity.setUnindexedProperty(PROPERTY_URL, new Text(rawrUrl));
        rawrEntity.setUnindexedProperty(PROPERTY_WRITER, writer);

        if (rawrUrl.contains(TYPE_GIF))
            rawrEntity.setProperty(PROPERTY_TYPE, TYPE_GIF);
        else if (rawrUrl.contains(TYPE_JPG))
            rawrEntity.setProperty(PROPERTY_TYPE, TYPE_JPG);

        String title = request.getParameter(PARAMETER_TITLE);
        if (Validation.isValidString(title))
            rawrEntity.setUnindexedProperty(PROPERTY_TITLE, new Text(title));

        datastore.put(rawrEntity);
    }

    private Key getWriterKey(boolean writer) {
        if (writer)
            return KeyFactory.createKey(ENTITY_WRITER, WRITER_DIANE);
        return KeyFactory.createKey(ENTITY_WRITER, WRITER_AARON);
    }
}
