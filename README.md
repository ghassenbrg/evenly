# Evenly – Mobile-First Expense Splitting App

A complete mobile-first PWA for tracking and splitting expenses with friends and roommates. Built with Nuxt 3 frontend, Keycloak authentication, and WireMock-mocked backend API.

## Quick Start

1. **Navigate to the webapp directory:**
   ```bash
   cd evenly-webapp
   ```

2. **Create environment file:**
   ```bash
   # Create .env file with the following variables:
   NUXT_PUBLIC_API_BASE=http://localhost:8080
   NUXT_PUBLIC_KEYCLOAK_URL=https://auth.ghassen.io
   NUXT_PUBLIC_KEYCLOAK_REALM=pockito
   NUXT_PUBLIC_KEYCLOAK_CLIENT_ID=pockito-web
   ```

3. **Install dependencies:**
   ```bash
   npm install
   ```

4. **Start the development server:**
   ```bash
   npm run dev
   ```

Then open:
- **Webapp**: http://localhost:3000
- **WireMock API**: http://localhost:8080 (if using docker-compose)
- **WireMock Admin**: http://localhost:8080/__admin (if using docker-compose)

## Architecture

### Frontend (`evenly-webapp`)
- **Nuxt 3** SPA with TypeScript
- **Vue 3** Composition API
- **Pinia** for state management
- **Tailwind CSS** for styling
- **PWA** support (installable, service worker, offline shell)
- **Keycloak** authentication integration
- **i18n** internationalization (English/Japanese)
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
- **Keycloak** SSO integration
- OAuth 2.0 / OpenID Connect
- PKCE flow for security
- Automatic token refresh
- Protected routes with middleware

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
- Filter by type and date range
- Participant selection
- Edit/delete active expenses
- Locked settled expenses
- Grouped by day (Today, Yesterday, Weekday, Full Date)
- Lazy loading with infinite scroll
- Category icons and visual indicators
- Notes and metadata support

✅ **Balance & Settlement**
- Real-time balance calculation
- Settlement preview with transfers
- Settlement history
- View settled expenses

✅ **Analytics & Dashboard**
- Dashboard with balance summary
- Category breakdown with visual indicators
- Recent expenses list
- Monthly/weekly/custom period expense tracking
- Interactive SVG charts (no external dependencies)
- Budget progress tracking
- Shared vs personal totals
- Expense trends and statistics

✅ **Mobile-First UI**
- Bottom tab navigation
- Floating Action Button (FAB)
- Bottom sheet modals
- Toast notifications
- iOS safe-area support
- Native-like transitions
- Dark mode optimized
- Responsive date range picker
- Infinite scroll for expense lists
- Grouped expense timeline view

## Project Structure

```
/
├── docker-compose.yml          # Orchestrates WireMock + frontend
├── evenly-webapp/              # Nuxt 3 frontend
│   ├── components/             # Vue components
│   │   ├── dashboard/         # Dashboard cards (Balance, Categories, Recent Expenses)
│   │   ├── expenses/           # Expense components (MonthlyTotalCard, ExpenseItem)
│   │   └── DateRangePicker.vue # Reusable date range picker
│   ├── composables/            # Composables (useAuth, useToast, useFormatting)
│   ├── i18n/                   # Internationalization (en.json, ja.json)
│   ├── layouts/                # Layout templates
│   ├── middleware/             # Route middleware (auth, guest)
│   ├── pages/                  # Route pages
│   │   └── keycloak-callback.vue # Keycloak OAuth callback handler
│   ├── plugins/                # Nuxt plugins
│   │   └── keycloak.client.ts  # Keycloak initialization
│   ├── stores/                 # Pinia stores (auth, workspaces)
│   ├── types/                  # TypeScript types
│   └── utils/                  # Utilities (API client)
├── evenly-core/                # Helidon backend (Java)
└── evenly-wiremock/            # WireMock mocks
    ├── mappings/               # API endpoint mappings
    └── __files/                # Mock data files
```

## API Endpoints (Mocked)

All endpoints from REQUIREMENTS.md are available via WireMock:

### Auth
- Authentication is handled via **Keycloak** (OAuth 2.0 / OpenID Connect)
- No direct API endpoints for login/register
- Token-based API authentication using Keycloak access tokens

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

The app will be available at http://localhost:3000

### Environment Variables

Create a `.env` file in `evenly-webapp/` directory:

```env
NUXT_PUBLIC_API_BASE=http://localhost:8080
NUXT_PUBLIC_KEYCLOAK_URL=https://auth.ghassen.io
NUXT_PUBLIC_KEYCLOAK_REALM=pockito
NUXT_PUBLIC_KEYCLOAK_CLIENT_ID=pockito-web
```

**Important:** After updating `.env` file, restart the dev server for changes to take effect.

### Keycloak Configuration

The app uses Keycloak for authentication. Make sure to configure the Keycloak client:

1. **Valid Redirect URIs:**
   - `http://localhost:3000/keycloak-callback`
   - `http://localhost:3000/*` (or use wildcard)

2. **Web Origins:**
   - `http://localhost:3000`

3. **Client Settings:**
   - Access Type: `public`
   - Standard Flow Enabled: `ON`
   - PKCE Code Challenge Method: `S256`

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

1. Start the app: `cd evenly-webapp && npm run dev`
2. Open http://localhost:3000
3. Click "Sign In" to authenticate via Keycloak
4. After authentication, explore the mobile-first interface:
   - **Dashboard**: Balance summary, category breakdown, recent expenses
   - **Expenses**: Monthly totals with charts, filtered expense lists
   - **History**: Settlement history and past expenses
   - **Settings**: Workspace management, categories, language selection
   - Add expenses via FAB button
   - Switch between workspaces
   - View analytics and trends

## Mobile Testing

The app is optimized for mobile devices (360-430px width). To test:

1. Open Chrome DevTools
2. Toggle device toolbar (Cmd/Ctrl + Shift + M)
3. Select iPhone SE or similar small device
4. Test touch interactions, bottom sheets, and navigation

## Key Technologies

- **Nuxt 3** - Vue.js framework with SSR capabilities
- **Vue 3** - Progressive JavaScript framework
- **TypeScript** - Type-safe JavaScript
- **Pinia** - State management
- **Tailwind CSS** - Utility-first CSS framework
- **Keycloak** - Identity and access management
- **keycloak-js** - Keycloak JavaScript adapter
- **@nuxtjs/i18n** - Internationalization plugin
- **@vite-pwa/nuxt** - PWA support
- **Chart.js** - Chart library (for future use)

## Internationalization

The app supports multiple languages:
- **English** (en) - Default
- **Japanese** (ja) - 日本語

Language can be switched via the language switcher in the UI. All user-facing strings are translatable.

## Next Steps

The frontend is complete and ready for backend integration. When the real Helidon backend is ready:

1. Remove WireMock from docker-compose.yml
2. Update `NUXT_PUBLIC_API_BASE` in `.env` if needed
3. Ensure Keycloak is properly configured for production
4. The frontend will work with the real API without changes

All API contracts match REQUIREMENTS.md exactly.

## Requirements

See `REQUIREMENTS.md` for complete specification.
