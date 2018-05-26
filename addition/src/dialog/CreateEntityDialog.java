package badtzmarupekkle.littlethings.dialog;

import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;

import badtzmarupekkle.littlethings.util.SystemMessageManager;


public abstract class CreateEntityDialog<E>  extends DialogFragment {
    public interface OnCreateEntityListener<E> {
        public void onCreateEntity(E e);
    }

    protected OnCreateEntityListener<E> cListener;
    protected SystemMessageManager smManager;

    public void setOnCreateEntityListener(OnCreateEntityListener<E> cListener) {
        this.cListener = cListener;
    }

    protected void onCreateEntity(E e) {
        if(cListener != null)
            cListener.onCreateEntity(e);

        dismiss();
    }
}
