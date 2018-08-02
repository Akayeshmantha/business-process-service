package eu.nimble.service.bp.impl.util.rest;

import org.camunda.bpm.engine.impl.util.json.JSONObject;

import java.util.Iterator;
import java.util.List;

public class JSONUtil {

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
