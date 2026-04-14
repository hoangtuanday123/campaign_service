# API Gateway

## Project Structure

```text
api_gateway
+-- pom.xml
+-- README.md
`-- src
    `-- main
        +-- java/com/example/apigateway
        |   +-- ApiGatewayApplication.java
        |   +-- common/api/ApiResponse.java
        |   +-- config
        |   +-- exception
        |   +-- filter
        |   +-- service
        |   `-- util
        `-- resources/application.yml
```

## Routes

- `GET|POST|PUT|DELETE /api/users/**` -> User Service (`/api/v1/users/**`)
- `GET|POST|PUT|DELETE /api/campaigns/**` -> Campaign Service (`/api/v1/campaigns/**`)
- `GET|POST /api/promotions/**` -> Promotion Engine (`/api/v1/promotions/**`)
- `GET /api/analytics/**` -> Analytics Service (`/api/v1/analytics/**`)

## Sample Requests

```bash
curl --request GET "http://localhost:8080/api/users/me" \
  --header "Authorization: Bearer <jwt-token>"

curl --request GET "http://localhost:8080/api/campaigns/7c87b1f2-18b3-4971-8d67-bf50a2d3ed66" \
  --header "Authorization: Bearer <jwt-token>"

curl --request POST "http://localhost:8080/api/promotions/apply" \
  --header "Authorization: Bearer <jwt-token>" \
  --header "Content-Type: application/json" \
  --data "{\"userId\":\"d89c10b2-a71a-4497-af6a-dd5e1b9cf2fa\",\"campaignId\":\"7c87b1f2-18b3-4971-8d67-bf50a2d3ed66\",\"orderAmount\":150.00}"

curl --request GET "http://localhost:8080/api/analytics/campaign/7c87b1f2-18b3-4971-8d67-bf50a2d3ed66" \
  --header "Authorization: Bearer <jwt-token>"
```
