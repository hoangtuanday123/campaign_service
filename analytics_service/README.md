# Analytics Service

Analytics Service consumes campaign lifecycle events from Kafka, stores aggregated campaign analytics in MongoDB, and exposes secured read APIs for campaign metrics and completed reports.

## Responsibilities

- Consume `campaign_activated` and `campaign_deactivated` events from Campaign Service.
- Initialize and finalize campaign analytics documents in MongoDB.
- Maintain extensible tracking methods for impressions, clicks, and conversions.
- Expose secured analytics endpoints using the shared `ApiResponse` contract.

## Sample Kafka Event Payload

```json
{
  "eventId": "5f67636f-3cd6-468c-a1df-f07dfc5ca6f9",
  "eventType": "campaign_activated",
  "timestamp": "2026-04-13T08:30:00Z",
  "data": {
    "campaignId": "f7d51325-b068-42e5-99b5-bae0ca1fb4d2"
  }
}
```

## Sample API Response

```json
{
  "timestamp": "2026-04-13T08:35:12.814063Z",
  "status": 200,
  "message": "Campaign analytics retrieved successfully",
  "data": {
    "campaignId": "f7d51325-b068-42e5-99b5-bae0ca1fb4d2",
    "impressions": 1240,
    "clicks": 97,
    "conversions": 11,
    "status": "ACTIVE",
    "startedAt": "2026-04-13T08:30:00Z",
    "endedAt": null
  }
}
```