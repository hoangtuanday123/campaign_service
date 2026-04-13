# Promotion Engine

Promotion Engine evaluates promotion eligibility in real time by validating the user through User Service, validating the campaign through Campaign Service, and enforcing one-time usage with Redis.

It also subscribes to Campaign Service lifecycle events so newly activated campaigns are loaded into the Redis cache immediately and deactivated campaigns are disabled without waiting for the next request.

## Compatibility Notes

- Existing User Service and Campaign Service both expose `UUID` identifiers, so this service uses `UUID` instead of `Long` in order to plug into the current system without changing upstream contracts.
- HTTP integration uses Spring `RestClient`, matching Campaign Service.
- API responses use the same `ApiResponse<T>` envelope shape used by both existing services.

## Project Structure

```text
promotion_engine/
  pom.xml
  README.md
  src/
    main/
      java/com/example/promotionengine/
        PromotionEngineApplication.java
        common/api/
        client/campaign/
        client/user/
        config/
        controller/
        dto/campaign/
        dto/promotion/
        dto/user/
        exception/
        service/
        service/impl/
      resources/
        application.yml
    test/
      java/com/example/promotionengine/service/impl/
```

## API Summary

- `POST /api/v1/promotions/apply`
- `POST /api/promotions/apply`

## Sample Request

```json
{
  "userId": "7cf9d01d-756b-4cdd-bbbf-354818f69fd3",
  "campaignId": "624c9392-b24d-4dce-ac53-6d65d12c9988"
}
```

## Sample Success Response

```json
{
  "timestamp": "2026-04-12T00:00:00Z",
  "status": 200,
  "message": "Promotion evaluated successfully",
  "data": {
    "eligible": true,
    "message": "Promotion applied successfully"
  }
}
```

## Sample Rejection Response

```json
{
  "timestamp": "2026-04-12T00:00:00Z",
  "status": 200,
  "message": "Promotion evaluated successfully",
  "data": {
    "eligible": false,
    "message": "User already used promotion"
  }
}
```

## Key Design Decisions

- Campaign data is cached in Redis to avoid repeated downstream reads on the hot path.
- Campaign activation and deactivation are synchronized in real time from the `campaign-events` Kafka topic.
- User usage is stored with the exact key format `campaignId:userId` as requested.
- Usage claiming and quota decrement run in a single Redis Lua script so duplicate redemption and quota oversubscription are prevented atomically.
- If Redis campaign cache lookup fails, the service falls back to Campaign Service.
- If the Redis usage claim path fails, the service fails closed and returns a temporary-unavailable decision rather than risking duplicate promotion usage.
- User Service calls support an optional bearer token because the current User Service protects `/api/v1/users/**` with JWT authentication.