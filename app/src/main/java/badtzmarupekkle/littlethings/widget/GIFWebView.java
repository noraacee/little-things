package badtzmarupekkle.littlethings.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import badtzmarupekkle.littlethings.dialog.ShowGIFDialog;
import badtzmarupekkle.littlethings.image.VolleyLruCache;

public class GIFWebView extends WebView {
    private static final String GIF_DELIMITER_URL = "%";
    private static final String GIF_ENCODING = "UTF-8";
    private static final String GIF_HTML = "<html>" +
            "<head>" +
            "<style type='text/css'>" +
            "body{margin:auto auto;text-align:center;} " +
            "img{width:100%;} " +
            "</style>" +
            "</head>" +
            "<body>" +
            "<img src='" + GIF_DELIMITER_URL + "'/>" +
            "</body>" +
            "</html>";
    private static final String GIF_MIME = "text/html";

    private boolean getFirstImage;
    private boolean imageLoaded;

    private String url;

    private Bitmap firstImage;
    private ImageView firstImageView;
    private ImageView playView;
    private VolleyLruCache imageCache;

    public GIFWebView(Context context) {
        super(context);
    }

    public GIFWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GIFWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void init(final Context context, final ShowGIFDialog gifDialog, VolleyLruCache imageCache, ImageView firstImageView, ImageView playView) {
        this.imageCache = imageCache;
        this.firstImageView = firstImageView;
        this.playView = playView;
        this.playView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                gifDialog.showGIF(context, url);
            }
        });

        init();
    }

    public void setFirstImage(String url) {
        this.url = url;
        Bitmap bm = imageCache.getBitmap(url);
        if (bm != null) {
            firstImageView.setImageBitmap(bm);
            firstImageView.setVisibility(View.VISIBLE);
            playView.setVisibility(View.VISIBLE);
            setVisibility(false, View.GONE);
        } else {
            loadData(getHtml(url), GIF_MIME, GIF_ENCODING);
            getFirstImage = true;
            setVisibility(true, View.VISIBLE);
        }
    }

    public void setVisibility(boolean everything, int visibility) {
        super.setVisibility(visibility);
        if (everything) {
            firstImageView.setVisibility(visibility);
            playView.setVisibility(visibility);
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (imageLoaded) {
            imageLoaded = false;
            getBitmap();
            imageCache.putBitmap(url, firstImage);
            firstImageView.setImageBitmap(firstImage);
            setVisibility(false, View.GONE);
        }
    }

    private void getBitmap() {
        firstImage = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(firstImage);
        canvas.drawColor(Color.TRANSPARENT);
        draw(canvas);
    }

    private String getHtml(String url) {
        return GIF_HTML.replace(GIF_DELIMITER_URL, url);
    }

    private void init() {
        getFirstImage = false;
        imageLoaded = false;
        firstImage = null;

        setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView webView, String url) {
                if (getFirstImage) {
                    getFirstImage = false;
                    imageLoaded = true;
                }
            }
        });
    }
}
