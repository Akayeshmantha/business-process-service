package eu.nimble.service.bp.impl;

import eu.nimble.service.bp.hyperjaxb.model.ProcessInstanceDAO;
import eu.nimble.service.bp.hyperjaxb.model.ProcessInstanceFederationDAO;
import eu.nimble.service.bp.hyperjaxb.model.ProcessInstanceGroupDAO;
import eu.nimble.service.bp.hyperjaxb.model.ProcessInstanceInputMessageDAO;
import eu.nimble.service.bp.impl.federation.BusinessProcessClient;
import eu.nimble.service.bp.impl.federation.ClientFactory;
import eu.nimble.service.bp.impl.federation.CoreFunctions;
import eu.nimble.service.bp.impl.util.camunda.CamundaEngine;
import eu.nimble.service.bp.impl.util.persistence.DAOUtility;
import eu.nimble.service.bp.impl.util.persistence.HibernateSwaggerObjectMapper;
import eu.nimble.service.bp.impl.util.persistence.HibernateUtilityRef;
import eu.nimble.service.bp.impl.util.persistence.ProcessInstanceGroupDAOUtility;
import eu.nimble.service.bp.processor.BusinessProcessContext;
import eu.nimble.service.bp.processor.BusinessProcessContextHandler;
import eu.nimble.service.bp.swagger.api.StartApi;
import eu.nimble.service.bp.swagger.model.ProcessInstance;
import eu.nimble.service.bp.swagger.model.ProcessInstanceInputMessage;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by yildiray on 5/25/2017.
 */
@Controller
public class StartController implements StartApi {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ClientFactory clientFactory;

    @Override
    @ApiOperation(value = "", notes = "Start an instance of a business process", response = ProcessInstance.class, tags={  })
    public ResponseEntity<ProcessInstance> startProcessInstance(@RequestBody ProcessInstanceInputMessage body
            ,@RequestParam(value = "federationInstanceId", required = true) String federationInstanceId
            ,@RequestParam(value = "initiatorInstanceId", required = true) String initiatorInstanceId
            ,@RequestHeader(value="Authorization", required=true) String authorization
            ,@RequestParam(value = "gid", required = false) String gid
            ,@RequestParam(value = "precedingPid", required = false) String precedingPid) {
        logger.debug(" $$$ Start Process with ProcessInstanceInputMessage {}", body.toString());
        ProcessInstance processInstance = null;
        // get BusinessProcessContext
        BusinessProcessContext businessProcessContext = BusinessProcessContextHandler.getBusinessProcessContextHandler().getBusinessProcessContext(null);
        try {
            ProcessInstanceInputMessageDAO processInstanceInputMessageDAO = HibernateSwaggerObjectMapper.createProcessInstanceInputMessage_DAO(body);
            HibernateUtilityRef.getInstance("bp-data-model").persist(processInstanceInputMessageDAO);

            // save the created process instance input message
            businessProcessContext.setCreatedMessageDAO(processInstanceInputMessageDAO);

            processInstance = CamundaEngine.startProcessInstance(businessProcessContext.getId(),body);

            ProcessInstanceDAO processInstanceDAO = HibernateSwaggerObjectMapper.createProcessInstance_DAO(processInstance,federationInstanceId);
            HibernateUtilityRef.getInstance("bp-data-model").persist(processInstanceDAO);

            // save ProcessInstanceDAO
            businessProcessContext.setCreatedProcessInstanceDAO(processInstanceDAO);

            // get the process previous process instance
            if(precedingPid != null) {
                ProcessInstanceDAO precedingInstance = DAOUtility.getProcessIntanceDAOByID(precedingPid);
                if (precedingInstance == null) {
                    String msg = "Invalid preceding process instance ID: %s";
                    logger.warn(String.format(msg, precedingPid));
                    return ResponseEntity.badRequest().body(null);
                }
                processInstanceDAO.setPrecedingProcess(precedingInstance);
                HibernateUtilityRef.getInstance("bp-data-model").update(processInstanceDAO);

                // update ProcessInstanceDAO
                businessProcessContext.setCreatedProcessInstanceDAO(processInstanceDAO);
            }

            // create process instance groups if this is the first process initializing the process group
            if (gid == null) {
                createProcessInstanceGroup(businessProcessContext.getId(),body, processInstance,federationInstanceId);
                // make a call to create corresponding group
                clientFactory.createResponseEntity(clientFactory.clientGenerator(initiatorInstanceId).clientCreateProcessInstanceGroup(body,federationInstanceId,initiatorInstanceId,processInstance.getProcessInstanceID(),ProcessInstanceGroupDAOUtility.getSubmissionDate(processInstance.getProcessInstanceID()),authorization));
            } else {
                addNewProcessInstanceToGroup(businessProcessContext.getId(), processInstance.getProcessInstanceID(), body,federationInstanceId,precedingPid);
                clientFactory.createResponseEntity(clientFactory.clientGenerator(initiatorInstanceId).clientAddNewProcessInstanceToGroup(body,federationInstanceId,initiatorInstanceId,processInstance.getProcessInstanceID(),precedingPid,ProcessInstanceGroupDAOUtility.getSubmissionDate(processInstance.getProcessInstanceID()),authorization));

            }


        }
        catch (Exception e){
            logger.error(" $$$ Failed to start process with ProcessInstanceInputMessage {}", body.toString(),e);
            businessProcessContext.handleExceptions();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        finally {
            BusinessProcessContextHandler.getBusinessProcessContextHandler().deleteBusinessProcessContext(businessProcessContext.getId());
        }
        return new ResponseEntity<>(processInstance, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> createProcessInstanceGroup(ProcessInstanceInputMessage body, String processInstanceId, String federationInstanceId,String submissionDate) {
        // get BusinessProcessContext
        BusinessProcessContext businessProcessContext = BusinessProcessContextHandler.getBusinessProcessContextHandler().getBusinessProcessContext(null);
        try {
            // check whether there is a group for the given process or not
            ProcessInstanceGroupDAO groupDAO = ProcessInstanceGroupDAOUtility.getProcessInstanceGroupDAO(processInstanceId,federationInstanceId);
            if(groupDAO == null){
                // create group for initiating party
                ProcessInstanceGroupDAO processInstanceGroupDAO = ProcessInstanceGroupDAOUtility.createProcessInstanceGroupDAO(
                        body.getVariables().getInitiatorID(),
                        processInstanceId,
                        CamundaEngine.getTransactions(body.getVariables().getProcessID()).get(0).getInitiatorRole().toString(),
                        body.getVariables().getRelatedProducts().toString(),
                        federationInstanceId,
                        submissionDate
                );
                HibernateUtilityRef.getInstance("bp-data-model").update(processInstanceGroupDAO);

                businessProcessContext.setCreatedProcessInstanceGroupDAO(processInstanceGroupDAO);
            }
        }
        catch (Exception e){
            businessProcessContext.handleExceptions();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        finally {
            BusinessProcessContextHandler.getBusinessProcessContextHandler().deleteBusinessProcessContext(businessProcessContext.getId());
        }

        return ResponseEntity.ok(null);
    }

    @Override
    public ResponseEntity<Void> addNewProcessInstanceToGroup(ProcessInstanceInputMessage body, String processInstanceId, String federationInstanceId,String precedingProcessId,String submissionDate) {
        // get BusinessProcessContext
        BusinessProcessContext businessProcessContext = BusinessProcessContextHandler.getBusinessProcessContextHandler().getBusinessProcessContext(null);
        try {
            ProcessInstanceGroupDAO group = ProcessInstanceGroupDAOUtility.getProcessInstanceGroupDAO(precedingProcessId,federationInstanceId);
            if(group == null){
                ProcessInstanceGroupDAO targetGroup = ProcessInstanceGroupDAOUtility.createProcessInstanceGroupDAO(
                        body.getVariables().getResponderID(),
                        processInstanceId,
                        CamundaEngine.getTransactions(body.getVariables().getProcessID()).get(0).getResponderRole().toString(),
                        body.getVariables().getRelatedProducts().toString(),
                        federationInstanceId,
                        submissionDate
                );
                HibernateUtilityRef.getInstance("bp-data-model").update(targetGroup);

                businessProcessContext.setCreatedProcessInstanceGroupDAO(targetGroup);
            }
            else {
                ProcessInstanceFederationDAO federationDAO = new ProcessInstanceFederationDAO();
                federationDAO.setProcessInstanceID(processInstanceId);
                federationDAO.setFederationInstanceId(federationInstanceId);
                group.getProcessInstances().add(federationDAO);
                HibernateUtilityRef.getInstance("bp-data-model").update(group);

                // save added federationDA
                businessProcessContext.setAddedProcessInstanceFederationDAO(federationDAO);
                // save sourceGroup
                businessProcessContext.setUpdatedProcessInstanceGroupDAO(group);
            }
        }
        catch (Exception e){
            businessProcessContext.handleExceptions();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        finally {
            BusinessProcessContextHandler.getBusinessProcessContextHandler().deleteBusinessProcessContext(businessProcessContext.getId());
        }

        return ResponseEntity.ok(null);
    }

    private void createProcessInstanceGroup(String businessContextId,ProcessInstanceInputMessage body, ProcessInstance processInstance,String federationInstanceId) {
        // create group for responder party
        ProcessInstanceGroupDAO processInstanceGroupDAO = ProcessInstanceGroupDAOUtility.createProcessInstanceGroupDAO(
                body.getVariables().getResponderID(),
                processInstance.getProcessInstanceID(),
                CamundaEngine.getTransactions(body.getVariables().getProcessID()).get(1).getInitiatorRole().toString(),
                body.getVariables().getRelatedProducts().toString(),
                federationInstanceId,
                ProcessInstanceGroupDAOUtility.getSubmissionDate(processInstance.getProcessInstanceID()));

        HibernateUtilityRef.getInstance("bp-data-model").update(processInstanceGroupDAO);

        // save ProcessInstanceGroupDAOs
        BusinessProcessContextHandler.getBusinessProcessContextHandler().getBusinessProcessContext(businessContextId).setCreatedProcessInstanceGroupDAO(processInstanceGroupDAO);
    }

    private void addNewProcessInstanceToGroup(String businessContextId, String processInstanceId, ProcessInstanceInputMessage body,String federationInstanceId,String precedingPid) {
        ProcessInstanceGroupDAO sourceGroup = ProcessInstanceGroupDAOUtility.getProcessInstanceGroupDAO(precedingPid,federationInstanceId);

        if(sourceGroup == null){
            ProcessInstanceGroupDAO targetGroup = ProcessInstanceGroupDAOUtility.createProcessInstanceGroupDAO(
                    body.getVariables().getResponderID(),
                    processInstanceId,
                    CamundaEngine.getTransactions(body.getVariables().getProcessID()).get(0).getResponderRole().toString(),
                    body.getVariables().getRelatedProducts().toString(),
                    federationInstanceId,
                    ProcessInstanceGroupDAOUtility.getSubmissionDate(processInstanceId)
            );
            HibernateUtilityRef.getInstance("bp-data-model").update(targetGroup);

            // save targetGroup
            BusinessProcessContextHandler.getBusinessProcessContextHandler().getBusinessProcessContext(businessContextId).setCreatedProcessInstanceGroupDAO(targetGroup);
        }
        else {
            ProcessInstanceFederationDAO federationDAO = new ProcessInstanceFederationDAO();
            federationDAO.setFederationInstanceId(federationInstanceId);
            federationDAO.setProcessInstanceID(processInstanceId);
            sourceGroup.getProcessInstances().add(federationDAO);
            sourceGroup = (ProcessInstanceGroupDAO) HibernateUtilityRef.getInstance("bp-data-model").update(sourceGroup);

            BusinessProcessContext businessProcessContext = BusinessProcessContextHandler.getBusinessProcessContextHandler().getBusinessProcessContext(businessContextId);
            // save added federationDA
            businessProcessContext.setAddedProcessInstanceFederationDAO(federationDAO);
            // save sourceGroup
            businessProcessContext.setUpdatedProcessInstanceGroupDAO(sourceGroup);
        }
    }
}
