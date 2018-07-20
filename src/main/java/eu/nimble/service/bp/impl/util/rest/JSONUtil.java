package eu.nimble.service.bp.impl.util.rest;

import org.camunda.bpm.engine.impl.util.json.JSONArray;
import org.camunda.bpm.engine.impl.util.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JSONUtil {
    public static String unify(List<String> results) {
        JSONObject union = new JSONObject(results.get(0));
        JSONObject responseHeader = union.getJSONObject("responseHeader");
        JSONObject params = responseHeader.getJSONObject("params");
        JSONArray unionFacetFieldNames = params.getJSONArray("facet.field");
        List<String> facetFieldNames = new ArrayList<>();
        for (int i = 0; i < unionFacetFieldNames.length(); i++) {
            facetFieldNames.add(unionFacetFieldNames.getString(i));
        }

        for (int i = 1; i < results.size(); i++) {
            String result = results.get(i);
            JSONObject object = new JSONObject(result);

            JSONObject response = object.getJSONObject("response");
            int numFound = response.getInt("numFound");
            JSONArray docs = response.getJSONArray("docs");
            JSONObject facet_counts = object.getJSONObject("facet_counts");
            JSONObject facet_fields = facet_counts.getJSONObject("facet_fields");

            JSONObject unionResponse = union.getJSONObject("response");
            int unionNumFound = unionResponse.getInt("numFound");
            JSONArray unionDocs = unionResponse.getJSONArray("docs");
            JSONObject union_facet_counts = union.getJSONObject("facet_counts");
            JSONObject union_facet_fields = union_facet_counts.getJSONObject("facet_fields");

            // First increase the number of found objects
            unionNumFound += numFound;
            unionResponse.put("numFound", unionNumFound);

            // Second merge the found objects
            for (int j = 0; j < docs.length(); j++) {
                unionDocs.put(docs.get(j));
            }

            // Finally merge facet fields
            for (String facetFieldName : facetFieldNames) {
                JSONObject facetFieldSource = facet_fields.getJSONObject(facetFieldName);
                JSONObject facetFieldTarget = union_facet_fields.getJSONObject(facetFieldName);

                Iterator<String> facetValues = facetFieldSource.keys();
                // add or increment all the target keys
                while (facetValues.hasNext()) {
                    String value = facetValues.next();
                    int valueCount = facetFieldSource.getInt(value);
                    for (int k = 0; k < valueCount; k++)
                        facetFieldTarget.increment(value);
                }
            }
        }
        return union.toString();
    }
}
