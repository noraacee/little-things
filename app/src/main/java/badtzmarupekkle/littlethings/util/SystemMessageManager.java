package badtzmarupekkle.littlethings.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;

import badtzmarupekkles.littlethings.R;

public class SystemMessageManager {
    public static final int ERROR_DEFAULT = 0;
    public static final int ERROR_EMPTY = 1;
    public static final int ERROR_NETWORK = 2;
    public static final int ERROR_BAD_REQUEST = 400;
    public static final int ERROR_UNAUTHORIZED = 401;
    public static final int ERROR_NOT_FOUND = 404;
    public static final int ERROR_GATEWAY_TIMEOUT = 504;
    public static final int ERROR_FILL_EMPTY = 0;
    public static final int ERROR_FILL_EXCEED_LIMIT = 1;


    private static final int DURATION_ERROR = 5000;

    private static final String ERROR_MESSAGE_DEFAULT = "An error has occured. Please try again in a few moments.";
    private static final String ERROR_MESSAGE_EMPTY = "You must fill something in";
    private static final String ERROR_MESSAGE_NETWORK = "Cannot establish a network connection";
    private static final String ERROR_MESSAGE_BAD_REQUEST = "You did not submit a proper request";
    private static final String ERROR_MESSAGE_UNAUTHORIZED = "You are unauthorized to use this app";
    private static final String ERROR_MESSAGE_NOT_FOUND = "Your request cannot be found";
    private static final String ERROR_MESSAGE_GATEWAY_TIMEOUT = "There has been a delay with the server. Please try again in a few moments.";
    private static final String ERROR_FILL_MESSAGE_EMPTY = "The * cannot be left empty";
    private static final String ERROR_FILL_MESSAGE_EXCEED_LIMIT = "The ** cannot be more than * characters";

    private static final SparseArray<String> errorMap;
    private static final SparseArray<String> errorFillMap;

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            removeMessage();
        }
    };

    private Activity context;

    private TextView systemMessage;

    static {
        errorMap = new SparseArray<>();
        errorMap.append(ERROR_DEFAULT, ERROR_MESSAGE_DEFAULT);
        errorMap.append(ERROR_EMPTY, ERROR_MESSAGE_EMPTY);
        errorMap.append(ERROR_NETWORK, ERROR_MESSAGE_NETWORK);
        errorMap.append(ERROR_BAD_REQUEST, ERROR_MESSAGE_BAD_REQUEST);
        errorMap.append(ERROR_UNAUTHORIZED, ERROR_MESSAGE_UNAUTHORIZED);
        errorMap.append(ERROR_NOT_FOUND, ERROR_MESSAGE_NOT_FOUND);
        errorMap.append(ERROR_GATEWAY_TIMEOUT, ERROR_MESSAGE_GATEWAY_TIMEOUT);

        errorFillMap = new SparseArray<>();
        errorFillMap.append(ERROR_FILL_EMPTY, ERROR_FILL_MESSAGE_EMPTY);
        errorFillMap.append(ERROR_FILL_EXCEED_LIMIT, ERROR_FILL_MESSAGE_EXCEED_LIMIT);
    }

    public SystemMessageManager(Activity context, TextView systemMessage) {
        this.context = context;
        this.systemMessage = systemMessage;
    }


    public void displayDefaultError() {
        systemMessage.setText(ERROR_MESSAGE_DEFAULT);
        setErrorBackground();
    }

    public void displayError(int errorCode) {
        systemMessage.setText(errorMap.get(errorCode));
        setErrorBackground();
    }

    public void removeMessage() {
        systemMessage.setVisibility(View.GONE);
    }

    private void setErrorBackground() {
        systemMessage.setBackgroundColor(context.getResources().getColor(R.color.red_500));
        systemMessage.setVisibility(View.VISIBLE);
        handler.sendEmptyMessageDelayed(0, DURATION_ERROR);
    }
}
