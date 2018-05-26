package badtzmarupekkle.littlethings.endpoint;

import java.util.ArrayList;
import java.util.List;

import badtzmarupekkle.littlethings.entity.Post;
import badtzmarupekkle.littlethings.entity.PostResponse;
import badtzmarupekkle.littlethings.util.ErrorManager;
import badtzmarupekkle.littlethings.util.GCMManager;
import badtzmarupekkle.littlethings.util.Validation;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;

@Api(name = "postendpoint", namespace = @ApiNamespace(ownerDomain = "littlethings.com", ownerName = "BadtzMaruPekkle", packagePath = "endpoint/post"))
public class PostEndpoint {
    private static final int LIMIT_PHOTOS_SIZE = 14;
    private static final int LIMIT_POSTS_SIZE = 10;

    private static final String ENTITY_BLOB = "Blob";
    private static final String ENTITY_PHOTO = "Photo";
    private static final String ENTITY_POST = "Post";
    private static final String PROPERTY_COMMENTS = "Comments";
    private static final String PROPERTY_PHOTO = "Photo";
    private static final String PROPERTY_PHOTO_KEY = "PhotoKey";
    private static final String PROPERTY_POST = "Post";
    private static final String PROPERTY_TIMESTAMP = "Timestamp";
    private static final String PROPERTY_VIDEO = "Video";
    private static final String PROPERTY_WRITER = "Writer";

    private DatastoreService datastore;

    public PostEndpoint() {
        datastore = DatastoreServiceFactory.getDatastoreService();
    }

    @ApiMethod(name = "post", httpMethod = HttpMethod.POST, path = "post/post")
    public PostResponse post(Post post) {
        PostResponse response = new PostResponse();

        if (!Validation.validateUser(post.getSecret())) {
            response.setErrorCode(ErrorManager.ERROR_UNAUTHORIZED);
            return response;
        }

        boolean writer = post.getWriter();
        long instant = System.currentTimeMillis();
        String postText = post.getPost();
        String photoBlobKey = post.getPhoto();
        String videoBlobKey = post.getVideo();

        if (!Validation.isValidString(postText) && !Validation.isValidString(photoBlobKey) && !Validation.isValidString(videoBlobKey)) {
            response.setErrorCode(ErrorManager.ERROR_BAD_REQUEST);
            return response;
        }

        Key writerKey = WriterEndpoint.getWriterKey(writer);
        Entity postEntity = new Entity(ENTITY_POST, writerKey);
        postEntity.setProperty(PROPERTY_COMMENTS, 0);
        postEntity.setProperty(PROPERTY_TIMESTAMP, instant);
        postEntity.setUnindexedProperty(PROPERTY_WRITER, writer);
        if (Validation.isValidString(post.getPost()))
            postEntity.setUnindexedProperty(PROPERTY_POST, new Text(postText.trim()));

        Key photoKey = null;
        String photoServingUrl = null;
        if (Validation.isValidString(photoBlobKey)) {
            photoKey = KeyFactory.createKey(writerKey, ENTITY_BLOB, photoBlobKey);
            ImagesService imagesService = ImagesServiceFactory.getImagesService();
            BlobKey blobKey = new BlobKey(photoBlobKey);
            ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(blobKey);
            photoServingUrl = imagesService.getServingUrl(options);
            postEntity.setUnindexedProperty(PROPERTY_PHOTO, photoServingUrl);
        }

        Key videoKey = null;
        if (Validation.isValidString(videoBlobKey)) {
            videoKey = KeyFactory.createKey(writerKey, ENTITY_BLOB, videoBlobKey);
            postEntity.setUnindexedProperty(PROPERTY_VIDEO, videoBlobKey);
        }

        int retries = ErrorManager.RETRIES;
        TransactionOptions options = TransactionOptions.Builder.withXG(true);
        while (true) {
            Transaction transaction = datastore.beginTransaction(options);
            try {
                Key postKey = datastore.put(postEntity);
                if (photoKey != null) {
                    datastore.delete(photoKey);
                    Entity photoEntity = new Entity(ENTITY_PHOTO, postKey.getId(), postKey);
                    photoEntity.setProperty(PROPERTY_TIMESTAMP, instant);
                    photoEntity.setUnindexedProperty(PROPERTY_PHOTO, photoServingUrl);
                    photoEntity.setUnindexedProperty(PROPERTY_PHOTO_KEY, photoBlobKey);
                    photoEntity.setUnindexedProperty(PROPERTY_WRITER, writer);
                    datastore.put(photoEntity);
                }
                if (videoKey != null)
                    datastore.delete(videoKey);
                transaction.commit();
                break;
            } catch (Exception e) {
                if (retries == 0) {
                    ErrorManager.logError(datastore, e.getMessage());
                    response.setErrorCode(ErrorManager.ERROR_GATEWAY_TIMEOUT);
                    return response;
                }
                retries--;
            } finally {
                if (transaction.isActive())
                    transaction.rollback();
            }
        }

        GCMManager.notifyWriterPost(post.getWriter(), datastore);

        response.setId(postEntity.getKey().getId());
        response.setUrl(photoServingUrl);
        response.setSuccess(true);
        return response;
    }

    @ApiMethod(name = "delete", httpMethod = HttpMethod.POST, path = "post/delete")
    public PostResponse delete(Post post) {
        PostResponse response = new PostResponse();

        if (!Validation.validateUser(post.getSecret())) {
            response.setErrorCode(ErrorManager.ERROR_UNAUTHORIZED);
            return response;
        }

        Key writerKey = WriterEndpoint.getWriterKey(post.getWriter());
        Key postKey = KeyFactory.createKey(writerKey, ENTITY_POST, post.getId());
        Key photoKey = KeyFactory.createKey(postKey, ENTITY_PHOTO, post.getId());
        Entity photoEntity;
        try {
            photoEntity = datastore.get(photoKey);
        } catch (EntityNotFoundException e) {
            photoEntity = null;
        }

        int retries = ErrorManager.RETRIES;
        while (true) {
            try {
                if (photoEntity != null) {
                    BlobKey blobKey = new BlobKey((String) photoEntity.getProperty(PROPERTY_PHOTO_KEY));
                    BlobstoreService blobstore = BlobstoreServiceFactory.getBlobstoreService();
                    blobstore.delete(blobKey);
                    datastore.delete(photoKey);
                }
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

    @ApiMethod(name = "get", httpMethod = HttpMethod.POST, path = "post/get")
    public PostResponse get(Post post) {
        PostResponse response = new PostResponse();

        if (!Validation.validateUser(post.getSecret())) {
            response.setErrorCode(ErrorManager.ERROR_UNAUTHORIZED);
            return response;
        }

        Key postKey = KeyFactory.createKey(ENTITY_POST, post.getId());
        Entity postEntity;
        try {
            postEntity = datastore.get(postKey);
        } catch (EntityNotFoundException e) {
            response.setErrorCode(ErrorManager.ERROR_NOT_FOUND);
            return response;
        }

        response.setPost(postEntityToPost(postEntity));
        response.setSuccess(true);
        return response;
    }

    @ApiMethod(name = "getPosts", httpMethod = HttpMethod.POST, path = "post/getposts")
    public PostResponse getPosts(Post post) {
        PostResponse response = new PostResponse();

        if (!Validation.validateUser(post.getSecret())) {
            response.setErrorCode(ErrorManager.ERROR_UNAUTHORIZED);
            return response;
        }

        List<Post> posts = new ArrayList<>();
        Query q = new Query(ENTITY_POST);
        Filter filter = new FilterPredicate(PROPERTY_TIMESTAMP, FilterOperator.LESS_THAN, post.getTimestamp());
        q.setFilter(filter);
        q.addSort(PROPERTY_TIMESTAMP, SortDirection.DESCENDING);
        PreparedQuery pq = datastore.prepare(q);

        for (Entity result : pq.asIterable(FetchOptions.Builder.withChunkSize(LIMIT_POSTS_SIZE).limit(LIMIT_POSTS_SIZE)))
            posts.add(postEntityToPost(result));

        response.setPosts(posts);
        response.setSuccess(true);
        return response;
    }

    @ApiMethod(name = "getPostsPhoto", httpMethod = HttpMethod.POST, path = "post/getpostsphoto")
    public PostResponse getPostsPhoto(Post post) {
        PostResponse response = new PostResponse();
        if (!Validation.validateUser(post.getSecret())) {
            response.setErrorCode(ErrorManager.ERROR_UNAUTHORIZED);
            return response;
        }

        List<Post> posts = new ArrayList<>();
        Query q = new Query(ENTITY_PHOTO);
        Filter filter = new FilterPredicate(PROPERTY_TIMESTAMP, FilterOperator.LESS_THAN, post.getTimestamp());
        q.setFilter(filter);
        q.addSort(PROPERTY_TIMESTAMP, SortDirection.DESCENDING);
        PreparedQuery pq = datastore.prepare(q);

        for (Entity result : pq.asIterable(FetchOptions.Builder.withChunkSize(LIMIT_PHOTOS_SIZE).limit(LIMIT_PHOTOS_SIZE)))
            posts.add(photoEntityToPost(result));

        response.setPosts(posts);
        response.setSuccess(true);
        return response;
    }

    @ApiMethod(name = "getPostsWriter", httpMethod = HttpMethod.POST, path = "post/getpostswriter")
    public PostResponse getPostsWriter(Post post) {
        PostResponse response = new PostResponse();
        if (!Validation.validateUser(post.getSecret())) {
            response.setErrorCode(ErrorManager.ERROR_UNAUTHORIZED);
            return response;
        }

        List<Post> posts = new ArrayList<>();
        Key writerKey = WriterEndpoint.getWriterKey(post.getWriter());
        Query q = new Query(ENTITY_POST).setAncestor(writerKey);
        Filter filter = new FilterPredicate(PROPERTY_TIMESTAMP, FilterOperator.LESS_THAN, post.getTimestamp());
        q.setFilter(filter);
        q.addSort(PROPERTY_TIMESTAMP, SortDirection.DESCENDING);
        PreparedQuery pq = datastore.prepare(q);

        for (Entity result : pq.asIterable(FetchOptions.Builder.withChunkSize(LIMIT_POSTS_SIZE).limit(LIMIT_POSTS_SIZE)))
            posts.add(postEntityToPost(result));

        response.setPosts(posts);
        response.setSuccess(true);
        return response;
    }

    @ApiMethod(name = "getUploadUrl", httpMethod = HttpMethod.POST, path = "post/getuploadurl")
    public PostResponse getUploadUrl(Post post) {
        PostResponse response = new PostResponse();

        if (!Validation.validateUser(post.getSecret())) {
            response.setErrorCode(ErrorManager.ERROR_UNAUTHORIZED);
            return response;
        }

        BlobstoreService blobstore = BlobstoreServiceFactory.getBlobstoreService();
        response.setUrl(blobstore.createUploadUrl("/upload"));
        response.setSuccess(true);
        return response;
    }

    private Post photoEntityToPost(Entity e) {
        Post post = new Post();
        post.setId(e.getKey().getId());
        post.setPhoto((String) e.getProperty(PROPERTY_PHOTO));
        post.setTimestamp((long) e.getProperty(PROPERTY_TIMESTAMP));
        post.setWriter((boolean) e.getProperty(PROPERTY_WRITER));

        return post;
    }

    private Post postEntityToPost(Entity e) {
        Post post = new Post();
        post.setComments(((Long) e.getProperty(PROPERTY_COMMENTS)).intValue());
        post.setId(e.getKey().getId());
        post.setTimestamp((long) e.getProperty(PROPERTY_TIMESTAMP));
        post.setWriter((boolean) e.getProperty(PROPERTY_WRITER));

        if (e.hasProperty(PROPERTY_PHOTO))
            post.setPhoto((String) e.getProperty(PROPERTY_PHOTO));
        if (e.hasProperty(PROPERTY_POST))
            post.setPost(((Text) e.getProperty(PROPERTY_POST)).getValue());
        if (e.hasProperty(PROPERTY_VIDEO))
            post.setVideo((String) e.getProperty(PROPERTY_VIDEO));

        return post;
    }
}
