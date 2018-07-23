package eu.nimble.service.bp.impl.federation;

import eu.nimble.service.bp.swagger.model.ProcessDocumentMetadata;
import eu.nimble.service.model.ubl.commonaggregatecomponents.ClauseType;
import eu.nimble.service.model.ubl.commonaggregatecomponents.DataMonitoringClauseType;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


@Headers("Content-Type: application/json")
public interface BusinessProcessClient {
    @RequestLine("POST delegate/start?gid={gid}&precedingPid={precedingPid}&initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    public ResponseEntity<eu.nimble.service.bp.swagger.model.ProcessInstance> clientStartProcessInstance(eu.nimble.service.bp.swagger.model.ProcessInstanceInputMessage body,
                                                                                                           @Param("gid") String gid,
                                                                                                           @Param("precedingPid") String precedingPid,
                                                                                                           @Param("initiatorInstanceId") String initiatorInstanceId,
                                                                                                           @Param("targetInstanceId") String targetInstanceId,
                                                                                                           @Param("bearerToken") String bearerToken) ;


    @RequestLine("GET delegate/content/{processID}?initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    ResponseEntity<eu.nimble.service.bp.swagger.model.Process> clientGetProcessDefinition(@Param("processID") String processID,
                                                                                            @Param("initiatorInstanceId") String initiatorInstanceId,
                                                                                            @Param("targetInstanceId") String targetInstanceId,
                                                                                            @Param("Authorization") String bearerToken);




    @RequestLine("GET delegate/content?initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    ResponseEntity<List<eu.nimble.service.bp.swagger.model.Process>> clientGetProcessDefinitions(@Param("initiatorInstanceId") String initiatorInstanceId,
                                                                                                   @Param("targetInstanceId") String targetInstanceId,
                                                                                                   @Param("Authorization") String bearerToken);



    @RequestLine("GET delegate/clauses/{clauseId}?initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    ResponseEntity clientGetClauseDetails(@Param("clauseId") String clauseId,
                                                                                            @Param("initiatorInstanceId") String initiatorInstanceId,
                                                                                            @Param("targetInstanceId") String targetInstanceId,
                                                                                            @Param("Authorization") String bearerToken);

    @RequestLine("PUT delegate/clauses/{clauseId}?initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    ResponseEntity clientUpdateClause(String deserializedClause,
                                                                                    @Param("clauseId") String clauseId,
                                                                                    @Param("initiatorInstanceId") String initiatorInstanceId,
                                                                                    @Param("targetInstanceId") String targetInstanceId,
                                                                                    @Param("Authorization") String bearerToken);


    @RequestLine("GET delegate/contracts?initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}&processInstanceId={processInstanceId}")
    @Headers("Authorization: {bearerToken}")
    ResponseEntity<eu.nimble.service.bp.swagger.model.Process> clientConstructContractForProcessInstances(@Param("processInstanceId") String processInstanceId,
                                                                                                          @Param("initiatorInstanceId") String initiatorInstanceId,
                                                                                                            @Param("targetInstanceId") String targetInstanceId,
                                                                                                            @Param("Authorization") String bearerToken);


    @RequestLine("GET delegate/contracts/{contractId}/clauses?initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    ResponseEntity clientGetClausesOfContract(@Param("contractId") String contractId,
                                                                                            @Param("initiatorInstanceId") String initiatorInstanceId,
                                                                                            @Param("targetInstanceId") String targetInstanceId,
                                                                                            @Param("Authorization") String bearerToken);





    @RequestLine("DELETE delegate/contracts/{contractId}/clauses/{clauseId}?initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    ResponseEntity clientDeleteClauseFromContract(@Param("contractId") String contractId,
                                                                                                @Param("clauseId") String clauseId,
                                                                                                 @Param("initiatorInstanceId") String initiatorInstanceId,
                                                                                                @Param("targetInstanceId") String targetInstanceId,
                                                                                                @Param("Authorization") String bearerToken);


    @RequestLine("GET delegate/documents/{documentId}/clauses?clauseType={clauseType}&initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    ResponseEntity<ClauseType> clientGetClauseDetails(@Param("documentId") String documentId,
                                                                                                @Param("clauseType") eu.nimble.service.bp.impl.model.ClauseType clauseType,
                                                                                                @Param("initiatorInstanceId") String initiatorInstanceId,
                                                                                                @Param("targetInstanceId") String targetInstanceId,
                                                                                                @Param("Authorization") String bearerToken);







    @RequestLine("PATCH delegate/documents/{documentId}/contract?clauseDocumentId={clauseDocumentId}&clauseType={clauseType}&initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    ResponseEntity clientAddDocumentClauseToContract(@Param("documentId") String documentId,
                                                                                                   @Param("clauseType") String clauseType,
                                                                                                   @Param("clauseDocumentId") String clauseDocumentId,
                                                                                                   @Param("initiatorInstanceId") String initiatorInstanceId,
                                                                                                   @Param("targetInstanceId") String targetInstanceId,
                                                                                                   @Param("Authorization") String bearerToken);





    @RequestLine("PATCH delegate/documents/{documentId}/contract?initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    ResponseEntity clientAddDataMonitoringClauseToContract(DataMonitoringClauseType dataMonitoringClause,
                                                                                                            @Param("documentId") String documentId,
                                                                                                            @Param("initiatorInstanceId") String initiatorInstanceId,
                                                                                                            @Param("targetInstanceId") String targetInstanceId,
                                                                                                            @Param("Authorization") String bearerToken);



    @RequestLine("GET delegate/contracts/create-terms?orderId={orderId}&sellerParty={sellerParty}&buyerParty={buyerParty}&incoterms={incoterms}&tradingTerms={tradingTerms}&initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    ResponseEntity clientGenerateOrderTermsAndConditionsAsText(
                                                                @Param("orderId") String orderId,
                                                                @Param("sellerParty") String sellerParty,
                                                                @Param("buyerParty") String buyerParty,
                                                                @Param("incoterms") String incoterms,
                                                                @Param("tradingTerms") String tradingTerms,
                                                                @Param("initiatorInstanceId") String initiatorInstanceId,
                                                                @Param("targetInstanceId") String targetInstanceId,
                                                                @Param("Authorization") String bearerToken);




    @RequestLine("GET delegate/document/json/{documentID}?initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    ResponseEntity<Object> clientGetDocumentJsonContent(
                                                             @Param("documentId") String documentId,
                                                             @Param("initiatorInstanceId") String initiatorInstanceId,
                                                             @Param("targetInstanceId") String targetInstanceId,
                                                             @Param("Authorization") String bearerToken);



    @RequestLine("GET delegate/document/xml/{documentID}?initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    ResponseEntity<String> clientGetDocumentXMLContent(
                                                            @Param("documentId") String documentId,
                                                            @Param("initiatorInstanceId") String initiatorInstanceId,
                                                            @Param("targetInstanceId") String targetInstanceId,
                                                            @Param("Authorization") String bearerToken);


    @RequestLine("GET delegate/document/{partnerID}/{type}?initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    ResponseEntity<List<ProcessDocumentMetadata>> clientGetDocuments(
            @Param("partnerID") String partnerID,
            @Param("type") String type,
            @Param("initiatorInstanceId") String initiatorInstanceId,
            @Param("targetInstanceId") String targetInstanceId,
            @Param("Authorization") String bearerToken);




    @RequestLine("GET delegate/document/{partnerID}/{type}?initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    ResponseEntity<List<eu.nimble.service.bp.swagger.model.ProcessDocumentMetadata>> clientGetDocumentXMLContent(
                                                                                                                @Param("partnerID") String partnerID,
                                                                                                                @Param("type") String type,
                                                                                                                @Param("initiatorInstanceId") String initiatorInstanceId,
                                                                                                                @Param("targetInstanceId") String targetInstanceId,
                                                                                                                @Param("Authorization") String bearerToken);








    @RequestLine("GET delegate/t-t/epc-details?epc={epc}&initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    ResponseEntity clientGetTTDetails(
                                    @Param("epc") String epc,
                                    @Param("initiatorInstanceId") String initiatorInstanceId,
                                    @Param("targetInstanceId") String targetInstanceId,
                                    @Param("Authorization") String bearerToken);



    @RequestLine("GET delegate/t-t/epc-codes?productId={productId}&initiatorInstanceId={initiatorInstanceId}&targetInstanceId={targetInstanceId}")
    @Headers("Authorization: {bearerToken}")
    ResponseEntity clientGetEPCCodesBelongsToProduct(
                                                @Param("productId") Long publishedProductID,
                                                @Param("initiatorInstanceId") String initiatorInstanceId,
                                                @Param("targetInstanceId") String targetInstanceId,
                                                @Param("Authorization") String bearerToken);




    @RequestLine("GET delegate/rest/engine/default/history/variable-instance?processInstanceIdIn={processInstanceIdIn}")
    @Headers("Authorization: {bearerToken}")
    String clientGetProcessDetailsHistory(
            @Param("processInstanceIdIn") String processInstanceIdIn,
            @Param("initiatorInstanceId") String initiatorInstanceId,
            @Param("targetInstanceId") String targetInstanceId,
            @Param("Authorization") String bearerToken);

    @RequestLine("GET delegate/rest/engine/default/history/activity-instance?processInstanceId={processInstanceId}&sortBy={sortBy}&sortOrder={sortOrder}&maxResults={maxResults}")
    @Headers("Authorization: {bearerToken}")
    String clientGetLastActivityForProcessInstance(
            @Param("processInstanceId") String processInstanceId,
            @Param("sortBy") String sortBy,
            @Param("sortOrder") String sortOrder,
            @Param("maxResults") String maxResults,
            @Param("initiatorInstanceId") String initiatorInstanceId,
            @Param("targetInstanceId") String targetInstanceId,
            @Param("Authorization") String bearerToken);

    @RequestLine("GET delegate/rest/engine/default/history/process-instance/{processInstanceId}")
    @Headers("Authorization: {bearerToken}")
    String clientGetProcessInstanceDetails(
            @Param("processInstanceId") String processInstanceId,
            @Param("initiatorInstanceId") String initiatorInstanceId,
            @Param("targetInstanceId") String targetInstanceId,
            @Param("Authorization") String bearerToken);

}





