package eu.nimble.service.bp.impl;

import eu.nimble.service.bp.impl.util.persistence.HibernateUtilityRef;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Created by dogukan on 28.07.2018.
 */
@Controller
public class ProcessInstanceController {

    @RequestMapping(value = "/processInstance/exists",
            method = RequestMethod.GET)
    public ResponseEntity processInstanceExists(@RequestParam(value = "relatedProducts", required = false) List<String> relatedProducts,
                                                @RequestParam(value = "relatedProductCategories", required = false) List<String> relatedProductCategories,
                                                @RequestParam(value = "tradingPartnerIDs", required = false) List<String> tradingPartnerIDs,
                                                @RequestParam(value = "initiationDateRange", required = false) String initiationDateRange,
                                                @RequestParam(value = "lastActivityDateRange", required = false) String lastActivityDateRange,
                                                @RequestParam(value = "processInstanceId", required = false) String processInstanceId) {
        return ResponseEntity.ok().body(checkExistenceQuery(relatedProducts,relatedProductCategories,tradingPartnerIDs,initiationDateRange,lastActivityDateRange,processInstanceId));
    }

    public boolean checkExistenceQuery(List<String> relatedProductIds,List<String> relatedProductCategories,List<String> tradingPartnerIds,
                                       String initiationDateRange,String lastActivityDateRange,String processInstanceId){
        String query = "SELECT pi.processInstanceID";

        query += " from " +
                "ProcessInstanceDAO pi, " +
                "ProcessDocumentMetadataDAO doc left join doc.relatedProductCategoriesItems relCat left join doc.relatedProductsItems relProd" +
                " where '" + processInstanceId + "' = pi.processInstanceID and doc.processInstanceID = pi.processInstanceID";

        if (relatedProductCategories != null && relatedProductCategories.size() > 0) {
            query += " and (";
            int i = 0;
            for (; i < relatedProductCategories.size() - 1; i++) {
                query += " relCat.item = '" + relatedProductCategories.get(i) + "' or";
            }
            query += " relCat.item = '" + relatedProductCategories.get(i) + "')";
        }
        if (relatedProductIds != null && relatedProductIds.size() > 0) {
            query += " and (";
            int i = 0;
            for (; i < relatedProductIds.size() - 1; i++) {
                query += " relProd.item = '" + relatedProductIds.get(i) + "' or";
            }
            query += " relProd.item = '" + relatedProductIds.get(i) + "')";
        }
        if (tradingPartnerIds != null && tradingPartnerIds.size() > 0) {
            query += " and (";
            int i = 0;
            for (; i < tradingPartnerIds.size() - 1; i++) {
                query += " (doc.initiatorID = '" + tradingPartnerIds.get(i) + "' or doc.responderID = '" + tradingPartnerIds.get(i) + "') or";
            }
            query += " (doc.initiatorID = '" + tradingPartnerIds.get(i) + "' or doc.responderID = '" + tradingPartnerIds.get(i) + "'))";
        }
        Object process = (Object) HibernateUtilityRef.getInstance("bp-data-model").loadIndividualItem(query);
        if(process == null){
            return false;
        }
        return true;
    }
}
