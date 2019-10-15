package it.unimib.disco.bigtwine.services.analysis.web.api;

import it.unimib.disco.bigtwine.services.analysis.domain.Analysis;
import it.unimib.disco.bigtwine.services.analysis.domain.Document;
import it.unimib.disco.bigtwine.services.analysis.domain.User;
import it.unimib.disco.bigtwine.services.analysis.domain.mapper.DocumentMapper;
import it.unimib.disco.bigtwine.services.analysis.security.SecurityUtils;
import it.unimib.disco.bigtwine.services.analysis.service.AnalysisService;
import it.unimib.disco.bigtwine.services.analysis.service.DocumentService;
import it.unimib.disco.bigtwine.services.analysis.web.api.errors.NoSuchEntityException;
import it.unimib.disco.bigtwine.services.analysis.web.api.errors.UnauthenticatedException;
import it.unimib.disco.bigtwine.services.analysis.web.api.errors.UnauthorizedException;
import it.unimib.disco.bigtwine.services.analysis.web.api.errors.UploadFailedException;
import it.unimib.disco.bigtwine.services.analysis.web.api.model.DocumentDTO;
import it.unimib.disco.bigtwine.services.analysis.web.api.util.AnalysisUtil;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.NativeWebRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@Service
public class DocumentsApiDelegateImpl implements DocumentsApiDelegate {
    private final Logger log = LoggerFactory.getLogger(DocumentsApiDelegateImpl.class);

    private final NativeWebRequest request;
    private DocumentService documentService;
    private AnalysisService analysisService;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public DocumentsApiDelegateImpl(
        NativeWebRequest request,
        DocumentService documentService,
        AnalysisService analysisService) {
        this.request = request;
        this.documentService = documentService;
        this.analysisService = analysisService;
    }

    private void checkFileOwnership(Document doc) {
        String userId = AnalysisUtil.getCurrentUserIdentifier()
            .orElseThrow(UnauthenticatedException::new);
        String docAnalysis = doc.getAnalysisId();

        if (StringUtils.isNotBlank(docAnalysis)) {
            Optional<Analysis> analysis = this.analysisService.findOne(docAnalysis);

            if (!(analysis.isPresent() && analysis.get().getOwner().getUid().equals(userId))) {
                throw new UnauthorizedException(String.format(
                    "Only the owner of the analysis '%s' can access this document",
                    docAnalysis));
            }
        } else {
            String docUploader = doc.getUser() != null ? doc.getUser().getUid() : null;
            if (!userId.equals(docUploader)) {
                throw new UnauthorizedException("Only the uploader can access this document");
            }
        }
    }

    private Document getDocumentById(String documentId) {
        Document doc = this.documentService.findOne(documentId)
            .orElseThrow(() -> new NoSuchEntityException(Document.class, documentId));

        this.checkFileOwnership(doc);

        return doc;
    }

    @Override
    public ResponseEntity<DocumentDTO> getDocumentMetaV1(String documentId) {
        Document doc = getDocumentById(documentId);

        return ResponseEntity.ok(DocumentMapper.INSTANCE.dtoFromDocument(doc));
    }

    @Override
    public ResponseEntity<Resource> downloadDocumentV1(String documentId) {
        Document doc = getDocumentById(documentId);
        Resource resource = this.documentService
            .getDownloadableResource(doc)
            .orElseThrow(() -> new NoSuchEntityException(Document.class, documentId));

        return ResponseEntity.ok(resource);
    }

    @Override
    public ResponseEntity<DocumentDTO> uploadDocumentV1(String documentType, String analysisType, String category) {
        String userId = SecurityUtils.getCurrentUserId().orElseThrow(UnauthenticatedException::new);
        String username = SecurityUtils.getCurrentUserLogin().orElse(null);
        HttpServletRequest request = (HttpServletRequest) this.request.getNativeRequest();
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);

        if (!isMultipart) {
            log.debug("Request isn't multipart");
            throw new UploadFailedException("Request isn't multipart");
        }

        ServletFileUpload upload = new ServletFileUpload();
        Document document = null;

        try {
            FileItemIterator iterStream = upload.getItemIterator(request);

            while (iterStream.hasNext()) {
                FileItemStream item = iterStream.next();
                InputStream stream = item.openStream();

                if (!item.isFormField()) {
                    // Process the InputStream
                    document = new Document()
                        .user(new User().uid(userId).username(username))
                        .type(documentType)
                        .filename(item.getName())
                        .contentType(item.getContentType())
                        .analysisType(analysisType)
                        .category(category);

                    document = this.documentService.uploadFromStream(stream, document);
                }
            }
        } catch (FileUploadException e) {
            log.debug("File upload exception", e);
            throw new UploadFailedException("Document upload failed");
        } catch (IOException e) {
            log.debug("IO exception during upload", e);
            throw new UploadFailedException("Document upload failed");
        }

        if (document != null && document.getId() != null) {
            document = this.documentService.findOne(document.getId())
                .orElseThrow(() -> new UploadFailedException("Document upload failed"));

            return ResponseEntity.ok(DocumentMapper.INSTANCE.dtoFromDocument(document));
        } else {
            throw new UploadFailedException("Document upload failed");
        }
    }

    @Override
    public ResponseEntity<List<DocumentDTO>> listDocumentsMetaV1(String documentType, String analysisType, String category) {
        String userId = AnalysisUtil.getCurrentUserIdentifier()
            .orElseThrow(UnauthenticatedException::new);

        final List<Document> documents = this.documentService
            .findBy(userId, documentType, analysisType, category, 100);

        return ResponseEntity.ok(DocumentMapper.INSTANCE.dtosFromDocuments(documents));
    }
}
