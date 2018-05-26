package badtzmarupekkle.littlethings.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.EditText;

import badtzmarupekkles.littlethings.R;
import badtzmarupekkle.littlethings.application.AppManager;

public class LogInActivity extends Activity {
    private static final int CHECK_PASSWORD = 2;
    private static final int CHECK_PASSWORD_DELAY = 500;

    private static final String PASSWORD_BADTZ_MARU = "pektzmaru";
    private static final String PASSWORD_PEKKLE = "pekkles";

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String password = passwordView.getText().toString();
            switch (password) {
                case PASSWORD_BADTZ_MARU:
                    new InitiateTask(true).execute();
                    break;
                case PASSWORD_PEKKLE:
                    new InitiateTask(false).execute();
                    break;
                default:
                    handler.sendEmptyMessageDelayed(CHECK_PASSWORD, CHECK_PASSWORD_DELAY);
                    break;
            }
        }
    };

    private EditText passwordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        passwordView = (EditText) findViewById(R.id.password);

        AppManager.setActivity(this);

        handler.sendEmptyMessageDelayed(CHECK_PASSWORD, CHECK_PASSWORD_DELAY);

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
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

            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {
            dialog.dismiss();
            Intent i = new Intent(LogInActivity.this, BlogActivity.class);
            handler.removeMessages(CHECK_PASSWORD);
            startActivity(i);
            finish();
        }
    }
}
