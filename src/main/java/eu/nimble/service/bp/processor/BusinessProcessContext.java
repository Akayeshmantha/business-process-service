package eu.nimble.service.bp.processor;

import eu.nimble.service.bp.hyperjaxb.model.*;
import eu.nimble.service.bp.impl.util.persistence.HibernateUtilityRef;
import eu.nimble.utility.Configuration;

public class BusinessProcessContext {

//    private ProcessInstanceInputMessageDAO messageDAO;
//    private ProcessDocumentMetadataDAO metadataDAO;
//    private ProcessInstanceDAO processInstanceDAO;
//    private ProcessInstanceGroupDAO processInstanceGroupDAO1;
//    private ProcessInstanceGroupDAO processInstanceGroupDAO2;
//    private ProcessInstanceGroupDAO sourceGroup;
//    private ProcessInstanceGroupDAO associatedGroup;
//    private ProcessInstanceGroupDAO targetGroup;
//    private ProcessInstanceStatus previousStatus;
//    private ProcessDocumentStatus previousDocumentMetadataStatus;
//    private ProcessDocumentMetadataDAO updatedDocumentMetadata;
//    private ProcessInstanceGroupDAO updatedAssociatedGroup;
//    private Object document;
//    private String id;

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
//        if (messageDAO != null) {
//            HibernateUtilityRef.getInstance("bp-data-model").delete(messageDAO);
//        }
//        if (metadataDAO != null) {
//            if (previousDocumentMetadataStatus != null) {
//                updatedDocumentMetadata.setStatus(previousDocumentMetadataStatus);
//                HibernateUtilityRef.getInstance("bp-data-model").update(updatedDocumentMetadata);
//            }
//            HibernateUtilityRef.getInstance("bp-data-model").delete(metadataDAO);
//        }
//        if (document != null) {
//            HibernateUtilityRef.getInstance(Configuration.UBL_PERSISTENCE_UNIT_NAME).delete(document);
//        }
//        if (previousStatus == null && processInstanceDAO != null) {
//            HibernateUtilityRef.getInstance("bp-data-model").delete(processInstanceDAO);
//        }
//
//        if (processInstanceGroupDAO1 != null) {
//            HibernateUtilityRef.getInstance("bp-data-model").delete(processInstanceGroupDAO1);
//        }
//        if (processInstanceGroupDAO2 != null) {
//            HibernateUtilityRef.getInstance("bp-data-model").delete(processInstanceGroupDAO2);
//        }
//        if (sourceGroup != null) {
//            for (ProcessInstanceGroupDAO.ProcessInstanceGroupDAOProcessInstanceIDsItem p : sourceGroup.getProcessInstanceIDsItems()) {
//                if (p.getItem().equals(processInstanceDAO.getProcessInstanceID())) {
//                    HibernateUtilityRef.getInstance("bp-data-model").delete(p);
//                }
//            }
//            if (targetGroup != null) {
//                for (ProcessInstanceGroupDAO.ProcessInstanceGroupDAOAssociatedGroupsItem p : sourceGroup.getAssociatedGroupsItems()) {
//                    if (p.getItem().equals(targetGroup.getID())) {
//                        HibernateUtilityRef.getInstance("bp-data-model").delete(p);
//                    }
//                }
//            }
//        }
//        if (associatedGroup != null) {
//            for (ProcessInstanceGroupDAO.ProcessInstanceGroupDAOProcessInstanceIDsItem p : associatedGroup.getProcessInstanceIDsItems()) {
//                if (p.getItem().equals(processInstanceDAO.getProcessInstanceID())) {
//                    HibernateUtilityRef.getInstance("bp-data-model").delete(p);
//                }
//            }
//        }
//        if (targetGroup != null) {
//            HibernateUtilityRef.getInstance("bp-data-model").delete(targetGroup);
//        }
//        if (previousStatus != null && processInstanceDAO != null) {
//            processInstanceDAO.setStatus(ProcessInstanceStatus.fromValue(previousStatus.toString()));
//            HibernateUtilityRef.getInstance("bp-data-model").update(processInstanceDAO);
//        }
//        if (updatedAssociatedGroup != null) {
//            HibernateUtilityRef.getInstance("bp-data-model").delete(updatedAssociatedGroup);
//        }
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
