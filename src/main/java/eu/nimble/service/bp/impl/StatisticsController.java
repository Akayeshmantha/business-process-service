package eu.nimble.service.bp.impl;

import eu.nimble.service.bp.impl.model.statistics.BusinessProcessCount;
import eu.nimble.service.bp.impl.model.statistics.NonOrderedProducts;
import eu.nimble.service.bp.impl.model.statistics.OverallStatistics;
import eu.nimble.service.bp.impl.util.bp.BusinessProcessUtility;
import eu.nimble.service.bp.impl.util.camunda.CamundaEngine;
import eu.nimble.service.bp.impl.util.controller.InputValidatorUtil;
import eu.nimble.service.bp.impl.util.controller.ValidationResponse;
import eu.nimble.service.bp.impl.util.persistence.bp.ProcessDocumentMetadataDAOUtility;
import eu.nimble.service.bp.impl.util.persistence.catalogue.StatisticsPersistenceUtility;
import eu.nimble.service.bp.impl.util.spring.SpringBridge;
import eu.nimble.service.bp.swagger.model.Transaction;
import eu.nimble.service.model.ubl.commonaggregatecomponents.PartyType;
import eu.nimble.utility.HttpResponseUtil;
import eu.nimble.utility.JsonSerializationUtility;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Api(value = "statistics", description = "The statistics API")
@RequestMapping(value = "/statistics")
@Controller
public class StatisticsController {
    private final Logger logger = LoggerFactory.getLogger(StatisticsController.class);

    @ApiOperation(value = "",notes = "Gets the total number of process instances which require an action")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Retrieved the total number of process instances which require an action successfully",response = int.class),
            @ApiResponse(code = 400, message = "Invalid role")
    })
    @RequestMapping(value = "/total-number/business-process/action-required",
            produces = {"application/json"},
            method = RequestMethod.GET)
    public ResponseEntity getActionRequiredProcessCount(@ApiParam(value = "The identifier of the party whose action required process count will be received", required = true) @RequestParam(value = "partyId", required = true) Integer partyId,
                                                        @ApiParam(value = "Whether the group which contains process instances is archived or not.", defaultValue = "false") @RequestParam(value = "archived", required = false, defaultValue="false") Boolean archived,
                                                        @ApiParam(value = "Role of the party in the business process.<br>Possible values: <ul><li>seller</li><li>buyer</li></ul>", required = true) @RequestParam(value = "role", required = true, defaultValue = "seller") String role,
                                                        @ApiParam(value = "The Bearer token provided by the identity service", required = true) @RequestHeader(value = "Authorization", required = true) String bearerToken) {
        logger.info("Getting total number of process instances which require an action for party id:{},archived: {}, role: {}",partyId,archived,role);
        // check role
        ValidationResponse response = InputValidatorUtil.checkRole(role, false);
        if (response.getInvalidResponse() != null) {
            return response.getInvalidResponse();
        }

        long count = StatisticsPersistenceUtility.getActionRequiredProcessCount(String.valueOf(partyId),role,archived);
        logger.info("Retrieved total number of process instances which require an action for company id:{},archived: {}, role: {}",partyId,archived,role);
        return ResponseEntity.ok(count);
    }

    @ApiOperation(value = "",notes = "Gets the total number (with both active and completed status) of specified business process")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Retrieved the number of processes successfully",response = int.class),
            @ApiResponse(code = 400, message = "Invalid parameter"),
            @ApiResponse(code = 401, message = "Invalid token. No user was found for the provided token"),
            @ApiResponse(code = 500, message = "Unexpected error while getting the total number of business processes")
    })
    @RequestMapping(value = "/total-number/business-process",
            produces = {"application/json"},
            method = RequestMethod.GET)
    public ResponseEntity getProcessCount(@ApiParam(value = "Business process type.<br>Examples:ORDER,NEGOTIATION,ITEM_INFORMATION_REQUEST", required = false) @RequestParam(value = "businessProcessType", required = false) String businessProcessType,
                                          @ApiParam(value = "Start date (DD-MM-YYYY) of the process", required = false) @RequestParam(value = "startDate", required = false) String startDateStr,
                                          @ApiParam(value = "End date (DD-MM-YYYY) of the process", required = false) @RequestParam(value = "endDate", required = false) String endDateStr,
                                          @ApiParam(value = "Identifier of the party as specified by the identity service", required = false) @RequestParam(value = "partyId", required = false) Integer partyId,
                                          @ApiParam(value = "Role of the party in the business process.<br>Possible values:<ul><li>seller</li><li>buyer</li></ul>", defaultValue = "seller", required = false) @RequestParam(value = "role", required = false, defaultValue = "seller") String role,
                                          @ApiParam(value = "State of the transaction.<br>Possible values: <ul><li>WaitingResponse</li><li>Approved</li><li>Denied</li></ul>", required = false) @RequestParam(value = "status", required = false) String status,
                                          @ApiParam(value = "The Bearer token provided by the identity service" ,required=true ) @RequestHeader(value="Authorization", required=true) String bearerToken
    ) {

        try {
            logger.info("Getting total number of documents for start date: {}, end date: {}, type: {}, party id: {}, role: {}, state: {}", startDateStr, endDateStr, businessProcessType, partyId, role, status);
            // check token
            ResponseEntity tokenCheck = eu.nimble.service.bp.impl.util.HttpResponseUtil.checkToken(bearerToken);
            if (tokenCheck != null) {
                return tokenCheck;
            }

            ValidationResponse response;

            // check start date
            response = InputValidatorUtil.checkDate(startDateStr, true);
            if (response.getInvalidResponse() != null) {
                return response.getInvalidResponse();
            }

            // check end date
            response = InputValidatorUtil.checkDate(endDateStr, true);
            if (response.getInvalidResponse() != null) {
                return response.getInvalidResponse();
            }

            // check role
            response = InputValidatorUtil.checkRole(role, true);
            if (response.getInvalidResponse() != null) {
                return response.getInvalidResponse();
            }
            role = response.getValidatedObject() != null ? (String) response.getValidatedObject() : null;

            // check business process type
            response = InputValidatorUtil.checkBusinessProcessType(businessProcessType, true);
            if (response.getInvalidResponse() != null) {
                return response.getInvalidResponse();
            }

            // get initiating document for the business process
            List<String> documentTypes = new ArrayList<>();
            if (businessProcessType != null) {
                documentTypes.add(BusinessProcessUtility.getInitialDocumentForProcess(businessProcessType).toString());

                // if there is no process specified, get the document list for all business processes
            } else {
                List<Transaction.DocumentTypeEnum> initialDocuments = BusinessProcessUtility.getInitialDocumentsForAllProcesses();
                initialDocuments.stream().forEach(type -> documentTypes.add(type.toString()));
            }

            // check status
            response = InputValidatorUtil.checkStatus(status, true);
            if (response.getInvalidResponse() != null) {
                return response.getInvalidResponse();
            }
            status = response.getValidatedObject() != null ? (String) response.getValidatedObject() : null;

            int count = ProcessDocumentMetadataDAOUtility.getTransactionCount(partyId, documentTypes, role, startDateStr, endDateStr, status);

            logger.info("Number of business process for start date: {}, end date: {}, type: {}, party id: {}, role: {}, state: {}", startDateStr, endDateStr, businessProcessType, partyId, role, status);
            return ResponseEntity.ok().body(count);

        } catch (Exception e) {
            return HttpResponseUtil.createResponseEntityAndLog(String.format("Unexpected error while getting the total number for business process type: %s, start date: %s, end date: %s, partyId id: %s, role: %s, state: %s", businessProcessType, startDateStr, endDateStr, partyId, role, status), e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "",notes = "Gets the total number (with both active and completed status) of specified business process." +
            " The number of business processes are broken down per company, process type and status")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Retrieved the number of processes successfully",response = BusinessProcessCount.class),
            @ApiResponse(code = 400, message = "Invalid parameter"),
            @ApiResponse(code = 401, message = "Invalid token. No user was found for the provided token"),
            @ApiResponse(code = 500, message = "Unexpected error while getting the total number of business processes")
    })
    @RequestMapping(value = "/total-number/business-process/break-down",
            produces = {"application/json"},
            method = RequestMethod.GET)
    public ResponseEntity getProcessCountBreakDown(@ApiParam(value = "Start date (DD-MM-YYYY) of the process", required = false) @RequestParam(value = "startDate", required = false) String startDateStr,
                                                   @ApiParam(value = "End date (DD-MM-YYYY) of the process", required = false) @RequestParam(value = "endDate", required = false) String endDateStr,
                                                   @ApiParam(value = "Identifier of the party as specified by the identity service", required = false) @RequestParam(value = "partyId", required = false) Integer partyId,
                                                   @ApiParam(value = "Role of the party in the business process.<br>Possible values:<ul><li>seller</li><li>buyer</li></ul>", defaultValue = "seller", required = false) @RequestParam(value = "role",required = false, defaultValue = "seller") String role,
                                                   @ApiParam(value = "The Bearer token provided by the identity service" ,required=true ) @RequestHeader(value="Authorization", required=true) String bearerToken) {

        try {
            logger.info("Getting total number of documents for start date: {}, end date: {}, party id: {}, role: {}", startDateStr, endDateStr, partyId, role);
            // check token
            ResponseEntity tokenCheck = eu.nimble.service.bp.impl.util.HttpResponseUtil.checkToken(bearerToken);
            if (tokenCheck != null) {
                return tokenCheck;
            }

            ValidationResponse response;

            // check start date
            response = InputValidatorUtil.checkDate(startDateStr, true);
            if (response.getInvalidResponse() != null) {
                return response.getInvalidResponse();
            }

            // check end date
            response = InputValidatorUtil.checkDate(endDateStr, true);
            if (response.getInvalidResponse() != null) {
                return response.getInvalidResponse();
            }

            BusinessProcessCount counts = ProcessDocumentMetadataDAOUtility.getGroupTransactionCounts(partyId, startDateStr, endDateStr,role,bearerToken);
            logger.info("Number of business process for start date: {}, end date: {}, company id: {}, role: {}", startDateStr, endDateStr, partyId, role);
            return ResponseEntity.ok().body(counts);

        } catch (Exception e) {
            return HttpResponseUtil.createResponseEntityAndLog(String.format("Unexpected error while getting the total number for start date: %s, end date: %s, party id: %s, role: %s", startDateStr, endDateStr, partyId, role), e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "",notes = "Get the products that are not ordered")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Retrieved the products that are not ordered",response = String.class),
            @ApiResponse(code = 400, message = "Invalid parameter"),
            @ApiResponse(code = 401, message = "Invalid token. No user was found for the provided token"),
            @ApiResponse(code = 500, message = "Unexpected error while getting non-ordered products")
    })
    @RequestMapping(value = "/non-ordered",
            produces = {"application/json"},
            method = RequestMethod.GET)
    public ResponseEntity getNonOrderedProducts(@ApiParam(value = "Identifier of the party as specified by the identity service", required = false) @RequestParam(value = "partyId", required = false) Integer partyId,
                                                @ApiParam(value = "The Bearer token provided by the identity service" ,required=true ) @RequestHeader(value="Authorization", required=true) String bearerToken
    ) {
        try {
            logger.info("Getting non-ordered products for party id: {}", partyId);
            // check token
            ResponseEntity tokenCheck = eu.nimble.service.bp.impl.util.HttpResponseUtil.checkToken(bearerToken);
            if (tokenCheck != null) {
                return tokenCheck;
            }

            NonOrderedProducts nonOrderedProducts = StatisticsPersistenceUtility.getNonOrderedProducts(bearerToken,partyId);
            String serializedResponse = JsonSerializationUtility.getObjectMapperForFilledFields().writeValueAsString(nonOrderedProducts);
            logger.info("Retrieved the products that are not ordered for party id: {}", partyId);
            return ResponseEntity.ok().body(serializedResponse);

        } catch (Exception e) {
            return HttpResponseUtil.createResponseEntityAndLog(String.format("Unexpected error while getting the total number for party id: %s", partyId), e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "",notes = "Get the trading volume")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Retrieved the trading volume successfully",response = double.class),
            @ApiResponse(code = 400, message = "Invalid parameter"),
            @ApiResponse(code = 401, message = "Invalid token. No user was found for the provided token"),
            @ApiResponse(code = 500, message = "Unexpected error while getting trading volume")
    })
    @RequestMapping(value = "/trading-volume",
            produces = {"application/json"},
            method = RequestMethod.GET)
    public ResponseEntity getTradingVolume(@ApiParam(value = "Start date (DD-MM-YYYY) of the transaction", required = false) @RequestParam(value = "startDate", required = false) String startDateStr,
                                          @ApiParam(value = "End date (DD-MM-YYYY) of the transaction", required = false) @RequestParam(value = "endDate", required = false) String endDateStr,
                                          @ApiParam(value = "Identifier of the party as specified by the identity service", required = false) @RequestParam(value = "partyId", required = false) Integer partyId,
                                          @ApiParam(value = "Role of the party in the business process.<br>Possible values:<ul><li>seller</li><li>buyer</li></ul>", required = false) @RequestParam(value = "role", required = false, defaultValue = "SELLER") String role,
                                          @ApiParam(value = "State of the transaction.<br>Possible values: <ul><li>WaitingResponse</li><li>Approved</li><li>Denied</li></ul>", required = false) @RequestParam(value = "status", required = false) String status,
                                          @ApiParam(value = "The Bearer token provided by the identity service" ,required=true ) @RequestHeader(value="Authorization", required=true) String bearerToken
    ) {
        try {
            logger.info("Getting total number of documents for start date: {}, end date: {}, party id: {}, role: {}, state: {}", startDateStr, endDateStr, partyId, role, status);
            // check token
            ResponseEntity tokenCheck = eu.nimble.service.bp.impl.util.HttpResponseUtil.checkToken(bearerToken);
            if (tokenCheck != null) {
                return tokenCheck;
            }
            ValidationResponse response;

            // check start date
            response = InputValidatorUtil.checkDate(startDateStr, true);
            if (response.getInvalidResponse() != null) {
                return response.getInvalidResponse();
            }

            // check end date
            response = InputValidatorUtil.checkDate(endDateStr, true);
            if (response.getInvalidResponse() != null) {
                return response.getInvalidResponse();
            }

            // check role
            response = InputValidatorUtil.checkRole(role, true);
            if (response.getInvalidResponse() != null) {
                return response.getInvalidResponse();
            }
            role = response.getValidatedObject() != null ? (String) response.getValidatedObject() : null;

            // check status
            response = InputValidatorUtil.checkStatus(status, true);
            if (response.getInvalidResponse() != null) {
                return response.getInvalidResponse();
            }
            status = response.getValidatedObject() != null ? (String) response.getValidatedObject() : null;

            double tradingVolume = StatisticsPersistenceUtility.getTradingVolume(partyId, role, startDateStr, endDateStr, status);

            logger.info("Number of business process for start date: {}, end date: {}, party id: {}, role: {}, state: {}", startDateStr, endDateStr, partyId, role, status);
            return ResponseEntity.ok().body(tradingVolume);

        } catch (Exception e) {
            return HttpResponseUtil.createResponseEntityAndLog(String.format("Unexpected error while getting the total number for start date: %s, end date: %s, party id: %s, role: %s, state: %s", startDateStr, endDateStr, partyId, role, status), e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Gets the inactive companies. (Companies that have not initiated a business process)")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Retrieved the inactive companies successfully"),
            @ApiResponse(code = 401, message = "Invalid token. No user was found for the provided token"),
            @ApiResponse(code = 500, message = "Unexpected error while getting inactive companies")
    })
    @RequestMapping(value = "/inactive-companies",
            produces = {"application/json"},
            method = RequestMethod.GET)
    public ResponseEntity getInactiveCompanies(@ApiParam(value = "The Bearer token provided by the identity service" ,required=true ) @RequestHeader(value="Authorization", required=true) String bearerToken) {
        try {
            logger.info("Getting inactive companies");
            // check token
            ResponseEntity tokenCheck = eu.nimble.service.bp.impl.util.HttpResponseUtil.checkToken(bearerToken);
            if (tokenCheck != null) {
                return tokenCheck;
            }

            List<PartyType> inactiveCompanies = StatisticsPersistenceUtility.getInactiveCompanies(bearerToken);
            String serializedResponse = JsonSerializationUtility.getObjectMapperForFilledFields().writeValueAsString(inactiveCompanies);
            logger.info("Retrieved the inactive companies");
            return ResponseEntity.ok().body(serializedResponse);

        } catch (Exception e) {
            return HttpResponseUtil.createResponseEntityAndLog(String.format("Unexpected error while getting the inactive companies"), e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Gets average response time for the party in terms of days")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Retrieved average response time for the party"),
            @ApiResponse(code = 401, message = "Invalid token. No user was found for the provided token"),
            @ApiResponse(code = 500, message = "Unexpected error while getting average response time")
    })
    @RequestMapping(value = "/response-time",
            produces = {"application/json"},
            method = RequestMethod.GET)
    public ResponseEntity getAverageResponseTime(@ApiParam(value = "Identifier of the party as specified by the identity service", required = true) @RequestParam(value = "partyId") String partyId,
                                                 @ApiParam(value = "The Bearer token provided by the identity service" ,required=true ) @RequestHeader(value="Authorization", required=true) String bearerToken){
        logger.info("Getting average response time for the party with id: {}",partyId);
        // check token
        ResponseEntity tokenCheck = eu.nimble.service.bp.impl.util.HttpResponseUtil.checkToken(bearerToken);
        if (tokenCheck != null) {
            return tokenCheck;
        }

        double averageResponseTime;
        try {
            averageResponseTime = StatisticsPersistenceUtility.calculateAverageResponseTime(partyId);
        }
        catch (Exception e){
            return HttpResponseUtil.createResponseEntityAndLog(String.format("Unexpected error while getting average response time for the party with id: %s", partyId), e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        logger.info("Retrieved average response time for the party with id: {}",partyId);
        return ResponseEntity.ok(averageResponseTime);
    }

    @ApiOperation(value = "Gets average collaboration time for the party in terms of days")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Retrieved average collaboration time for the party"),
            @ApiResponse(code = 401, message = "Invalid token. No user was found for the provided token")
    })
    @RequestMapping(value = "/collaboration-time",
            produces = {"application/json"},
            method = RequestMethod.GET)
    public ResponseEntity getAverageCollaborationTime(@ApiParam(value = "Identifier of the party as specified by the identity service", required = true) @RequestParam(value = "partyId") String partyId,
                                                    @ApiParam(value = "The Bearer token provided by the identity service" ,required=true ) @RequestHeader(value="Authorization", required=true) String bearerToken){
        logger.info("Getting average negotiation time for the party with id: {}",partyId);
        // check token
        ResponseEntity tokenCheck = eu.nimble.service.bp.impl.util.HttpResponseUtil.checkToken(bearerToken);
        if (tokenCheck != null) {
            return tokenCheck;
        }

        double averageNegotiationTime = StatisticsPersistenceUtility.calculateAverageCollaborationTime(partyId,bearerToken);
        logger.info("Retrieved average negotiation time for the party with id: {}",partyId);
        return ResponseEntity.ok(averageNegotiationTime);
    }

    @ApiOperation(value = "Gets statistics (average collaboration time,average response time,trading volume and number of transactions) for the party")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Retrieved statistics for the party",response = OverallStatistics.class),
            @ApiResponse(code = 401, message = "Invalid token. No user was found for the provided token"),
            @ApiResponse(code = 500, message = "Unexpected error while getting overall statistics")
    })
    @RequestMapping(value = "/overall",
            produces = {"application/json"},
            method = RequestMethod.GET)
    public ResponseEntity getStatistics(@ApiParam(value = "Identifier of the party as specified by the identity service") @RequestParam(value = "partyId") String partyId,
                                        @ApiParam(value = "Role of the party in the business process.<br>Possible values:<ul><li>seller</li><li>buyer</li></ul>", defaultValue = "seller", required = false) @RequestParam(value = "role", required = false, defaultValue = "SELLER") String role,
                                        @ApiParam(value = "The Bearer token provided by the identity service" ,required=true ) @RequestHeader(value="Authorization", required=true) String bearerToken){
        logger.info("Getting statistics for the party with id: {}",partyId);
        OverallStatistics statistics = new OverallStatistics();
        try {
            // check token
            ResponseEntity tokenCheck = eu.nimble.service.bp.impl.util.HttpResponseUtil.checkToken(bearerToken);
            if (tokenCheck != null) {
                return tokenCheck;
            }

            statistics.setAverageCollaborationTime((double) getAverageCollaborationTime(partyId,bearerToken).getBody());
            statistics.setAverageResponseTime((double)getAverageResponseTime(partyId,bearerToken).getBody());
            statistics.setTradingVolume((double) getTradingVolume(null,null,Integer.valueOf(partyId), role,null,bearerToken).getBody());
            statistics.setNumberOfTransactions((int)getProcessCount(null,null,null,Integer.valueOf(partyId),role,null,bearerToken).getBody());
        }
        catch (Exception e){
            return HttpResponseUtil.createResponseEntityAndLog(String.format("Unexpected error while getting statistics for the party with id: %s", partyId), e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        logger.info("Retrieved statistics for the party with id: {}",partyId);
        return ResponseEntity.ok(statistics);
    }
}
