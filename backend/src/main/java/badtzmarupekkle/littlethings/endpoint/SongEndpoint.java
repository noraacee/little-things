package badtzmarupekkle.littlethings.endpoint;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

import java.util.ArrayList;
import java.util.List;

import badtzmarupekkle.littlethings.entity.Song;
import badtzmarupekkle.littlethings.entity.SongResponse;
import badtzmarupekkle.littlethings.util.ErrorManager;
import badtzmarupekkle.littlethings.util.Validation;

@Api(name = "songendpoint", namespace = @ApiNamespace(ownerDomain = "littlethings.com", ownerName = "BadtzMaruPekkle", packagePath = "endpoint/song"))
public class SongEndpoint {
    private static final int LIMIT_SONGS_SIZE = 10;

    private static final String ENTITY_SONG = "Song";
    private static final String PROPERTY_MESSAGE = "message";
    private static final String PROPERTY_TIMESTAMP = "timestamp";

    private DatastoreService datastore;

    public SongEndpoint() {
        datastore = DatastoreServiceFactory.getDatastoreService();
    }

    @ApiMethod(name = "get", httpMethod = ApiMethod.HttpMethod.POST, path = "song/get")
    public SongResponse get(Song song) {
        SongResponse response = new SongResponse();

        if(!Validation.validateUser(song.getSecret())) {
            response.setErrorCode(ErrorManager.ERROR_UNAUTHORIZED);
            return response;
        }

        List<Song> songs = new ArrayList<>();
        Query q = new Query(ENTITY_SONG);
        Query.Filter filter = new Query.FilterPredicate(PROPERTY_TIMESTAMP, Query.FilterOperator.LESS_THAN, song.getTimestamp());
        q.setFilter(filter);
        q.addSort(PROPERTY_TIMESTAMP, Query.SortDirection.DESCENDING);
        PreparedQuery pq = datastore.prepare(q);

        for (Entity result : pq.asIterable(FetchOptions.Builder.withChunkSize(LIMIT_SONGS_SIZE).limit(LIMIT_SONGS_SIZE)))
            songs.add(entityToSong(result));

        response.setSongs(songs);
        response.setSuccess(true);
        return response;
    }

    @ApiMethod(name = "delete", httpMethod = ApiMethod.HttpMethod.POST, path = "song/delete")
    public SongResponse delete(Song song) {
        SongResponse response = new SongResponse();

        if (!Validation.validateUser(song.getSecret())) {
            response.setErrorCode(ErrorManager.ERROR_UNAUTHORIZED);
            return response;
        }

        Key writerKey = WriterEndpoint.getWriterKey(song.getWriter());
        Key postKey = KeyFactory.createKey(writerKey, ENTITY_SONG, song.getKey());

        int retries = ErrorManager.RETRIES;
        while (true) {
            try {
                BlobKey blobKey = new BlobKey(song.getKey());
                BlobstoreService blobstore = BlobstoreServiceFactory.getBlobstoreService();
                blobstore.delete(blobKey);
                datastore.delete(postKey);
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

    private Song entityToSong(Entity e) {
        Song song = new Song();
        song.setKey(e.getKey().getName());
        song.setMessage((String) e.getProperty(PROPERTY_MESSAGE));
        song.setTimestamp((long) e.getProperty(PROPERTY_TIMESTAMP));
        song.setWriter(false);

        return song;
    }
}
