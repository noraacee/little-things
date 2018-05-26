package badtzmarupekkle.littlethings.image;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.toolbox.ImageLoader;

public class VolleyLruCache extends LruCache<String, VolleyBitmap> implements ImageLoader.ImageCache {
    private static final int BYTE_SIZE = 1024;
    private static final int CACHE_SIZE_FACTOR = 8;

    public VolleyLruCache() {
       super(getCacheSize());
    }

    @Override
    public Bitmap getBitmap(String url) {
        VolleyBitmap value = get(url);
        if (value != null)
            return value.getBitmap();
        else return null;
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        put(url, new VolleyBitmap(bitmap));
    }

    @Override
    public int sizeOf(String key, VolleyBitmap value) {
        return value.getBitmap().getRowBytes() * value.getBitmap().getHeight() / BYTE_SIZE;
    }

    @Override
    protected void entryRemoved(boolean evicted, String key, VolleyBitmap oldValue, VolleyBitmap newValue) {
        oldValue.removeCache();
    }

    public void setDisplay(boolean isDisplayed, String key) {
        if (key != null) {
            VolleyBitmap value = get(key);
            if (value != null)
                value.display(isDisplayed);
        }
    }

    private static int getCacheSize() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / BYTE_SIZE);
        return maxMemory / CACHE_SIZE_FACTOR;
    }
}
