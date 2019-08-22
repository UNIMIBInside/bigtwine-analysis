package it.unimib.disco.bigtwine.services.analysis.web.api;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoGridFSException;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSFile;
import it.unimib.disco.bigtwine.services.analysis.security.SecurityUtils;
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
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
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

@Service
public class DocumentsApiDelegateImpl implements DocumentsApiDelegate {
    private final Logger log = LoggerFactory.getLogger(DocumentsApiDelegateImpl.class);

    private final String METADATA_USERID_KEY = "userid";
    private final String METADATA_USERNAME_KEY = "username";

    private final NativeWebRequest request;
    private GridFsTemplate gridFsTemplate;
    private MongoDbFactory dbFactory;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public DocumentsApiDelegateImpl(NativeWebRequest request, GridFsTemplate gridFsTemplate, MongoDbFactory dbFactory) {
        this.request = request;
        this.gridFsTemplate = gridFsTemplate;
        this.dbFactory = dbFactory;
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
            .filename(file.getFilename())
            .size(file.getLength())
            .user(new UserDTO()
                .uid(file.getMetadata().getString(METADATA_USERID_KEY))
                .username(file.getMetadata().getString(METADATA_USERNAME_KEY)))
            .uploadDate(OffsetDateTime.ofInstant(file.getUploadDate().toInstant(), ZoneOffset.UTC))
            .contentType(contentType);
    }

    private GridFSBucket getGridFs(String bucket) {
        MongoDatabase db = dbFactory.getDb();
        return bucket == null ? GridFSBuckets.create(db) : GridFSBuckets.create(db, bucket);
    }

    @Override
    public ResponseEntity<DocumentDTO> getDocumentMetaV1(String documentId) {
        AnalysisUtil.getCurrentUserIdentifier()
            .orElseThrow(UnauthenticatedException::new);
        GridFSFile file = this.getFile(documentId);

        return ResponseEntity.ok(this.createDocumentFromFile(file));
    }

    @Override
    public ResponseEntity<Resource> downloadDocumentV1(String documentId) {
        String userId = AnalysisUtil.getCurrentUserIdentifier().orElseThrow(UnauthenticatedException::new);

        GridFSFile file = this.getFile(documentId);

        if (!userId.equals(file.getMetadata().get(METADATA_USERID_KEY))) {
            throw new UnauthorizedException("Only the uploader can download this document");
        }

        InputStream stream = this.getGridFs(null).openDownloadStream(file.getObjectId());
        Resource resource = new GridFsResource(file, stream);

        return ResponseEntity.ok(resource);
    }

    @Override
    public ResponseEntity<DocumentDTO> uploadDocumentV1() {
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
}
