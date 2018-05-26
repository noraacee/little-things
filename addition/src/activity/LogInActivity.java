package badtzmarupekkle.littlethings.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.EditText;

import java.util.Random;

import badtzmarupekkle.littlethings.R;
import badtzmarupekkle.littlethings.application.AppManager;
import badtzmarupekkle.littlethings.application.LittleThingsApplication;
import badtzmarupekkle.littlethings.widget.SnowflakeView;

public class LogInActivity extends Activity {
    private static final int ADD_SNOWFLAKE = 1;
    private static final int ADD_SNOWFLAKE_BASE_DELAY = 100;
    private static final int CHECK_PASSWORD = 2;
    private static final int CHECK_PASSWORD_DELAY = 500;
    private static final int LIMIT_ADD_SNOWFLAKE_DELAY = 14;
    private static final int TICK_SNOWFLAKES = 0;
    private static final int TICK_SNOWFLAKES_DELAY = 10;

    private static final String PASSWORD_BADTZ_MARU = "pektzmaru";
    private static final String PASSWORD_PEKKLE = "pekkles";

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TICK_SNOWFLAKES:
                    snowflakeView.invalidate();
                    handler.sendEmptyMessageDelayed(TICK_SNOWFLAKES, TICK_SNOWFLAKES_DELAY);
                    break;
                case ADD_SNOWFLAKE:
                    snowflakeView.addSnowflake();
                    handler.sendEmptyMessageDelayed(ADD_SNOWFLAKE, (random.nextInt(LIMIT_ADD_SNOWFLAKE_DELAY) + 1) * ADD_SNOWFLAKE_BASE_DELAY);
                    break;
                case CHECK_PASSWORD:
                    String password = passwordView.getText().toString();
                    if (password.equals(PASSWORD_BADTZ_MARU)) {
                        new InitiateTask(true).execute();
                    } else if (password.equals(PASSWORD_PEKKLE)) {
                        new InitiateTask(false).execute();
                    } else {
                        handler.sendEmptyMessageDelayed(CHECK_PASSWORD, CHECK_PASSWORD_DELAY);
                    }
                    break;
            }
        }
    };

    private EditText passwordView;
    private Random random;
    private SnowflakeView snowflakeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        snowflakeView = (SnowflakeView) findViewById(R.id.snow);
        passwordView = (EditText) findViewById(R.id.password);

        AppManager.setActivity(this);

        snowflakeView.invalidate();
        random = snowflakeView.getRandom();
        handler.sendEmptyMessageDelayed(ADD_SNOWFLAKE, (random.nextInt(LIMIT_ADD_SNOWFLAKE_DELAY) + 1) * ADD_SNOWFLAKE_BASE_DELAY);
        handler.sendEmptyMessageDelayed(CHECK_PASSWORD, CHECK_PASSWORD_DELAY);
        handler.sendEmptyMessageDelayed(TICK_SNOWFLAKES, TICK_SNOWFLAKES_DELAY);

    }

    @Override
    protected void onPause() {
        super.onPause();
        LittleThingsApplication.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LittleThingsApplication.onResume();
    }

    private class InitiateTask extends AsyncTask<Void, Void, Void> {
        private boolean writer;
        private ProgressDialog dialog;

        public InitiateTask(boolean writer) {
            this.writer = writer;
        }

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(LogInActivity.this);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... nothing) {
            AppManager.updateColor();
            AppManager.setWriter(writer);
            AppManager.checkGCMRegistration();

            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {
            dialog.dismiss();
            Intent i = new Intent(LogInActivity.this, BlogActivity.class);
            handler.removeMessages(ADD_SNOWFLAKE);
            handler.removeMessages(CHECK_PASSWORD);
            handler.removeMessages(TICK_SNOWFLAKES);
            startActivity(i);
            finish();
        }
    }
}
