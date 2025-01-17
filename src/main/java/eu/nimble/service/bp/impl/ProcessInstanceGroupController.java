package eu.nimble.service.bp.impl;

import eu.nimble.service.bp.hyperjaxb.model.*;
import eu.nimble.service.bp.impl.util.email.EmailSenderUtil;
import eu.nimble.service.bp.impl.util.persistence.bp.*;
import eu.nimble.service.bp.impl.util.persistence.catalogue.TrustPersistenceUtility;
import eu.nimble.service.bp.impl.util.spring.SpringBridge;
import eu.nimble.service.bp.swagger.api.ProcessInstanceGroupsApi;
import eu.nimble.service.bp.swagger.model.ProcessInstanceGroup;
import eu.nimble.service.bp.swagger.model.ProcessInstanceGroupFilter;
import eu.nimble.service.model.ubl.order.OrderType;
import eu.nimble.utility.HttpResponseUtil;
import eu.nimble.utility.persistence.GenericJPARepository;
import eu.nimble.utility.persistence.JPARepositoryFactory;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.HEAD;
import java.io.IOException;
import java.util.List;

/**
 * Created by suat on 06-Feb-18.
 */
@Controller
public class ProcessInstanceGroupController implements ProcessInstanceGroupsApi {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ProcessInstanceController processInstanceController;
    @Autowired
    private DocumentController documentController;
    @Autowired
    private EmailSenderUtil emailSenderUtil;
    @Autowired
    private JPARepositoryFactory repoFactory;

    @Override
    @ApiOperation(value = "", notes = "Deletes the process instance group")
    public ResponseEntity<Void> deleteProcessInstanceGroup(@ApiParam(value = "Identifier of the ProcessInstanceGroup to be deleted (processInstanceGroup.id)", required = true) @PathVariable("id") String id,
                                                           @ApiParam(value = "The Bearer token provided by the identity service" ,required=true ) @RequestHeader(value="Authorization", required=true) String bearerToken) {
        logger.debug("Deleting ProcessInstanceGroup ID: {}", id);
        // check token
        ResponseEntity tokenCheck = eu.nimble.service.bp.impl.util.HttpResponseUtil.checkToken(bearerToken);
        if (tokenCheck != null) {
            return tokenCheck;
        }

        ProcessInstanceGroupDAOUtility.deleteProcessInstanceGroupDAOByID(id);

        ResponseEntity response = ResponseEntity.status(HttpStatus.OK).body("true");
        logger.debug("Deleted ProcessInstanceGroups: {}", id);
        return response;
    }

    @Override
    @ApiOperation(value = "", notes = "Retrieves the process instance group specified with the ID")
    public ResponseEntity<ProcessInstanceGroup> getProcessInstanceGroup(@ApiParam(value = "Identifier of the ProcessInstanceGroup to be received (processInstanceGroup.id)", required = true) @PathVariable("id") String id,
                                                                        @ApiParam(value = "The Bearer token provided by the identity service" ,required=true ) @RequestHeader(value="Authorization", required=true) String bearerToken) {
        logger.debug("Getting ProcessInstanceGroup: {}", id);
        // check token
        ResponseEntity tokenCheck = eu.nimble.service.bp.impl.util.HttpResponseUtil.checkToken(bearerToken);
        if (tokenCheck != null) {
            return tokenCheck;
        }

        ProcessInstanceGroupDAO processInstanceGroupDAO = ProcessInstanceGroupDAOUtility.getProcessInstanceGroupDAO(id);

        ProcessInstanceGroup processInstanceGroup = HibernateSwaggerObjectMapper.convertProcessInstanceGroupDAO(processInstanceGroupDAO);
        ResponseEntity response = ResponseEntity.status(HttpStatus.OK).body(processInstanceGroup);
        logger.debug("Retrieved ProcessInstanceGroup: {}", id);
        return response;
    }

    @Override
    @ApiOperation(value = "", notes = "Generates detailed filtering criteria for the current query parameters in place." +
            " The filter can be used to restrict the set of groups for easy discovery.")
    public ResponseEntity<ProcessInstanceGroupFilter> getProcessInstanceGroupFilters(
            @ApiParam(value = "The Bearer token provided by the identity service", required = true) @RequestHeader(value = "Authorization", required = true) String bearerToken,
            @ApiParam(value = "Identifier of the party as specified by the identity service") @RequestParam(value = "partyId", required = false) String partyId,
            @ApiParam(value = "Names of the products for which the collaboration activities are performed") @RequestParam(value = "relatedProducts", required = false) List<String> relatedProducts,
            @ApiParam(value = "Categories of the products.<br>For example:MDF raw,Split air conditioner") @RequestParam(value = "relatedProductCategories", required = false) List<String> relatedProductCategories,
            @ApiParam(value = "Identifier (party id) of the corresponding trading partners") @RequestParam(value = "tradingPartnerIDs", required = false) List<String> tradingPartnerIDs,
            @ApiParam(value = "Whether the collaboration group is archived or not", defaultValue = "false") @RequestParam(value = "archived", required = false, defaultValue = "false") Boolean archived,
            @ApiParam(value = "Role of the party in the collaboration.<br>Possible values: <ul><li>SELLER</li><li>BUYER</li></ul>") @RequestParam(value = "collaborationRole", required = false) String collaborationRole,
            @ApiParam(value = "Status of the process instance included in the group.<br>Possible values: <ul><li>STARTED</li><li>WAITING</li><li>CANCELLED</li><li>COMPLETED</li></ul>") @RequestParam(value = "status", required = false) List<String> status,
            @ApiParam(value = "Identify Project Or Not", defaultValue = "false") @RequestParam(value = "isProject", required = false, defaultValue = "false") Boolean isProject) {

        ProcessInstanceGroupFilter filters = CollaborationGroupDAOUtility.getFilterDetails(partyId, collaborationRole, archived, tradingPartnerIDs, relatedProducts, relatedProductCategories, status, null, null, bearerToken,isProject);
        ResponseEntity response = ResponseEntity.status(HttpStatus.OK).body(filters);
        logger.debug("Filters retrieved for partyId: {}, archived: {}, products: {}, categories: {}, parties: {}", partyId, archived,
                relatedProducts != null ? relatedProducts.toString() : "[]",
                relatedProductCategories != null ? relatedProductCategories.toString() : "[]",
                tradingPartnerIDs != null ? tradingPartnerIDs.toString() : "[]");
        return response;
    }

    @Override
    @ApiOperation(value = "", notes = "Gets the order document included in a business process group")
    public ResponseEntity<Void> getOrderDocument(@ApiParam(value = "Identifier of a process instance included in the group", required = true) @RequestParam(value = "processInstanceId", required = true) String processInstanceId,
                                                @ApiParam(value = "The Bearer token provided by the identity service", required = true) @RequestHeader(value = "Authorization", required = true) String bearerToken) {
        try {
            // check token
            ResponseEntity tokenCheck = eu.nimble.service.bp.impl.util.HttpResponseUtil.checkToken(bearerToken);
            if (tokenCheck != null) {
                return tokenCheck;
            }

            // check whether the process instance id exists
            ProcessInstanceDAO pi = ProcessInstanceDAOUtility.getById(processInstanceId);
            if (pi == null) {
                return HttpResponseUtil.createResponseEntityAndLog(String.format("No process ID exists for the process id: %s", processInstanceId), null, HttpStatus.NOT_FOUND, LogLevel.INFO);
            }
            // get the order
            String orderId = ProcessInstanceGroupDAOUtility.getOrderIdInGroup(processInstanceId);

            // get the order content
            ResponseEntity response;
            if (orderId != null) {
                String orderJson = (String) documentController.getDocumentJsonContent(orderId,bearerToken).getBody();
                response = ResponseEntity.status(HttpStatus.OK).body(orderJson);

            } else {
                response = ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return response;

        } catch (Exception e) {
            return HttpResponseUtil.createResponseEntityAndLog(String.format("Unexpected error while getting the order content for process id: %s", processInstanceId), e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "", notes = "Cancels the collaboration (negotiation) which is represented by the specified ProcessInstanceGroup")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Cancelled the collaboration for the given group id successfully."),
            @ApiResponse(code = 400, message = "The specified group cannot be cancelled"),
            @ApiResponse(code = 401, message = "Invalid token. No user was found for the provided token"),
            @ApiResponse(code = 404, message = "There does not exist a process instance group with the given id"),
            @ApiResponse(code = 500, message = "Unexpected error while cancelling the collaboration")
    })
    @RequestMapping(value = "/process-instance-groups/{id}/cancel",
            method = RequestMethod.POST)
    public ResponseEntity cancelCollaboration(@ApiParam(value = "Identifier of the process instance group to be cancelled", required = true) @PathVariable(value = "id", required = true) String id,
                                              @ApiParam(value = "The Bearer token provided by the identity service", required = true) @RequestHeader(value = "Authorization", required = true) String bearerToken) {
        try {
            logger.debug("Cancelling the collaboration for the group id: {}", id);
            // check token
            ResponseEntity tokenCheck = eu.nimble.service.bp.impl.util.HttpResponseUtil.checkToken(bearerToken);
            if (tokenCheck != null) {
                return tokenCheck;
            }

            ProcessInstanceGroupDAO groupDAO = ProcessInstanceGroupDAOUtility.getProcessInstanceGroupDAO(id);
            if (groupDAO == null) {
                String msg = String.format("There does not exist a process instance group with the id: %s", id);
                logger.warn(msg);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(msg);
            }

            // check whether the group consists of an approved order or an accepted transport execution plan
            List<String> processInstanceIDs = groupDAO.getProcessInstanceIDs();
            boolean isCancellableGroup = cancellableGroup(processInstanceIDs);
            if (!isCancellableGroup) {
                logger.error("Process instance group with id:{} can not be cancelled since the contractually-binding step is approved already.", id);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            // update the group of the party initiating the cancel request
            GenericJPARepository repo = repoFactory.forBpRepository();
            groupDAO.setStatus(GroupStatus.CANCELLED);
            groupDAO = repo.updateEntity(groupDAO);

            // cancel processes in the group
            for (String processID : groupDAO.getProcessInstanceIDs()) {
                ProcessInstanceDAO instanceDAO = ProcessInstanceDAOUtility.getById(processID);
                // if process is completed or already cancelled, continue
                if (instanceDAO.getStatus() == ProcessInstanceStatus.COMPLETED || instanceDAO.getStatus() == ProcessInstanceStatus.CANCELLED) {
                    instanceDAO.setStatus(ProcessInstanceStatus.CANCELLED);
                    repo.updateEntity(instanceDAO);
                    continue;
                }
                // otherwise, cancel the process
                processInstanceController.cancelProcessInstance(processID, bearerToken);
            }

            // update the groups associated with the first group
            for (String groupId : groupDAO.getAssociatedGroups()) {
                ProcessInstanceGroupDAO group = ProcessInstanceGroupDAOUtility.getProcessInstanceGroupDAO(groupId);
                // check whether the associated group can be cancelled or not
                isCancellableGroup = cancellableGroup(group.getProcessInstanceIDs());
                // if it is ok, change status of the associated group
                if (isCancellableGroup) {
                    group.setStatus(GroupStatus.CANCELLED);
                    repo.updateEntity(group);
                }
            }

            // create completed tasks for both parties
            String processInstanceID = groupDAO.getProcessInstanceIDs().get(groupDAO.getProcessInstanceIDs().size() - 1);
            TrustPersistenceUtility.createCompletedTasksForBothParties(processInstanceID, bearerToken, "Cancelled");

            // send email to the trading partner
            emailSenderUtil.sendCancellationEmail(bearerToken, groupDAO);

            logger.debug("Cancelled the collaboration for the group id: {} successfully", id);
            return ResponseEntity.ok(null);

        } catch (Exception e) {
            return HttpResponseUtil.createResponseEntityAndLog(String.format("Unexpected error while cancelling the group: %s", id), e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean cancellableGroup(List<String> processInstanceIDs) {
        for (String instanceID : processInstanceIDs) {
            List<ProcessDocumentMetadataDAO> metadataDAOS = ProcessDocumentMetadataDAOUtility.findByProcessInstanceID(instanceID);
            for (ProcessDocumentMetadataDAO metadataDAO : metadataDAOS) {
                if (metadataDAO.getType() == DocumentType.ORDERRESPONSESIMPLE && metadataDAO.getStatus() == ProcessDocumentStatus.APPROVED ||
                        metadataDAO.getType() == DocumentType.TRANSPORTEXECUTIONPLAN && metadataDAO.getStatus() == ProcessDocumentStatus.APPROVED) {
                    return false;
                }
            }
        }
        return true;
    }
}
