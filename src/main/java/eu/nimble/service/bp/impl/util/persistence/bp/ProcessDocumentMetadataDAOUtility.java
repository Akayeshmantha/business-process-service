package eu.nimble.service.bp.impl.util.persistence.bp;

import eu.nimble.service.bp.hyperjaxb.model.DocumentType;
import eu.nimble.service.bp.hyperjaxb.model.ProcessDocumentMetadataDAO;
import eu.nimble.service.bp.hyperjaxb.model.ProcessDocumentStatus;
import eu.nimble.service.bp.hyperjaxb.model.RoleType;
import eu.nimble.service.bp.impl.model.export.TransactionSummary;
import eu.nimble.service.bp.impl.model.statistics.BusinessProcessCount;
import eu.nimble.service.bp.impl.util.bp.BusinessProcessUtility;
import eu.nimble.service.bp.impl.util.persistence.catalogue.DocumentPersistenceUtility;
import eu.nimble.service.bp.impl.util.spring.SpringBridge;
import eu.nimble.service.bp.processor.BusinessProcessContext;
import eu.nimble.service.bp.processor.BusinessProcessContextHandler;
import eu.nimble.service.bp.swagger.model.ProcessDocumentMetadata;
import eu.nimble.service.bp.swagger.model.Transaction;
import eu.nimble.service.model.ubl.commonaggregatecomponents.DocumentReferenceType;
import eu.nimble.service.model.ubl.commonaggregatecomponents.PartyType;
import eu.nimble.service.model.ubl.commonaggregatecomponents.PersonType;
import eu.nimble.service.model.ubl.document.IDocument;
import eu.nimble.utility.DateUtility;
import eu.nimble.utility.JsonSerializationUtility;
import eu.nimble.utility.persistence.GenericJPARepository;
import eu.nimble.utility.persistence.JPARepositoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Created by suat on 16-Oct-18.
 */
public class ProcessDocumentMetadataDAOUtility {
    private static final String QUERY_GET_BY_DOCUMENT_ID = "SELECT pdm FROM ProcessDocumentMetadataDAO pdm WHERE pdm.documentID = :documentId";
    private static final String QUERY_GET_BY_PROCESS_INSTANCE_ID = "SELECT pdm FROM ProcessDocumentMetadataDAO pdm WHERE pdm.processInstanceID = :processInstanceId ORDER BY pdm.submissionDate ASC";
    private static final String QUERY_GET_BY_RESPONDER_ID = "SELECT DISTINCT metadataDAO.processInstanceID FROM ProcessDocumentMetadataDAO metadataDAO WHERE metadataDAO.responderID = :responderId";
    private static final String QUERY_GET_BY_PARTY_ID = "SELECT pdm FROM ProcessDocumentMetadataDAO pdm WHERE pdm.initiatorID = :partyId OR pdm.responderID = :partyId";

    /**
     * The conditions for the queries below are initialized during the query instantiation
     */
    private static final String QUERY_GET_TRANSACTION_COUNT = "SELECT count(*) FROM ProcessDocumentMetadataDAO documentMetadata %s";
    private static final String QUERY_GET_DOCUMENT_IDS = "SELECT documentMetadata.documentID FROM ProcessDocumentMetadataDAO documentMetadata %s";
    private static final String QUERY_GET_GROUPED_TRANSACTIONS = "SELECT documentMetadata.initiatorID, documentMetadata.type, documentMetadata.status, count(*) FROM ProcessDocumentMetadataDAO documentMetadata %s";
    private static final String QUERY_GET_METADATA_AND_ARCHIVED_STATUS_BY_ARBITRARY_CONDITIONS = "" +
            "SELECT DISTINCT documentMetadata FROM" +
            " CollaborationGroupDAO cg join cg.associatedProcessInstanceGroups pig join pig.processInstanceIDsItems pid," +
            " ProcessDocumentMetadataDAO documentMetadata" +
            " %s AND documentMetadata.processInstanceID = pid.item " +
            " ORDER BY documentMetadata.submissionDate ASC";
    private static final String QUERY_GET_METADATA_BY_ARBITRARY_CONDITIONS = "SELECT document FROM ProcessDocumentMetadataDAO document WHERE (%s)";
    private static final String QUERY_GET_METADATA_BY_PROCESS_INSTANCE_ID_AND_ARBITRARY_CONDITIONS = "SELECT documentMetadata FROM ProcessDocumentMetadataDAO documentMetadata WHERE documentMetadata.processInstanceID=:processInstanceId AND %s";

    private static final Logger logger = LoggerFactory.getLogger(ProcessDocumentMetadataDAOUtility.class);

    public static ProcessDocumentMetadataDAO findByDocumentID(String documentId) {
        return new JPARepositoryFactory().forBpRepository(true).getSingleEntity(QUERY_GET_BY_DOCUMENT_ID, new String[]{"documentId"}, new Object[]{documentId});
    }

    public static List<ProcessDocumentMetadataDAO> findByProcessInstanceID(String processInstanceId) {
        return new JPARepositoryFactory().forBpRepository(true).getEntities(QUERY_GET_BY_PROCESS_INSTANCE_ID, new String[]{"processInstanceId"}, new Object[]{processInstanceId});
    }

    public static List<ProcessDocumentMetadataDAO> findByPartyID(String partyId) {
        return new JPARepositoryFactory().forBpRepository(true).getEntities(QUERY_GET_BY_PARTY_ID, new String[]{"partyId"}, new Object[]{partyId});
    }

    public static List<String> getProcessInstanceIds(String responderId) {
        return new JPARepositoryFactory().forBpRepository().getEntities(QUERY_GET_BY_RESPONDER_ID, new String[]{"responderId"}, new Object[]{responderId});
    }

    public static ProcessDocumentMetadataDAO getDocumentOfTheOtherParty(String processInstanceId, String thisPartyId) {
        List<ProcessDocumentMetadataDAO> documentMetadataDAOs = ProcessDocumentMetadataDAOUtility.findByProcessInstanceID(processInstanceId);
        // if this party is the initiator party, return the second document metadata
        if(documentMetadataDAOs.get(0).getInitiatorID().contentEquals(thisPartyId)) {
            if(documentMetadataDAOs.size() > 1) {
                return documentMetadataDAOs.get(1);
            } else {
                return null;
            }
        } else {
            return documentMetadataDAOs.get(0);
        }
    }

    public static String getTradingPartnerId(String processInstanceId, String thisPartyId) {
        ProcessDocumentMetadataDAO firstDocumentMetadataDAO = ProcessDocumentMetadataDAOUtility.findByProcessInstanceID(processInstanceId).get(0);
        return getTradingPartnerId(firstDocumentMetadataDAO, thisPartyId);
    }

    public static String getTradingPartnerId(ProcessDocumentMetadataDAO documentMetadata, String thisPartyId) {
        if(documentMetadata.getInitiatorID().contentEquals(thisPartyId)) {
            return documentMetadata.getResponderID();
        } else {
            return documentMetadata.getInitiatorID();
        }
    }

    public static RoleType getPartnerRoleInTransaction(ProcessDocumentMetadataDAO documentMetadata, String thisPartyId) {
        if(documentMetadata.getInitiatorID().contentEquals(thisPartyId)) {
            return RoleType.BUYER;
        } else {
            return RoleType.SELLER;
        }
    }

    public static List<ProcessDocumentMetadataDAO> getProcessDocumentMetadata(String partnerID, String type) {
        return getProcessDocumentMetadata(partnerID, type, null, null);
    }

    public static List<ProcessDocumentMetadataDAO> getProcessDocumentMetadata(String partnerID, String type, String source) {
        return getProcessDocumentMetadata(partnerID, type, null, source);
    }

    public static List<ProcessDocumentMetadataDAO> getProcessDocumentMetadata(String partnerID, String type, String status, String source) {
        List<String> parameterNames = new ArrayList<>();
        List<String> parameterValues = new ArrayList<>();
        String conditions = "";

        if (source != null && partnerID != null) {
            String attribute = source.equals("SENT") ? "initiatorID" : "responderID";
            conditions += " document." + attribute + " = :partnerId ";
            parameterNames.add("partnerId");
            parameterValues.add(partnerID);

        } else if (source == null && partnerID != null) {
            conditions += " (document.initiatorID = :partnerId or document.responderID = :partnerId) ";
            parameterNames.add("partnerId");
            parameterValues.add(partnerID);
        }

        if (type != null) {
            conditions += " and document.type = '" + DocumentType.valueOf(type).toString() + "' ";
        }

        if (status != null) {
            conditions += " and document.status = '" + ProcessDocumentStatus.valueOf(status).toString() + "'";
        }

        String query = String.format(QUERY_GET_METADATA_BY_ARBITRARY_CONDITIONS, conditions);
        List<ProcessDocumentMetadataDAO> resultSet = new JPARepositoryFactory().forBpRepository(true).getEntities(query, parameterNames.toArray(new String[parameterNames.size()]), parameterValues.toArray(new String[parameterValues.size()]));
        return resultSet;
    }

    public static List<String> getDocumentIds(Integer partyId, List<String> documentTypes, String role, String startDateStr, String endDateStr, String status) {
        if(role == null) {
            role = RoleType.SELLER.toString();
        }
        DocumentMetadataQuery query = getDocumentMetadataQuery(partyId, documentTypes, role, startDateStr, endDateStr, status, null, null, DocumentMetadataQueryType.DOCUMENT_IDS);
        List<String> documentIds = new JPARepositoryFactory().forBpRepository().getEntities(query.query, query.parameterNames.toArray(new String[query.parameterNames.size()]), query.parameterValues.toArray());
        return documentIds;
    }

    public static int getTransactionCount(Integer partyId, List<String> documentTypes, String role, String startDateStr, String endDateStr, String status) {
        if(role == null) {
            role = RoleType.SELLER.toString();
        }
        DocumentMetadataQuery query = getDocumentMetadataQuery(partyId, documentTypes, role, startDateStr, endDateStr, status, null, null, DocumentMetadataQueryType.TOTAL_TRANSACTION_COUNT);
        int count = ((Long) new JPARepositoryFactory().forBpRepository().getSingleEntity(query.query, query.parameterNames.toArray(new String[query.parameterNames.size()]), query.parameterValues.toArray())).intValue();
        return count;
    }

    public static List<TransactionSummary> getTransactionSummaries(String partyId, String userId, String direction, Boolean archived, String bearerToken) {
        List<TransactionSummary> summaries = new ArrayList<>();
        ExecutorService threadPool = Executors.newCachedThreadPool();

        try {
            String role = null;
            if (direction != null) {
                if (direction.compareToIgnoreCase("incoming") == 0) {
                    role = RoleType.SELLER.toString();
                } else {
                    role = RoleType.BUYER.toString();
                }
            }

            DocumentMetadataQuery query = getDocumentMetadataQuery(Integer.parseInt(partyId), null, role, null, null, null, userId, archived, DocumentMetadataQueryType.TRANSACTION_METADATA);
            List<ProcessDocumentMetadataDAO> metadataObjects = new JPARepositoryFactory().forBpRepository(true).getEntities(query.query, query.parameterNames.toArray(new String[query.parameterNames.size()]), query.parameterValues.toArray());
            Set<String> partyIds = new HashSet<>();
            Set<String> personIds = new HashSet<>();
            for (ProcessDocumentMetadataDAO documentMetadata : metadataObjects) {
                TransactionSummary transactionSummary = new TransactionSummary();

                // set process instance id
                transactionSummary.setBusinessProcessInstanceId(documentMetadata.getProcessInstanceID());

                //set user id
                RoleType partyRoleInTransaction = getPartnerRoleInTransaction(documentMetadata, partyId);
                if (partyRoleInTransaction.equals(RoleType.BUYER)) {
                    transactionSummary.setCompanyUserId(documentMetadata.getCreatorUserID());
                }
                personIds.add(documentMetadata.getCreatorUserID());

                // set corresponding partner id
                String tradingPartnerId = getTradingPartnerId(documentMetadata, partyId);
                transactionSummary.setCorrespondingCompanyId(tradingPartnerId);
                partyIds.add(tradingPartnerId);

                // set direction
                if (partyRoleInTransaction.equals(RoleType.BUYER)) {
                    transactionSummary.setTransactionDirection("outgoing");
                } else {
                    transactionSummary.setTransactionDirection("incoming");
                }

                // set document id and document itself
                IDocument document = DocumentPersistenceUtility.getUBLDocument(documentMetadata.getDocumentID(), documentMetadata.getType());
                if(document == null) {
                    logger.warn("No document found for the document metadata: {}", JsonSerializationUtility.serializeEntitySilently(documentMetadata));
                    continue;
                }
                transactionSummary.setExchangedDocument(document);
                transactionSummary.setExchangedDocumentId(documentMetadata.getDocumentID());

                // set transaction date
                transactionSummary.setTransactionTime(documentMetadata.getSubmissionDate());

                // set auxiliary files
                transactionSummary.setAuxiliaryFiles(document.getAdditionalDocuments());
                transactionSummary.setAuxiliaryFileIds(new ArrayList<>());
                for (DocumentReferenceType auxiliaryFile : document.getAdditionalDocuments()) {
                    transactionSummary.getAuxiliaryFileIds().add(auxiliaryFile.getAttachment().getEmbeddedDocumentBinaryObject().getUri());
                }

                summaries.add(transactionSummary);
            }

            // fetch and set company names
            Future<List<PartyType>> partiesFuture = getParties(threadPool, partyIds, bearerToken);
            List<Future<PersonType>> personFutures = getPersons(threadPool, personIds, bearerToken);

            try {
                List<PartyType> parties = partiesFuture.get();
                for(TransactionSummary summary : summaries) {
                    for(PartyType party : parties) {
                        if(summary.getCorrespondingCompanyId().equals(party.getPartyIdentification().get(0).getID())) {
                            summary.setCorrespondingCompanyName(party.getPartyName().get(0).getName().getValue());
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("Failed to retrieve parties while exporting transactions: {}", partyIds.toString(), e);
            }

            // fetch and set company names
            for(Future<PersonType> personFuture : personFutures) {
                try {
                    PersonType person = personFuture.get();
                    if(person == null) {
                        continue;
                    }
                    for(TransactionSummary summary : summaries) {
                        if(summary.getCompanyUserId() != null && summary.getCompanyUserId().equals(person.getID())) {
                            summary.setCompanyUserName(person.getFirstName() + " " + person.getFamilyName());
                        }
                    }

                } catch (Exception e) {
                    logger.warn("Failed to retrieve persons while exporting transactions: {}", partyIds.toString(), e);
                }
            }
        } finally {
            if(threadPool != null) {
                threadPool.shutdown();
            }
        }

        return summaries;
    }

    private static Future<List<PartyType>> getParties(ExecutorService threadPool, Set<String> partyIds, String bearerToken) {
        Callable<List<PartyType>> callable = () -> {
            try {
                return SpringBridge.getInstance().getiIdentityClientTyped().getParties(bearerToken, new ArrayList<>(partyIds));
            } catch (IOException e) {
                logger.error("Failed to get parties while exporting transactions: {}", partyIds, e);
                return new ArrayList<>();
            }
        };
        return threadPool.submit(callable);
    }

    private static List<Future<PersonType>> getPersons(ExecutorService threadPool, Set<String> personIds, String bearerToken) {
        List<Future<PersonType>> personFutures = new ArrayList<>();
        for(String personId : personIds) {
            Callable<PersonType> callable = () -> {
                try {
                    return SpringBridge.getInstance().getiIdentityClientTyped().getPerson(bearerToken, personId);
                } catch (IOException e) {
                    logger.error("Failed to get person while exporting transactions: {}", personId, e);
                    return null;
                }
            };
            personFutures.add(threadPool.submit(callable));
        }
        return personFutures;
    }

    public static BusinessProcessCount getGroupTransactionCounts(Integer partyId, String startDateStr, String endDateStr, String role, String bearerToken) {
        if(role == null) {
            role = RoleType.SELLER.toString();
        }
        DocumentMetadataQuery query = getDocumentMetadataQuery(partyId, new ArrayList<>(), role, startDateStr, endDateStr, null, null, null, DocumentMetadataQueryType.GROUPED_TRANSACTION_COUNT);

        List<Object> results = new JPARepositoryFactory().forBpRepository(true).getEntities(query.query, query.parameterNames.toArray(new String[query.parameterNames.size()]), query.parameterValues.toArray());

        BusinessProcessCount counts = new BusinessProcessCount();
        for (Object result : results) {
            Object[] resultItems = (Object[]) result;
            PartyType partyType = null;
            try {
                partyType = SpringBridge.getInstance().getiIdentityClientTyped().getParty(bearerToken, (String) resultItems[0]);
            } catch (IOException e) {
                String msg = String.format("Failed to get transaction counts for party: %s, role: %s", partyId, role);
                logger.error("msg");
                throw new RuntimeException(msg, e);
            }
            counts.addCount((String) resultItems[0], resultItems[1].toString(), resultItems[2].toString(), (Long) resultItems[3], partyType.getPartyName().get(0).getName().getValue());
        }
        return counts;
    }

    private static DocumentMetadataQuery getDocumentMetadataQuery(Integer partyId,
                                                                  List<String> documentTypes,
                                                                  String role,
                                                                  String startDateStr,
                                                                  String endDateStr,
                                                                  String status,
                                                                  String userId,
                                                                  Boolean belongsToArchivedCollaborationGroup,
                                                                  DocumentMetadataQueryType queryType) {
        DocumentMetadataQuery query = new DocumentMetadataQuery();
        List<String> parameterNames = query.parameterNames;
        List<Object> parameterValues = query.parameterValues;

        String conditions = "";
        boolean filterExists = false;

        if (partyId != null) {
            if(role != null) {
                conditions += " where documentMetadata." + (role.equals(RoleType.BUYER.toString()) ? "initiatorID" : "responderID") + " = :partyId ";
            } else {
                conditions += " where (documentMetadata.initiatorID = :partyId OR documentMetadata.responderID = :partyId) ";
            }

            filterExists = true;
            parameterNames.add("partyId");
            parameterValues.add(partyId.toString());
        }

        if (startDateStr != null || endDateStr != null) {
            if (!filterExists) {
                conditions += " where";
            } else {
                conditions += " and";
            }

            if (startDateStr != null && endDateStr != null) {
                conditions += " documentMetadata.submissionDate between :startTime and :endTime";

                parameterNames.add("startTime");
                parameterValues.add(DateUtility.transformInputDateToDbDate(startDateStr));
                parameterNames.add("endTime");
                parameterValues.add(DateUtility.transformInputDateToMaxDbDate(endDateStr));

            } else if (startDateStr != null) {
                conditions += " documentMetadata.submissionDate >= :startTime";

                parameterNames.add("startTime");
                parameterValues.add(DateUtility.transformInputDateToDbDate(startDateStr));

            } else {
                conditions += " documentMetadata.submissionDate <= :endTime";

                parameterNames.add("endTime");
                parameterValues.add(DateUtility.transformInputDateToMaxDbDate(endDateStr));
            }
            filterExists = true;
        }

        if (documentTypes != null && documentTypes.size() > 0) {
            if (!filterExists) {
                conditions += " where (";
            } else {
                conditions += " and(";
            }
            for (int i = 0; i < documentTypes.size() - 1; i++) {
                conditions += " documentMetadata.type = '" + DocumentType.valueOf(documentTypes.get(i)).toString() + "' or";
            }
            conditions += " documentMetadata.type = '" + DocumentType.valueOf(documentTypes.get(documentTypes.size() - 1)).toString() + "')";
            filterExists = true;
        }

        if (status != null) {
            if (!filterExists) {
                conditions += " where ";
            } else {
                conditions += " and ";
            }
            conditions += " documentMetadata.status = '" + ProcessDocumentStatus.valueOf(status).toString() + "'";
        }

        if(userId != null) {
            if (!filterExists) {
                conditions += " where ";
            } else {
                conditions += " and ";
            }
            conditions += " documentMetadata.creatorUserID = :userId";

            parameterNames.add("userId");
            parameterValues.add(userId);
        }

        if (queryType.equals(DocumentMetadataQueryType.GROUPED_TRANSACTION_COUNT)) {
            conditions += " group by documentMetadata.initiatorID, documentMetadata.type, documentMetadata.status";
        } else if (queryType.equals(DocumentMetadataQueryType.TRANSACTION_METADATA)) {
            // check archived status
            if(belongsToArchivedCollaborationGroup != null) {
                conditions += " AND cg.archived = :archived";

                parameterNames.add("archived");
                parameterValues.add(belongsToArchivedCollaborationGroup);
            }
        }

        if (queryType.equals(DocumentMetadataQueryType.TOTAL_TRANSACTION_COUNT)) {
            query.query = String.format(QUERY_GET_TRANSACTION_COUNT, conditions);
        } else if (queryType.equals(DocumentMetadataQueryType.DOCUMENT_IDS)) {
            query.query = String.format(QUERY_GET_DOCUMENT_IDS, conditions);
        } else if (queryType.equals(DocumentMetadataQueryType.GROUPED_TRANSACTION_COUNT)) {
            query.query = String.format(QUERY_GET_GROUPED_TRANSACTIONS, conditions);
        } else if (queryType.equals(DocumentMetadataQueryType.TRANSACTION_METADATA)) {
            query.query = String.format(QUERY_GET_METADATA_AND_ARCHIVED_STATUS_BY_ARBITRARY_CONDITIONS, conditions);
        }
        return query;
    }

    public static ProcessDocumentMetadata getDocumentMetadata(String documentId) {
        ProcessDocumentMetadataDAO processDocumentDAO = ProcessDocumentMetadataDAOUtility.findByDocumentID(documentId);
        ProcessDocumentMetadata processDocument = HibernateSwaggerObjectMapper.createProcessDocumentMetadata(processDocumentDAO);
        return processDocument;
    }

    public static void updateDocumentMetadata(String processContextId, ProcessDocumentMetadata body) {
        BusinessProcessContext businessProcessContext = BusinessProcessContextHandler.getBusinessProcessContextHandler().getBusinessProcessContext(processContextId);

        ProcessDocumentMetadataDAO storedDocumentDAO = ProcessDocumentMetadataDAOUtility.findByDocumentID(body.getDocumentID());

        businessProcessContext.setPreviousDocumentMetadataStatus(storedDocumentDAO.getStatus());

        ProcessDocumentMetadataDAO newDocumentDAO = HibernateSwaggerObjectMapper.createProcessDocumentMetadata_DAO(body);

        GenericJPARepository repo = new JPARepositoryFactory().forBpRepository();
        repo.deleteEntityByHjid(ProcessDocumentMetadataDAO.class, storedDocumentDAO.getHjid());
        repo.persistEntity(newDocumentDAO);

        businessProcessContext.setUpdatedDocumentMetadata(newDocumentDAO);
    }

    public static ProcessDocumentMetadata getOrderResponseMetadataByOrderId(String documentID) {
        String id = DocumentPersistenceUtility.getOrderResponseIdByOrderId(documentID);
        return getDocumentMetadata(id);
    }

    public static ProcessDocumentMetadata getRequestMetadata(String processInstanceId) {
        List<Transaction.DocumentTypeEnum> documentTypes = BusinessProcessUtility.getInitialDocumentsForAllProcesses();
        List<String> parameterNames = new ArrayList<>();
        List<Object> parameterValues = new ArrayList<>();
        parameterNames.add("processInstanceId");
        parameterValues.add(processInstanceId);
        String query = String.format(QUERY_GET_METADATA_BY_PROCESS_INSTANCE_ID_AND_ARBITRARY_CONDITIONS, createConditionsForMetadataQuery(documentTypes, parameterNames, parameterValues));
        ProcessDocumentMetadataDAO processDocumentDAO = new JPARepositoryFactory().forBpRepository(true).getSingleEntity(query, parameterNames.toArray(new String[parameterNames.size()]), parameterValues.toArray());
        return HibernateSwaggerObjectMapper.createProcessDocumentMetadata(processDocumentDAO);
    }

    public static ProcessDocumentMetadata getResponseMetadata(String processInstanceId) {
        List<Transaction.DocumentTypeEnum> documentTypes = BusinessProcessUtility.getResponseDocumentsForAllProcesses();
        List<String> parameterNames = new ArrayList<>();
        List<Object> parameterValues = new ArrayList<>();
        parameterNames.add("processInstanceId");
        parameterValues.add(processInstanceId);
        String query = String.format(QUERY_GET_METADATA_BY_PROCESS_INSTANCE_ID_AND_ARBITRARY_CONDITIONS, createConditionsForMetadataQuery(documentTypes, parameterNames, parameterValues));
        ProcessDocumentMetadataDAO processDocumentDAO = new JPARepositoryFactory().forBpRepository(true).getSingleEntity(query, parameterNames.toArray(new String[parameterNames.size()]), parameterValues.toArray());
        if (processDocumentDAO == null) {
            return null;
        }
        return HibernateSwaggerObjectMapper.createProcessDocumentMetadata(processDocumentDAO);
    }

    private static String createConditionsForMetadataQuery(List<Transaction.DocumentTypeEnum> documentTypes, List<String> parameterNames, List<Object> parameterValues) {
        StringBuilder sb = new StringBuilder("(");
        int i=0;
        for(; i<documentTypes.size()-1; i++) {
            sb.append("documentMetadata.type = :doc").append(i).append(" OR ");
            parameterNames.add("doc" + i);
            parameterValues.add(DocumentType.valueOf(documentTypes.get(i).toString()));
        }
        sb.append("documentMetadata.type = :doc").append(i);
        parameterNames.add("doc" + i);
        parameterValues.add(DocumentType.valueOf(documentTypes.get(i).toString()));
        sb.append(")");
        return sb.toString();
    }

    private enum DocumentMetadataQueryType {
        DOCUMENT_IDS, TOTAL_TRANSACTION_COUNT, GROUPED_TRANSACTION_COUNT, TRANSACTION_METADATA
    }

    private static class DocumentMetadataQuery {
        private String query;
        private List<String> parameterNames = new ArrayList<>();
        private List<Object> parameterValues = new ArrayList<>();
    }
}
