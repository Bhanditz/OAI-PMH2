package eu.europeana.oaipmh.client;

import eu.europeana.oaipmh.model.GetRecord;
import eu.europeana.oaipmh.model.Header;
import eu.europeana.oaipmh.model.RDFMetadata;
import eu.europeana.oaipmh.model.Record;
import eu.europeana.oaipmh.model.response.GetRecordResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GetRecordQuery extends BaseQuery implements OAIPMHQuery {

    private static final Logger LOG = LogManager.getLogger(GetRecordQuery.class);

    @Value("${GetRecord.metadataPrefix}")
    private String metadataPrefix;

    @Value("${GetRecord.identifier}")
    private String identifier;

    public GetRecordQuery() {
    }

    public GetRecordQuery(String metadataPrefix, String identifier) {
        this.metadataPrefix = metadataPrefix;
        this.identifier = identifier;
    }

    @Override
    public String getVerbName() {
        return "GetRecord";
    }

    @Override
    public void execute(OAIPMHServiceClient oaipmhServer) {
        execute(oaipmhServer, identifier);
    }

    private void execute(OAIPMHServiceClient oaipmhServer, String currentIdentifier) {
        long start = System.currentTimeMillis();

        String request = getRequest(oaipmhServer.getOaipmhServer(), currentIdentifier);

        GetRecordResponse response = (GetRecordResponse) oaipmhServer.makeRequest(request, GetRecordResponse.class);
        GetRecord responseObject = response.getGetRecord();
        if (responseObject != null) {
            Record record = responseObject.getRecord();
            if (record == null) {
                LOG.error("No record in GetRecordResponse for identifier " + currentIdentifier);
                return;
            }
            Header header = record.getHeader();
            if (header != null && currentIdentifier.equals(header.getIdentifier())) {
                RDFMetadata metadata = record.getMetadata();
                if (metadata == null || metadata.getMetadata() == null || metadata.getMetadata().isEmpty()) {
                    LOG.error("Empty metadata for identifier " + currentIdentifier);
                }
            }
        }

        LOG.debug("GetRecord for identifier {} executed in {} ms", currentIdentifier, (System.currentTimeMillis() - start));
    }

    private String getRequest(String oaipmhServer, String identifier) {
        StringBuilder sb = new StringBuilder();
        sb.append(getBaseRequest(oaipmhServer, getVerbName()));
        sb.append(String.format(METADATA_PREFIX_PARAMETER, metadataPrefix));
        if (identifier != null && !identifier.isEmpty()) {
            sb.append(String.format(IDENTIFIER_PARAMETER, identifier));
        }

        return sb.toString();
    }
}
