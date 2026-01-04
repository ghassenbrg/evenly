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

export interface Expense {
  id: string
  workspaceId: string
  amount: number
  paidByUserId: string
  categoryId: string
  date: string
  note?: string
  status: ExpenseStatus
  settlementId: string | null
  createdByUserId: string
  createdAt: string
  updatedAt: string
  paidBy?: User
  category?: Category
  participants?: ExpenseParticipant[]
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
  paidByUserId: string
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

export interface Payment {
  id: string
  workspaceId: string
  payerId: string
  payeeId: string
  amount: number
  currency: string
  type: PaymentType
  status: PaymentStatus
  note?: string
  reference?: string
  createdAt: string
  payer?: User
  payee?: User
}

export interface Notification {
  id: string
  workspaceId: string
  title: string
  message: string
  read: boolean
  createdAt: string
  workspace?: Workspace
}

export interface UnreadCountResponse {
  unreadCount: number
}

export interface PaginatedNotifications {
  items: Notification[]
  total: number
  page: number
  pageSize: number
  hasMore: boolean
}

