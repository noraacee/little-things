package badtzmarupekkle.littlethings.image;

import android.content.Context;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

public class VolleySingleton {
    private static VolleySingleton instance;
    private ImageLoader imageLoader;
    private VolleyLruCache lruCache;

    private VolleySingleton(Context context){
        lruCache = new VolleyLruCache();
        imageLoader = new ImageLoader(Volley.newRequestQueue(context), lruCache);
    }

    public static VolleySingleton getInstance(Context context){
        if(instance == null){
            instance = new VolleySingleton(context);
        }
        return instance;
    }

    public ImageLoader getImageLoader(){
        return imageLoader;
    }

    public VolleyLruCache getLruCache() {
        return lruCache;
    }

}