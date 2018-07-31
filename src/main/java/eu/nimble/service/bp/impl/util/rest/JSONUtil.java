package eu.nimble.service.bp.impl.util.rest;

import org.camunda.bpm.engine.impl.util.json.JSONArray;
import org.camunda.bpm.engine.impl.util.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JSONUtil {
    public static String unify(List<String> results) {
        //TODO: Rewrite this method using SolrJ
        JSONObject union = new JSONObject(results.get(0));
        JSONObject responseHeader = union.getJSONObject("responseHeader");
        JSONObject params = responseHeader.getJSONObject("params");
        JSONArray unionFacetFieldNames;

        if(params.get("facet.field").getClass() == JSONArray.class){
            unionFacetFieldNames = params.getJSONArray("facet.field");
        }
        else {
            unionFacetFieldNames = new JSONArray("["+params.get("facet.field").toString()+"]");
        }
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

    public static String unifySuggestions(List<String> results){
        //TODO: Rewrite this method using SolrJ
        int i = 0;
        for( ; i < results.size() ; i++){
            JSONObject union = new JSONObject(results.get(i));
            JSONObject suggestions = union.getJSONObject("suggestions");
            int count = suggestions.getInt("suggestion_count");
            if(count != 0){
                break;
            }
        }
        // no suggestions
        if(i == results.size()){
            return new JSONObject(results.get(0)).toString();
        }

        JSONObject union = new JSONObject(results.get(i));
        JSONObject suggestions = union.getJSONObject("suggestions");
        JSONObject suggestionsFacets = suggestions.getJSONObject("suggestion_facets");
        JSONObject itemNames = suggestionsFacets.getJSONObject("item_name");

        for(i++; i<results.size();i++){
            JSONObject itemNames2 = new JSONObject(results.get(i)).getJSONObject("suggestions").getJSONObject("suggestion_facets").getJSONObject("item_name");

            Iterator<String> names = itemNames2.keys();
            while (names.hasNext()) {
                String value = names.next();
                if(!itemNames.has(value)){
                    itemNames.put(value,itemNames2.get(value));
                }
            }
        }
        suggestionsFacets.put("item_name",itemNames);
        suggestions.put("suggestion_count",itemNames.length());
        suggestions.put("suggestion_facets",suggestionsFacets);
        union.put("suggestions",suggestions);

        return union.toString();
    }
}
