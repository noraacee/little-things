package badtzmarupekkle.littlethings.util;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;

public class ErrorManager {
    public static final int ERROR_BAD_REQUEST = 400;
    public static final int ERROR_UNAUTHORIZED = 401;
    public static final int ERROR_NOT_FOUND = 404;
    public static final int ERROR_GATEWAY_TIMEOUT = 504;
    public static final int RETRIES = 3;

    private static final String ENTITY_ERROR = "Error";
    private static final String PROPERTY_ERROR_MESSAGE = "ErrorMessage";
    private static final String PROPERTY_TIMESTAMP = "Timestamp";

    public static void logError(DatastoreService datastore, String error) {
        Entity e = new Entity(ENTITY_ERROR);
        e.setProperty(PROPERTY_ERROR_MESSAGE, error);
        e.setProperty(PROPERTY_TIMESTAMP, System.currentTimeMillis());
        datastore.put(e);
    }
}
