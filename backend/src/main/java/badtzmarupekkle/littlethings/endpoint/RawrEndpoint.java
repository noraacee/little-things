package badtzmarupekkle.littlethings.endpoint;

import java.util.ArrayList;
import java.util.List;

import badtzmarupekkle.littlethings.entity.EntityModel;
import badtzmarupekkle.littlethings.entity.Rawr;
import badtzmarupekkle.littlethings.entity.RawrResponse;
import badtzmarupekkle.littlethings.util.ErrorManager;
import badtzmarupekkle.littlethings.util.Validation;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Text;

@Api(name = "rawrendpoint", namespace = @ApiNamespace(ownerDomain = "littlethings.com", ownerName = "BadtzMaruPekkle", packagePath = "endpoint/rawr"))
public class RawrEndpoint {
    private static final int LIMIT_RAWRS_SIZE = 5;

    private static final String ENTITY_RAWR = "Rawr";
    private static final String PROPERTY_TIMESTAMP = "Timestamp";
    private static final String PROPERTY_TITLE = "Title";
    private static final String PROPERTY_TYPE = "Type";
    private static final String PROPERTY_URL = "Url";
    private static final String PROPERTY_WRITER = "Writer";

    private DatastoreService datastore;

    public RawrEndpoint() {
        datastore = DatastoreServiceFactory.getDatastoreService();
    }

    @ApiMethod(name = "getRawrs", httpMethod = HttpMethod.POST, path = "post/getRawrs")
    public RawrResponse getRawrs(Rawr rawr) {
        RawrResponse response = new RawrResponse();

        if (!Validation.validateUser(rawr.getSecret())) {
            response.setErrorCode(ErrorManager.ERROR_UNAUTHORIZED);
            return response;
        }

        List<Rawr> rawrs = new ArrayList<>();
        Query q = new Query(ENTITY_RAWR);
        Filter filter = new FilterPredicate(PROPERTY_TIMESTAMP, FilterOperator.LESS_THAN, rawr.getTimestamp());
        q.setFilter(filter);
        q.addSort(PROPERTY_TIMESTAMP, SortDirection.DESCENDING);
        PreparedQuery pq = datastore.prepare(q);

        for (Entity result : pq.asIterable(FetchOptions.Builder.withChunkSize(LIMIT_RAWRS_SIZE).limit(LIMIT_RAWRS_SIZE)))
            rawrs.add(rawrEntityToRawr(result));

        response.setRawrs(rawrs);
        response.setSuccess(true);
        return response;
    }

    private Rawr rawrEntityToRawr(Entity e) {
        Rawr rawr = new Rawr();
        rawr.setId(e.getKey().getId());
        rawr.setTimestamp((long) e.getProperty(PROPERTY_TIMESTAMP));
        rawr.setType((String) e.getProperty(PROPERTY_TYPE));
        rawr.setUrl(((Text) e.getProperty(PROPERTY_URL)).getValue());
        rawr.setWriter((boolean) e.getProperty(PROPERTY_WRITER));

        if (e.hasProperty(PROPERTY_TITLE))
            rawr.setTitle(((Text) e.getProperty(PROPERTY_TITLE)).getValue());

        return rawr;
    }
}
