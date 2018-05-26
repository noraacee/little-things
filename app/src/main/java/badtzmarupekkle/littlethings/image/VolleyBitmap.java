package badtzmarupekkle.littlethings.image;

import android.graphics.Bitmap;

public class VolleyBitmap {
    private boolean cached;
    private boolean displayed;

    private int displayCount;

    private Bitmap bitmap;

    public VolleyBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        cached = true;
        displayed = false;
        displayCount = 0;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void display(boolean isDisplayed) {
        synchronized (this) {
            if (isDisplayed) {
                displayCount++;
                displayed = true;
            } else {
                displayCount--;
            }
        }

        checkState();
    }

    public void removeCache() {
        cached = false;
        checkState();
    }

    private synchronized void checkState() {
        if (cached && displayCount <= 0 && displayed && hasValidBitmap())
            bitmap.recycle();
    }

    private synchronized boolean hasValidBitmap() {
        return bitmap != null && !bitmap.isRecycled();
    }
}
