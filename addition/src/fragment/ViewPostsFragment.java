package badtzmarupekkle.littlethings.fragment;

import java.io.IOException;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import badtzmarupekkle.littlethings.activity.BlogActivity;
import badtzmarupekkle.littlethings.application.AppManager;
import badtzmarupekkle.littlethings.dialog.CreateEntityDialog;
import badtzmarupekkle.littlethings.dialog.CreatePostDialog;
import badtzmarupekkle.littlethings.util.SystemMessageManager;
import badtzmarupekkle.littlethings.util.ImageManager.AdapterType;
import badtzmarupekkle.littlethings.R;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.littlethings.endpoint.post.postendpoint.Postendpoint;
import com.littlethings.endpoint.post.postendpoint.Postendpoint.Builder;
import com.littlethings.endpoint.post.postendpoint.model.Post;
import com.littlethings.endpoint.post.postendpoint.model.PostResponse;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewPostsFragment extends ViewEntitiesFragment<Post> {
    private static final int LIMIT_POSTS_SIZE = 10;

    private static final String TAG_CREATE_POST = "createPost";

    private ImageView createView;

    private Postendpoint endpoint;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new PostsAdapter(activity);
        iManager.addAdapter(AdapterType.VIEW_POSTS, adapter);

        Builder builder = new Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
        endpoint = builder.build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        entitiesView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        });
        entitiesView.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final Post post = adapter.getItem(position);
                if (post.getWriter() != AppManager.getWriter())
                    return false;

                final Dialog dialog = new Dialog(getActivity());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_delete);

                int color = AppManager.getColor();

                ImageView accept = (ImageView) dialog.findViewById(R.id.accept);
                accept.setBackgroundColor(color);
                accept.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new DeletePostTask(position, dialog, post).execute();
                    }
                });

                ImageView cancel = (ImageView) dialog.findViewById(R.id.decline);
                cancel.setBackgroundColor(color);
                cancel.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
                return true;
            }
        });

        entitiesView.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (!loading && firstVisibleItem + visibleItemCount == totalItemCount - 2) {
                    if (AppManager.checkNetworkConnection())
                        new GetPostsTask(false).execute();
                    else
                        smManager.displayError(SystemMessageManager.ERROR_NETWORK);
                }
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
        });

        createView = (ImageView) rootView.findViewById(R.id.create);
        createView.setBackgroundColor(AppManager.getColor());
        createView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                CreatePostDialog cpDialog = new CreatePostDialog();
                cpDialog.setOnCreateEntityListener(new CreateEntityDialog.OnCreateEntityListener<Post>() {
                    @Override
                    public void onCreateEntity(Post post) {
                        adapter.insert(post);
                        adapter.notifyDataSetChanged();
                    }
                });

                cpDialog.show(activity.getSupportFragmentManager(), TAG_CREATE_POST);
            }
        });

        if(AppManager.checkNetworkConnection()) {
            entitiesView.setRefreshing(true);
            timestamp=System.currentTimeMillis();
            new GetPostsTask(false).execute();
        } else {
            smManager.displayError(SystemMessageManager.ERROR_NETWORK);
        }

        return rootView;
    }

    @Override
    public View getCreateEntityView() {
        return createView;
    }

    @Override
    protected int getEntitiesLimit() {
        return LIMIT_POSTS_SIZE;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_view_posts;
    }

    @Override
    protected GetEntitiesTask getTask(boolean refresh) {
        return new GetPostsTask(refresh);
    }

    @Override
    protected void setRefreshing(boolean refreshing) {
        entitiesView.setRefreshing(refreshing);
    }

    @Override
    protected void updateTimestamp() {
        timestamp = adapter.getItem(adapter.getCount() - 1).getTimestamp();
    }

    private class DeletePostTask extends AsyncTask<Void,Void,PostResponse> {
        private int position;
        private Dialog dialog;
        private Post post;
        private ProgressDialog pDialog;

        public DeletePostTask(int position, Dialog dialog, Post post) {
            this.position=position;
            this.dialog = dialog;
            this.post=post;
            pDialog=new ProgressDialog(getActivity());
            pDialog.setMessage("Deleting...");
            pDialog.show();
        }

        @Override
        protected PostResponse doInBackground(Void... nothing) {
            post.setSecret(AppManager.getSecret());

            try {
                return endpoint.delete(post).execute();
            } catch(IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(PostResponse response) {
            pDialog.dismiss();
            dialog.dismiss();
            if(response!=null) {
                if(response.getSuccess()) {
                    adapter.remove(position);
                    adapter.notifyDataSetChanged();
                } else {
                    smManager.displayError(response.getErrorCode());
                }
            } else {
                smManager.displayDefaultError();
            }
        }
    }

    private class GetPostsTask extends GetEntitiesTask {

        public GetPostsTask(boolean refresh) {
            super(refresh);
        }

        @Override
        protected EntityResponse doInBackground(Void... nothing) {
            Post post=new Post();
            post.setSecret(AppManager.getSecret());
            post.setTimestamp(timestamp);

            try {
                PostResponse response = endpoint.getPosts(post).execute();
                return new EntityResponse(response.getSuccess(), response.getErrorCode(), response.getPosts());
            } catch(IOException e) {
                return null;
            }
        }
    }

    private class PostsAdapter extends EntityAdapter {
        private Drawable badtzMaru;
        private Drawable pekkle;

        public PostsAdapter(BlogActivity activity) {
            super(activity);

            badtzMaru=getResources().getDrawable(R.drawable.badtz_maru);
            pekkle=getResources().getDrawable(R.drawable.pekkle);
        }

        @Override
        public long getItemId(int position) {
            return entities.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView==null) {
                convertView=activity.getLayoutInflater().inflate(R.layout.list_post, parent, false);
                ViewHolder holder=new ViewHolder();
                holder.imageView=(ImageView)convertView.findViewById(R.id.image);
                holder.postView=(TextView)convertView.findViewById(R.id.post);
                holder.timestampView=(TextView)convertView.findViewById(R.id.timestamp);
                holder.writer=true;
                holder.writerView=(ImageView)convertView.findViewById(R.id.writer);
                holder.writerView.setImageDrawable(badtzMaru);

                convertView.setTag(holder);
            }

            final ViewHolder holder=(ViewHolder)convertView.getTag();
            final Post post=getItem(position);
            String postString= post.getPost();
            if(postString!=null && postString.length()>0) {
                holder.postView.setText(postString);
                holder.postView.setVisibility(View.VISIBLE);
            } else {
                holder.postView.setText("");
                holder.postView.setVisibility(View.GONE);
            }

            DateTime timestamp=new DateTime(post.getTimestamp());
            DateTimeFormatter dtf;
            if(AppManager.is24Hour())
                dtf=DateTimeFormat.forPattern(TIMESTAMP_PATTERN_24_HOURS);
            else
                dtf=DateTimeFormat.forPattern(TIMESTAMP_PATTERN_12_HOURS);
            holder.timestampView.setText(dtf.print(timestamp));

            if(post.getWriter()) {
                if(!holder.writer) {
                    holder.writer=true;
                    holder.writerView.setImageDrawable(badtzMaru);
                }
            } else {
                if(holder.writer) {
                    holder.writer=false;
                    holder.writerView.setImageDrawable(pekkle);
                }
            }

            if(holder.imageView.getDrawable()!=null) {
                holder.imageView.setImageBitmap(null);
                holder.imageView.setVisibility(View.GONE);
                iManager.removeImageFromDisplay(holder.id);
            }

            String photoUrl=post.getPhoto();
            if(photoUrl!=null && photoUrl.length()>0) {
                iManager.loadImageInto(AdapterType.VIEW_POSTS, holder.imageView, getImageKey(holder.writer, post.getId()), photoUrl);

                holder.imageView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO: add onClick
                    }
                });
            }

            holder.id=getImageKey(holder.writer, post.getId());

            return convertView;
        }

        private long getImageKey(boolean writer, long key) {
            if(!writer) {
                String stringKey=1+Long.toString(key);
                return Long.valueOf(stringKey);
            }
            return key;
        }

        private class ViewHolder {
            public boolean writer;
            public long id;
            public ImageView imageView;
            public ImageView writerView;
            public TextView postView;
            public TextView timestampView;
        }
    }
}
