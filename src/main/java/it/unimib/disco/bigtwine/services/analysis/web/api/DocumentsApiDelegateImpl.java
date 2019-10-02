package it.unimib.disco.bigtwine.services.analysis.web.api;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoGridFSException;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import it.unimib.disco.bigtwine.services.analysis.domain.Analysis;
import it.unimib.disco.bigtwine.services.analysis.security.SecurityUtils;
import it.unimib.disco.bigtwine.services.analysis.service.AnalysisService;
import it.unimib.disco.bigtwine.services.analysis.web.api.errors.NoSuchEntityException;
import it.unimib.disco.bigtwine.services.analysis.web.api.errors.UnauthenticatedException;
import it.unimib.disco.bigtwine.services.analysis.web.api.errors.UnauthorizedException;
import it.unimib.disco.bigtwine.services.analysis.web.api.errors.UploadFailedException;
import it.unimib.disco.bigtwine.services.analysis.web.api.model.DocumentDTO;
import it.unimib.disco.bigtwine.services.analysis.web.api.model.UserDTO;
import it.unimib.disco.bigtwine.services.analysis.web.api.util.AnalysisUtil;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsCriteria;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.NativeWebRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DocumentsApiDelegateImpl implements DocumentsApiDelegate {
    private final Logger log = LoggerFactory.getLogger(DocumentsApiDelegateImpl.class);

    private static final String METADATA_USERID_KEY = "userid";
    private static final String METADATA_USERNAME_KEY = "username";
    private static final String METADATA_DOCTYPE_KEY = "doctype";
    private static final String METADATA_ANALYSISID_KEY = "analysisid";
    private static final String METADATA_ANALYSISTYPE_KEY = "analysistype";
    private static final String METADATA_CATEGORY_KEY = "category";

    private final NativeWebRequest request;
    private GridFsTemplate gridFsTemplate;
    private MongoDbFactory dbFactory;
    private AnalysisService analysisService;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public DocumentsApiDelegateImpl(
        NativeWebRequest request,
        GridFsTemplate gridFsTemplate,
        MongoDbFactory dbFactory,
        AnalysisService analysisService) {
        this.request = request;
        this.gridFsTemplate = gridFsTemplate;
        this.dbFactory = dbFactory;
        this.analysisService = analysisService;
    }

    private GridFSFile getFile(String objectId) {
        ObjectId oid = new ObjectId(objectId);
        return this.getFile(oid);
    }

    private GridFSFile getFile(ObjectId objectId) {
        Query query = new Query(Criteria.where("_id").is(objectId));
        GridFSFile file = gridFsTemplate.findOne(query);

        if (file == null) {
            throw new NoSuchEntityException(String.format("Document with uid %s not found", objectId));
        }

        return file;
    }

    private DocumentDTO createDocumentFromFile(GridFSFile file) {
        GridFsResource resource = new GridFsResource(file);
        String contentType;
        try {
            contentType = resource.getContentType();
        } catch (MongoGridFSException e) {
            contentType = null;
        }

        return new DocumentDTO()
            .documentId(file.getObjectId().toString())
            .documentType(file.getMetadata().getString(METADATA_DOCTYPE_KEY))
            .filename(file.getFilename())
            .size(file.getLength())
            .user(new UserDTO()
                .uid(file.getMetadata().getString(METADATA_USERID_KEY))
                .username(file.getMetadata().getString(METADATA_USERNAME_KEY)))
            .category(file.getMetadata().getString(METADATA_CATEGORY_KEY))
            .analysisType(file.getMetadata().getString(METADATA_ANALYSISTYPE_KEY))
            .analysisId(file.getMetadata().getString(METADATA_ANALYSISID_KEY))
            .uploadDate(OffsetDateTime.ofInstant(file.getUploadDate().toInstant(), ZoneOffset.UTC))
            .contentType(contentType);
    }

    private GridFSBucket getGridFs(String bucket) {
        MongoDatabase db = dbFactory.getDb();
        return bucket == null ? GridFSBuckets.create(db) : GridFSBuckets.create(db, bucket);
    }

    private void checkFileOwnership(GridFSFile file) {
        String userId = AnalysisUtil.getCurrentUserIdentifier()
            .orElseThrow(UnauthenticatedException::new);
        String docAnalysis = (String)file.getMetadata().get(METADATA_ANALYSISID_KEY);

        if (StringUtils.isNotBlank(docAnalysis)) {
            Optional<Analysis> analysis = this.analysisService.findOne(docAnalysis);

            if (!(analysis.isPresent() && analysis.get().getOwner().getUid().equals(userId))) {
                throw new UnauthorizedException(String.format(
                    "Only the owner of the analysis '%s' can access this document",
                    docAnalysis));
            }
        } else {
            String docUploader = (String)file.getMetadata().get(METADATA_USERID_KEY);
            if (!userId.equals(docUploader)) {
                throw new UnauthorizedException("Only the uploader can access this document");
            }
        }
    }

    @Override
    public ResponseEntity<DocumentDTO> getDocumentMetaV1(String documentId) {
        GridFSFile file = this.getFile(documentId);
        this.checkFileOwnership(file);

        return ResponseEntity.ok(this.createDocumentFromFile(file));
    }

    @Override
    public ResponseEntity<Resource> downloadDocumentV1(String documentId) {
        GridFSFile file = this.getFile(documentId);
        this.checkFileOwnership(file);

        InputStream stream = this.getGridFs(null).openDownloadStream(file.getObjectId());
        Resource resource = new GridFsResource(file, stream);

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
        ObjectId objectId = null;

        try {
            FileItemIterator iterStream = upload.getItemIterator(request);

            while (iterStream.hasNext()) {
                FileItemStream item = iterStream.next();
                InputStream stream = item.openStream();

                if (!item.isFormField()) {
                    // Process the InputStream
                    DBObject metadata = new BasicDBObject();
                    metadata.put(METADATA_USERID_KEY, userId);
                    metadata.put(METADATA_USERNAME_KEY, username);

                    if (StringUtils.isNotBlank(documentType)) {
                        metadata.put(METADATA_DOCTYPE_KEY, documentType);
                    }

                    if (analysisType != null) {
                        metadata.put(METADATA_ANALYSISTYPE_KEY, analysisType.toString());
                    }

                    if (StringUtils.isNotBlank(category)) {
                        metadata.put(METADATA_CATEGORY_KEY, category);
                    }

                    objectId = gridFsTemplate.store(stream, item.getName(), item.getContentType(), metadata);
                }
            }
        } catch (FileUploadException e) {
            log.debug("File upload exception", e);
            throw new UploadFailedException("Document upload failed");
        } catch (IOException e) {
            log.debug("IO exception during upload", e);
            throw new UploadFailedException("Document upload failed");
        }

        GridFSFile file = null;

        if (objectId != null) {
            try {
                file = getFile(objectId);
            } catch (NoSuchEntityException e) {
                throw new UploadFailedException("Document upload failed");
            }
        }

        if (file != null) {
            return ResponseEntity.ok(createDocumentFromFile(file));
        } else {
            throw new UploadFailedException("Document upload failed");
        }
    }

    @Override
    public ResponseEntity<List<DocumentDTO>> listDocumentsMetaV1(String documentType, String analysisType, String category) {
        String userId = AnalysisUtil.getCurrentUserIdentifier()
            .orElseThrow(UnauthenticatedException::new);
        Query query = new Query(GridFsCriteria.whereMetaData(METADATA_USERID_KEY).is(userId));

        if (documentType != null) {
            query.addCriteria(GridFsCriteria.whereMetaData(METADATA_DOCTYPE_KEY).is(documentType));
        }

        if (analysisType != null) {
            query.addCriteria(GridFsCriteria.whereMetaData(METADATA_ANALYSISTYPE_KEY).is(analysisType.toString()));
        }

        if (category != null) {
            query.addCriteria(GridFsCriteria.whereMetaData(METADATA_CATEGORY_KEY).is(category));
        }

        query.limit(100);

        GridFSFindIterable files = gridFsTemplate.find(query);
        final List<DocumentDTO> documents = new ArrayList<>();
        files
            .iterator()
            .forEachRemaining(f -> {
                documents.add(this.createDocumentFromFile(f));
            });

        return ResponseEntity.ok(documents);
    }
}
