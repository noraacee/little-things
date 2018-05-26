package badtzmarupekkle.littlethings.util;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONValue;

import badtzmarupekkle.littlethings.endpoint.WriterEndpoint;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

public class GCMManager {
    private static final int STATUS_CODE_OK = 200;
    private static final int STATUS_BAD_REQUEST = 400;
    private static final int STATUS_UNAUTHORIZED = 401;

    private static final String API_KEY = "AIzaSyA844qRRRpxI82vFMaI8ip8CbEovYJgsd8";
    private static final String AUTHORIZATION_KEY = "key=";
    private static final String COLLAPSE_KEY_POST = "new_blog_post";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String ERROR_NOT_REGISTERED = "NotRegistered";
    private static final String GCM_URL = "https://android.googleapis.com/gcm/send";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String PROPERTY_CANONICAL_IDS = "canonical_ids";
    private static final String PROPERTY_COLLAPSE_KEY = "collapse_key";
    private static final String PROPERTY_DATA = "data";
    private static final String PROPERTY_DELAY_WHILE_IDLE = "delay_while_idle";
    private static final String PROPERTY_DRY_RUN = "dry_run";
    private static final String PROPERTY_ERROR = "error";
    private static final String PROPERTY_FAILURE = "failure";
    private static final String PROPERTY_NOTIFICATION_KEY = "notification_key";
    private static final String PROPERTY_REGISTRATION_ID = "registration_id";
    private static final String PROPERTY_REGISTRATION_IDS = "registration_ids";
    private static final String PROPERTY_RESTRICTED_PACKAGE_NAME = "restricted_package_name";
    private static final String PROPERTY_RESULTS = "results";
    private static final String PROPERTY_TIME_TO_LIVE = "time_to_live";
    private static final String UTF_8 = "UTF-8";

    public static String createJSONMessage(List<String> regIds, boolean delayWhileIdle, boolean dryRun, long timeToLive, Map<String, String> payload,
                                           String collapseKey, String notificationKey, String restrictedPackageName) {
        if (regIds == null || regIds.size() == 0)
            return null;

        Map<String, Object> message = new HashMap<String, Object>();
        message.put(PROPERTY_REGISTRATION_IDS, regIds);

        if (delayWhileIdle)
            message.put(PROPERTY_DELAY_WHILE_IDLE, true);
        if (dryRun)
            message.put(PROPERTY_DRY_RUN, true);
        if (timeToLive != -1)
            message.put(PROPERTY_TIME_TO_LIVE, timeToLive);
        if (payload != null)
            message.put(PROPERTY_DATA, payload);
        if (collapseKey != null)
            message.put(PROPERTY_COLLAPSE_KEY, collapseKey);
        if (notificationKey != null)
            message.put(PROPERTY_NOTIFICATION_KEY, notificationKey);
        if (restrictedPackageName != null)
            message.put(PROPERTY_RESTRICTED_PACKAGE_NAME, restrictedPackageName);

        return JSONValue.toJSONString(message);
    }

    public static void notifyWriterPost(boolean writer, DatastoreService datastore) {
        String regId = WriterEndpoint.getRegistrationId(!writer, datastore);
        if (regId == null)
            return;

        List<String> regIds = new LinkedList<String>();
        regIds.add(regId);

        send(writer, datastore, createJSONMessage(regIds, false, false, -1, null, COLLAPSE_KEY_POST, null, null), regIds);
    }

    @SuppressWarnings("unchecked")
    private static void send(boolean writer, DatastoreService datastore, String json, List<String> regIds) {
        HTTPResponse response;
        URL url;
        HTTPRequest request;
        try {
            url = new URL(GCM_URL);
            request = new HTTPRequest(url, HTTPMethod.POST);
            request.addHeader(new HTTPHeader(HEADER_AUTHORIZATION, AUTHORIZATION_KEY + API_KEY));
            request.addHeader(new HTTPHeader(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON));
            request.setPayload(json.getBytes(UTF_8));
            response = URLFetchServiceFactory.getURLFetchService().fetch(request);
        } catch (Exception e) {
            ErrorManager.logError(datastore, e.getMessage());
            return;
        }

        switch (response.getResponseCode()) {
            case STATUS_CODE_OK:
                String jsonResponse;
                try {
                    jsonResponse = new String(response.getContent(), UTF_8);
                } catch (UnsupportedEncodingException e) {
                    return;
                }
                Map<String, Object> jsonObject = (Map<String, Object>) JSONValue.parse(jsonResponse);

                int canonicalIds = ((Long) jsonObject.get(PROPERTY_CANONICAL_IDS)).intValue();
                int failure = ((Long) jsonObject.get(PROPERTY_FAILURE)).intValue();
                if (canonicalIds == 0 && failure == 0)
                    return;

                List<Object> jsonArray = (List<Object>) jsonObject.get(PROPERTY_RESULTS);
                for (int i = 0; i < jsonArray.size(); i++) {
                    Map<String, Object> result = (Map<String, Object>) JSONValue.parse(JSONValue.toJSONString(jsonArray.get(i)));
                    String error = (String) result.get(PROPERTY_ERROR);
                    String regId = (String) result.get(PROPERTY_REGISTRATION_ID);
                    if (regId != null) {
                        WriterEndpoint.register(!writer, datastore, regId);
                    } else if (error != null) {
                        if (error.equals(ERROR_NOT_REGISTERED))
                            WriterEndpoint.register(!writer, datastore, null);
                        else
                            ErrorManager.logError(datastore, error);
                    }
                }
                break;
            case STATUS_BAD_REQUEST:
                ErrorManager.logError(datastore, "Invalid JSON request for sending GCM");
                break;
            case STATUS_UNAUTHORIZED:
                ErrorManager.logError(datastore, "Unauthorized to send GCM");
                break;
            default:
                ErrorManager.logError(datastore, "Error in sending GCM");
                break;
        }
    }
}
