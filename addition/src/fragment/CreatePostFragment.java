package badtzmarupekkle.littlethings.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.littlethings.endpoint.post.postendpoint.Postendpoint;
import com.littlethings.endpoint.post.postendpoint.model.Post;
import com.littlethings.endpoint.post.postendpoint.model.PostResponse;
import com.nineoldandroids.animation.Animator;

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
import badtzmarupekkle.littlethings.activity.BlogActivity;
import badtzmarupekkle.littlethings.application.AppManager;
import badtzmarupekkle.littlethings.interf.OnAnimateListener;
import badtzmarupekkle.littlethings.interf.OnMultiTouchListener;
import badtzmarupekkle.littlethings.interf.OnCreateListener;
import badtzmarupekkle.littlethings.util.AnimationManager;
import badtzmarupekkle.littlethings.util.AnimationManager.AnimationManagerInput;
import badtzmarupekkle.littlethings.util.ImageManager;
import badtzmarupekkle.littlethings.util.SystemMessageManager;

public class CreatePostFragment extends Fragment implements OnCreateListener {

    private static final String ERROR_MESSAGE_UPLOAD_IMAGE = "There has been an error uploading your image. Please try again in a moment.";
    private static final String ERROR_MESSAGE_UPLOAD_LINK = "There has been an error creating the upload link. Please try again in a moment.";
    private static final String PARAMETER_BLOB_KEY = "blobKey";
    private static final String PARAMETER_IMAGE = "image";
    private static final String PARAMETER_WRITER = "writer";

    private int padding;

    private EditText postView;
    private ImageView addView;
    private ImageView imageView;
    private LinearLayout addLayout;

    private Animator animator;
    private AnimationManagerInput input;
    private Bitmap image;
    private Drawable add;
    private Drawable delete;
    private Drawable photo;
    private ImageManager iManager;
    private OnMultiTouchListener mtListener;
    private OnCreateListener sListener;
    private Postendpoint endpoint;
    private ProgressDialog dialog;
    private SystemMessageManager smManager;
    private Uri imageUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        add = getResources().getDrawable(R.drawable.add);
        delete = getResources().getDrawable(R.drawable.decline);
        photo = getResources().getDrawable(R.drawable.gallery);

        input = new AnimationManagerInput(new OnAnimateListener() {
            @Override
            public Animator getAnimator() {
                return animator;
            }

            @Override
            public void nullifySet() {
                animator = null;
            }

            @Override
            public void onAnimationEnd() {
                if (imageView.getVisibility() == View.VISIBLE) {
                    addView.setImageDrawable(delete);
                    imageView.setOnTouchListener(mtListener);
                } else {
                    addView.setImageDrawable(photo);
                }
            }

            @Override
            public void onAnimationStart() {
            }
        });

        mtListener = new OnMultiTouchListener(getActivity(), OnMultiTouchListener.Mode.SINGLE_TOUCH) {
            @Override
            public boolean onSwipeDown() {
                if (animator != null)
                    animator.cancel();
                imageView.setOnClickListener(null);
                animator = AnimationManager.translateDown(input);
                animator.start();
                return true;
            }
        };

        iManager = new ImageManager(this);
        image = null;
        imageUri = null;

        smManager = ((BlogActivity) getActivity()).getSystemMessageManager();

        Postendpoint.Builder builder = new Postendpoint.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
        endpoint = builder.build();

        dialog = new ProgressDialog(getActivity());
        dialog.setMessage("Submitting");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_create_post, container, false);
        postView = (EditText) rootView.findViewById(R.id.post);
        addView = (ImageView) rootView.findViewById(R.id.add);
        imageView = (ImageView) getActivity().findViewById(R.id.image);
        addLayout = (LinearLayout) getActivity().findViewById(R.id.add_layout);

        input.view = imageView;
        padding = imageView.getPaddingLeft();

        int color = AppManager.getColor();
        rootView.findViewById(R.id.divider_horizontal).setBackgroundColor(color);

        addView.setBackgroundColor(color);
        addView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (animator != null)
                    return;
                if (imageView.getVisibility() == View.VISIBLE) {
                    imageView.setImageBitmap(null);
                    imageView.setVisibility(View.GONE);

                    if (image != null)
                        image.recycle();
                    image = null;
                    imageUri = null;
                    addView.setImageDrawable(add);
                } else if (image != null) {
                    animator = AnimationManager.translateDownReverse(input);
                    animator.start();
                } else {
                    if (addLayout.getVisibility() == View.VISIBLE)
                        addLayout.setVisibility(View.GONE);
                    else
                        addLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        imageView.setOnTouchListener(mtListener);

        ImageView camera = (ImageView) getActivity().findViewById(R.id.camera);
        camera.setBackgroundColor(color);
        camera.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addLayout.setVisibility(View.GONE);
                imageUri = iManager.getImageFromCamera();
            }
        });

        ImageView gallery = (ImageView) getActivity().findViewById(R.id.gallery);
        gallery.setBackgroundColor(color);
        gallery.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addLayout.setVisibility(View.GONE);
                iManager.getImageFromGallery();
            }
        });

        ImageView submit = (ImageView) rootView.findViewById(R.id.submit);
        submit.setBackgroundColor(color);
        submit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (postView.getText().toString().trim().length() > 0 || imageUri != null) {
                    if (AppManager.checkNetworkConnection()) {
                        smManager.removeMessage();
                        dialog.show();
                        if (imageUri == null)
                            new PostTask().execute();
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

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (image != null)
            image.recycle();
        imageUri = iManager.onActivityResult(requestCode, data, imageUri);
        image = ImageManager.getImageFromUri(getActivity(), image, imageUri);

        if (image != null) {
            addView.setImageDrawable(delete);
            if (ImageManager.isPortrait(image))
                imageView.setPadding(padding, 0, padding, 0);
            else
                imageView.setPadding(padding, padding, padding, 0);
        }

        imageView.setImageBitmap(image);
        imageView.setOnTouchListener(mtListener);
        imageView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSubmit(Post post) {
        if (image != null)
            image.recycle();
        image = null;
        imageUri = null;

        addLayout.setVisibility(View.GONE);
        addView.setImageDrawable(add);
        imageView.setImageBitmap(null);
        imageView.setVisibility(View.GONE);
        postView.setText("");

        sListener.onSubmit(post);
    }

    public void setOnSubmitListener(OnCreateListener sListener) {
        this.sListener = sListener;
    }

    private class PostTask extends AsyncTask<Void,Void,PostResponse> {
        private Post post;
        private String photoBlobKey;

        public PostTask() {
            post = new Post();
            photoBlobKey = null;
        }

        public PostTask(String photoBlobKey) {
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
            dialog.dismiss();
            if (response != null)
                if (response.getSuccess()) {
                    post.setId(response.getId());
                    post.setPhoto(response.getUrl());
                    post.setTimestamp(System.currentTimeMillis());
                    onSubmit(post);
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
                        dialog.dismiss();
                        smManager.displayError(ERROR_MESSAGE_UPLOAD_LINK);
                    }
                } else {
                    dialog.dismiss();
                    smManager.displayError(response.getErrorCode());
                }
            } else {
                dialog.dismiss();
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
                FileBody fileBody = new FileBody(new File(ImageManager.getPathFromUri(getActivity(), imageUri)));
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
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
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
                new PostTask(blobKey).execute();
            } else {
                dialog.dismiss();
                smManager.displayError(ERROR_MESSAGE_UPLOAD_IMAGE);
            }
        }
    }
}
