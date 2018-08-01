package eu.nimble.service.bp.impl.util.federation;

import java.util.ArrayList;
import java.util.List;

public class FederationUtil {
    // returns the urls of the NIMBLE instances in the federation...
    public static List<String> getFederationEndpoints() {
        List<String> nimbleURLs = new ArrayList<>();

        nimbleURLs.add("http://localhost:8082");
        nimbleURLs.add("http://localhost:8083");

        return nimbleURLs;
    }

    // The id of the NIMBLE instance...
    public static String getFederationEndpoint(String id) {
        return "http://localhost:8082";
    }

}
