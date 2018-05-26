package badtzmarupekkle.littlethings.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import badtzmarupekkles.littlethings.R;
import badtzmarupekkle.littlethings.application.AppManager;

public class ShowGIFDialog {
    private static final String GIF_DELIMITER_URL = "%";
    private static final String GIF_ENCODING = "UTF-8";
    private static final String GIF_HTML = "<html>" +
            "<head>" +
            "<style type='text/css'>" +
            "body{margin:auto auto; text-align:center;} " +
            "img{width:100%;} " +
            "</style>" +
            "</head>" +
            "<body>" +
            "<img src='" + GIF_DELIMITER_URL + "'/>" +
            "</body>" +
            "</html>";
    private static final String GIF_MIME = "text/html";

    private String url;

    @SuppressLint("deprecation")
    public Dialog createDialog(Context context) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_show_gif);

        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams layoutParams = dialogWindow.getAttributes();
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialogWindow.setAttributes(layoutParams);
        dialogWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        GradientDrawable loadingBackground = (GradientDrawable) context.getResources().getDrawable(R.drawable.circle);
        loadingBackground.setColor(AppManager.getColor());
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN)
            dialog.findViewById(R.id.loading).setBackgroundDrawable(loadingBackground);
        else
            dialog.findViewById(R.id.loading).setBackground(loadingBackground);

        final View loadingView = dialog.findViewById(R.id.loading);

        WebView gifView = (WebView) dialog.findViewById(R.id.gif);
        gifView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView webView, String url) {
                loadingView.setVisibility(View.GONE);
            }
        });

        gifView.loadData(GIF_HTML.replace(GIF_DELIMITER_URL, url), GIF_MIME, GIF_ENCODING);
        gifView.setBackgroundColor(Color.TRANSPARENT);

        dialog.setCanceledOnTouchOutside(true);

        return dialog;
    }

    public void showGIF(Context context, String url) {
        this.url = url;
        createDialog(context).show();
    }
}
