package badtzmarupekkle.littlethings.handler;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobstoreServiceFactory;

import java.io.IOException;

public class UploadSongServlet extends HttpServlet {
    private static final long serialVersionUID = 3695219428874622842L;

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String uploadUrl = BlobstoreServiceFactory.getBlobstoreService().createUploadUrl("/song");

        resp.getWriter().write("<!DOCTYPE html><html><body>");
        resp.getWriter().write("<h1>Song</h1>");
        resp.getWriter().write("<form action=\"" + uploadUrl + "\" method=\"post\" enctype=\"multipart/form-data\">");
        resp.getWriter().write("<p><input type=\"text\" name=\"secret\" value=\"\" placeholder=\"Secret\"></p>" +
                               "<p><input type=\"text\" name=\"message\" value=\"\" placeholder=\"Message\"></p>" +
                               "<p><input type=\"file\" name=\"song\" accept=\".mp3\"></p>" +
                               "<p><input type=\"submit\" value=\"Submit\"></p>");
        resp.getWriter().write("</form></body></html>");
        resp.getWriter().close();
    }
}
