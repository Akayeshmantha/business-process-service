package eu.nimble.service.bp.impl;

import eu.nimble.service.bp.impl.util.federation.FederationUtil;
import eu.nimble.service.bp.impl.util.rest.JSONUtil;
import eu.nimble.service.bp.impl.util.rest.URLConnectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@Controller
public class SearchController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String marmottaURL = "http://nimble-staging.salzburgresearch.at/marmotta/solr/catalogue2/select";

    @RequestMapping(value = "/search/fields",
            produces = {"application/json"},
            method = RequestMethod.GET)
    public ResponseEntity getFields() {
        String result = URLConnectionUtil.get(marmottaURL + "?q=*:*&rows=0&wt=csv", "UTF-8",null,null,null);

        return new ResponseEntity<String>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/search/query",
            produces = {"application/json"},
            method = RequestMethod.GET)
    public ResponseEntity search(HttpServletRequest request, @RequestParam(value = "query", required = false) String query,
                                 @RequestParam(value = "facets", required = false) List<String> facets,
                                 @RequestParam(value = "facetQueries", required = false) List<String> facetQueries,
                                 @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                                 @RequestParam(value = "federated", required = false, defaultValue = "false") Boolean federated,
                                 @RequestParam(value = "initiatorInstanceId", required = true) String initiatorInstanceId,
                                 @RequestParam(value = "targetInstanceId", required = true) String targetInstanceId,
                                 @RequestHeader(value="Authorization", required=true) String bearerToken) {
        logger.debug(" Query: {]", query);
        logger.debug(" Facets: {]", facets);
        logger.debug(" FacetQueries: {]", facetQueries);

        int start = page * 10 - 10;
        String queryString = "";
        try {
            queryString = "q=" + URLEncoder.encode(query, "UTF-8") + "&start=" + start + "&";
            queryString += "facet=true&sort=score%20desc&rows=10&facet.sort=count&facet.limit=30&facet.mincount=1&json.nl=map&wt=json";

            for (String facet: facets) {
                queryString += "&facet.field=" + facet;
            }

            for (String facetQuery: facetQueries) {
                queryString += "&fq=" + URLEncoder.encode(facetQuery, "UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
            logger.error("", e);
        }

        logger.debug(" $$$ Query: {}", marmottaURL + "?" + queryString);

        String result = URLConnectionUtil.get(marmottaURL + "?" + queryString, "UTF-8",null,null,null);

        // if it is federated send the query to other NIMBLE instances...
        String unifiedResult = "";
        if (federated) {
            List<String> searchURLsInTheFederation = FederationUtil.getFederationEndpoints();
            List<String> results = new ArrayList<>();
            results.add(result);

            String reconstructedQueryString = "query=" + query + "&";
            reconstructedQueryString += "facets=" + request.getParameter("facets") + "&";
            reconstructedQueryString += "facetQueries=" + request.getParameter("facetQueries") + "&";
            reconstructedQueryString += "page=" + page + "&";
            reconstructedQueryString += "federated=false";

            for (String searchURL : searchURLsInTheFederation) {
                results.add(URLConnectionUtil.get(searchURL + "/delegate/search/query?" + reconstructedQueryString, "UTF-8",initiatorInstanceId,targetInstanceId,bearerToken));
            }
            try {
                unifiedResult = JSONUtil.unify(results);
            }
            catch (Exception e){
                logger.error("",e);
            }

        } else {
            unifiedResult = result;
        }

        return new ResponseEntity<String>(unifiedResult, HttpStatus.OK);
    }

    @RequestMapping(value = "/search/retrieve",
            produces = {"application/json"},
            method = RequestMethod.GET)
    public ResponseEntity search(@RequestParam(value = "id", required = false) String id,
                                 @RequestParam(value = "initiatorInstanceId", required = true) String initiatorInstanceId,
                                 @RequestParam(value = "targetInstanceId", required = true) String targetInstanceId,
                                 @RequestHeader(value="Authorization", required=true) String bearerToken) {
        String queryString = "q=*&rows=1&wt=json&fq=item_id:" + id;

        logger.debug(" $$$ Query: {}", marmottaURL + "?" + queryString);

        String result = URLConnectionUtil.get(marmottaURL + "?" + queryString, "UTF-8",null,null,null);

        return new ResponseEntity<String>(result, HttpStatus.OK);
    }
}