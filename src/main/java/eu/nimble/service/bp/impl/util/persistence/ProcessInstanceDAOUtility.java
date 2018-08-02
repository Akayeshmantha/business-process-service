package eu.nimble.service.bp.impl.util.persistence;

import eu.nimble.service.bp.hyperjaxb.model.ProcessDocumentMetadataDAO;

import java.util.List;

public class ProcessInstanceDAOUtility {

    public static ProcessDocumentMetadataDAO checkExistence(List<String> relatedProductIds, List<String> relatedProductCategories, List<String> tradingPartnerIds,
                                                            String initiationDateRange, String lastActivityDateRange, String processInstanceId){
        String query = "SELECT doc";

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
        ProcessDocumentMetadataDAO documentMetadataDAO = (ProcessDocumentMetadataDAO) HibernateUtilityRef.getInstance("bp-data-model").loadIndividualItem(query);
        return documentMetadataDAO;
    }
}
