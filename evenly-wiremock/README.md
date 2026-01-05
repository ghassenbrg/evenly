# WireMock Integration for Evenly API

This directory contains WireMock mappings that serve as a complete mock of the Evenly API, using `endpoints.json` as the **single source of truth** for all API definitions.

## Overview

All WireMock mappings are generated from `endpoints.json` to ensure 100% behavioral parity with the production API. The mappings include:

- Exact HTTP methods and paths
- Path parameter matching using `urlPathPattern`
- Query parameter support
- Response bodies matching `endpoints.json` structure
- CORS headers for browser compatibility
- Proper status codes
- Error responses (400, 401, 403, 404, 409, 500)

## API Base URL Configuration

The frontend can switch between WireMock and the live API using the `NUXT_PUBLIC_API_BASE` environment variable:

### Using WireMock (Default)
```bash
NUXT_PUBLIC_API_BASE=http://localhost:8080
```

### Using Live API
```bash
NUXT_PUBLIC_API_BASE=https://api.evenly.example.com
```

The configuration is set in:
- `evenly-webapp/nuxt.config.ts` - Defaults to `http://localhost:8080`
- `docker-compose.yml` - Can be overridden via environment variable

## Mappings Structure

All mappings are in the `mappings/` directory and follow this naming convention:
- `{resource}-{action}.json` - e.g., `workspaces-list.json`, `notifications-mark-as-read.json`
- `{resource}-{action}-{status}.json` - Error cases, e.g., `workspaces-get-404.json`, `workspaces-create-400.json`

### Response Structure

All responses follow the structure defined in `endpoints.json`:
- Most endpoints wrap data in a `data` property: `{ data: ... }`
- PUT `/api/workspaces/{id}` uses `workspace` property: `{ workspace: ... }`
- GET `/api/notifications` includes both `data` and `unreadCount`: `{ data: [...], unreadCount: 2 }`
- Paginated responses include `data`, `page`, and `sort`: `{ data: [...], page: {...}, sort: {...} }`

The frontend API utility (`evenly-webapp/utils/api.ts`) automatically extracts the `data` property when present, ensuring seamless integration.

## Endpoints Covered

### Notifications
- `GET /api/notifications` - List notifications with unread count
- `GET /api/notifications/unread-count` - Get unread count
- `POST /api/notifications/{id}/mark-as-read` - Mark notification as read
- `POST /api/notifications/mark-all-as-read` - Mark all as read

### Workspaces
- `GET /api/workspaces` - List workspaces
- `GET /api/workspaces/{id}` - Get workspace details
- `POST /api/workspaces` - Create workspace
- `PUT /api/workspaces/{id}` - Update workspace
- `DELETE /api/workspaces/{id}` - Delete workspace
- `GET /api/workspaces/{id}/settle-up` - Get settlement preview
- `POST /api/workspaces/{id}/pay` - Create payment

### Analytics
- `GET /api/workspaces/{id}/analytics/balance-summary` - Balance summary
- `GET /api/workspaces/{id}/analytics/expenses-snapshot` - Expenses by category
- `GET /api/workspaces/{id}/analytics/recent-expenses` - Recent expenses

### Expenses
- `GET /api/workspaces/{id}/expenses` - List expenses (with pagination)

### Payments
- `GET /api/workspaces/{id}/payments` - List payments (with pagination)

## Error Responses

WireMock includes error response mappings for:
- **400 Bad Request** - Validation errors (e.g., `workspaces-create-400.json`)
- **401 Unauthorized** - Authentication required (`api-error-401.json`)
- **404 Not Found** - Resource not found (e.g., `workspaces-get-404.json`)
- **409 Conflict** - Business rule violations (e.g., `workspaces-delete-409.json`)
- **500 Internal Server Error** - Server errors (`api-error-500.json`)

Error responses follow the API contract:
```json
{
  "error": "ERROR_CODE",
  "message": "Human-readable error message",
  "errors": [
    {
      "field": "fieldName",
      "message": "Field-specific error"
    }
  ]
}
```

## Running WireMock

### Using Docker Compose
```bash
docker-compose up wiremock
```

WireMock will be available at `http://localhost:8080`

### Standalone
```bash
docker run -it --rm \
  -p 8080:8080 \
  -v $(pwd)/mappings:/home/wiremock/mappings \
  -v $(pwd)/__files:/home/wiremock/__files \
  wiremock/wiremock:3.3.1 \
  --global-response-templating --verbose
```

## Frontend Integration

The frontend is configured to work seamlessly with WireMock:

1. **API Utility** (`evenly-webapp/utils/api.ts`):
   - Automatically extracts `data` property from responses
   - Handles special cases (notifications, workspace updates, pagination)
   - No frontend code changes needed when switching APIs
   - Proper error handling with API error schema

2. **TypeScript Types** (`evenly-webapp/types/api.ts`):
   - All types match `endpoints.json` exactly
   - Field names, enums, and structures align with API contract

3. **Composables**:
   - Use exact API contract (field names, pagination, sorting)
   - Handle pagination with `page` and `sort` objects
   - Map API responses to UI-friendly formats where needed

4. **Environment Variables**:
   - Set `NUXT_PUBLIC_API_BASE` to switch between WireMock and live API
   - Defaults to `http://localhost:8080` (WireMock)

5. **No Mock-Specific Logic**:
   - All API calls use the same code paths
   - Identical behavior for success, error, and edge cases
   - Same loading states and error handling

## Contract Testing

A smoke test is provided to verify API contract compliance:

```bash
# Run contract tests against WireMock
node evenly-wiremock/test-contract.js

# Or test against live API
API_BASE_URL=https://api.evenly.example.com node evenly-wiremock/test-contract.js
```

The test verifies:
- Critical endpoints return expected response structures
- Required fields are present
- HTTP status codes are correct

## Behavioral Parity

The WireMock setup ensures:
- ✅ Identical response payloads
- ✅ Same pagination structure (`page`, `sort`)
- ✅ Same sorting behavior
- ✅ Same error response format
- ✅ Same CORS headers
- ✅ Same status codes
- ✅ Field names match exactly

## Updating Mappings

When `endpoints.json` is updated:

1. Update the corresponding mapping file in `mappings/`
2. Ensure response structure matches `endpoints.json` exactly
3. Update TypeScript types in `evenly-webapp/types/api.ts` if needed
4. Test with the frontend to verify behavior
5. Run contract tests: `node evenly-wiremock/test-contract.js`
6. Update this README if new endpoints are added

## Development Workflow

1. **Local Development with WireMock**:
   ```bash
   docker-compose up wiremock evenly-webapp
   ```

2. **Testing Against Live API**:
   ```bash
   NUXT_PUBLIC_API_BASE=https://api.evenly.example.com docker-compose up evenly-webapp
   ```

3. **Contract Testing**:
   ```bash
   node evenly-wiremock/test-contract.js
   ```

## Notes

- Query parameters are supported but not strictly validated in mappings (WireMock matches any query params)
- Path parameters use regex patterns in `urlPathPattern` for flexibility
- CORS headers are included in all responses for browser compatibility
- The frontend handles response transformation automatically via the API utility
- All UI components are adapted to use the exact API contract structure

