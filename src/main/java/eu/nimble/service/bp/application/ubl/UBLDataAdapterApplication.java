package eu.nimble.service.bp.application.ubl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.nimble.service.bp.application.IBusinessProcessApplication;
import eu.nimble.service.bp.impl.util.persistence.DocumentDAOUtility;
import eu.nimble.service.bp.swagger.model.ProcessDocumentMetadata;
import eu.nimble.service.model.ubl.despatchadvice.DespatchAdviceType;
import eu.nimble.service.model.ubl.order.ObjectFactory;
import eu.nimble.service.model.ubl.order.OrderType;
import eu.nimble.service.model.ubl.orderresponsesimple.OrderResponseSimpleType;
import eu.nimble.service.model.ubl.quotation.QuotationType;
import eu.nimble.service.model.ubl.receiptadvice.ReceiptAdviceType;
import eu.nimble.service.model.ubl.requestforquotation.RequestForQuotationType;
import eu.nimble.service.model.ubl.transportexecutionplan.TransportExecutionPlanType;
import eu.nimble.service.model.ubl.transportexecutionplanrequest.TransportExecutionPlanRequestType;
import eu.nimble.utility.DateUtility;
import eu.nimble.utility.JAXBUtility;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by yildiray on 6/5/2017.
 */
public class UBLDataAdapterApplication implements IBusinessProcessApplication {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Object createDocument(String initiatorID, String responderID, String content, ProcessDocumentMetadata.TypeEnum documentType) {
        ObjectMapper mapper = new ObjectMapper();
        mapper = mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper = mapper.configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true);

        if(documentType == ProcessDocumentMetadata.TypeEnum.ORDER) {
            try {
                OrderType order = mapper.readValue(content, OrderType.class);

                ObjectFactory factory = new ObjectFactory();
                logger.debug(" $$$ Created document: {}", JAXBUtility.serialize(order, factory.createOrder(order)));

                return order;
            } catch (IOException e) {
                logger.error("", e);
            }
        } else if(documentType == ProcessDocumentMetadata.TypeEnum.ORDERRESPONSESIMPLE) {
            try {
                OrderResponseSimpleType orderResponse = mapper.readValue(content, OrderResponseSimpleType.class);

                eu.nimble.service.model.ubl.orderresponsesimple.ObjectFactory factory = new eu.nimble.service.model.ubl.orderresponsesimple.ObjectFactory();
                logger.debug(" $$$ Created document: {}", JAXBUtility.serialize(orderResponse, factory.createOrderResponseSimple(orderResponse)));

                return orderResponse;
            } catch (IOException e) {
                logger.error("", e);
            }
        } else if(documentType == ProcessDocumentMetadata.TypeEnum.REQUESTFORQUOTATION) {
            try {
                RequestForQuotationType rfq = mapper.readValue(content, RequestForQuotationType.class);

                eu.nimble.service.model.ubl.requestforquotation.ObjectFactory factory = new eu.nimble.service.model.ubl.requestforquotation.ObjectFactory();
                logger.debug(" $$$ Created document: {}", JAXBUtility.serialize(rfq, factory.createRequestForQuotation(rfq)));

                return rfq;
            } catch (IOException e) {
                logger.error("", e);
            }
        } else if(documentType == ProcessDocumentMetadata.TypeEnum.QUOTATION) {
            try {
                QuotationType quotation = mapper.readValue(content, QuotationType.class);

                eu.nimble.service.model.ubl.quotation.ObjectFactory factory = new eu.nimble.service.model.ubl.quotation.ObjectFactory();
                logger.debug(" $$$ Created document: {}", JAXBUtility.serialize(quotation, factory.createQuotation(quotation)));

                return quotation;
            } catch (IOException e) {
                logger.error("", e);
            }
        } else if(documentType == ProcessDocumentMetadata.TypeEnum.DESPATCHADVICE) {
            try {
                DespatchAdviceType despatchAdvice = mapper.readValue(content, DespatchAdviceType.class);

                eu.nimble.service.model.ubl.despatchadvice.ObjectFactory factory = new eu.nimble.service.model.ubl.despatchadvice.ObjectFactory();
                logger.debug(" $$$ Created document: {}", JAXBUtility.serialize(despatchAdvice, factory.createDespatchAdvice(despatchAdvice)));

                return despatchAdvice;
            } catch (IOException e) {
                logger.error("", e);
            }
        } else if(documentType == ProcessDocumentMetadata.TypeEnum.RECEIPTADVICE) {
            try {
                ReceiptAdviceType receiptAdvice = mapper.readValue(content, ReceiptAdviceType.class);

                eu.nimble.service.model.ubl.receiptadvice.ObjectFactory factory = new eu.nimble.service.model.ubl.receiptadvice.ObjectFactory();
                logger.debug(" $$$ Created document: {}", JAXBUtility.serialize(receiptAdvice, factory.createReceiptAdvice(receiptAdvice)));

                return receiptAdvice;
            } catch (IOException e) {
                logger.error("", e);
            }

        } else if(documentType == ProcessDocumentMetadata.TypeEnum.TRANSPORTEXECUTIONPLANREQUEST) {
            try {
                TransportExecutionPlanRequestType transportExecutionPlanRequest = mapper.readValue(content, TransportExecutionPlanRequestType.class);

                eu.nimble.service.model.ubl.transportexecutionplanrequest.ObjectFactory factory = new eu.nimble.service.model.ubl.transportexecutionplanrequest.ObjectFactory();
                logger.debug(" $$$ Created document: {}", JAXBUtility.serialize(transportExecutionPlanRequest, factory.createTransportExecutionPlanRequest(transportExecutionPlanRequest)));

                return transportExecutionPlanRequest;
            } catch (IOException e) {
                logger.error("", e);
            }

        } else if(documentType == ProcessDocumentMetadata.TypeEnum.TRANSPORTEXECUTIONPLAN) {
            try {
                TransportExecutionPlanType transportExecutionPlan = mapper.readValue(content, TransportExecutionPlanType.class);

                eu.nimble.service.model.ubl.transportexecutionplan.ObjectFactory factory = new eu.nimble.service.model.ubl.transportexecutionplan.ObjectFactory();
                logger.debug(" $$$ Created document: {}", JAXBUtility.serialize(transportExecutionPlan, factory.createTransportExecutionPlan(transportExecutionPlan)));

                return transportExecutionPlan;
            } catch (IOException e) {
                logger.error("", e);
            }
        }

        return null;
    }

    @Override
    public void saveDocument(String processInstanceId, String initiatorID, String responderID,
                            Object document) {
        ProcessDocumentMetadata documentMetadata = new ProcessDocumentMetadata();
        documentMetadata.setInitiatorID(initiatorID);
        documentMetadata.setResponderID(responderID);
        documentMetadata.setProcessInstanceID(processInstanceId);

        DateTime submissionDate = new DateTime();
        documentMetadata.setSubmissionDate(DateUtility.convert(submissionDate));

        if(document instanceof OrderType) {
            OrderType order = (OrderType) document;
            documentMetadata.setDocumentID(order.getID());
            documentMetadata.setStatus(ProcessDocumentMetadata.StatusEnum.WAITINGRESPONSE);
            documentMetadata.setType(ProcessDocumentMetadata.TypeEnum.ORDER);

        } else if(document instanceof OrderResponseSimpleType) {
            OrderResponseSimpleType orderResponse = (OrderResponseSimpleType) document;
            documentMetadata.setDocumentID(orderResponse.getID());
            documentMetadata.setStatus(ProcessDocumentMetadata.StatusEnum.APPROVED);
            documentMetadata.setType(ProcessDocumentMetadata.TypeEnum.ORDERRESPONSESIMPLE);

        } else if(document instanceof  RequestForQuotationType) {
            RequestForQuotationType rfq = (RequestForQuotationType) document;
            documentMetadata.setDocumentID(rfq.getID());
            documentMetadata.setStatus(ProcessDocumentMetadata.StatusEnum.WAITINGRESPONSE);
            documentMetadata.setType(ProcessDocumentMetadata.TypeEnum.REQUESTFORQUOTATION);

        } else if(document instanceof  QuotationType) {
            QuotationType quotation = (QuotationType) document;
            documentMetadata.setDocumentID(quotation.getID());
            documentMetadata.setStatus(ProcessDocumentMetadata.StatusEnum.APPROVED);
            documentMetadata.setType(ProcessDocumentMetadata.TypeEnum.QUOTATION);

        } else if(document instanceof  DespatchAdviceType) {
            DespatchAdviceType despatchAdvice = (DespatchAdviceType) document;
            documentMetadata.setDocumentID(despatchAdvice.getID());
            documentMetadata.setStatus(ProcessDocumentMetadata.StatusEnum.WAITINGRESPONSE);
            documentMetadata.setType(ProcessDocumentMetadata.TypeEnum.DESPATCHADVICE);

        } else if(document instanceof  ReceiptAdviceType) {
            ReceiptAdviceType receiptAdvice = (ReceiptAdviceType) document;
            documentMetadata.setDocumentID(receiptAdvice.getID());
            documentMetadata.setStatus(ProcessDocumentMetadata.StatusEnum.APPROVED);
            documentMetadata.setType(ProcessDocumentMetadata.TypeEnum.RECEIPTADVICE);

        } else if(document instanceof TransportExecutionPlanRequestType) {
            TransportExecutionPlanRequestType transportExecutionPlanRequestType = (TransportExecutionPlanRequestType) document;
            documentMetadata.setDocumentID(transportExecutionPlanRequestType.getID());
            documentMetadata.setStatus(ProcessDocumentMetadata.StatusEnum.WAITINGRESPONSE);
            documentMetadata.setType(ProcessDocumentMetadata.TypeEnum.TRANSPORTEXECUTIONPLANREQUEST);

        } else if(document instanceof TransportExecutionPlanType) {
            TransportExecutionPlanType transportExecutionPlanType = (TransportExecutionPlanType) document;
            documentMetadata.setDocumentID(transportExecutionPlanType.getID());
            documentMetadata.setStatus(ProcessDocumentMetadata.StatusEnum.APPROVED);
            documentMetadata.setType(ProcessDocumentMetadata.TypeEnum.TRANSPORTEXECUTIONPLAN);
        }

        // persist the document metadata
        DocumentDAOUtility.addDocumentWithMetadata(documentMetadata, document);
    }

    @Override
    public void sendDocument(String processInstanceId, String initiatorID, String responderID, Object document) {
        // TODO: Send email notification to the responder...

        // if this document is a response to an initiating document, set the response code of the initiating document
        // e.g OrderResponse to an Order
        if(document instanceof OrderResponseSimpleType) {
            OrderResponseSimpleType orderResponse = (OrderResponseSimpleType) document;
            String orderID = orderResponse.getOrderReference().getDocumentReference().getID();
            boolean isAccepted = orderResponse.isAcceptedIndicator();

            ProcessDocumentMetadata initiatingDocumentMetadata = DocumentDAOUtility.getDocumentMetadata(orderID);
            if(isAccepted)
                initiatingDocumentMetadata.setStatus(ProcessDocumentMetadata.StatusEnum.APPROVED);
            else
                initiatingDocumentMetadata.setStatus(ProcessDocumentMetadata.StatusEnum.DENIED);

            DocumentDAOUtility.updateDocumentMetadata(initiatingDocumentMetadata);

        } else if(document instanceof QuotationType) {
            QuotationType quotation = (QuotationType) document;
            String rfqID = quotation.getRequestForQuotationDocumentReference().getID();
            ProcessDocumentMetadata initiatingDocumentMetadata = DocumentDAOUtility.getDocumentMetadata(rfqID);
            initiatingDocumentMetadata.setStatus(ProcessDocumentMetadata.StatusEnum.APPROVED);

        } else if(document instanceof ReceiptAdviceType) {
            ReceiptAdviceType receiptAdvice = (ReceiptAdviceType) document;
            String despatchAdviceID = receiptAdvice.getDespatchDocumentReference().get(0).getID();
            ProcessDocumentMetadata initiatingDocumentMetadata = DocumentDAOUtility.getDocumentMetadata(despatchAdviceID);
            initiatingDocumentMetadata.setStatus(ProcessDocumentMetadata.StatusEnum.APPROVED);

        } else if(document instanceof TransportExecutionPlanType) {
            TransportExecutionPlanType transportExecutionPlanType = (TransportExecutionPlanType) document;
            String despatchAdviceID = transportExecutionPlanType.getTransportExecutionPlanRequestDocumentReference().getID();
            ProcessDocumentMetadata initiatingDocumentMetadata = DocumentDAOUtility.getDocumentMetadata(despatchAdviceID);
            initiatingDocumentMetadata.setStatus(ProcessDocumentMetadata.StatusEnum.APPROVED);
        }
    }

}
