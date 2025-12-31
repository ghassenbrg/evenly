# Evenly – Mobile-First Expense Splitting App

A complete mobile-first PWA for tracking and splitting expenses with friends and roommates. Built with Nuxt 3 frontend and WireMock-mocked backend API.

## Quick Start

1. **Copy environment variables:**
   ```bash
   cp .env.example .env
   ```
   
   Edit `.env` if you need to customize ports, database credentials, or API URLs.

2. **Start the application:**
   ```bash
   docker compose up --build
   ```

Then open:
- **Webapp**: http://localhost:3000
- **WireMock API**: http://localhost:8080
- **WireMock Admin**: http://localhost:8080/__admin

## Architecture

### Frontend (`evenly-webapp`)
- **Nuxt 3** SPA with TypeScript
- **Vue 3** Composition API
- **Pinia** for state management
- **Tailwind CSS** for styling
- **PWA** support (installable, service worker, offline shell)
- **Mobile-first** design (optimized for 360-430px width)

### Backend Mocking (`evenly-wiremock`)
- **WireMock** server providing complete API mocks
- All endpoints from REQUIREMENTS.md are mocked
- Realistic mock data and stateful responses
- Ready to swap with real Helidon backend

### Services
- `wiremock` - Mock API server (port 8080)
- `evenly-webapp` - Nuxt frontend (port 3000)
- `evenly-core` - Real backend (disabled by default, use `--profile production`)

## Features

✅ **Authentication**
- User registration and login
- JWT token management
- Protected routes

✅ **Workspaces**
- Create and manage workspaces
- Switch between workspaces
- Equal and weighted split modes
- Monthly budget limits

✅ **Dynamic Categories**
- Icon-based category picker
- Custom colors
- Fully dynamic (create/edit/delete)
- Workspace-scoped

✅ **Expenses**
- Add shared or personal expenses
- Filter by type
- Participant selection
- Edit/delete active expenses
- Locked settled expenses

✅ **Balance & Settlement**
- Real-time balance calculation
- Settlement preview with transfers
- Settlement history
- View settled expenses

✅ **Analytics**
- Dashboard with balance summary
- Category breakdown
- Budget progress tracking
- Shared vs personal totals

✅ **Mobile-First UI**
- Bottom tab navigation
- Floating Action Button (FAB)
- Bottom sheet modals
- Toast notifications
- iOS safe-area support
- Native-like transitions

## Project Structure

```
/
├── docker-compose.yml          # Orchestrates WireMock + frontend
├── evenly-webapp/              # Nuxt 3 frontend
│   ├── components/             # Vue components
│   ├── composables/            # Composables (useAuth, useToast)
│   ├── layouts/                # Layout templates
│   ├── middleware/             # Route middleware
│   ├── pages/                  # Route pages
│   ├── stores/                 # Pinia stores
│   ├── types/                  # TypeScript types
│   └── utils/                  # Utilities (API client)
└── evenly-wiremock/            # WireMock mocks
    ├── mappings/               # API endpoint mappings
    └── __files/                # Mock data files
```

## API Endpoints (Mocked)

All endpoints from REQUIREMENTS.md are available via WireMock:

### Auth
- `POST /auth/register` - Register new user
- `POST /auth/login` - Login user

### Workspaces
- `GET /api/workspaces` - List workspaces
- `POST /api/workspaces` - Create workspace
- `GET /api/workspaces/{id}` - Get workspace
- `PUT /api/workspaces/{id}/settings` - Update settings
- `PUT /api/workspaces/{id}/members/weights` - Update member weights

### Categories
- `GET /api/categories` - List categories
- `POST /api/categories` - Create category
- `PUT /api/categories/{id}` - Update category
- `DELETE /api/categories/{id}` - Delete (disable) category

### Expenses
- `GET /api/expenses` - List expenses
- `POST /api/expenses` - Create expense
- `PUT /api/expenses/{id}` - Update expense
- `DELETE /api/expenses/{id}` - Delete expense

### Analytics
- `GET /api/analytics/summary` - Summary analytics
- `GET /api/analytics/categories` - Category breakdown

### Balance & Settlement
- `GET /api/balance` - Get balances
- `POST /api/settlements` - Create settlement
- `GET /api/settlements` - List settlements
- `GET /api/settlements/{id}` - Get settlement
- `GET /api/settlements/{id}/expenses` - Get settlement expenses
- `GET /api/settlements/{id}/transfers` - Get settlement transfers

## Development

### Frontend Development

```bash
cd evenly-webapp
npm install
npm run dev
```

### Using Real Backend

To use the real Helidon backend instead of WireMock:

```bash
docker compose --profile production up --build
```

This will start:
- PostgreSQL database
- Helidon backend (evenly-core)
- Nuxt frontend

Update `NUXT_PUBLIC_API_BASE` in `.env` if backend runs on different port.

## Testing the UI

1. Start the app: `docker compose up --build`
2. Open http://localhost:3000
3. Register a new account (or use: alice@example.com / any password)
4. Explore the mobile-first interface:
   - Dashboard shows balance and analytics
   - Add expenses via FAB button
   - View history and settlements
   - Manage categories in Settings

## Mobile Testing

The app is optimized for mobile devices (360-430px width). To test:

1. Open Chrome DevTools
2. Toggle device toolbar (Cmd/Ctrl + Shift + M)
3. Select iPhone SE or similar small device
4. Test touch interactions, bottom sheets, and navigation

## Next Steps

The frontend is complete and ready for backend integration. When the real Helidon backend is ready:

1. Remove WireMock from docker-compose.yml
2. Update `NUXT_PUBLIC_API_BASE` in `.env` if needed
3. The frontend will work with the real API without changes

All API contracts match REQUIREMENTS.md exactly.

## Requirements

See `REQUIREMENTS.md` for complete specification.
