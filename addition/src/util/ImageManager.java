package badtzmarupekkle.littlethings.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import badtzmarupekkle.littlethings.application.AppManager;

public class ImageManager {
    private static final int CACHE_SIZE_FACTOR = 8;
    private static final int MAX_LOAD_QUEUE_SIZE = 10;
    private static final int MAX_LOADING_SIZE = 5;
    private static final int REQUEST_IMAGE_CAPTURE = 0;
    private static final int REQUEST_GALLERY_PICK = 1;
    private static final int SAMPLE_IMAGE_HEIGHT = 150;

    public static enum AdapterType {
        VIEW_POSTS, VIEW_RAWRS;
    }

    public static enum Mode {
        GET_IMAGE, LOAD_IMAGE;
    }

    private int loadingCount;
    private int screenHeight;
    private int screenWidth;

    private Fragment fragment;
    private List<KeyTypeUrl> loadQueue;
    private Map<AdapterType, BaseAdapter> adapters;
    private Mode mode;
    private LruCache<Long, RecyclingBitmap> imageMemoryCache;

    public ImageManager(Fragment fragment) {
        this.fragment = fragment;
        mode = Mode.GET_IMAGE;
    }

    @SuppressLint("NewApi")
    public ImageManager(Context context) {
        adapters = new HashMap<>();
        mode = Mode.LOAD_IMAGE;
        loadingCount = 0;
        loadQueue = new ArrayList<>();

        screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        screenWidth = context.getResources().getDisplayMetrics().widthPixels;

        createLruCache();
    }

    public static float calculateHeight(Bitmap image, float width) {
        return width * image.getHeight() / image.getWidth();
    }

    public static float calculateWidth(Bitmap image, float height) {
        return height * image.getWidth() / image.getHeight();
    }

    public static Bitmap createSampleImage(Bitmap image) {
        if (image == null)
            return null;
        int width = image.getWidth() / (image.getHeight() / SAMPLE_IMAGE_HEIGHT);
        return Bitmap.createScaledBitmap(image, width, SAMPLE_IMAGE_HEIGHT, true);
    }

    public static Bitmap getImageFromUri(Context context, Bitmap oldImage, Uri imageUri) {
        if (imageUri == null)
            return null;
        Bitmap originalImage;
        try {
            originalImage = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
        } catch (Exception e) {
            return null;
        }
        int orientation = getImageOrientation(context, imageUri);
        if (oldImage != null) {
            oldImage.recycle();
        }
        if (orientation > 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);
            return Bitmap.createBitmap(originalImage, 0, 0, originalImage.getWidth(), originalImage.getHeight(), matrix, true);
        } else {
            return originalImage;
        }
    }

    public static Bitmap getImageFromUrlNoAsync(String url) {
        try {
            URL imageUrl = new URL(url);
            return BitmapFactory.decodeStream(imageUrl.openConnection().getInputStream());
        } catch (Exception e) {
            return null;
        }
    }

    public static int getImageOrientation(Context context, Uri uri) {
        String path = getPathFromUri(context, uri);
        if (path == null)
            return 0;

        try {
            ExifInterface exif = new ExifInterface(path);
            int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (exifOrientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                case ExifInterface.ORIENTATION_NORMAL:
                    break;
            }
        } catch (IOException e) {
        }

        Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.Media.ORIENTATION}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int orientation = cursor.getInt(0);
            cursor.close();
            return orientation;
        }
        if (cursor != null)
            cursor.close();
        return 0;
    }

    public static String getPathFromUri(Context context, Uri uri) {
        if (uri == null)
            return null;
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(uri, proj, null, null, null);
            if (cursor == null)
                return uri.getPath();
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e) {
            return null;
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }

    public static boolean isPortrait(Bitmap image) {
        if (image == null)
            return false;
        return image.getHeight() > image.getWidth();
    }

    public static void loadImageInto(ImageView imageView, URL url) {
        if (url == null)
            return;

        LoadImageIntoTask.newInstance(imageView, url).execute();
    }

    public static Bitmap rotateBitmap(Bitmap source, float angle) {
        if (source == null)
            return null;

        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public static Bitmap scaleBitmap(Bitmap bitmap, int newHeight, int newWidth) {
        if (bitmap == null)
            return null;

        Bitmap scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Config.ARGB_8888);

        float sx = newWidth / (float) bitmap.getWidth();
        float sy = newHeight / (float) bitmap.getHeight();

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(sx, sy);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));

        return scaledBitmap;
    }

    public void addAdapter(AdapterType type, BaseAdapter adapter) {
        if (mode != Mode.LOAD_IMAGE)
            return;

        adapters.put(type, adapter);
    }

    public Uri getImageFromCamera() {
        Uri imageUri = null;
        if (mode != Mode.GET_IMAGE)
            return imageUri;

        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (i.resolveActivity(fragment.getActivity().getPackageManager()) != null) {
            File cameraFile = new File(AppManager.getAppImagesFileDirectory(), "LT" + Long.toString(System.currentTimeMillis()) + ".jpg");
            imageUri = Uri.fromFile(cameraFile);
            i.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            fragment.startActivityForResult(i, REQUEST_IMAGE_CAPTURE);
        }

        return imageUri;
    }

    public void getImageFromGallery() {
        if (mode != Mode.GET_IMAGE)
            return;

        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType("image/*");
        fragment.startActivityForResult(i, REQUEST_GALLERY_PICK);
    }

    public Bitmap getImageWithKey(long key) {
        if (mode != Mode.LOAD_IMAGE)
            return null;

        RecyclingBitmap rBitmap = imageMemoryCache.get(key);
        if (rBitmap != null) {
            Bitmap image = rBitmap.getBitmap();
            if (image != null && image.isRecycled())
                return null;
            return image;
        }
        return null;
    }

    public void loadImageInto(AdapterType type, ImageView imageView, long key, String url) {
        if (mode != Mode.LOAD_IMAGE)
            return;

        RecyclingBitmap rBitmap = imageMemoryCache.get(key);
        if (rBitmap != null) {
            Bitmap image = rBitmap.getBitmap();
            if (image == null || image.isRecycled()) {
                imageMemoryCache.remove(key);
                addUrlToLoadQueue(key, type, url);
                imageView.setImageBitmap(null);
                imageView.setVisibility(View.GONE);
            } else {
                rBitmap.setIsDisplayed(true);
                imageView.setImageBitmap(image);
                imageView.setVisibility(View.VISIBLE);
            }
        } else {
            addUrlToLoadQueue(key, type, url);
            imageView.setImageBitmap(null);
            imageView.setVisibility(View.GONE);
        }
    }

    public Uri onActivityResult(int requestCode, Intent data, Uri imageUri) {
        if (mode != Mode.GET_IMAGE)
            return null;

        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE:
                return imageUri;
            case REQUEST_GALLERY_PICK:
                if (data != null)
                    return data.getData();
                break;
        }
        return null;
    }

    public void removeImageFromDisplay(long key) {
        if (mode != Mode.LOAD_IMAGE)
            return;

        if (key > 0) {
            RecyclingBitmap rBitmap = imageMemoryCache.get(key);
            if (rBitmap != null) {
                rBitmap.setIsDisplayed(false);
            }
        }
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqHeight, int reqWidth) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight && width > reqWidth) {
            while ((height / inSampleSize) > reqHeight && (width / inSampleSize) > reqWidth)
                inSampleSize *= 2;
            inSampleSize /= 2;
        }

        return inSampleSize;
    }

    private synchronized void addUrlToLoadQueue(long key, AdapterType type, String url) {
        KeyTypeUrl keyTypeUrl = new KeyTypeUrl(key, type, url);
        if (!loadQueue.contains(keyTypeUrl)) {
            loadQueue.add(keyTypeUrl);
            loadImageAsync(false);
        } else {
            loadQueue.remove(keyTypeUrl);
        }
    }

    private synchronized void checkSize() {
        if (loadQueue.size() > MAX_LOAD_QUEUE_SIZE)
            for (int i = 0; i < loadQueue.size() - MAX_LOAD_QUEUE_SIZE; i++) {
                loadQueue.remove(0);
            }
    }

    private synchronized void loadImageAsync(boolean loadFinish) {
        if (loadFinish)
            loadingCount--;
        while (loadingCount < MAX_LOADING_SIZE && loadQueue.size() > 0) {
            loadingCount++;
            new GetImage(loadQueue.get(loadQueue.size() - 1)).execute();
            loadQueue.remove(loadQueue.size() - 1);
        }
        checkSize();
    }

    private void createLruCache() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / CACHE_SIZE_FACTOR;
        imageMemoryCache = new LruCache<Long, RecyclingBitmap>(cacheSize) {
            @Override
            public void entryRemoved(boolean evicted, Long key, RecyclingBitmap oldBitmap, RecyclingBitmap newBitmap) {
                oldBitmap.setIsCached(false);
            }

            @Override
            protected int sizeOf(Long key, RecyclingBitmap image) {
                Bitmap bitmap = image.getBitmap();
                if (bitmap != null && !bitmap.isRecycled())
                    return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
                else
                    return 0;
            }
        };
    }

    private static class LoadImageIntoTask extends AsyncTask<Void, Void, Bitmap> {
        private ImageView imageView;
        private URL url;

        public LoadImageIntoTask(ImageView imageView, URL url) {
            this.imageView = imageView;
            this.url = url;
        }

        public static LoadImageIntoTask newInstance(ImageView imageView, URL url) {
            return new LoadImageIntoTask(imageView, url);
        }

        @Override
        protected Bitmap doInBackground(Void... nothing) {
            Bitmap image = null;
            try {
                image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch (Exception e) {
            }
            return image;
        }

        @Override
        protected void onPostExecute(Bitmap image) {
            if (image != null)
                imageView.setImageBitmap(image);
        }
    }

    private class GetImage extends AsyncTask<Void, Void, Void> {
        private AdapterType type;
        private KeyTypeUrl keyTypeUrl;

        public GetImage(KeyTypeUrl keyTypeUrl) {
            this.keyTypeUrl = keyTypeUrl;
            type = keyTypeUrl.getType();
        }

        @Override
        protected Void doInBackground(Void... nothing) {
            try {
                URL imageUrl = new URL(keyTypeUrl.getUrl());
                InputStream is = imageUrl.openConnection().getInputStream();
                Bitmap image = null;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(is, null, options);

                is.close();
                is = imageUrl.openConnection().getInputStream();

                options.inSampleSize = calculateInSampleSize(options, screenHeight, screenWidth);
                options.inJustDecodeBounds = false;

                if (options.inSampleSize == 1)
                    image = BitmapFactory.decodeStream(is);
                else
                    image = BitmapFactory.decodeStream(is, null, options);

                is.close();
                RecyclingBitmap rBitmap = new RecyclingBitmap(image);
                if (imageMemoryCache.get(keyTypeUrl.getKey()) == null) {
                    imageMemoryCache.put(keyTypeUrl.getKey(), rBitmap);
                    rBitmap.setIsCached(true);
                } else {
                    Bitmap bitmap = imageMemoryCache.get(keyTypeUrl.getKey()).getBitmap();
                    if (bitmap == null || bitmap.isRecycled()) {
                        imageMemoryCache.put(keyTypeUrl.getKey(), rBitmap);
                        rBitmap.setIsCached(true);
                    }
                }
            } catch (Exception e) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {
            BaseAdapter adapter = adapters.get(type);
            if (adapter != null)
                adapter.notifyDataSetChanged();
            loadImageAsync(true);
        }
    }

    private class KeyTypeUrl {
        private long key;
        private AdapterType type;
        private String url;

        public KeyTypeUrl(long key, AdapterType type, String url) {
            this.key = key;
            this.type = type;
            this.url = url;
        }

        public long getKey() {
            return key;
        }

        public AdapterType getType() {
            return type;
        }

        public String getUrl() {
            return url;
        }

        @Override
        public int hashCode() {
            return (int) key;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null)
                return false;
            if (!(o instanceof KeyTypeUrl))
                return false;
            if (o == this)
                return true;
            return key == ((KeyTypeUrl) o).getKey();
        }
    }

    private class RecyclingBitmap {
        private int cacheCount;
        private int displayCount;

        private Bitmap bitmap;

        public RecyclingBitmap(Bitmap bitmap) {
            this.bitmap = bitmap;
            cacheCount = 0;
            displayCount = 0;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        public void setIsCached(boolean isCached) {
            synchronized (this) {
                if (isCached)
                    cacheCount++;
                else
                    cacheCount--;
            }
            shouldRecycle();
        }

        public void setIsDisplayed(boolean isDisplayed) {
            synchronized (this) {
                if (isDisplayed)
                    displayCount++;
                else
                    displayCount--;
            }
            shouldRecycle();
        }

        public synchronized void shouldRecycle() {
            if (bitmap != null && cacheCount <= 0 && displayCount <= 0) {
                bitmap.recycle();
                bitmap = null;
            }
        }
    }
}
