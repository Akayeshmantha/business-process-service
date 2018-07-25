package eu.nimble.service.bp.impl.util.rest;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class URLConnectionUtil {
    private static final Logger logger = LoggerFactory.getLogger(URLConnectionUtil.class);

    public static String get(String url, String charset,String initiatorInstanceId,String targetInstanceId,String bearerToken) {

        String result = null;
        try {
            if (initiatorInstanceId == null){
                HttpResponse<String> response = Unirest.get(url)
                        .header("Accept-Charset",charset).asString();
                result = response.getBody();
            }
            else {
                HttpResponse<String> response = Unirest.get(url)
                        .queryString("initiatorInstanceId",initiatorInstanceId)
                        .queryString("targetInstanceId",targetInstanceId)
                        .header("Authorization",bearerToken).asString();
                result = response.getBody();
            }
        }
        catch (Exception e){
            logger.error("", e);
        }
        return result;
    }
}
