package eu.nimble.service.bp.impl.util.federation;

import java.util.ArrayList;
import java.util.List;

public class FederationUtil {
    // returns the urls of the NIMBLE instances in the federation...
    public static List<String> getFederationEndpoints() {
        List<String> nimbleURLs = new ArrayList<>();

        nimbleURLs.add("http://192.168.1.26:8081");

        return nimbleURLs;
    }

    // The id of the NIMBLE instance...
    public static String getFederationEndpoint(String id) {
        return "http://192.168.1.26:8081";
    }

}
