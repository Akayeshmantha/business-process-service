package eu.nimble.service.bp.impl.util.rest;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;

public class URLConnectionUtil {
    private static final Logger logger = LoggerFactory.getLogger(URLConnectionUtil.class);

    public static String get(String url, String charset) {
        URLConnection connection = null;
        try {
            connection = new URL(url).openConnection();
        } catch (IOException e) {
            logger.error("", e);
        }
        connection.setRequestProperty("Accept-Charset", charset);

        String result = null;
        try {
            InputStream response = connection.getInputStream();
            StringWriter writer = new StringWriter();
            IOUtils.copy(response, writer, charset);
            result = writer.toString();

            logger.debug(" $$$ Result: {}", result);
        } catch (Exception e) {
            logger.error("", e);
        }
        return result;
    }
}
