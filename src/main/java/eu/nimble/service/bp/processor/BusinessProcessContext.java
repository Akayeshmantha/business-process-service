package eu.nimble.service.bp.processor;

import eu.nimble.service.bp.hyperjaxb.model.*;
import eu.nimble.service.bp.impl.util.persistence.HibernateUtilityRef;
import eu.nimble.utility.Configuration;

public class BusinessProcessContext {

    private ProcessInstanceInputMessageDAO createdMessageDAO;   // it is created when a process is started
    private ProcessDocumentMetadataDAO createdMetadataDAO;
    private Object createdDocument;
    private ProcessInstanceDAO createdProcessInstanceDAO;
    private ProcessInstanceGroupDAO createdProcessInstanceGroupDAO; // it is the group which is created for the process
    private ProcessInstanceFederationDAO addedProcessInstanceFederationDAO; // the process instance federation which is added to the
    private ProcessInstanceGroupDAO updatedProcessInstanceGroupDAO; // the group which is updated by adding a new process instance federation

    private ProcessDocumentStatus previousDocumentMetadataStatus; // while updating process document metadata,previous status should be saved
    private ProcessDocumentMetadataDAO updatedMetadataDAO;  // the process document metadata updated

    private ProcessInstanceDAO updatedProcessInstanceDAO; // updated while continuing a process
    private ProcessInstanceStatus previousProcessInstanceStatus; // status value of a process before it is updated

    private String id;                                          // each business process context has its unique id

    public void handleExceptions() {
        if(createdMessageDAO != null){
            HibernateUtilityRef.getInstance("bp-data-model").delete(createdMessageDAO);
        }
        if(createdMetadataDAO != null){
            HibernateUtilityRef.getInstance("bp-data-model").delete(createdMetadataDAO);
        }
        if(createdDocument != null){
            HibernateUtilityRef.getInstance(Configuration.UBL_PERSISTENCE_UNIT_NAME).delete(createdDocument);
        }
        if(createdProcessInstanceDAO != null){
            HibernateUtilityRef.getInstance("bp-data-model").delete(createdProcessInstanceDAO);
        }
        if(createdProcessInstanceGroupDAO != null){
            HibernateUtilityRef.getInstance("bp-data-model").delete(createdProcessInstanceGroupDAO);
        }
        if(updatedProcessInstanceGroupDAO != null && addedProcessInstanceFederationDAO != null){
            for(ProcessInstanceFederationDAO federationDAO: updatedProcessInstanceGroupDAO.getProcessInstances()){
                if(federationDAO.getProcessInstanceID().equals(addedProcessInstanceFederationDAO.getProcessInstanceID())  && federationDAO.getFederationInstanceId().equals(addedProcessInstanceFederationDAO.getFederationInstanceId())){
                    updatedProcessInstanceGroupDAO.getProcessInstances().remove(federationDAO);
                    HibernateUtilityRef.getInstance("bp-data-model").update(updatedProcessInstanceGroupDAO);
                }
            }
        }
        if(updatedMetadataDAO != null && previousDocumentMetadataStatus != null){
            updatedMetadataDAO.setStatus(previousDocumentMetadataStatus);
            HibernateUtilityRef.getInstance("bp-data-model").update(updatedMetadataDAO);
        }
        if(updatedProcessInstanceDAO != null && previousProcessInstanceStatus != null){
            updatedProcessInstanceDAO.setStatus(previousProcessInstanceStatus);
            HibernateUtilityRef.getInstance("bp-data-model").update(updatedProcessInstanceDAO);
        }
    }

    // Getters and Setters

    public ProcessInstanceInputMessageDAO getCreatedMessageDAO() {
        return createdMessageDAO;
    }

    public void setCreatedMessageDAO(ProcessInstanceInputMessageDAO createdMessageDAO) {
        this.createdMessageDAO = createdMessageDAO;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ProcessDocumentMetadataDAO getCreatedMetadataDAO() {
        return createdMetadataDAO;
    }

    public void setCreatedMetadataDAO(ProcessDocumentMetadataDAO createdMetadataDAO) {
        this.createdMetadataDAO = createdMetadataDAO;
    }

    public Object getCreatedDocument() {
        return createdDocument;
    }

    public void setCreatedDocument(Object createdDocument) {
        this.createdDocument = createdDocument;
    }

    public ProcessInstanceDAO getCreatedProcessInstanceDAO() {
        return createdProcessInstanceDAO;
    }

    public void setCreatedProcessInstanceDAO(ProcessInstanceDAO createdProcessInstanceDAO) {
        this.createdProcessInstanceDAO = createdProcessInstanceDAO;
    }

    public ProcessInstanceGroupDAO getCreatedProcessInstanceGroupDAO() {
        return createdProcessInstanceGroupDAO;
    }

    public void setCreatedProcessInstanceGroupDAO(ProcessInstanceGroupDAO createdProcessInstanceGroupDAO) {
        this.createdProcessInstanceGroupDAO = createdProcessInstanceGroupDAO;
    }

    public ProcessInstanceFederationDAO getAddedProcessInstanceFederationDAO() {
        return addedProcessInstanceFederationDAO;
    }

    public void setAddedProcessInstanceFederationDAO(ProcessInstanceFederationDAO addedProcessInstanceFederationDAO) {
        this.addedProcessInstanceFederationDAO = addedProcessInstanceFederationDAO;
    }

    public ProcessInstanceGroupDAO getUpdatedProcessInstanceGroupDAO() {
        return updatedProcessInstanceGroupDAO;
    }

    public void setUpdatedProcessInstanceGroupDAO(ProcessInstanceGroupDAO updatedProcessInstanceGroupDAO) {
        this.updatedProcessInstanceGroupDAO = updatedProcessInstanceGroupDAO;
    }

    public ProcessDocumentStatus getPreviousDocumentMetadataStatus() {
        return previousDocumentMetadataStatus;
    }

    public void setPreviousDocumentMetadataStatus(ProcessDocumentStatus previousDocumentMetadataStatus) {
        this.previousDocumentMetadataStatus = previousDocumentMetadataStatus;
    }

    public ProcessDocumentMetadataDAO getUpdatedMetadataDAO() {
        return updatedMetadataDAO;
    }

    public void setUpdatedMetadataDAO(ProcessDocumentMetadataDAO updatedMetadataDAO) {
        this.updatedMetadataDAO = updatedMetadataDAO;
    }

    public ProcessInstanceDAO getUpdatedProcessInstanceDAO() {
        return updatedProcessInstanceDAO;
    }

    public void setUpdatedProcessInstanceDAO(ProcessInstanceDAO updatedProcessInstanceDAO) {
        this.updatedProcessInstanceDAO = updatedProcessInstanceDAO;
    }

    public ProcessInstanceStatus getPreviousProcessInstanceStatus() {
        return previousProcessInstanceStatus;
    }

    public void setPreviousProcessInstanceStatus(ProcessInstanceStatus previousProcessInstanceStatus) {
        this.previousProcessInstanceStatus = previousProcessInstanceStatus;
    }
}
