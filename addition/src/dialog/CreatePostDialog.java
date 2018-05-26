package badtzmarupekkle.littlethings.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.littlethings.endpoint.post.postendpoint.Postendpoint;
import com.littlethings.endpoint.post.postendpoint.model.Post;
import com.littlethings.endpoint.post.postendpoint.model.PostResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import badtzmarupekkle.littlethings.R;
import badtzmarupekkle.littlethings.application.AppManager;
import badtzmarupekkle.littlethings.util.ImageManager;
import badtzmarupekkle.littlethings.util.SystemMessageManager;

public class CreatePostDialog extends CreateEntityDialog<Post> {
    private static final String ERROR_MESSAGE_UPLOAD_IMAGE = "There has been an error uploading your photo. Please try again in a moment.";
    private static final String ERROR_MESSAGE_UPLOAD_LINK = "There has been an error creating the upload link. Please try again in a moment.";
    private static final String PARAMETER_BLOB_KEY = "blobKey";
    private static final String PARAMETER_IMAGE = "image";
    private static final String PARAMETER_WRITER = "writer";

    boolean swapped;

    private EditText postView;
    private ImageView deleteView;
    private ImageView photoView;
    private ImageView swapView;

    private Bitmap photo;
    private ImageManager iManager;
    private InputMethodManager imManager;
    private Postendpoint endpoint;
    private Uri photoUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        swapped = false;

        iManager = new ImageManager(this);
        imManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        photo = null;
        photoUri = null;

        Postendpoint.Builder builder = new Postendpoint.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
        endpoint = builder.build();
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity());

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_create_post);

        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams layoutParams = dialogWindow.getAttributes();
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialogWindow.setAttributes(layoutParams);

        postView = (EditText) dialog.findViewById(R.id.post);
        deleteView = (ImageView) dialog.findViewById(R.id.delete);
        photoView = (ImageView) dialog.findViewById(R.id.photo);
        swapView = (ImageView) dialog.findViewById(R.id.swap);
        smManager = new SystemMessageManager(getActivity(), (TextView) dialog.findViewById(R.id.system_message));

        swapView.setBackgroundColor(AppManager.getColor());
        dialog.findViewById(R.id.add_layout).setBackgroundColor(AppManager.getColor());
        dialog.findViewById(R.id.create_layout).setBackgroundColor(AppManager.getColor());

        dialog.findViewById(R.id.accept).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (postView.getText().toString().trim().length() > 0 || photoUri != null) {
                    if (AppManager.checkNetworkConnection()) {
                        smManager.removeMessage();
                        //TODO: show loading
                        if (photoUri == null)
                            new CreatePostTask().execute();
                        else
                            new RequestUploadUrlTask().execute();
                    } else {
                        smManager.displayError(SystemMessageManager.ERROR_NETWORK);
                    }
                } else {
                    smManager.displayError(SystemMessageManager.ERROR_EMPTY);
                }
            }
        });

        dialog.findViewById(R.id.camera).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                photoUri = iManager.getImageFromCamera();
            }
        });

        dialog.findViewById(R.id.decline).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        dialog.findViewById(R.id.gallery).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                iManager.getImageFromGallery();
            }
        });

        deleteView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                photoView.setImageBitmap(null);
                photoView.setVisibility(View.GONE);
                deleteView.setVisibility(View.GONE);
                swapView.setVisibility(View.GONE);
                postView.setVisibility(View.VISIBLE);

                if (photo != null)
                    photo.recycle();
                photo = null;
                photoUri = null;
            }
        });

        swapView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (swapped) {
                    deleteView.setVisibility(View.GONE);
                    photoView.setVisibility(View.GONE);
                    postView.setVisibility(View.VISIBLE);
                } else {
                    imManager.hideSoftInputFromWindow(postView.getWindowToken(), 0);
                    deleteView.setVisibility(View.VISIBLE);
                    photoView.setVisibility(View.VISIBLE);
                    postView.setVisibility(View.GONE);
                }

                swapped = !swapped;
            }
        });

        return dialog;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (photo != null)
            photo.recycle();
        photoUri = iManager.onActivityResult(requestCode, data, photoUri);
        photo = ImageManager.getImageFromUri(getActivity(), photo, photoUri);

        imManager.hideSoftInputFromWindow(postView.getWindowToken(), 0);

        photoView.setImageBitmap(photo);
        photoView.setVisibility(View.VISIBLE);
        deleteView.setVisibility(View.VISIBLE);
        swapView.setVisibility(View.VISIBLE);
        postView.setVisibility(View.GONE);

        swapped = true;
    }

    @Override
    protected void onCreateEntity(Post post) {
        if (photo != null)
            photo.recycle();
        photo = null;
        photoUri = null;

        super.onCreateEntity(post);
    }

    private class CreatePostTask extends AsyncTask<Void,Void,PostResponse> {
        private Post post;
        private String photoBlobKey;

        public CreatePostTask() {
            post = new Post();
            photoBlobKey = null;
        }

        public CreatePostTask(String photoBlobKey) {
            post = new Post();
            this.photoBlobKey = photoBlobKey;
        }

        @Override
        protected PostResponse doInBackground(Void... nothing) {
            post.setSecret(AppManager.getSecret());
            post.setWriter(AppManager.getWriter());

            String postText = postView.getText().toString().trim();
            if (postText.length() > 0) {
                post.setPost(postText);
            }

            if (photoBlobKey != null) {
                post.setPhoto(photoBlobKey);
            }

            try {
                return endpoint.post(post).execute();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(PostResponse response) {
            if (response != null)
                if (response.getSuccess()) {
                    post.setId(response.getId());
                    post.setPhoto(response.getUrl());
                    post.setTimestamp(System.currentTimeMillis());
                    onCreateEntity(post);
                } else {
                    smManager.displayError(response.getErrorCode());
                }
            else
                smManager.displayDefaultError();
        }
    }

    private class RequestUploadUrlTask extends AsyncTask<Void,Void,PostResponse> {
        @Override
        protected PostResponse doInBackground(Void... nothing) {
            Post post = new Post();
            post.setSecret(AppManager.getSecret());

            try {
                return endpoint.getUploadUrl(post).execute();
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(PostResponse response) {
            if (response != null) {
                if (response.getSuccess()) {
                    String uploadUrl = response.getUrl();
                    if (uploadUrl != null && uploadUrl.length() > 0) {
                        new UploadImageTask(uploadUrl).execute();
                    } else {
                        //TODO: dismiss loading
                        smManager.displayError(ERROR_MESSAGE_UPLOAD_LINK);
                    }
                } else {
                    //TODO: dismiss loading
                    smManager.displayError(response.getErrorCode());
                }
            } else {
                //TODO: dismiss loading
                smManager.displayDefaultError();
            }
        }
    }

    private class UploadImageTask extends AsyncTask<Void, Void, String> {
        private String uploadUrl;

        public UploadImageTask(String uploadUrl) {
            this.uploadUrl = uploadUrl;
        }

        @Override
        protected String doInBackground(Void... nothing) {
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(uploadUrl);
                FileBody fileBody = new FileBody(new File(ImageManager.getPathFromUri(getActivity(), photoUri)));
                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                builder.addPart(PARAMETER_IMAGE, fileBody);
                builder.addTextBody(PARAMETER_WRITER, Boolean.toString(AppManager.getWriter()));

                httpPost.setEntity(builder.build());
                HttpResponse response = httpClient.execute(httpPost);
                HttpEntity entity = response.getEntity();
                InputStream inputStream = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                String resultString = sb.toString();
                JSONObject result = new JSONObject(resultString);
                return result.getString(PARAMETER_BLOB_KEY);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String blobKey) {
            if (blobKey != null && blobKey.length() > 0) {
                new CreatePostTask(blobKey).execute();
            } else {
                //TODO: dismiss loading
                smManager.displayError(ERROR_MESSAGE_UPLOAD_IMAGE);
            }
        }
    }
}
