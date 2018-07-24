package eu.nimble.service.bp.impl;


import eu.nimble.service.bp.config.GenericConfig;
import eu.nimble.service.bp.impl.contract.ContractGenerator;
import eu.nimble.service.bp.impl.federation.BusinessProcessClient;
import eu.nimble.service.bp.impl.federation.ClientFactory;
import eu.nimble.service.bp.impl.federation.CoreFunctions;
import eu.nimble.service.bp.swagger.model.Process;
import eu.nimble.service.bp.swagger.model.ProcessDocumentMetadata;
import eu.nimble.service.model.ubl.commonaggregatecomponents.ClauseType;
import eu.nimble.service.model.ubl.commonaggregatecomponents.DataMonitoringClauseType;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@RequestMapping(value = "/delegate")
public class DelegateController {

    //WRAPPERS FOR DELEGATE PROCESSES

@Autowired
private StartController startController;
@Autowired
private ContentController contentController;
@Autowired
private  ContractController contractController;
@Autowired
private ContractGeneratorController contractGeneratorController;
@Autowired
private DocumentController documentController;
@Autowired
private EPCController epcController;
@Autowired
private SearchController searchController;

@Autowired
private GenericConfig config;

@Autowired
private ClientFactory factory;

@Autowired
private CoreFunctions core;


//COMMON TOKEN VALIDATOR
public boolean isValid( String initiatorInstanceId,String bearerToken){

    /*
    TODO:
    CHECK ID AND TOKEN
     */
    return true;
}


public BusinessProcessClient clientGenerator(String instanceid){
    String url=core.getEndpointFromInstanceId(instanceid);
    return factory.createClient(BusinessProcessClient.class,url);
}


//CAMUNDA REST CONTROLLERS

    @RequestMapping(value = "/rest/engine/default/history/variable-instance",
            produces = { "application/json" },
            consumes = { "application/json" },
            method = RequestMethod.GET)
    public  String delegateGetProcessDetailsHistory(
            @RequestParam(value = "processInstanceIdIn", required = true) String processInstanceIdIn,
            @RequestParam(value = "initiatorInstanceId", required = true) String initiatorInstanceId,
            @RequestParam(value = "targetInstanceId", required = true) String targetInstanceId,
            @RequestHeader(value="Authorization", required=true) String bearerToken) {

        if(config.getInstanceid().equals(targetInstanceId))
            return clientGenerator(config.getInstanceid()).clientGetProcessDetailsHistory(processInstanceIdIn,initiatorInstanceId,targetInstanceId,bearerToken);
        else
            return clientGenerator(targetInstanceId).clientGetProcessDetailsHistory(processInstanceIdIn,initiatorInstanceId,targetInstanceId,bearerToken);


    }


    @RequestMapping(value = "/rest/engine/default/history/process-instance/{processInstanceId}",
            produces = { "application/json" },
            consumes = { "application/json" },
            method = RequestMethod.GET)
    public  String delegateGetProcessInstanceDetails(
            @PathVariable(value = "processInstanceId", required = true) String processInstanceId,
            @RequestParam(value = "initiatorInstanceId", required = true) String initiatorInstanceId,
            @RequestParam(value = "targetInstanceId", required = true) String targetInstanceId,
            @RequestHeader(value="Authorization", required=true) String bearerToken) {

        if(config.getInstanceid().equals(targetInstanceId))
            return clientGenerator(config.getInstanceid()).clientGetProcessInstanceDetails(processInstanceId,initiatorInstanceId,targetInstanceId,bearerToken);
        else
            return clientGenerator(targetInstanceId).clientGetProcessInstanceDetails(processInstanceId,initiatorInstanceId,targetInstanceId,bearerToken);

    }

    @RequestMapping(value = "rest/engine/default/history/activity-instance",
            produces = { "application/json" },
            consumes = { "application/json" },
            method = RequestMethod.GET)
    public  String delegateGetLastActivityForProcessInstance(
            @PathVariable(value = "processInstanceId", required = true) String processInstanceId,
            @PathVariable(value = "sortBy", required = true) String sortBy,
            @PathVariable(value = "sortOrder", required = true) String sortOrder,
            @PathVariable(value = "maxResults", required = true) String maxResults,
            @RequestParam(value = "initiatorInstanceId", required = true) String initiatorInstanceId,
            @RequestParam(value = "targetInstanceId", required = true) String targetInstanceId,
            @RequestHeader(value="Authorization", required=true) String bearerToken) {

        if(config.getInstanceid().equals(targetInstanceId))
            return clientGenerator(config.getInstanceid()).clientGetLastActivityForProcessInstance(processInstanceId,sortBy,sortOrder,maxResults,initiatorInstanceId,targetInstanceId,bearerToken);
        else
            return clientGenerator(targetInstanceId).clientGetLastActivityForProcessInstance(processInstanceId,sortBy,sortOrder,maxResults,initiatorInstanceId,targetInstanceId,bearerToken);


    }







    @RequestMapping(value = "/search/fields",
            produces = { "application/json" },
            method = RequestMethod.GET)
    public ResponseEntity delegateGetFields(@RequestParam(value = "initiatorInstanceId", required = true) String initiatorInstanceId,
                                                                @RequestParam(value = "targetInstanceId", required = true) String targetInstanceId,
                                                                @RequestHeader(value="Authorization", required=true) String bearerToken) {


        if(config.getInstanceid().equals(targetInstanceId))
            return searchController.getFields();
        else
            return clientGenerator(targetInstanceId).clientGetFields(initiatorInstanceId,targetInstanceId,bearerToken);

    }

    @RequestMapping(value = "/search/query",
            produces = { "application/json" },
            method = RequestMethod.GET)
    public ResponseEntity delegateSearch(HttpServletRequest request,
                                         @RequestParam(value = "query", required = false) String query,
                                         @RequestParam(value = "facets", required = false) List<String> facets,
                                         @RequestParam(value = "facetQueries", required = false) List<String> facetQueries,
                                         @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                                         @RequestParam(value = "federated", required = false, defaultValue = "false") Boolean federated,
                                         @RequestParam(value = "initiatorInstanceId", required = true) String initiatorInstanceId,
                                         @RequestParam(value = "targetInstanceId", required = true) String targetInstanceId,
                                         @RequestHeader(value="Authorization", required=true) String bearerToken) {


        if(config.getInstanceid().equals(targetInstanceId))
            return searchController.search(request,query,facets,facetQueries,page,federated);
        else
            return clientGenerator(targetInstanceId).clientSearch(query,facets,facetQueries,page,federated,initiatorInstanceId,targetInstanceId,bearerToken);

    }





    @RequestMapping(value = "/search/retrieve",
            produces = { "application/json" },
            method = RequestMethod.GET)
    public ResponseEntity delegateSearch(@RequestParam(value = "id", required = false) String id,
                                            @RequestParam(value = "initiatorInstanceId", required = true) String initiatorInstanceId,
                                            @RequestParam(value = "targetInstanceId", required = true) String targetInstanceId,
                                            @RequestHeader(value="Authorization", required=true) String bearerToken) {


        if(config.getInstanceid().equals(targetInstanceId))
            return searchController.search(id);
        else
            return clientGenerator(targetInstanceId).clientSearch(id,initiatorInstanceId,targetInstanceId,bearerToken);

    }

//STARTCONTROLLER

@RequestMapping(value = "/start",
                produces = { "application/json" },
                consumes = { "application/json" },
                method = RequestMethod.POST)
public ResponseEntity<eu.nimble.service.bp.swagger.model.ProcessInstance> delegateStartProcessInstance( @RequestBody eu.nimble.service.bp.swagger.model.ProcessInstanceInputMessage body,
                                                                                                            @RequestParam(value = "gid", required = false) String gid,
                                                                                                            @RequestParam(value = "precedingPid", required = false) String precedingPid,
                                                                                                            @RequestParam(value = "initiatorInstanceId", required = true) String initiatorInstanceId,
                                                                                                            @RequestParam(value = "targetInstanceId", required = true) String targetInstanceId,
                                                                                                            @RequestHeader(value="Authorization", required=true) String bearerToken) {

    /*
    TODO:
    1)Check if origin of order is from own instance or not.
    2)If instance is foreign then validate the bearer token from main core or validate it from it's own instance.
    3)Else if the instance is local then check token locally.
    */

    if(config.getInstanceid().equals(targetInstanceId))
        return startController.startProcessInstance(body,bearerToken, gid, precedingPid);
    else
        return clientGenerator(targetInstanceId).clientStartProcessInstance(body,gid,precedingPid,initiatorInstanceId,targetInstanceId,bearerToken);


}


//CONTENTCONTROLLER
    @RequestMapping(value = "/content/{processID}",
                    produces = { "application/json" },
                    method = RequestMethod.GET)
    ResponseEntity<Process> delegateGetProcessDefinition(@PathVariable("processID") String processID,
                                                         @RequestParam(value = "initiatorInstanceId", required = true) String initiatorInstanceId,
                                                         @RequestParam(value = "targetInstanceId", required = true) String targetInstanceId,
                                                         @RequestHeader(value="Authorization", required=true) String bearerToken){


        if(config.getInstanceid().equals(targetInstanceId))
            return contentController.getProcessDefinition(processID,bearerToken);
        else
            return clientGenerator(targetInstanceId).clientGetProcessDefinition(processID,initiatorInstanceId,targetInstanceId,bearerToken);


    }


    @RequestMapping(value = "/content",
                    produces = { "application/json" },
                    method = RequestMethod.GET)
    ResponseEntity<List<Process>> delegateGetProcessDefinitions(@RequestParam(value = "initiatorInstanceId", required = true) String initiatorInstanceId,
                                                                @RequestParam(value = "targetInstanceId", required = true) String targetInstanceId,
                                                                @RequestHeader(value="Authorization", required=true) String bearerToken){

        if(config.getInstanceid().equals(targetInstanceId))
            return contentController.getProcessDefinitions(bearerToken);
        else
            return clientGenerator(targetInstanceId).clientGetProcessDefinitions(initiatorInstanceId,targetInstanceId,bearerToken);



    }

//CONTRACTCONTROLLER
    @RequestMapping(value = "/clauses/{clauseId}",
                    produces = {"application/json"},
                    method = RequestMethod.GET)
    public ResponseEntity delegateGetClauseDetails(@PathVariable(value = "clauseId", required = true) String clauseId,
                                                   @RequestParam(value = "initiatorInstanceId", required = true) String initiatorInstanceId,
                                                   @RequestParam(value = "targetInstanceId", required = true) String targetInstanceId,
                                                   @RequestHeader(value="Authorization", required=true) String bearerToken) {


        if(config.getInstanceid().equals(targetInstanceId))
            return contractController.getClauseDetails(clauseId,bearerToken);
        else
            return clientGenerator(targetInstanceId).clientGetClauseDetails(clauseId,initiatorInstanceId,targetInstanceId,bearerToken);


    }


    @RequestMapping(value = "/clauses/{clauseId}",
                    method = RequestMethod.PUT)
    public ResponseEntity delegateUpdateClause(@RequestBody() String deserializedClause,
                                               @PathVariable(value = "clauseId") String clauseId,
                                               @RequestParam(value = "initiatorInstanceId", required = true) String initiatorInstanceId,
                                               @RequestParam(value = "targetInstanceId", required = true) String targetInstanceId,
                                               @RequestHeader(value="Authorization", required=true) String bearerToken) {



        if(config.getInstanceid().equals(targetInstanceId))
            return contractController.updateClause(clauseId,deserializedClause,bearerToken);
        else
            return clientGenerator(targetInstanceId).clientUpdateClause(clauseId,deserializedClause,initiatorInstanceId,targetInstanceId,bearerToken);

    }


    @RequestMapping(value = "/contracts",
                    method = RequestMethod.GET)
    public ResponseEntity delegateConstructContractForProcessInstances(@RequestParam(value = "processInstanceId") String processInstanceId,
                                                                       @RequestParam(value = "initiatorInstanceId", required = true) String initiatorInstanceId,
                                                                       @RequestParam(value = "targetInstanceId", required = true) String targetInstanceId,
                                                                       @RequestHeader(value="Authorization", required=true) String bearerToken) {



        if(config.getInstanceid().equals(targetInstanceId))
            return contractController.constructContractForProcessInstances(processInstanceId,bearerToken);
        else
            return clientGenerator(targetInstanceId).clientConstructContractForProcessInstances(processInstanceId,initiatorInstanceId,targetInstanceId,bearerToken);

    }

    @RequestMapping(value = "/contracts/{contractId}/clauses",
            produces = {"application/json"},
            method = RequestMethod.GET)
    public ResponseEntity delegateGetClausesOfContract(@PathVariable(value = "contractId") String contractId,
                                                       @RequestParam(value = "initiatorInstanceId", required = true) String initiatorInstanceId,
                                                       @RequestParam(value = "targetInstanceId", required = true) String targetInstanceId,
                                                       @RequestHeader(value="Authorization", required=true) String bearerToken) {

        if(config.getInstanceid().equals(targetInstanceId))
            return contractController.getClausesOfContract(contractId,bearerToken);
        else
            return clientGenerator(targetInstanceId).clientGetClausesOfContract(contractId,initiatorInstanceId,targetInstanceId,bearerToken);

    }

    @RequestMapping(value = "/contracts/{contractId}/clauses/{clauseId}",
            method = RequestMethod.DELETE)
    public ResponseEntity delegateDeleteClauseFromContract(@PathVariable(value = "contractId") String contractId,
                                                           @PathVariable(value = "clauseId") String clauseId,
                                                           @RequestParam(value = "initiatorInstanceId", required = true) String initiatorInstanceId,
                                                           @RequestParam(value = "targetInstanceId", required = true) String targetInstanceId,
                                                           @RequestHeader(value="Authorization", required=true) String bearerToken) {
        if(config.getInstanceid().equals(targetInstanceId))
            return contractController.deleteClauseFromContract(contractId,clauseId,bearerToken);
        else
            return clientGenerator(targetInstanceId).clientDeleteClauseFromContract(contractId,clauseId,initiatorInstanceId,targetInstanceId,bearerToken);

    }

    @RequestMapping(value = "/documents/{documentId}/clauses",
            produces = {"application/json"},
            method = RequestMethod.GET)
    public ResponseEntity<ClauseType> delegateGetClauseDetails(@PathVariable(value = "documentId", required = true) String documentId,
                                                               @RequestParam(value = "clauseType", required = true) eu.nimble.service.bp.impl.model.ClauseType clauseType,
                                                               @RequestParam(value = "initiatorInstanceId", required = true) String initiatorInstanceId,
                                                               @RequestParam(value = "targetInstanceId", required = true) String targetInstanceId,
                                                               @RequestHeader(value="Authorization", required=true) String bearerToken) {

        if(config.getInstanceid().equals(targetInstanceId))
            return contractController.getClauseDetails(documentId,clauseType,bearerToken);
        else
            return clientGenerator(targetInstanceId).clientGetClauseDetails(documentId,clauseType,initiatorInstanceId,targetInstanceId,bearerToken);

    }


    @RequestMapping(value = "/documents/{documentId}/contract",
            produces = {"application/json"},
            method = RequestMethod.PATCH)
    public ResponseEntity delegateAddDocumentClauseToContract(@PathVariable(value = "documentId") String documentId,
                                                              @RequestParam(value = "clauseType") String clauseType,
                                                              @RequestParam(value = "clauseDocumentId") String clauseDocumentId,
                                                              @RequestParam(value = "initiatorInstanceId", required = true) String initiatorInstanceId,
                                                              @RequestParam(value = "targetInstanceId", required = true) String targetInstanceId,
                                                              @RequestHeader(value="Authorization", required=true) String bearerToken) {


        if(config.getInstanceid().equals(targetInstanceId))
            return contractController.addDocumentClauseToContract(documentId,clauseType,clauseDocumentId,bearerToken);
        else
            return clientGenerator(targetInstanceId).clientAddDocumentClauseToContract(documentId,clauseType,clauseDocumentId,initiatorInstanceId,targetInstanceId,bearerToken);

    }



    @RequestMapping(value = "/documents/{documentId}/contract",
            consumes = {"application/json"},
            produces = {"application/json"},
            method = RequestMethod.PATCH)
    public ResponseEntity addDataMonitoringClauseToContract(@PathVariable(value = "documentId") String documentId,
                                                            @RequestBody() DataMonitoringClauseType dataMonitoringClause,
                                                            @RequestParam(value = "initiatorInstanceId", required = true) String initiatorInstanceId,
                                                            @RequestParam(value = "targetInstanceId", required = true) String targetInstanceId,
                                                            @RequestHeader(value="Authorization", required=true) String bearerToken) {


        if(config.getInstanceid().equals(targetInstanceId))
            return contractController.addDataMonitoringClauseToContract(documentId,dataMonitoringClause,bearerToken);
        else
            return clientGenerator(targetInstanceId).clientAddDataMonitoringClauseToContract(dataMonitoringClause,documentId,initiatorInstanceId,targetInstanceId,bearerToken);

    }


    //CONTRACTGENERATOR
    @RequestMapping(value = "/contracts/create-bundle",
                    method = RequestMethod.GET,
                    produces = {"application/zip"})
    public void delegateGenerateContract(@RequestParam(value = "orderId", required = true) String orderId,
                                         HttpServletResponse response,
                                         @RequestParam(value = "initiatorInstanceId", required = true) String initiatorInstanceId,
                                         @RequestParam(value = "targetInstanceId", required = true) String targetInstanceId,
                                         @RequestHeader(value="Authorization", required=true) String bearerToken){


     contractGeneratorController.generateContract(orderId,response,bearerToken);


    }



    @RequestMapping(value = "/contracts/create-terms",
            produces = {MediaType.TEXT_PLAIN_VALUE},
            method = RequestMethod.GET)
    public ResponseEntity delegateGenerateOrderTermsAndConditionsAsText(@RequestParam(value = "orderId", required = true) String orderId,
                                                                @RequestParam(value = "sellerParty", required = true) String sellerParty,
                                                                @RequestParam(value = "buyerParty", required = true) String buyerParty,
                                                                @RequestParam(value = "incoterms", required = true) String incoterms,
                                                                @RequestParam(value = "tradingTerms", required = true) String tradingTerms,
                                                                @RequestParam(value = "initiatorInstanceId", required = true) String initiatorInstanceId,
                                                                        @RequestParam(value = "targetInstanceId", required = true) String targetInstanceId,
                                                                        @RequestHeader(value="Authorization", required=true) String bearerToken){



        if(config.getInstanceid().equals(targetInstanceId))
            return contractGeneratorController.generateOrderTermsAndConditionsAsText(orderId,sellerParty,buyerParty,incoterms,tradingTerms,bearerToken);
        else
            return clientGenerator(targetInstanceId).clientGenerateOrderTermsAndConditionsAsText(orderId,sellerParty,buyerParty,incoterms,tradingTerms,initiatorInstanceId,targetInstanceId,bearerToken);


    }

//DOCUMENT CONTROLLER
    @RequestMapping(value = "/document/json/{documentID}",
            produces = {"application/json"},
            method = RequestMethod.GET)
    ResponseEntity<Object> delegateGetDocumentJsonContent(@PathVariable("documentID") String documentID,
                                                          @RequestParam(value = "initiatorInstanceId", required = true) String initiatorInstanceId,
                                                          @RequestParam(value = "targetInstanceId", required = true) String targetInstanceId,
                                                          @RequestHeader(value="Authorization", required=true) String bearerToken) {



        if(config.getInstanceid().equals(targetInstanceId))
            return documentController.getDocumentJsonContent(documentID,bearerToken);
        else
            return clientGenerator(targetInstanceId).clientGetDocumentJsonContent(documentID,initiatorInstanceId,targetInstanceId,bearerToken);


    }


    @RequestMapping(value = "/document/xml/{documentID}",
            produces = {"application/json"},
            method = RequestMethod.GET)
    public ResponseEntity<String> delegateGetDocumentXMLContent(@PathVariable("documentID") String documentID,
                                                                @RequestParam(value = "initiatorInstanceId", required = true) String initiatorInstanceId,
                                                                @RequestParam(value = "targetInstanceId", required = true) String targetInstanceId,
                                                                @RequestHeader(value="Authorization", required=true) String bearerToken) {

        if(config.getInstanceid().equals(targetInstanceId))
            return documentController.getDocumentXMLContent(documentID,bearerToken);
        else
            return clientGenerator(targetInstanceId).clientGetDocumentXMLContent(documentID,initiatorInstanceId,targetInstanceId,bearerToken);


    }



    @RequestMapping(value = "/document/{partnerID}/{type}",
            produces = { "application/json" },
            method = RequestMethod.GET)
    public ResponseEntity<List<ProcessDocumentMetadata>> delegateGetDocuments(@PathVariable("partnerID") String partnerID,
                                                                              @PathVariable("type") String type,
                                                                              @RequestParam(value = "initiatorInstanceId", required = true) String initiatorInstanceId,
                                                                              @RequestParam(value = "targetInstanceId", required = true) String targetInstanceId,
                                                                              @RequestHeader(value="Authorization", required=true) String bearerToken) {


        if(config.getInstanceid().equals(targetInstanceId))
            return documentController.getDocuments(partnerID,type,bearerToken);
        else
            return clientGenerator(targetInstanceId).clientGetDocuments(partnerID,type,initiatorInstanceId,targetInstanceId,bearerToken);

    }

//EPC CONTROLLER

    @RequestMapping(value = "/t-t/epc-details",
            produces = {"application/json"},
            method = RequestMethod.GET)
    public ResponseEntity delegateGetTTDetails(@RequestParam(value = "epc", required = true) String epc,
                                       @RequestParam(value = "initiatorInstanceId", required = true) String initiatorInstanceId,
                                       @RequestParam(value = "targetInstanceId", required = true) String targetInstanceId,
                                       @RequestHeader(value="Authorization", required=true) String bearerToken
) {


        if(config.getInstanceid().equals(targetInstanceId))
            return epcController.getTTDetails(epc,bearerToken);
        else
            return clientGenerator(targetInstanceId).clientGetTTDetails(epc,initiatorInstanceId,targetInstanceId,bearerToken);


    }

    @RequestMapping(value = "/t-t/epc-codes",
                    produces = {"application/json"},
                    method = RequestMethod.GET)
    public ResponseEntity delegateGetEPCCodesBelongsToProduct( @RequestParam(value = "productId", required = true) Long publishedProductID,
                                                       @RequestParam(value = "initiatorInstanceId", required = true) String initiatorInstanceId,
                                                       @RequestParam(value = "targetInstanceId", required = true) String targetInstanceId,
                                                       @RequestHeader(value="Authorization", required=true) String bearerToken){


        if(config.getInstanceid().equals(targetInstanceId))
            return epcController.getEPCCodesBelongsToProduct(publishedProductID,bearerToken);
        else
            return clientGenerator(targetInstanceId).clientGetEPCCodesBelongsToProduct(publishedProductID,initiatorInstanceId,targetInstanceId,bearerToken);


    }




}









