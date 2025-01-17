package eu.nimble.service.bp.impl;

import eu.nimble.service.bp.hyperjaxb.model.ProcessDocumentMetadataDAO;
import eu.nimble.service.bp.impl.util.camunda.CamundaEngine;
import eu.nimble.service.bp.impl.util.persistence.bp.*;
import eu.nimble.service.bp.impl.util.persistence.catalogue.DocumentPersistenceUtility;
import eu.nimble.service.bp.impl.util.persistence.catalogue.TrustPersistenceUtility;
import eu.nimble.service.bp.impl.util.spring.SpringBridge;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import springfox.documentation.annotations.ApiIgnore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ApiIgnore
@Controller
public class TransactionsController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @ApiOperation(value = "",notes = "Deletes the transactions belonging to the given party. Request/Response documents,document metadatas, process instances, ratings" +
            ",reviews and collaboration groups are deleted.Moreover, it deletes the transactions of other parties which involve in a deleted transaction recursively.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Deleted transactions successfully for the given party"),
            @ApiResponse(code = 401, message = "Invalid token. No user was found for the provided token"),
            @ApiResponse(code = 500, message = "Unexpected error while deleting transactions for the given party")
    })
    @RequestMapping(value = "/transactions/{partyId}",
            method = RequestMethod.DELETE)
    public ResponseEntity deleteTransactions(@ApiParam(value = "Identifier of the party for which the transactions to be deleted", required = true) @PathVariable(value = "partyId", required = true) String partyId,
                                             @ApiParam(value = "The Bearer token provided by the identity service", required = true) @RequestHeader(value = "Authorization", required = true) String bearerToken) {
        logger.debug("Incoming request to delete transactions for party: {}", partyId);

        // check token
        boolean isValid = false;
        try {
            isValid = SpringBridge.getInstance().getiIdentityClientTyped().getUserInfo(bearerToken);
            if(!isValid){
                String msg = String.format("No user exists for the given token : %s",bearerToken);
                logger.error(msg);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (Exception e) {
            String msg = "Unexpected error while deleting transactions for the party: %s";
            logger.error(String.format(msg, partyId),e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(String.format(msg, partyId));
        }

        deleteTransactionsForParty(partyId);
        logger.debug("Deleted transactions successfully for party: {}", partyId);
        return ResponseEntity.ok(null);
    }

    private void deleteTransactionsForParty(String partyId) {
        logger.debug("Deleting transactions for party : {}", partyId);

        // get process document metadatas
        List<ProcessDocumentMetadataDAO> processDocumentMetadataDAOs = ProcessDocumentMetadataDAOUtility.findByPartyID(partyId);
        // extract party ids from process document metadatas
        List<String> partyIds = new ArrayList<>();
        // extract document ids from process document metadatas
        List<String> documentIds = new ArrayList<>();
        // extract process instance ids from process document metadatas
        Set<String> processInstanceIds = new HashSet<>();
        for (ProcessDocumentMetadataDAO processDocumentMetadataDAO : processDocumentMetadataDAOs) {
            String initiatorId = processDocumentMetadataDAO.getInitiatorID();
            String responderId = processDocumentMetadataDAO.getResponderID();

            if (!partyIds.contains(initiatorId)) {
                partyIds.add(initiatorId);
            }
            if (!partyIds.contains(responderId)) {
                partyIds.add(responderId);
            }

            documentIds.add(processDocumentMetadataDAO.getDocumentID());
            processInstanceIds.add(processDocumentMetadataDAO.getProcessInstanceID());
        }

        if (partyIds.size() != 0) {
            // get process instance group ids
            List<String> processInstanceGroupIds = ProcessInstanceGroupDAOUtility.getProcessInstanceGroupIdsForParty(partyIds);
            if(processInstanceGroupIds.size() > 0){
                // get collaboration group hjids
                List<Long> collaborationGroupHjids = CollaborationGroupDAOUtility.getCollaborationGroupHjidOfProcessInstanceGroups(processInstanceGroupIds);
                // delete collaboration groups (it deletes process instance groups as well)
                CollaborationGroupDAOUtility.deleteCollaborationGroupDAOsByID(collaborationGroupHjids);
            }
            // delete process document metadatas/documents
            DocumentPersistenceUtility.deleteDocumentsWithMetadatas(documentIds);
            // delete camunda related stuff
            CamundaEngine.deleteProcessInstances(processInstanceIds);
            // delete process instance daos
            ProcessInstanceDAOUtility.deleteByIds(new ArrayList<>(processInstanceIds));
            // delete process instance input message and process variables
            ProcessInstanceInputMessageDAOUtility.deleteProcessInstanceInputMessageDAOAndProcessVariablesByPartyId(partyId);
            // delete completed tasks
            TrustPersistenceUtility.deleteCompletedTasks(new ArrayList<>(processInstanceIds));

            partyIds.remove(partyId);
            for (String id : partyIds) {
                deleteTransactionsForParty(id);
            }
        }

        logger.debug("Deleted transactions for party: {}", partyId);
    }
}