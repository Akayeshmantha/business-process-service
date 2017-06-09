package eu.nimble.service.bp.application;

import eu.nimble.service.bp.swagger.model.ProcessDocumentMetadata;

/**
 * Created by yildiray on 6/5/2017.
 */
public interface IBusinessProcessApplication {
    public Object createDocument(String initiatorID, String responderID, String content,
                                 ProcessDocumentMetadata.TypeEnum documentType, String responseToDocumentID);

    public void saveDocument(String processInstanceId, String initiatorID, String responderID,
                     Object documentObject, ProcessDocumentMetadata.TypeEnum documentType, ProcessDocumentMetadata.StatusEnum documentStatus);

    public void sendDocument(String processInstanceId, String initiatorID, String responderID,
                             Object documentObject, ProcessDocumentMetadata.TypeEnum documentType, ProcessDocumentMetadata.StatusEnum documentStatus,
                             String initiatingDocumentID, ProcessDocumentMetadata.StatusEnum responseStatus);
}
