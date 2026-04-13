package com.example.promotionengine.client.campaign;

import com.example.promotionengine.dto.campaign.CampaignDetails;
import com.example.promotionengine.exception.DownstreamServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
public class RestCampaignServiceClient implements CampaignServiceClient {

    private static final Logger log = LoggerFactory.getLogger(RestCampaignServiceClient.class);

    private final RestClient restClient;
    private final CampaignServiceProperties properties;

    public RestCampaignServiceClient(RestClient restClient, CampaignServiceProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    @Override
    public Optional<CampaignDetails> getCampaign(UUID campaignId) {
        if (properties.getBaseUrl() == null || properties.getBaseUrl().isBlank()) {
            throw new DownstreamServiceException("Campaign Service base URL is not configured", null);
        }

        try {
            RestClient.RequestHeadersSpec<?> request = restClient.get()
                    .uri(properties.getBaseUrl() + properties.getCampaignByIdPath(), campaignId);

            if (properties.getApiKey() != null && !properties.getApiKey().isBlank()) {
                request = request.header("X-API-Key", properties.getApiKey());
            }

            CampaignEnvelope response = request.retrieve().body(CampaignEnvelope.class);

            if (response == null || response.data() == null) {
                return Optional.empty();
            }

            return Optional.of(response.data());
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                return Optional.empty();
            }

            log.warn("Campaign Service lookup failed for campaign {} with status {}", campaignId, ex.getStatusCode().value());
            throw new DownstreamServiceException("Campaign Service is unavailable or unauthorized", ex);
        } catch (Exception ex) {
            log.warn("Campaign Service lookup failed for campaign {}", campaignId, ex);
            throw new DownstreamServiceException("Campaign Service is unavailable or unauthorized", ex);
        }
    }

    private record CampaignEnvelope(
            Instant timestamp,
            int status,
            String message,
            CampaignDetails data
    ) {
    }
}