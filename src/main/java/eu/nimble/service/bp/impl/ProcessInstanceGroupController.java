package eu.nimble.service.bp.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.nimble.service.bp.hyperjaxb.model.ProcessInstanceGroupDAO;
import eu.nimble.service.bp.impl.util.federation.FederationUtil;
import eu.nimble.service.bp.impl.util.persistence.HibernateSwaggerObjectMapper;
import eu.nimble.service.bp.impl.util.persistence.HibernateUtilityRef;
import eu.nimble.service.bp.impl.util.persistence.ProcessInstanceGroupDAOUtility;
import eu.nimble.service.bp.impl.util.rest.URLConnectionUtil;
import eu.nimble.service.bp.swagger.api.GroupApi;
import eu.nimble.service.bp.swagger.model.ProcessInstance;
import eu.nimble.service.bp.swagger.model.ProcessInstanceGroup;
import eu.nimble.service.bp.swagger.model.ProcessInstanceGroupFilter;
import eu.nimble.service.bp.swagger.model.ProcessInstanceGroupResponse;
import io.swagger.annotations.ApiOperation;
import eu.nimble.utility.DateUtility;
import io.swagger.annotations.ApiParam;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by suat on 06-Feb-18.
 */
@Controller
public class ProcessInstanceGroupController implements GroupApi {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ProcessInstanceGroupDAOUtility groupDaoUtility;

    @Override
    @ApiOperation(value = "", notes = "Add a new process instance to the specified")
    public ResponseEntity<ProcessInstanceGroup> addProcessInstanceToGroup(
            @ApiParam(value = "Identifier of the process instance group to which a new process instance id is added", required = true) @PathVariable("ID") String ID,
            @ApiParam(value = "Identifier of the process instance to be added", required = true) @RequestParam(value = "processInstanceID", required = true) String processInstanceID,
            @ApiParam(value = "", required = true) @RequestHeader(value = "Authorization", required = true) String authorization) {
        logger.debug("Adding process instance: {} to ProcessInstanceGroup: {}", ID);
        ProcessInstanceGroupDAO processInstanceGroupDAO = null;
        try {
            processInstanceGroupDAO = ProcessInstanceGroupDAOUtility.getProcessInstanceGroupDAO(ID);
            processInstanceGroupDAO.getProcessInstanceIDs().add(processInstanceID);
        } catch (Exception e) {
            logger.error("Failed to get process instance group with the given id : {}", ID, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        HibernateUtilityRef.getInstance("bp-data-model").update(processInstanceGroupDAO);

        // retrieve the group DAO again to populate the first/last activity times
        processInstanceGroupDAO = ProcessInstanceGroupDAOUtility.getProcessInstanceGroupDAO(ID);
        ProcessInstanceGroup processInstanceGroup = HibernateSwaggerObjectMapper.convertProcessInstanceGroupDAO(processInstanceGroupDAO);

        ResponseEntity response = ResponseEntity.status(HttpStatus.OK).body(processInstanceGroup);
        logger.debug("Added process instance: {} to ProcessInstanceGroup: {}", ID);
        return response;
    }

    @Override
    @ApiOperation(value = "", notes = "Delete the process instance from the process instance group")
    public ResponseEntity<ProcessInstanceGroup> deleteProcessInstanceFromGroup(@ApiParam(value = "Identifier of the process instance group from which the process instance id is deleted", required = true) @PathVariable("ID") String ID, @ApiParam(value = "Identifier of the process instance to be deleted", required = true) @RequestParam(value = "processInstanceID", required = true) String processInstanceID, @ApiParam(value = "", required = true) @RequestHeader(value = "Authorization", required = true) String authorization
    ) {
        logger.debug("Deleting process instance: {} from ProcessInstanceGroup: {}", ID);

        ProcessInstanceGroupDAO processInstanceGroupDAO = ProcessInstanceGroupDAOUtility.getProcessInstanceGroupDAO(ID);
        processInstanceGroupDAO.getProcessInstanceIDs().remove(processInstanceID);
        HibernateUtilityRef.getInstance("bp-data-model").update(processInstanceGroupDAO);

        processInstanceGroupDAO = ProcessInstanceGroupDAOUtility.getProcessInstanceGroupDAO(ID);
        ProcessInstanceGroup processInstanceGroup = HibernateSwaggerObjectMapper.convertProcessInstanceGroupDAO(processInstanceGroupDAO);
        ResponseEntity response = ResponseEntity.status(HttpStatus.OK).body(processInstanceGroup);
        logger.debug("Deleted process instance: {} from ProcessInstanceGroup: {}", ID);
        return response;
    }

    @Override
    @ApiOperation(value = "", notes = "Retrieve the process instances for the given process group")
    public ResponseEntity<List<ProcessInstance>> getProcessInstancesOfGroup(@ApiParam(value = "Identifier of the process instance group for which the associated process instances will be retrieved", required = true) @PathVariable("ID") String ID, @ApiParam(value = "", required = true) @RequestHeader(value = "Authorization", required = true) String authorization) {
        logger.debug("Getting ProcessInstances for group: {}", ID);

        ProcessInstanceGroupDAO processInstanceGroupDAO = ProcessInstanceGroupDAOUtility.getProcessInstanceGroupDAO(ID);
        List<ProcessInstance> processInstances = HibernateSwaggerObjectMapper.createProcessInstances(ProcessInstanceGroupDAOUtility.getProcessInstances(processInstanceGroupDAO.getProcessInstanceIDs()));
        ResponseEntity response = ResponseEntity.status(HttpStatus.OK).body(processInstances);
        logger.debug("Retrieved ProcessInstances for group: {}", ID);
        return response;
    }

    @Override
    @ApiOperation(value = "", notes = "Delete the process instance group along with the included process instances")
    public ResponseEntity<Void> deleteProcessInstanceGroup(@ApiParam(value = "", required = true) @PathVariable("ID") String ID, @ApiParam(value = "", required = true) @RequestHeader(value = "Authorization", required = true) String authorization
    ) {
        logger.debug("Deleting ProcessInstanceGroup ID: {}", ID);

        ProcessInstanceGroupDAOUtility.deleteProcessInstanceGroupDAOByID(ID);

        ResponseEntity response = ResponseEntity.status(HttpStatus.OK).body("true");
        logger.debug("Deleted ProcessInstanceGroups: {}", ID);
        return response;
    }

    @Override
    @ApiOperation(value = "", notes = "Retrieve the process instance group specified with the ID")
    public ResponseEntity<ProcessInstanceGroup> getProcessInstanceGroup(@ApiParam(value = "", required = true) @PathVariable("ID") String ID, @ApiParam(value = "", required = true) @RequestHeader(value = "Authorization", required = true) String authorization
    ) {
        logger.debug("Getting ProcessInstanceGroup: {}", ID);

        ProcessInstanceGroupDAO processInstanceGroupDAO = ProcessInstanceGroupDAOUtility.getProcessInstanceGroupDAO(ID);

        ProcessInstanceGroup processInstanceGroup = HibernateSwaggerObjectMapper.convertProcessInstanceGroupDAO(processInstanceGroupDAO);
        ResponseEntity response = ResponseEntity.status(HttpStatus.OK).body(processInstanceGroup);
        logger.debug("Retrieved ProcessInstanceGroup: {}", ID);
        return response;
    }

    @Override
    @ApiOperation(value = "", notes = "Retrieve process instance groups for the specified party. If no partyID is specified, then all groups are returned")
    public ResponseEntity<ProcessInstanceGroupResponse> getProcessInstanceGroups(@ApiParam(value = "", required = true) @RequestParam(value = "initiatorInstanceId", required = true) String initiatorInstanceId



            ,@ApiParam(value = "", required = true) @RequestParam(value = "targetInstanceId", required = true) String targetInstanceId



            ,
                                                                                 @ApiParam(value = "" ,required=true ) @RequestHeader(value="Authorization", required=true) String authorization


            ,@ApiParam(value = "Identifier of the party") @RequestParam(value = "partyID", required = false) String partyID



            ,@ApiParam(value = "Related products") @RequestParam(value = "relatedProducts", required = false) List<String> relatedProducts



            ,@ApiParam(value = "Related product categories") @RequestParam(value = "relatedProductCategories", required = false) List<String> relatedProductCategories



            ,@ApiParam(value = "Identifier of the corresponsing trading partner ID") @RequestParam(value = "tradingPartnerIDs", required = false) List<String> tradingPartnerIDs



            ,@ApiParam(value = "Initiation date range for the first process instance in the group") @RequestParam(value = "initiationDateRange", required = false) String initiationDateRange



            ,@ApiParam(value = "Last activity date range. It is the latest submission date of the document to last process instance in the group") @RequestParam(value = "lastActivityDateRange", required = false) String lastActivityDateRange



            ,@ApiParam(value = "Offset of the first result among the complete result set satisfying the given criteria", defaultValue = "0") @RequestParam(value = "offset", required = false, defaultValue="0") Integer offset



            ,@ApiParam(value = "Number of results to be included in the result set", defaultValue = "10") @RequestParam(value = "limit", required = false, defaultValue="10") Integer limit



            ,@ApiParam(value = "", defaultValue = "false") @RequestParam(value = "archived", required = false, defaultValue="false") Boolean archived



            ,@ApiParam(value = "") @RequestParam(value = "collaborationRole", required = false) String collaborationRole
    ) {
        logger.debug("Getting ProcessInstanceGroups for party: {}", partyID);

        List<ProcessInstanceGroupDAO> results = ProcessInstanceGroupDAOUtility.getProcessInstanceGroupDAOs(partyID, collaborationRole, archived, tradingPartnerIDs, relatedProducts, relatedProductCategories, null, null, limit, offset);
        int totalSize = ProcessInstanceGroupDAOUtility.getProcessInstanceGroupSize(partyID, collaborationRole, archived, tradingPartnerIDs, relatedProducts, relatedProductCategories, null, null);
        logger.debug(" There are {} process instance groups in total", results.size());
        List<ProcessInstanceGroup> processInstanceGroups = new ArrayList<>();
        for (ProcessInstanceGroupDAO result : results) {
            processInstanceGroups.add(HibernateSwaggerObjectMapper.convertProcessInstanceGroupDAO(result));
        }

        ProcessInstanceGroupResponse groupResponse = new ProcessInstanceGroupResponse();
        groupResponse.setProcessInstanceGroups(processInstanceGroups);
        groupResponse.setSize(totalSize);

        // For federation: Check the process instance groups in other and join them to ..
        String queryString = constructQueryString(partyID, relatedProducts, relatedProductCategories, tradingPartnerIDs, initiationDateRange, lastActivityDateRange, offset, limit, archived, collaborationRole);
        List<String> nimbleURLs = FederationUtil.getFederationEndpoints();
        for (String nimbleURL : nimbleURLs) {
            String groupResponseFederationJson = URLConnectionUtil.get(nimbleURL + "/delegate/group?" + queryString, "UTF-8", null, null, null);
            try {
                ProcessInstanceGroupResponse groupResponseFromFederation = new ObjectMapper().readValue(groupResponseFederationJson, ProcessInstanceGroupResponse.class);
                groupResponse.getProcessInstanceGroups().addAll(groupResponseFromFederation.getProcessInstanceGroups());
                groupResponse.setSize(groupResponse.getSize() + groupResponseFromFederation.getSize());
            } catch (IOException e) {
                logger.error("", e);
            }
        }
        /////////////////////////////////////////////////////////

        ResponseEntity response = ResponseEntity.status(HttpStatus.OK).body(groupResponse);
        logger.debug("Retrieved ProcessInstanceGroups for party: {}", partyID);
        return response;
    }

    private String constructQueryString(String partyID, List<String> relatedProducts, List<String> relatedProductCategories, List<String> tradingPartnerIDs,
                                        String initiationDateRange, String lastActivityDateRange, Integer offset, Integer limit, Boolean archived, String collaborationRole) {
        String queryString = "";
        if (partyID != null)
            queryString += "partyID=" + partyID + "&";
        if (relatedProducts != null)
            queryString += "relatedProducts=" + String.join(",", relatedProducts) + "&";
        if (relatedProductCategories != null)
            queryString += "relatedProductCategories=" + String.join(",", relatedProductCategories) + "&";
        if (tradingPartnerIDs != null)
            queryString += "tradingPartnerIDs=" + String.join(",", tradingPartnerIDs) + "&";
        if (initiationDateRange != null)
            queryString += "initiationDateRange=" + initiationDateRange + "&";
        if (lastActivityDateRange != null)
            queryString += "lastActivityDateRange=" + lastActivityDateRange + "&";
        if (offset != null)
            queryString += "offset=" + offset + "&";
        if (limit != null)
            queryString += "limit=" + limit + "&";
        if (archived != null)
            queryString += "archived=" + archived + "&";
        if (collaborationRole != null)
            queryString += "collaborationRole=" + collaborationRole + "&";

        return queryString.substring(0,queryString.length()-1);
    }

    @Override
    @ApiOperation(value = "", notes = "Generate detailed filtering criteria for the current query parameters in place")
    public ResponseEntity<ProcessInstanceGroupFilter> getProcessInstanceGroupFilters(
            @ApiParam(value = "", required = true) @RequestParam(value = "initiatorInstanceId", required = true) String initiatorInstanceId
            , @ApiParam(value = "", required = true) @RequestParam(value = "targetInstanceId", required = true) String targetInstanceId
            , @ApiParam(value = "", required = true) @RequestHeader(value = "Authorization", required = true) String authorization


            , @ApiParam(value = "Identifier of the party") @RequestParam(value = "partyID", required = false) String partyID


            , @ApiParam(value = "Related products") @RequestParam(value = "relatedProducts", required = false) List<String> relatedProducts


            , @ApiParam(value = "Related product categories") @RequestParam(value = "relatedProductCategories", required = false) List<String> relatedProductCategories


            , @ApiParam(value = "Identifier of the corresponsing trading partner ID") @RequestParam(value = "tradingPartnerIDs", required = false) List<String> tradingPartnerIDs


            , @ApiParam(value = "Initiation date range for the first process instance in the group") @RequestParam(value = "initiationDateRange", required = false) String initiationDateRange


            , @ApiParam(value = "Last activity date range. It is the latest submission date of the document to last process instance in the group") @RequestParam(value = "lastActivityDateRange", required = false) String lastActivityDateRange


            , @ApiParam(value = "", defaultValue = "false") @RequestParam(value = "archived", required = false, defaultValue = "false") Boolean archived


            , @ApiParam(value = "") @RequestParam(value = "collaborationRole", required = false) String collaborationRole) {

        ProcessInstanceGroupFilter filters = groupDaoUtility.getFilterDetails(partyID, collaborationRole, archived, tradingPartnerIDs, relatedProducts, relatedProductCategories, null, null, authorization);

        // For federation: Check the process instance groups in other and join them to filters..
        String queryString = constructQueryString(partyID, relatedProducts, relatedProductCategories, tradingPartnerIDs, initiationDateRange, lastActivityDateRange, null, null, archived, collaborationRole);
        List<String> nimbleURLs = FederationUtil.getFederationEndpoints();
        for (String nimbleURL : nimbleURLs) {
            String groupFiltersJson = URLConnectionUtil.get(nimbleURL + "/delegate/group/filters?" + queryString, "UTF-8", initiatorInstanceId, targetInstanceId, authorization);
            try {
                ProcessInstanceGroupFilter filtersFromFederation = new ObjectMapper().readValue(groupFiltersJson, ProcessInstanceGroupFilter.class);

                filters.getTradingPartnerIDs().addAll(filtersFromFederation.getTradingPartnerIDs());
                filters.getRelatedProductCategories().addAll(filtersFromFederation.getRelatedProductCategories());
                filters.getRelatedProducts().addAll(filtersFromFederation.getRelatedProducts());
                filters.getTradingPartnerNames().addAll(filtersFromFederation.getTradingPartnerNames());

                DateTime startDate = DateUtility.convert(filters.getStartDate());
                DateTime endDate = DateUtility.convert(filters.getEndDate());

                DateTime federationStartDate = DateUtility.convert(filtersFromFederation.getStartDate());
                DateTime federationEndDate = DateUtility.convert(filtersFromFederation.getEndDate());

                if (federationStartDate.isBefore(startDate))
                    filters.setStartDate(DateUtility.convert(federationStartDate));

                if (federationEndDate.isAfter(endDate))
                    filters.setEndDate(DateUtility.convert(federationEndDate));
            } catch (IOException e) {
                logger.error("", e);
            }
        }
        /////////////////////////////////////////////////////////

        ResponseEntity response = ResponseEntity.status(HttpStatus.OK).body(filters);
        logger.debug("Filters retrieved for partyId: {}, archived: {}, products: {}, categories: {}, parties: {}", partyID, archived,
                relatedProducts != null ? relatedProducts.toString() : "[]",
                relatedProductCategories != null ? relatedProductCategories.toString() : "[]",
                tradingPartnerIDs != null ? tradingPartnerIDs.toString() : "[]");

        return response;
    }

    @Override
    @ApiOperation(value = "", notes = "Archive the process instance group specified with ID")
    public ResponseEntity<ProcessInstanceGroup> archiveGroup(@ApiParam(value = "Identifier of the process instance group to be archived", required = true) @PathVariable("ID") String ID, @ApiParam(value = "", required = true) @RequestHeader(value = "Authorization", required = true) String authorization
    ) {
        logger.debug("Archiving ProcessInstanceGroup: {}", ID);

        ProcessInstanceGroupDAO processInstanceGroupDAO = ProcessInstanceGroupDAOUtility.getProcessInstanceGroupDAO(ID);
        processInstanceGroupDAO.setArchived(true);

        HibernateUtilityRef.getInstance("bp-data-model").update(processInstanceGroupDAO);

        ProcessInstanceGroup processInstanceGroup = HibernateSwaggerObjectMapper.convertProcessInstanceGroupDAO(processInstanceGroupDAO);
        ResponseEntity response = ResponseEntity.status(HttpStatus.OK).body(processInstanceGroup);
        logger.debug("Archived ProcessInstanceGroup: {}", ID);
        return response;
    }

    @Override
    @ApiOperation(value = "", notes = "Restore the archived process instance group specified with ID")
    public ResponseEntity<ProcessInstanceGroup> restoreGroup(@ApiParam(value = "Identifier of the process instance group to be restored", required = true) @PathVariable("ID") String ID, @ApiParam(value = "", required = true) @RequestHeader(value = "Authorization", required = true) String authorization
    ) {
        logger.debug("Restoring ProcessInstanceGroup: {}", ID);

        ProcessInstanceGroupDAO processInstanceGroupDAO = ProcessInstanceGroupDAOUtility.getProcessInstanceGroupDAO(ID);
        processInstanceGroupDAO.setArchived(false);

        HibernateUtilityRef.getInstance("bp-data-model").update(processInstanceGroupDAO);

        ProcessInstanceGroup processInstanceGroup = HibernateSwaggerObjectMapper.convertProcessInstanceGroupDAO(processInstanceGroupDAO);
        ResponseEntity response = ResponseEntity.status(HttpStatus.OK).body(processInstanceGroup);
        logger.debug("Restored ProcessInstanceGroup: {}", ID);
        return response;
    }

    @Override
    @ApiOperation(value = "", notes = "Save a process group along with the initial process instance")
    public ResponseEntity<Void> saveProcessInstanceGroup(@ApiParam(value = "The content of the process instance group to be saved", required = true) @RequestBody ProcessInstanceGroup processInstanceGroup, @ApiParam(value = "", required = true) @RequestHeader(value = "Authorization", required = true) String authorization
    ) {
        logger.debug("Saving ProcessInstanceGroup {}", processInstanceGroup.toString());
        ProcessInstanceGroupDAO processInstanceGroupDAO = HibernateSwaggerObjectMapper.createProcessInstanceGroup_DAO(processInstanceGroup);
        HibernateUtilityRef.getInstance("bp-data-model").persist(processInstanceGroupDAO);

        ResponseEntity response = ResponseEntity.status(HttpStatus.OK).body("true");
        logger.debug("Saved ProcessInstanceGroup {}", processInstanceGroup.toString());
        return response;
    }

    @Override
    @ApiOperation(value = "", notes = "Archive all the groups of the given party")
    public ResponseEntity<Void> archiveAllGroups(@ApiParam(value = "Identifier of the party of which groups will be archived", required = true) @RequestParam(value = "partyID", required = true) String partyID, @ApiParam(value = "", required = true) @RequestHeader(value = "Authorization", required = true) String authorization
    ) {
        logger.debug("Archiving ProcessInstanceGroups for party {}", partyID);

        ProcessInstanceGroupDAOUtility.archiveAllGroupsForParty(partyID);

        ResponseEntity response = ResponseEntity.status(HttpStatus.OK).body("true");
        logger.debug("Archived ProcessInstanceGroup for party {}", partyID);
        return response;
    }

    @Override
    @ApiOperation(value = "", notes = "Delete all the archived groups of the given party")
    public ResponseEntity<Void> deleteAllArchivedGroups(@ApiParam(value = "Identifier of the party of which groups will be deleted", required = true) @RequestParam(value = "partyID", required = true) String partyID, @ApiParam(value = "", required = true) @RequestHeader(value = "Authorization", required = true) String authorization
    ) {
        logger.debug("Deleting archived ProcessInstanceGroups for party {}", partyID);

        ProcessInstanceGroupDAOUtility.deleteArchivedGroupsForParty(partyID);

        ResponseEntity response = ResponseEntity.status(HttpStatus.OK).body("true");
        logger.debug("Deleted archived ProcessInstanceGroups for party {}", partyID);
        return response;
    }
}
