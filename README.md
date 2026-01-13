# Evenly Database Schema Design

## Overview

This document describes the normalized PostgreSQL database schema for the Evenly expense tracking application. The schema is designed to support shared expense tracking, workspace management, settlements, payments, and user notifications.

The schema follows **Third Normal Form (3NF)** principles to minimize data redundancy while maintaining query performance through strategic indexing.

## Entity Relationship Overview

- **Users** can belong to multiple **Workspaces**
- **Workspaces** contain **Expenses**, **Categories**, **Payments**, and **Settlements**
- **Expenses** have multiple **Participants** (many-to-many)
- **Settlements** contain multiple **Transfers** between users
- **Workspaces** can have **Invites** for joining
- **Users** receive **Notifications**

## Tables

### 1. `users`

Stores user account information. Users are authenticated externally (e.g., Keycloak), but their profile data is stored here.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PRIMARY KEY | Unique user identifier (matches external auth system) |
| `email` | VARCHAR(255) | NOT NULL, UNIQUE | User's email address |
| `display_name` | VARCHAR(255) | NOT NULL | User's display name |
| `username` | VARCHAR(100) | NOT NULL, UNIQUE | Username for display |
| `avatar_url` | TEXT | NULL | URL to user's avatar image |
| `preferred_currency` | VARCHAR(3) | NOT NULL | ISO 4217 currency code (e.g., 'JPY', 'USD') |
| `locale` | VARCHAR(10) | NOT NULL, DEFAULT 'en-US' | User's locale preference (e.g., 'en-US', 'ja-JP') |
| `timezone` | VARCHAR(50) | NOT NULL, DEFAULT 'UTC' | User's timezone (e.g., 'UTC', 'Asia/Tokyo') |
| `created_at` | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() | Account creation timestamp |

**Indexes:**
- Primary key on `id`
- Unique index on `email`
- Unique index on `username`
- Index on `preferred_currency` (for currency-based queries)

**Business Rules:**
- Email must be unique across all users
- Username must be unique across all users
- Preferred currency should reference a valid currency code (enforced via application logic or FK to currencies table if needed)

---

### 2. `workspaces`

Represents a shared expense group (e.g., a couple, roommates, or project team). Each workspace has its own currency and split mode configuration.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PRIMARY KEY | Unique workspace identifier |
| `name` | VARCHAR(255) | NOT NULL | Workspace display name |
| `default_split_mode` | VARCHAR(20) | NOT NULL, CHECK | Split mode: 'EQUAL' or 'WEIGHTED' |
| `monthly_shared_limit` | NUMERIC(15,2) | NULL | Optional monthly budget limit for the workspace |
| `is_personal` | BOOLEAN | NOT NULL, DEFAULT FALSE | Whether this is a personal workspace (single user) |
| `currency` | VARCHAR(3) | NOT NULL | ISO 4217 currency code for this workspace |
| `created_at` | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() | Workspace creation timestamp |
| `updated_at` | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() | Last update timestamp (auto-updated on changes) |

**Indexes:**
- Primary key on `id`
- Index on `currency` (for currency-based queries)
- Index on `is_personal` (for filtering personal workspaces)

**Check Constraints:**
- `default_split_mode IN ('EQUAL', 'WEIGHTED')`
- `monthly_shared_limit IS NULL OR monthly_shared_limit > 0`

**Business Rules:**
- Currency should reference a valid currency code
- Personal workspaces typically have only one member
- Monthly shared limit is optional and can be NULL

---

### 3. `workspace_members`

Junction table representing the many-to-many relationship between users and workspaces. Stores role and weight information for each member.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `workspace_id` | UUID | NOT NULL, FK → workspaces.id | Reference to workspace |
| `user_id` | UUID | NOT NULL, FK → users.id | Reference to user |
| `role` | VARCHAR(20) | NOT NULL, CHECK | Member role: 'OWNER' or 'MEMBER' |
| `weight_percent` | NUMERIC(5,2) | NOT NULL, DEFAULT 100.00 | Weight percentage for weighted splits (0-100) |
| `personal_monthly_limit` | NUMERIC(15,2) | NULL | Optional personal monthly budget limit |
| `joined_at` | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() | When the user joined the workspace |

**Primary Key:** `(workspace_id, user_id)`

**Indexes:**
- Composite primary key on `(workspace_id, user_id)`
- Index on `user_id` (for finding all workspaces a user belongs to)
- Index on `workspace_id` (for finding all members of a workspace)
- Index on `role` (for filtering by role)

**Foreign Keys:**
- `workspace_id` → `workspaces.id` ON DELETE CASCADE
- `user_id` → `users.id` ON DELETE CASCADE

**Check Constraints:**
- `role IN ('OWNER', 'MEMBER')`
- `weight_percent >= 0 AND weight_percent <= 100`
- `personal_monthly_limit IS NULL OR personal_monthly_limit > 0`

**Business Rules:**
- Each workspace must have at least one OWNER
- Weight percentages should sum to 100 for all members in a workspace (enforced via application logic)
- When a workspace is deleted, all member records are deleted (CASCADE)
- When a user is deleted, all their workspace memberships are deleted (CASCADE)

---

### 4. `categories`

Expense categories. Can be workspace-specific (workspace_id IS NOT NULL) or global (workspace_id IS NULL).

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PRIMARY KEY | Unique category identifier |
| `workspace_id` | UUID | NULL, FK → workspaces.id | Reference to workspace (NULL for global categories) |
| `name` | VARCHAR(255) | NOT NULL | Category display name |
| `slug` | VARCHAR(255) | NOT NULL | URL-friendly identifier |
| `icon` | VARCHAR(100) | NOT NULL | Icon identifier (e.g., 'groceries', 'fa-solid fa-utensils') |
| `color` | VARCHAR(7) | NOT NULL | Hex color code (e.g., '#10b981') |
| `is_active` | BOOLEAN | NOT NULL, DEFAULT TRUE | Whether the category is active (soft delete) |
| `sort_order` | INTEGER | NOT NULL, DEFAULT 0 | Display order within workspace/global list |
| `created_at` | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() | Category creation timestamp |
| `updated_at` | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() | Last update timestamp |

**Indexes:**
- Primary key on `id`
- Index on `workspace_id` (for filtering workspace categories)
- Unique index on `(workspace_id, slug)` (ensures unique slugs per workspace/global)
- Index on `is_active` (for filtering active categories)
- Index on `(workspace_id, sort_order)` (for ordered category lists)

**Foreign Keys:**
- `workspace_id` → `workspaces.id` ON DELETE CASCADE (when workspace is deleted, its categories are deleted)

**Check Constraints:**
- `color` matches hex color pattern (e.g., '#[0-9A-Fa-f]{6}')
- `sort_order >= 0`

**Business Rules:**
- Global categories (workspace_id IS NULL) are shared across all workspaces
- Workspace-specific categories override global ones when both exist
- Categories are soft-deleted (is_active = FALSE) rather than hard-deleted to preserve expense history
- Slug must be unique within the same workspace (or globally if workspace_id IS NULL)

---

### 5. `expenses`

Records of expenses made within a workspace. Each expense is paid by one user and can have multiple participants.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PRIMARY KEY | Unique expense identifier |
| `workspace_id` | UUID | NOT NULL, FK → workspaces.id | Reference to workspace |
| `category_id` | UUID | NULL, FK → categories.id | Reference to category (nullable for uncategorized) |
| `amount` | NUMERIC(15,2) | NOT NULL | Expense amount |
| `currency` | VARCHAR(3) | NOT NULL | ISO 4217 currency code |
| `effective_date` | DATE | NOT NULL | Date when the expense occurred |
| `note` | TEXT | NULL | Optional note/description |
| `paid_by_user_id` | UUID | NOT NULL, FK → users.id | User who paid the expense |
| `created_by_user_id` | UUID | NOT NULL, FK → users.id | User who created the expense record |
| `created_at` | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() | Expense creation timestamp |
| `updated_at` | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() | Last update timestamp |

**Indexes:**
- Primary key on `id`
- Index on `workspace_id` (for listing expenses by workspace)
- Index on `category_id` (for filtering by category)
- Index on `paid_by_user_id` (for finding expenses paid by a user)
- Composite index on `(workspace_id, effective_date DESC)` (for paginated expense lists)
- Composite index on `(workspace_id, status, effective_date DESC)` (for filtered pagination)
- Index on `effective_date` (for date range queries)

**Foreign Keys:**
- `workspace_id` → `workspaces.id` ON DELETE RESTRICT (prevent deletion if expenses exist)
- `category_id` → `categories.id` ON DELETE SET NULL (preserve expense if category deleted)
- `paid_by_user_id` → `users.id` ON DELETE RESTRICT
- `created_by_user_id` → `users.id` ON DELETE RESTRICT

**Check Constraints:**
- `amount > 0`
- `currency` is valid ISO 4217 code (application-level validation)

**Business Rules:**
- Amount must be positive
- Currency should match workspace currency (enforced via application logic)

---

### 6. `expense_participants`

Junction table representing which users participate in (share) an expense. Used to calculate how expenses are split.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `expense_id` | UUID | NOT NULL, FK → expenses.id | Reference to expense |
| `user_id` | UUID | NOT NULL, FK → users.id | Reference to participating user |

**Primary Key:** `(expense_id, user_id)`

**Indexes:**
- Composite primary key on `(expense_id, user_id)`
- Index on `user_id` (for finding all expenses a user participates in)
- Index on `expense_id` (for finding all participants of an expense)

**Foreign Keys:**
- `expense_id` → `expenses.id` ON DELETE CASCADE
- `user_id` → `users.id` ON DELETE CASCADE

**Business Rules:**
- Each expense must have at least one participant (enforced via application logic)
- Participants should be members of the expense's workspace (enforced via application logic)
- When an expense is deleted, all participant records are deleted (CASCADE)
- In business logic, if no participants are specified in the request when creating an expense, all workspace members are treated as participants by default.


---

### 7. `payments`

Records of direct payments between users within a workspace. Separate from expenses, these represent money transfers (e.g., reimbursements, direct payments).

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PRIMARY KEY | Unique payment identifier |
| `workspace_id` | UUID | NOT NULL, FK → workspaces.id | Reference to workspace |
| `payee_user_id` | UUID | NOT NULL, FK → users.id | User receiving the payment |
| `paid_by_user_id` | UUID | NOT NULL, FK → users.id | User making the payment |
| `amount` | NUMERIC(15,2) | NOT NULL | Payment amount |
| `currency` | VARCHAR(3) | NOT NULL | ISO 4217 currency code |
| `effective_date` | DATE | NOT NULL | Date when payment occurred |
| `note` | TEXT | NULL | Optional note/description |
| `status` | VARCHAR(20) | NOT NULL, CHECK | Status: 'COMPLETED', 'PENDING', or 'FAILED' |
| `created_at` | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() | Payment creation timestamp |
| `updated_at` | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() | Last update timestamp |

**Indexes:**
- Primary key on `id`
- Index on `workspace_id` (for listing payments by workspace)
- Index on `payee_user_id` (for finding payments received by a user)
- Index on `paid_by_user_id` (for finding payments made by a user)
- Index on `status` (for filtering by status)
- Composite index on `(workspace_id, effective_date DESC)` (for paginated payment lists)
- Composite index on `(workspace_id, status, effective_date DESC)` (for filtered pagination)

**Foreign Keys:**
- `workspace_id` → `workspaces.id` ON DELETE RESTRICT
- `payee_user_id` → `users.id` ON DELETE RESTRICT
- `paid_by_user_id` → `users.id` ON DELETE RESTRICT

**Check Constraints:**
- `status IN ('COMPLETED', 'PENDING', 'FAILED')`
- `amount > 0`
- `payee_user_id != paid_by_user_id` (user cannot pay themselves)

**Business Rules:**
- Amount must be positive
- Payee and payer must be different users
- Currency should match workspace currency (enforced via application logic)
- Both payee and payer should be members of the workspace (enforced via application logic)

---

### 10. `invites`

Workspace invitation codes that allow users to join workspaces without explicit invitation.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PRIMARY KEY | Unique invite identifier |
| `workspace_id` | UUID | NOT NULL, FK → workspaces.id | Reference to workspace |
| `code` | VARCHAR(50) | NOT NULL, UNIQUE | Unique invitation code |
| `max_uses` | INTEGER | NOT NULL | Maximum number of times the invite can be used |
| `uses_count` | INTEGER | NOT NULL, DEFAULT 0 | Current number of times the invite has been used |
| `expires_at` | TIMESTAMPTZ | NULL | Expiration timestamp (NULL = never expires) |
| `created_at` | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() | Invite creation timestamp |

**Indexes:**
- Primary key on `id`
- Unique index on `code` (for fast lookup by code)
- Index on `workspace_id` (for listing invites by workspace)
- Index on `expires_at` (for finding expired invites)

**Foreign Keys:**
- `workspace_id` → `workspaces.id` ON DELETE CASCADE

**Check Constraints:**
- `max_uses > 0`
- `uses_count >= 0 AND uses_count <= max_uses`
- `expires_at IS NULL OR expires_at > created_at`

**Business Rules:**
- Code must be unique across all invites
- Uses count cannot exceed max uses
- Invite is considered expired if expires_at is set and current time > expires_at
- When a workspace is deleted, all its invites are deleted (CASCADE)

---

### 11. `notifications`

User notifications for various events (expense added, payment received, etc.).

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PRIMARY KEY | Unique notification identifier |
| `user_id` | UUID | NOT NULL, FK → users.id | User who receives the notification |
| `type` | VARCHAR(50) | NOT NULL | Notification type (e.g., 'message', 'alert', 'reminder') |
| `content` | TEXT | NOT NULL | Notification message content |
| `workspace_id` | UUID | NULL, FK → workspaces.id | Reference to workspace (if notification is workspace-related) |
| `read` | BOOLEAN | NOT NULL, DEFAULT FALSE | Whether the notification has been read |
| `timestamp` | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() | Notification creation timestamp |
| `context` | VARCHAR(50) | NOT NULL, DEFAULT 'general' | Context of the notification (e.g., 'PAYMENT', 'EXPENSE', 'WORKSPACE', 'GENERAL') |


**Indexes:**
- Primary key on `id`
- Index on `user_id` (for finding all notifications for a user)
- Index on `workspace_id` (for workspace-related notifications)
- Composite index on `(user_id, read, timestamp DESC)` (for unread notifications list)
- Index on `timestamp` (for chronological ordering)

**Foreign Keys:**
- `user_id` → `users.id` ON DELETE CASCADE
- `workspace_id` → `workspaces.id` ON DELETE SET NULL (preserve notification if workspace deleted)

**Business Rules:**
- Each notification belongs to one user
- Workspace_id is optional (some notifications may not be workspace-specific)
- When a user is deleted, all their notifications are deleted (CASCADE)
- When a workspace is deleted, notifications are preserved but workspace_id is set to NULL

---

### 12. `currencies`

Reference data table for supported currencies. This is typically read-only and populated with standard currency data.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `code` | VARCHAR(3) | PRIMARY KEY | ISO 4217 currency code (e.g., 'JPY', 'USD') |
| `name` | VARCHAR(100) | NOT NULL | Currency display name (e.g., 'Japanese Yen') |
| `symbol` | VARCHAR(10) | NOT NULL | Currency symbol (e.g., '¥', '$') |

**Indexes:**
- Primary key on `code`

**Business Rules:**
- Code must be a valid ISO 4217 currency code
- This table is typically populated once and rarely updated
- Used for validation and display purposes

---

## Relationships Summary

1. **Users ↔ Workspaces**: Many-to-many via `workspace_members`
2. **Workspaces → Categories**: One-to-many (categories can be global or workspace-specific)
3. **Workspaces → Expenses**: One-to-many
4. **Expenses ↔ Users**: Many-to-many via `expense_participants` (participants) + direct FK (paid_by)
5. **Workspaces → Payments**: One-to-many
8. **Workspaces → Invites**: One-to-many
9. **Users → Notifications**: One-to-many

## Indexing Strategy

### Primary Indexes
- All tables have UUID primary keys for efficient lookups

### Foreign Key Indexes
- All foreign keys are indexed to optimize JOIN operations

### Composite Indexes
- `(workspace_id, effective_date DESC)` on expenses and payments for paginated date-sorted lists
- `(workspace_id, status, effective_date DESC)` for filtered pagination
- `(workspace_id, slug)` on categories for unique slug enforcement
- `(user_id, read, timestamp DESC)` on notifications for unread notifications queries
- `(settlement_id, from_user_id, to_user_id)` on transfers for settlement queries

### Query Optimization Indexes
- Status fields are indexed for filtering
- Date fields are indexed for range queries
- Workspace_id is indexed on all workspace-related tables for efficient workspace-scoped queries

## Referential Integrity Rules

### ON DELETE CASCADE
- `workspace_members` when workspace or user is deleted
- `expense_participants` when expense or user is deleted
- `transfers` when settlement is deleted
- `invites` when workspace is deleted
- `notifications` when user is deleted

### ON DELETE RESTRICT
- `expenses` when workspace is deleted (prevent data loss)
- `payments` when workspace is deleted (prevent data loss)
- `settlements` when workspace is deleted (prevent data loss)
- `expenses` when paid_by_user or created_by_user is deleted (prevent orphaned expenses)
- `payments` when payee or payer is deleted (prevent orphaned payments)
- `settlements` when creator is deleted (prevent orphaned settlements)
- `transfers` when from_user or to_user is deleted (prevent orphaned transfers)

### ON DELETE SET NULL
- `expenses.category_id` when category is deleted (preserve expense, mark as uncategorized)
- `expenses.settlement_id` when settlement is deleted (preserve expense, mark as active)
- `notifications.workspace_id` when workspace is deleted (preserve notification, remove workspace reference)

## Data Types Rationale

- **UUID**: Used for all primary keys for distributed system compatibility and security (no sequential IDs)
- **NUMERIC(15,2)**: Used for monetary amounts to ensure precision (supports up to 999 trillion with 2 decimal places)
- **TIMESTAMPTZ**: Used for all timestamps to handle timezone-aware operations
- **VARCHAR**: Used with appropriate length limits based on expected data size
- **TEXT**: Used for potentially long text fields (notes, content)
- **BOOLEAN**: Used for simple true/false flags
- **DATE**: Used for effective dates (no time component needed)

## Future Considerations

1. **Audit Trail**: Consider adding `created_by` and `updated_by` audit fields to critical tables
2. **Soft Deletes**: Some tables (expenses, payments) might benefit from soft delete flags instead of hard deletes
3. **Versioning**: Consider adding version numbers for optimistic locking on frequently updated tables
4. **Partitioning**: For high-volume tables (expenses, payments), consider date-based partitioning
5. **Full-Text Search**: Consider adding full-text search indexes on note and content fields
6. **Currency Conversion**: Consider adding exchange rate tracking if multi-currency support is needed

---

## Persistence & Transactions

This application uses **JPA (Jakarta Persistence)** with **Hibernate** as the JPA provider, configured for **RESOURCE_LOCAL** transactions. The persistence layer is designed to be clean, minimal, and performant.

### Architecture Overview

#### EntityManagerFactoryProducer
- **Location**: `io.evenly.core.infrastructure.persistence.postgres.EntityManagerFactoryProducer`
- **Scope**: `@ApplicationScoped` (singleton)
- **Purpose**: Produces a singleton `EntityManagerFactory` that is shared across the application
- **Configuration**: Reads Hibernate settings from MicroProfile Config (`application.yaml`)

#### EntityManagerProducer
- **Location**: `io.evenly.core.infrastructure.persistence.postgres.EntityManagerProducer`
- **Scope**: Produces `@RequestScoped` `EntityManager`
- **Purpose**: Provides a request-scoped `EntityManager` that is properly closed at the end of each HTTP request
- **Lifecycle**: Automatically closed via CDI `@Disposes` method

#### JpaTx Transaction Helper
- **Location**: `io.evenly.core.infrastructure.persistence.postgres.JpaTx`
- **Purpose**: Provides transaction management for RESOURCE_LOCAL mode (since `@Transactional` requires JTA)
- **Usage**:
  ```java
  @Inject
  private JpaTx jpaTx;
  
  // Read-only transaction
  User user = jpaTx.read(() -> userRepository.findById(id));
  
  // Write transaction
  jpaTx.write(() -> {
      user.setEmail(newEmail);
      userRepository.save(user);
  });
  
  // Transaction with return value
  User saved = jpaTx.required(() -> userRepository.save(user));
  ```

### Transaction Boundaries

**Important**: Repositories **never** manage transactions. Services own transaction boundaries using `JpaTx`:

- **Repositories**: Thin data access layer, no `@Transactional`, no transaction logic
- **Services**: Use `JpaTx.read()`, `JpaTx.write()`, or `JpaTx.required()` to wrap operations
- **REST Resources**: Never access JPA directly; they call services

### Configuration

#### Database Configuration (`application.yaml`)
```yaml
db:
  url: ${DB_URL:jdbc:postgresql://localhost:5432/evenly}
  user: ${DB_USER:evenly}
  password: ${DB_PASSWORD:evenly}

hibernate:
  show_sql: ${HIBERNATE_SHOW_SQL:false}
  format_sql: ${HIBERNATE_FORMAT_SQL:false}
  jdbc:
    batch_size: ${HIBERNATE_BATCH_SIZE:20}
    time_zone: ${HIBERNATE_TIME_ZONE:UTC}
  order_inserts: ${HIBERNATE_ORDER_INSERTS:true}
  order_updates: ${HIBERNATE_ORDER_UPDATES:true}
```

#### Environment Variables
- `DB_URL`: Database connection URL (default: `jdbc:postgresql://localhost:5432/evenly`)
- `DB_USER`: Database username (default: `evenly`)
- `DB_PASSWORD`: Database password (default: `evenly`)
- `HIBERNATE_SHOW_SQL`: Enable SQL logging (default: `false`)
- `HIBERNATE_FORMAT_SQL`: Format SQL output (default: `false`)
- `HIBERNATE_BATCH_SIZE`: JDBC batch size (default: `20`)
- `HIBERNATE_TIME_ZONE`: Timezone for timestamps (default: `UTC`)

### Dev vs Prod Configuration

#### Development
Enable SQL logging for debugging:
```yaml
hibernate:
  show_sql: true
  format_sql: true
```

Or via environment variables:
```bash
export HIBERNATE_SHOW_SQL=true
export HIBERNATE_FORMAT_SQL=true
```

#### Production
- SQL logging disabled by default
- Batch processing enabled for performance
- Connection pooling via HikariCP (configured in `DataSourceProvider`)
- Schema validation (no auto-update)

### Performance Optimizations

1. **JDBC Batching**: Enabled with `hibernate.jdbc.batch_size=20`
2. **Ordered Inserts/Updates**: Enabled for better batch performance
3. **Connection Pooling**: HikariCP with sensible defaults (min: 1, max: 10)
4. **Query Optimization**: Use fetch joins and entity graphs to prevent N+1 queries
5. **Timezone Handling**: All timestamps use UTC

### Database Migrations

**Flyway** is integrated and runs automatically on application startup:
- **Location**: `src/main/resources/db/migration`
- **Execution**: Automatic via `DataSourceProvider` observer
- **Baseline**: `baselineOnMigrate=true` for existing databases

### Health Checks

A MicroProfile Health Check is available at `/health/ready`:
- **Location**: `io.evenly.core.infrastructure.health.DatabaseHealthCheck`
- **Checks**: DataSource connectivity and EntityManagerFactory status
- **Endpoint**: `/health/ready` (readiness probe)

### How to Add a New Entity/Repository

1. **Create Entity**:
   ```java
   @Entity
   @Table(name = "my_table")
   @Data
   @Builder
   @NoArgsConstructor
   @AllArgsConstructor
   public class MyEntity {
       @Id
       @Column(name = "id", columnDefinition = "UUID")
       private UUID id;
       
       @Column(name = "name", nullable = false, length = 255)
       private String name;
   }
   ```

2. **Create Repository Interface** (in `domain.repository` package):
   ```java
   public interface MyEntityRepository {
       Optional<MyEntity> findById(UUID id);
       MyEntity save(MyEntity entity);
   }
   ```

3. **Implement Repository** (in `features.*.persistence` package):
   ```java
   @ApplicationScoped
   public class MyEntityRepositoryImpl implements MyEntityRepository {
       @Inject
       private EntityManager entityManager;
       
       @Override
       public Optional<MyEntity> findById(UUID id) {
           MyEntity entity = entityManager.find(MyEntity.class, id);
           return Optional.ofNullable(entity);
       }
       
       @Override
       public MyEntity save(MyEntity entity) {
           if (entity.getId() == null) {
               entity.setId(UUID.randomUUID());
               entityManager.persist(entity);
               return entity;
           } else {
               return entityManager.merge(entity);
           }
       }
   }
   ```

4. **Create Service** (in `features.*.impl` package):
   ```java
   @ApplicationScoped
   public class MyEntityServiceImpl implements MyEntityService {
       @Inject
       private MyEntityRepository repository;
       
       @Inject
       private JpaTx jpaTx;
       
       @Override
       public Optional<MyEntityDto> findById(String id) {
           UUID uuid = UUID.fromString(id);
           return jpaTx.read(() -> repository.findById(uuid)
               .map(this::toDto));
       }
       
       @Override
       public MyEntityDto create(CreateMyEntityRequest request) {
           return jpaTx.required(() -> {
               MyEntity entity = MyEntity.builder()
                   .name(request.getName())
                   .build();
               entity = repository.save(entity);
               return toDto(entity);
           });
       }
   }
   ```

### Best Practices

1. **Always use `JpaTx` in services** - Never use `@Transactional` (requires JTA)
2. **Repositories are thin** - No business logic, no transactions
3. **Services own transactions** - Use `JpaTx.read()` for reads, `JpaTx.write()` or `JpaTx.required()` for writes
4. **Prevent N+1 queries** - Use fetch joins or entity graphs when loading related entities
5. **Use JPQL over native SQL** - Better portability and type safety
6. **Configure via `application.yaml`** - Keep configuration centralized

---

## Mock Mode Configuration

The Evenly Core application supports a **mock mode** that uses in-memory mock implementations instead of database-backed services. This is useful for local development, testing, and demos.

### Package Structure

All mock-related code is isolated in dedicated packages:

- `io.evenly.core.mock.data` - Mock data provider (`MockDataProvider`)
- `io.evenly.core.mock.service` - Mock service implementations
- `io.evenly.core.mock.config` - Mock profile activation and configuration

### Enabling Mock Mode

Mock mode is controlled by the MicroProfile Config property `mp.config.profile`. Set it to `mock` to enable mock implementations.

#### Via Environment Variable

```bash
export MP_CONFIG_PROFILE=mock
```

#### Via System Property

```bash
java -Dmp.config.profile=mock -jar evenly-core.jar
```

#### Via Configuration File

Add to `src/main/resources/META-INF/microprofile-config.properties`:

```properties
mp.config.profile=mock
```

#### Via Docker Compose

```yaml
services:
  evenly-core:
    environment:
      MP_CONFIG_PROFILE: mock
```

### Running in Mock Mode

**Local Development:**
```bash
# Set profile and run
export MP_CONFIG_PROFILE=mock
mvn exec:java
```

**With Maven:**
```bash
mvn exec:java -Dmp.config.profile=mock
```

**Production/Default Mode:**
Simply omit the profile or set it to any value other than `mock`:
```bash
# Default mode (uses real database implementations)
mvn exec:java
```

### How It Works

1. **Profile Detection**: `MockProfileActivator` checks if `mp.config.profile=mock` is set
2. **CDI Extension**: `MockProfileExtension` conditionally enables/disables mock beans based on profile
3. **Service Selection**: Mock services are marked with `@Alternative` and `@Priority`, making them take precedence when enabled
4. **Data Provider**: `MockDataProvider` is only instantiated when mock profile is active

### Mock Data

The mock implementation provides realistic test data including:
- Pre-configured users (gbargougui, alice, bob, diana)
- Multiple workspaces with different configurations
- Historical expenses, payments, and settlements
- Categories and currencies
- Notifications

### Switching Between Modes

- **Mock → Real**: Remove or change `mp.config.profile` from `mock` to any other value
- **Real → Mock**: Set `mp.config.profile=mock`

The application will automatically use the appropriate implementations based on the active profile.

### Important Notes

- Mock implementations are **never** loaded in production when profile is not `mock`
- No production code should depend on mock classes (compile-time dependency avoided)
- Mock data is in-memory and does not persist between restarts
- All mock services implement the same interfaces as real services, ensuring compatibility
