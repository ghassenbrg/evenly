// API Types matching the backend contract

export type SplitMode = 'EQUAL' | 'WEIGHTED'
export type MemberRole = 'OWNER' | 'MEMBER'
export type ExpenseStatus = 'ACTIVE' | 'SETTLED'
export type PaymentType = 'EXPENSE_SETTLEMENT' | 'REIMBURSEMENT' | 'MANUAL_TRANSFER' | 'SETTLEMENT'
export type PaymentStatus = 'COMPLETED' | 'PENDING' | 'FAILED'

export interface Currency {
  code: string
  name: string
  symbol: string
}

export interface User {
  id: string
  email: string
  displayName: string
  username?: string
  avatarUrl?: string
  preferredCurrency?: string
  locale?: string
  timezone?: string
  createdAt: string
}

export interface Workspace {
  id: string
  name: string
  defaultSplitMode: SplitMode
  monthlySharedLimit: number | null
  isPersonal: boolean
  currency?: string
  createdAt: string
  updatedAt: string
}

export interface WorkspaceMember {
  userId: string
  role: MemberRole
  weightPercent: number
  personalMonthlyLimit: number | null
  user?: User
}

export interface Category {
  id: string
  workspaceId: string
  name: string
  slug: string
  icon: string
  color?: string
  isActive: boolean
  sortOrder: number
  createdAt: string
  updatedAt: string
}

// Expense types matching endpoints.json
export interface Expense {
  id: string
  categoryId: string | null
  categoryName: string
  categoryIcon: string
  categoryColor?: string
  amount: number
  currency: string
  effectiveDate: string
  note?: string
  status: ExpenseStatus
  paidByUserId: string
  paidByUserName: string
}

export interface ExpenseParticipant {
  userId: string
  user?: User
}

export interface Invite {
  id: string
  workspaceId: string
  code: string
  maxUses: number
  usesCount: number
  expiresAt: string
  createdAt: string
}

export interface Balance {
  userId: string
  paid: number
  expected: number
  balance: number
  user?: User
}

export interface Transfer {
  id: string
  settlementId: string
  fromUserId: string
  toUserId: string
  amount: number
  fromUser?: User
  toUser?: User
}

export interface Settlement {
  id: string
  workspaceId: string
  createdByUserId: string
  createdAt: string
  createdBy?: User
}

// Analytics summary - keeping for backward compatibility but API uses balance-summary
export interface AnalyticsSummary {
  total: number
  youOwe?: number
  youAreOwed?: number
  sharedBudgetUsage?: {
    limit: number
    spent: number
    percentage: number
    warning: boolean
  }
  personalBudgetUsage?: {
    limit: number
    spent: number
    percentage: number
    warning: boolean
  }
}

// Analytics types matching endpoints.json
export interface ExpenseSnapshotItem {
  categoryId: string | null
  categoryName: string
  categoryIcon: string
  categoryColor?: string
  totalAmount: number
  spentPercentage: number
  expensesCount: number | null
}

export interface ExpenseSnapshotResponse {
  data: ExpenseSnapshotItem[]
  categoriesCount: number
  remainingCategoriesCount: number
}

export interface BalanceSummary {
  totalAmount?: number // Legacy field, kept for backward compatibility
  userTotalPaidAmount: number
  userTotalExpectedAmount: number
  workspaceTotalPaidAmount: number
  budgetLimit: number | null
  spentPercentage: number
  currency: string
}

export interface LinearChartDataPoint {
  date: string
  amount: number
}

export interface ExpenseSummary {
  totalAmount: number
  expensesCount: number
  averagePerDay: number
  currency: string
  largestExpenseAmount: number
  linearChartData: LinearChartDataPoint[]
}

export interface CategoryAnalytics {
  categoryId: string
  category?: Category
  total: number
  count: number
}

// Request/Response types

export interface RegisterRequest {
  email: string
  password: string
  displayName: string
  username: string
  preferredCurrency: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface AuthResponse {
  token: string
  user: User
}

export interface CreateWorkspaceRequest {
  name: string
  defaultSplitMode: SplitMode
  monthlySharedLimit?: number | null
  currency?: string
}

export interface UpdateWorkspaceSettingsRequest {
  name?: string
  defaultSplitMode?: SplitMode
  monthlySharedLimit?: number | null
}

export interface UpdateMemberWeightsRequest {
  weights: Array<{
    userId: string
    weightPercent: number
    personalMonthlyLimit?: number | null
  }>
}

export interface CreateInviteRequest {
  maxUses: number
  expiresInDays?: number
}

export interface JoinInviteRequest {
  code: string
}

export interface CreateCategoryRequest {
  name: string
  icon: string
  color?: string
}

export interface UpdateCategoryRequest {
  name?: string
  icon?: string
  color?: string
  isActive?: boolean
  sortOrder?: number
}

export interface CreateExpenseRequest {
  workspaceId?: string  // Optional - backend may determine from context
  amount: number
  categoryId: string
  date: string
  note?: string
  participantIds?: string[]
}

export interface UpdateExpenseRequest {
  amount?: number
  paidByUserId?: string
  categoryId?: string
  date?: string
  note?: string
  participantIds?: string[]
}

export interface CreateSettlementRequest {
  note?: string
}

// Settle-up types matching endpoints.json
export interface SettleUpMember {
  userId: string
  userFullName: string
  paidAmount: number
  expectedAmount: number
}

export interface SettleUpResponse {
  currency: string
  currentUser: SettleUpMember
  otherMembers: SettleUpMember[]
}

// Payment request types
export interface CreatePaymentRequest {
  payeeUserId: string
  amount: number
  note?: string
  effectiveDate?: string
}

// Payment types matching endpoints.json
export interface Payment {
  id: string
  payeeUserId: string
  payeeUserName: string
  amount: number
  currency: string
  effectiveDate: string
  note?: string
  status: PaymentStatus
  paidByUserId: string
  paidByUserName: string
}

// Notification types matching endpoints.json
export interface Notification {
  id: string
  type: string
  content: string
  title?: string
  message?: string
  actorUserId?: string
  entityType?: string
  entityId?: string
  timestamp: string
  workspaceId: string
  read: boolean
}

export interface UnreadCountResponse {
  count: number
}

// Pagination types matching endpoints.json
export interface PageInfo {
  number: number
  size: number
  totalElements: number
  totalPages: number
}

export interface SortInfo {
  sorted: boolean
  direction: 'ASC' | 'DESC'
  property: string
}

export interface PaginatedResponse<T> {
  data: T[]
  page: PageInfo
  sort: SortInfo
}

// Error response types
export interface ApiErrorResponse {
  error?: string
  message?: string
  status?: number
  errors?: Array<{
    field: string
    message: string
  }>
}
