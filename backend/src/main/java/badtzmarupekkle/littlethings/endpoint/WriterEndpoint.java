package badtzmarupekkle.littlethings.endpoint;

import badtzmarupekkle.littlethings.entity.Response;
import badtzmarupekkle.littlethings.entity.Writer;
import badtzmarupekkle.littlethings.util.ErrorManager;
import badtzmarupekkle.littlethings.util.Validation;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@Api(name = "writerendpoint", namespace = @ApiNamespace(ownerDomain = "littlethings.com", ownerName = "BadtzMaruPekkle", packagePath = "endpoint/writer"))
public class WriterEndpoint {

    private static final String ENTITY_WRITER = "Writer";
    private static final String PROPERTY_GCM_REGISTRATION_ID = "GCMRegistrationId";
    private static final String WRITER_AARON = "Aaron";
    private static final String WRITER_DIANE = "Diane";

    private DatastoreService datastore;

    public WriterEndpoint() {
        datastore = DatastoreServiceFactory.getDatastoreService();
    }

    @ApiMethod(name = "register", httpMethod = HttpMethod.POST, path = "writer/register")
    public Response register(Writer writer) {
        Response response = new Response();

        if (!Validation.validateUser(writer.getSecret())) {
            response.setErrorCode(ErrorManager.ERROR_UNAUTHORIZED);
            return response;
        }

        String regId = writer.getRegistrationId();
        if (!Validation.isValidString(regId)) {
            response.setErrorCode(ErrorManager.ERROR_BAD_REQUEST);
            return response;
        }

        int retries = ErrorManager.RETRIES;
        while (true) {
            try {
                register(writer.getWriter(), datastore, writer.getRegistrationId());
                break;
            } catch (Exception e) {
                if (retries == 0) {
                    ErrorManager.logError(datastore, e.getMessage());
                    response.setErrorCode(ErrorManager.ERROR_GATEWAY_TIMEOUT);
                    return response;
                }
                retries--;
            }
        }

        response.setSuccess(true);
        return response;
    }

    public static String getRegistrationId(boolean writer, DatastoreService datastore) {
        Key writerKey = getWriterKey(writer);
        Entity writerEntity;
        try {
            writerEntity = datastore.get(writerKey);
        } catch (EntityNotFoundException e) {
            return null;
        }

        if (writerEntity.hasProperty(PROPERTY_GCM_REGISTRATION_ID))
            return (String) writerEntity.getProperty(PROPERTY_GCM_REGISTRATION_ID);
        return null;
    }

    public static Key getWriterKey(boolean writer) {
        if (writer)
            return KeyFactory.createKey(ENTITY_WRITER, WRITER_DIANE);
        return KeyFactory.createKey(ENTITY_WRITER, WRITER_AARON);
    }

    public static void register(boolean writer, DatastoreService datastore, String regId) {
        Entity writerEntity = getWriterEntity(writer);
        writerEntity.setUnindexedProperty(PROPERTY_GCM_REGISTRATION_ID, regId);
        datastore.put(writerEntity);
    }

    private static Entity getWriterEntity(boolean writer) {
        if (writer)
            return new Entity(ENTITY_WRITER, WRITER_DIANE);
        return new Entity(ENTITY_WRITER, WRITER_AARON);
    }
}
