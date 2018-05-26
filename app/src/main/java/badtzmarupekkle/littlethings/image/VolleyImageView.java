package badtzmarupekkle.littlethings.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

public class VolleyImageView extends NetworkImageView {

    private String oldUrl;
    private String newUrl;
    private VolleyLruCache lruCache;

    public VolleyImageView(Context context) {
        super(context);
    }

    public VolleyImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VolleyImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        lruCache.setDisplay(false, oldUrl);
        lruCache.setDisplay(true, newUrl);
        super.setImageBitmap(bm);
    }

    @Override
    public void setImageUrl(String url, ImageLoader imageLoader) {
        oldUrl = newUrl;
        newUrl = url;
        super.setImageUrl(url, imageLoader);

    }

    public void setLruCache(VolleyLruCache lruCache) {
        this.lruCache = lruCache;
    }
}
