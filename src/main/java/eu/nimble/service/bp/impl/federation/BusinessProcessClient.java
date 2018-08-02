package eu.nimble.service.bp.impl.federation;

import eu.nimble.service.bp.swagger.model.ProcessInstanceInputMessage;
import eu.nimble.service.model.ubl.commonaggregatecomponents.DataMonitoringClauseType;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import feign.Response;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


@Headers("Content-Type: application/json")
public interface BusinessProcessClient {


    @RequestLine("GET delegate/group?collaborationRole={collaborationRole}&archived={archived}&limit={limit}&offset={offset}&lastActivityDateRange={lastActivityDateRange}&initiationDateRange={initiationDateRange}&tradingPartnerIDs={tradingPartnerIDs}&relatedProductCategories={relatedProductCategories}&relatedProducts={relatedProducts}&partyID={partyID}&initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    Response clientGetProcessInstanceGroups(
            @Param("partyID") String partyID,
            @Param("relatedProducts") List<String> relatedProducts,
            @Param("relatedProductCategories") List<String> relatedProductCategories,
            @Param("tradingPartnerIDs") List<String> tradingPartnerIDs,
            @Param("initiationDateRange") String initiationDateRange,
            @Param("lastActivityDateRange") String lastActivityDateRange,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit,
            @Param("archived") Boolean archived,
            @Param("collaborationRole") String collaborationRole,
            @Param("initiatorInstanceId") String initiatorInstanceId,
            @Param("targetInstanceId") String targetInstanceId,
            @Param("bearerToken") String bearerToken);


    @RequestLine("GET delegate/group/filters?collaborationRole={collaborationRole}&archived={archived}&lastActivityDateRange={lastActivityDateRange}&initiationDateRange={initiationDateRange}&tradingPartnerIDs={tradingPartnerIDs}&relatedProductCategories={relatedProductCategories}&relatedProducts={relatedProducts}&partyID={partyID}&initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    Response clientGetProcessInstanceGroupFilters(
            @Param("partyID") String partyID,
            @Param("relatedProducts") List<String> relatedProducts,
            @Param("relatedProductCategories") List<String> relatedProductCategories,
            @Param("tradingPartnerIDs") List<String> tradingPartnerIDs,
            @Param("initiationDateRange") String initiationDateRange,
            @Param("lastActivityDateRange") String lastActivityDateRange,
            @Param("archived") Boolean archived,
            @Param("collaborationRole") String collaborationRole,
            @Param("initiatorInstanceId") String initiatorInstanceId,
            @Param("targetInstanceId") String targetInstanceId,
            @Param("bearerToken") String bearerToken);


    @RequestLine("POST delegate/start?gid={gid}&precedingPid={precedingPid}&initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    Response clientStartProcessInstance(eu.nimble.service.bp.swagger.model.ProcessInstanceInputMessage body,
                                        @Param("gid") String gid,
                                        @Param("precedingPid") String precedingPid,
                                        @Param("initiatorInstanceId") String initiatorInstanceId,
                                        @Param("targetInstanceId") String targetInstanceId,
                                        @Param("bearerToken") String bearerToken);

    @RequestLine("POST delegate/start/createGroup/{processInstanceId}?initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    Response clientCreateProcessInstanceGroup(eu.nimble.service.bp.swagger.model.ProcessInstanceInputMessage body,
                                        @Param("initiatorInstanceId") String initiatorInstanceId,
                                        @Param("targetInstanceId") String targetInstanceId,
                                        @Param("processInstanceId") String processInstanceId,
                                        @Param("bearerToken") String bearerToken);

    @RequestLine("POST delegate/start/addToGroup/{processInstanceId}?initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}&precedingProcessId={precedingProcessId}")
    @Headers("Authorization: {bearerToken}")
    Response clientAddNewProcessInstanceToGroup(eu.nimble.service.bp.swagger.model.ProcessInstanceInputMessage body,
                                              @Param("initiatorInstanceId") String initiatorInstanceId,
                                              @Param("targetInstanceId") String targetInstanceId,
                                              @Param("processInstanceId") String processInstanceId,
                                              @Param("precedingProcessId") String precedingProcessId,
                                              @Param("bearerToken") String bearerToken);

    @RequestLine("GET delegate/content/{processID}?initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    Response clientGetProcessDefinition(@Param("processID") String processID,
                                        @Param("initiatorInstanceId") String initiatorInstanceId,
                                        @Param("targetInstanceId") String targetInstanceId,
                                        @Param("bearerToken") String bearerToken);

    @RequestLine("GET delegate/continue?initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}&gid={gid}")
    @Headers("Authorization: {bearerToken}")
    Response clientContinueProcessInstance(ProcessInstanceInputMessage body,
                                            @Param("gid") String gid,
                                            @Param("targetInstanceId") String targetInstanceId,
                                            @Param("initiatorInstanceId") String initiatorInstanceId,
                                            @Param("bearerToken") String bearerToken);

    @RequestLine("GET delegate/content?initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    Response clientGetProcessDefinitions(@Param("initiatorInstanceId") String initiatorInstanceId,
                                         @Param("targetInstanceId") String targetInstanceId,
                                         @Param("bearerToken") String bearerToken);


    @RequestLine("GET delegate/clauses/{clauseId}?initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    Response clientGetClauseDetails(@Param("clauseId") String clauseId,
                                    @Param("initiatorInstanceId") String initiatorInstanceId,
                                    @Param("targetInstanceId") String targetInstanceId,
                                    @Param("bearerToken") String bearerToken);

    @RequestLine("PUT delegate/clauses/{clauseId}?initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    Response clientUpdateClause(String deserializedClause,
                                @Param("clauseId") String clauseId,
                                @Param("initiatorInstanceId") String initiatorInstanceId,
                                @Param("targetInstanceId") String targetInstanceId,
                                @Param("bearerToken") String bearerToken);


    @RequestLine("GET delegate/contracts?initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}&processInstanceId={processInstanceId}")
    @Headers("Authorization: {bearerToken}")
    Response clientConstructContractForProcessInstances(@Param("processInstanceId") String processInstanceId,
                                                        @Param("initiatorInstanceId") String initiatorInstanceId,
                                                        @Param("targetInstanceId") String targetInstanceId,
                                                        @Param("bearerToken") String bearerToken);


    @RequestLine("GET delegate/contracts/{contractId}/clauses?initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    Response clientGetClausesOfContract(@Param("contractId") String contractId,
                                        @Param("initiatorInstanceId") String initiatorInstanceId,
                                        @Param("targetInstanceId") String targetInstanceId,
                                        @Param("bearerToken") String bearerToken);


    @RequestLine("DELETE delegate/contracts/{contractId}/clauses/{clauseId}?initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    Response clientDeleteClauseFromContract(@Param("contractId") String contractId,
                                            @Param("clauseId") String clauseId,
                                            @Param("initiatorInstanceId") String initiatorInstanceId,
                                            @Param("targetInstanceId") String targetInstanceId,
                                            @Param("bearerToken") String bearerToken);


    @RequestLine("GET delegate/documents/{documentId}/clauses?clauseType={clauseType}&initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    Response clientGetClauseDetails(@Param("documentId") String documentId,
                                    @Param("clauseType") eu.nimble.service.bp.impl.model.ClauseType clauseType,
                                    @Param("initiatorInstanceId") String initiatorInstanceId,
                                    @Param("targetInstanceId") String targetInstanceId,
                                    @Param("bearerToken") String bearerToken);


    @RequestLine("PATCH delegate/documents/{documentId}/contract?clauseDocumentId={clauseDocumentId}&clauseType={clauseType}&initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    Response clientAddDocumentClauseToContract(@Param("documentId") String documentId,
                                               @Param("clauseType") String clauseType,
                                               @Param("clauseDocumentId") String clauseDocumentId,
                                               @Param("initiatorInstanceId") String initiatorInstanceId,
                                               @Param("targetInstanceId") String targetInstanceId,
                                               @Param("bearerToken") String bearerToken);


    @RequestLine("PATCH delegate/documents/{documentId}/contract?initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    Response clientAddDataMonitoringClauseToContract(DataMonitoringClauseType dataMonitoringClause,
                                                     @Param("documentId") String documentId,
                                                     @Param("initiatorInstanceId") String initiatorInstanceId,
                                                     @Param("targetInstanceId") String targetInstanceId,
                                                     @Param("bearerToken") String bearerToken);


    @RequestLine("POST delegate/contracts/create-bundle?orderId={orderId}&initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    Response clientGenerateContract(
            @Param(value = "orderId") String orderId,
            HttpServletResponse response,
            @Param(value = "initiatorInstanceId") String initiatorInstanceId,
            @Param(value = "targetInstanceId") String targetInstanceId,
            @Param(value=" bearerToken") String bearerToken);

    @RequestLine("GET delegate/contracts/create-terms?orderId={orderId}&sellerParty={sellerParty}&buyerParty={buyerParty}&incoterms={incoterms}&tradingTerms={tradingTerms}&initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    Response clientGenerateOrderTermsAndConditionsAsText(
            @Param("orderId") String orderId,
            @Param("sellerParty") String sellerParty,
            @Param("buyerParty") String buyerParty,
            @Param("incoterms") String incoterms,
            @Param("tradingTerms") String tradingTerms,
            @Param("initiatorInstanceId") String initiatorInstanceId,
            @Param("targetInstanceId") String targetInstanceId,
            @Param("bearerToken") String bearerToken);


    @RequestLine("GET delegate/document/json/{documentId}?initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    Response clientGetDocumentJsonContent(
            @Param("documentId") String documentId,
            @Param("initiatorInstanceId") String initiatorInstanceId,
            @Param("targetInstanceId") String targetInstanceId,
            @Param("bearerToken") String bearerToken);


    @RequestLine("GET delegate/document/xml/{documentID}?initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    Response clientGetDocumentXMLContent(
            @Param("documentId") String documentId,
            @Param("initiatorInstanceId") String initiatorInstanceId,
            @Param("targetInstanceId") String targetInstanceId,
            @Param("bearerToken") String bearerToken);


    @RequestLine("GET delegate/document/{partnerID}/{type}?initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    Response clientGetDocuments(
            @Param("partnerID") String partnerID,
            @Param("type") String type,
            @Param("initiatorInstanceId") String initiatorInstanceId,
            @Param("targetInstanceId") String targetInstanceId,
            @Param("bearerToken") String bearerToken);


    @RequestLine("GET delegate/document/{partnerID}/{type}?initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    Response clientGetDocumentXMLContent(
            @Param("partnerID") String partnerID,
            @Param("type") String type,
            @Param("initiatorInstanceId") String initiatorInstanceId,
            @Param("targetInstanceId") String targetInstanceId,
            @Param("bearerToken") String bearerToken);


    @RequestLine("GET delegate/t-t/epc-details?epc={epc}&initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    Response clientGetTTDetails(
            @Param("epc") String epc,
            @Param("initiatorInstanceId") String initiatorInstanceId,
            @Param("targetInstanceId") String targetInstanceId,
            @Param("bearerToken") String bearerToken);


    @RequestLine("GET delegate/t-t/epc-codes?productId={productId}&initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    Response clientGetEPCCodesBelongsToProduct(
            @Param("productId") Long publishedProductID,
            @Param("initiatorInstanceId") String initiatorInstanceId,
            @Param("targetInstanceId") String targetInstanceId,
            @Param("bearerToken") String bearerToken);


    @RequestLine("GET delegate/rest/engine/default/history/variable-instance?processInstanceIdIn={processInstanceIdIn}&initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    Response clientGetProcessDetailsHistory(
            @Param("processInstanceIdIn") String processInstanceIdIn,
            @Param("initiatorInstanceId") String initiatorInstanceId,
            @Param("targetInstanceId") String targetInstanceId,
            @Param("bearerToken") String bearerToken);

    @RequestLine("GET delegate/processInstance/exists?relatedProducts={relatedProducts}&relatedProductCategories={relatedProductCategories}&" +
            "tradingPartnerIDs={tradingPartnerIDs}&initiationDateRange={initiationDateRange}&lastActivityDateRange={lastActivityDateRange}&" +
            "processInstanceId={processInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    Response clientProcessInstanceExists(
            @Param(value = "relatedProducts") List<String> relatedProducts,
            @Param(value = "relatedProductCategories") List<String> relatedProductCategories,
            @Param(value = "tradingPartnerIDs") List<String> tradingPartnerIDs,
            @Param(value = "initiationDateRange") String initiationDateRange,
            @Param(value = "lastActivityDateRange") String lastActivityDateRange,
            @Param(value = "processInstanceId") String processInstanceId,
            @Param(value = "targetInstanceId") String targetInstanceId);

    @RequestLine("GET rest/engine/default/history/variable-instance?processInstanceIdIn={processInstanceIdIn}")
    Response clientGetProcessDetailsHistory(
            @Param("processInstanceIdIn") String processInstanceIdIn);

    @RequestLine("GET delegate/rest/engine/default/history/activity-instance?processInstanceId={processInstanceId}&sortBy={sortBy}&sortOrder={sortOrder}&maxResults={maxResults}&initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    Response clientGetLastActivityForProcessInstance(
            @Param("processInstanceId") String processInstanceId,
            @Param("sortBy") String sortBy,
            @Param("sortOrder") String sortOrder,
            @Param("maxResults") String maxResults,
            @Param("initiatorInstanceId") String initiatorInstanceId,
            @Param("targetInstanceId") String targetInstanceId,
            @Param("bearerToken") String bearerToken);

    @RequestLine("GET rest/engine/default/history/activity-instance?processInstanceId={processInstanceId}&sortBy={sortBy}&sortOrder={sortOrder}&maxResults={maxResults}")
    Response clientGetLastActivityForProcessInstance(
            @Param("processInstanceId") String processInstanceId,
            @Param("sortBy") String sortBy,
            @Param("sortOrder") String sortOrder,
            @Param("maxResults") String maxResults);

    @RequestLine("GET delegate/rest/engine/default/history/process-instance/{processInstanceId}?initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    Response clientGetProcessInstanceDetails(
            @Param("processInstanceId") String processInstanceId,
            @Param("initiatorInstanceId") String initiatorInstanceId,
            @Param("targetInstanceId") String targetInstanceId,
            @Param("bearerToken") String bearerToken);

    @RequestLine("GET rest/engine/default/history/process-instance/{processInstanceId}")
    Response clientGetProcessInstanceDetails(
            @Param("processInstanceId") String processInstanceId);

    @RequestLine("GET delegate/search/fields?initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    Response clientGetFields(
            @Param("initiatorInstanceId") String initiatorInstanceId,
            @Param("targetInstanceId") String targetInstanceId,
            @Param("bearerToken") String bearerToken);


    @RequestLine("GET delegate/search/query?query={query}&facets={facets}&facetQueries={facetQueries}&page={page}&federated={federated}&initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    Response clientSearch(
            @Param("query") String query,
            @Param("facets") List<String> facets,
            @Param("facetQueries") List<String> facetQueries,
            @Param("page") Integer page,
            @Param("federated") Boolean federated,
            @Param("initiatorInstanceId") String initiatorInstanceId,
            @Param("targetInstanceId") String targetInstanceId,
            @Param("bearerToken") String bearerToken);

    @RequestLine("GET delegate/search/suggest?query={query}&wt={wt}facets={facets}&category={category}&federated={federated}&initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    Response clientGetSuggestions(
            @Param("query") String query,
            @Param("wt") String wt,
            @Param("facets") List<String> facets,
            @Param("category") String category,
            @Param("federated") Boolean federated,
            @Param("initiatorInstanceId") String initiatorInstanceId,
            @Param("targetInstanceId") String targetInstanceId,
            @Param("bearerToken") String bearerToken);

    @RequestLine("GET delegate/search/select?id={id}&initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    Response clientSearch(
            @Param("id") String id,
            @Param("initiatorInstanceId") String initiatorInstanceId,
            @Param("targetInstanceId") String targetInstanceId,
            @Param("bearerToken") String bearerToken);

}





