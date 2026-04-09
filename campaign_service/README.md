# Campaign Service

Campaign Service manages campaign lifecycle only. It does not evaluate campaign rules or execute promotion logic.

## Project Structure

```text
campaign_service/
  pom.xml
  src/
    main/
      java/com/example/campaignservice/
        CampaignServiceApplication.java
        common/api/
        config/
        controller/
        domain/entity/
        domain/enums/
        dto/campaign/
        dto/event/
        exception/
        integration/kafka/
        integration/redis/
        integration/user/
        repository/
        service/
        service/impl/
      resources/
        application.yml
        db/migration/
    test/
      java/com/example/campaignservice/service/impl/
```

## API Summary

- POST /api/campaigns
- GET /api/campaigns/{id}
- PUT /api/campaigns/{id}
- PUT /api/campaigns/{id}/activate
- PUT /api/campaigns/{id}/deactivate
- GET /api/campaigns/active

OpenAPI UI is exposed at /swagger-ui/index.html.

## Activate Campaign Flow

1. Controller receives PUT /api/campaigns/{id}/activate.
2. Service locks the campaign row and validates:
   - not already active
   - current time is between startTime and endTime
   - remaining quota is greater than zero
3. Service updates status to ACTIVE in MySQL.
4. After transaction commit:
   - Redis stores campaign:active:{id}
   - Redis stores campaign:quota:{id}
   - Redis updates campaign:active:list
   - Kafka publishes a campaign_activated event
5. Response returns the current campaign snapshot.

## Redis Keys

- campaign:active:list
- campaign:active:{id}
- campaign:quota:{id}

## Optional User Service Integration

User Service integration is isolated behind a client interface and is not used in the core lifecycle path. Configure services.user.base-url only when campaign eligibility needs to inspect user profile data.