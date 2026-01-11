package io.evenly.core.shared.common;

import io.evenly.core.features.auth.dto.User;
import io.evenly.core.features.workspaces.dto.Workspace;
import io.evenly.core.features.workspaces.dto.WorkspaceMember;
import io.evenly.core.features.categories.dto.Category;
import io.evenly.core.features.expenses.dto.Expense;
import io.evenly.core.features.payments.dto.Payment;
import io.evenly.core.features.settlements.dto.Settlement;
import io.evenly.core.features.invites.dto.Invite;
import io.evenly.core.features.notifications.dto.Notification;
import io.evenly.core.features.currencies.dto.Currency;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Mock data provider that generates realistic test data.
 * This can be easily swapped with real database-backed implementations.
 * 
 * CDI bean that provides mock data for development and testing.
 */
@ApplicationScoped
public class MockDataProvider {
    
    private final Random random = new Random(42); // Fixed seed for consistency
    
    // Predefined users
    private final Map<String, User> users = new HashMap<>();
    
    // Store data by workspace
    private final Map<String, List<Workspace>> userWorkspaces = new HashMap<>();
    private final Map<String, Workspace> workspaces = new HashMap<>();
    private final Map<String, List<WorkspaceMember>> workspaceMembers = new HashMap<>();
    private final Map<String, List<Category>> workspaceCategories = new HashMap<>();
    private final List<Category> globalCategories = new ArrayList<>();
    private final Map<String, List<Expense>> workspaceExpenses = new HashMap<>();
    private final Map<String, List<Payment>> workspacePayments = new HashMap<>();
    private final Map<String, List<Settlement>> workspaceSettlements = new HashMap<>();
    private final Map<String, List<Invite>> workspaceInvites = new HashMap<>();
    private final Map<String, List<Notification>> userNotifications = new HashMap<>();
    
    private final List<Currency> currencies = new ArrayList<>();
    
    @PostConstruct
    public void init() {
        initializeUsers();
        initializeCurrencies();
        initializeGlobalCategories();
        initializeWorkspaces();
    }
    
    private void initializeUsers() {
        createUser("user1", "alice@example.com", "alice", "Alice Johnson");
        createUser("user2", "bob@example.com", "bob", "Bob Smith");
        createUser("gbargougui", "charlie@example.com", "charlie", "Charlie Brown");
        createUser("user4", "diana@example.com", "diana", "Diana Prince");
    }
    
    private void createUser(String id, String email, String username, String displayName) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setUsername(username);
        user.setDisplayName(displayName);
        user.setPreferredCurrency("USD");
        user.setLocale("en-US");
        user.setTimezone("America/New_York");
        user.setCreatedAt(OffsetDateTime.now().minusMonths(6));
        users.put(id, user);
    }
    
    private void initializeCurrencies() {
        currencies.add(new Currency("USD", "US Dollar", "$"));
        currencies.add(new Currency("EUR", "Euro", "€"));
        currencies.add(new Currency("GBP", "British Pound", "£"));
        currencies.add(new Currency("JPY", "Japanese Yen", "¥"));
        currencies.add(new Currency("CAD", "Canadian Dollar", "C$"));
        currencies.add(new Currency("AUD", "Australian Dollar", "A$"));
        currencies.add(new Currency("CHF", "Swiss Franc", "Fr"));
        currencies.add(new Currency("CNY", "Chinese Yuan", "¥"));
    }
    
    private void initializeGlobalCategories() {
        String[] categoryNames = {"Food", "Transport", "Shopping", "Entertainment", "Bills", "Health", "Travel", "Other"};
        String[] categoryIcons = {"🍕", "🚗", "🛒", "🎬", "💡", "🏥", "✈️", "📦"};
        String[] categoryColors = {"#FF6B6B", "#4ECDC4", "#45B7D1", "#FFA07A", "#98D8C8", "#F7DC6F", "#BB8FCE", "#85C1E2"};
        
        for (int i = 0; i < categoryNames.length; i++) {
            Category category = new Category();
            category.setId("global-cat-" + (i + 1));
            category.setName(categoryNames[i]);
            category.setSlug(categoryNames[i].toLowerCase().replace(" ", "-"));
            category.setIcon(categoryIcons[i]);
            category.setColor(categoryColors[i]);
            category.setIsActive(true);
            category.setSortOrder(i + 1);
            category.setCreatedAt(OffsetDateTime.now().minusMonths(3));
            category.setUpdatedAt(OffsetDateTime.now().minusMonths(3));
            globalCategories.add(category);
        }
    }
    
    private void initializeWorkspaces() {
        // Workspace 1: Shared apartment
        Workspace ws1 = createWorkspace("ws1", "Shared Apartment", "EQUAL", "USD", 2000.0, false);
        userWorkspaces.computeIfAbsent("user1", k -> new ArrayList<>()).add(ws1);
        userWorkspaces.computeIfAbsent("user2", k -> new ArrayList<>()).add(ws1);
        userWorkspaces.computeIfAbsent("gbargougui", k -> new ArrayList<>()).add(ws1);
        
        List<WorkspaceMember> members1 = new ArrayList<>();
        members1.add(createMember("user1", "OWNER", users.get("user1"), 33.33));
        members1.add(createMember("user2", "MEMBER", users.get("user2"), 33.33));
        members1.add(createMember("gbargougui", "MEMBER", users.get("gbargougui"), 33.34));
        workspaceMembers.put("ws1", members1);
        
        initializeWorkspaceCategories("ws1");
        initializeWorkspaceExpenses("ws1");
        initializeWorkspacePayments("ws1");
        initializeWorkspaceSettlements("ws1");
        
        // Workspace 2: Personal workspace
        Workspace ws2 = createWorkspace("ws2", "Personal", "EQUAL", "USD", null, true);
        userWorkspaces.computeIfAbsent("user1", k -> new ArrayList<>()).add(ws2);
        List<WorkspaceMember> members2 = new ArrayList<>();
        members2.add(createMember("user1", "OWNER", users.get("user1"), 100.0));
        workspaceMembers.put("ws2", members2);
        initializeWorkspaceCategories("ws2");
        
        // Workspace 3: Vacation trip
        Workspace ws3 = createWorkspace("ws3", "Summer Trip", "EQUAL", "EUR", 3000.0, false);
        userWorkspaces.computeIfAbsent("user1", k -> new ArrayList<>()).add(ws3);
        userWorkspaces.computeIfAbsent("user4", k -> new ArrayList<>()).add(ws3);
        List<WorkspaceMember> members3 = new ArrayList<>();
        members3.add(createMember("user1", "OWNER", users.get("user1"), 50.0));
        members3.add(createMember("user4", "MEMBER", users.get("user4"), 50.0));
        workspaceMembers.put("ws3", members3);
        initializeWorkspaceCategories("ws3");
        initializeWorkspaceExpenses("ws3");
    }
    
    private Workspace createWorkspace(String id, String name, String splitMode, String currency, 
                                             Double monthlyLimit, boolean isPersonal) {
        Workspace workspace = new Workspace();
        workspace.setId(id);
        workspace.setName(name);
        workspace.setDefaultSplitMode(splitMode);
        workspace.setCurrency(currency);
        workspace.setMonthlySharedLimit(monthlyLimit);
        workspace.setIsPersonal(isPersonal);
        workspace.setCreatedAt(OffsetDateTime.now().minusMonths(2));
        workspace.setUpdatedAt(OffsetDateTime.now().minusDays(5));
        workspaces.put(id, workspace);
        return workspace;
    }
    
    private WorkspaceMember createMember(String userId, String role, User user, double weightPercent) {
        WorkspaceMember member = new WorkspaceMember();
        member.setUserId(userId);
        member.setRole(role);
        member.setUser(user);
        member.setWeightPercent(weightPercent);
        return member;
    }
    
    private void initializeWorkspaceCategories(String workspaceId) {
        List<Category> categories = new ArrayList<>();
        String[] names = {"Groceries", "Utilities", "Rent", "Restaurants", "Transport"};
        String[] icons = {"🛒", "💡", "🏠", "🍽️", "🚇"};
        String[] colors = {"#FF6B6B", "#4ECDC4", "#45B7D1", "#FFA07A", "#98D8C8"};
        
        for (int i = 0; i < names.length; i++) {
            Category category = new Category();
            category.setId(workspaceId + "-cat-" + (i + 1));
            category.setWorkspaceId(workspaceId);
            category.setName(names[i]);
            category.setSlug(names[i].toLowerCase().replace(" ", "-"));
            category.setIcon(icons[i]);
            category.setColor(colors[i]);
            category.setIsActive(true);
            category.setSortOrder(i + 1);
            category.setCreatedAt(OffsetDateTime.now().minusMonths(2));
            category.setUpdatedAt(OffsetDateTime.now().minusMonths(2));
            categories.add(category);
        }
        workspaceCategories.put(workspaceId, categories);
    }
    
    private void initializeWorkspaceExpenses(String workspaceId) {
        List<Expense> expenses = new ArrayList<>();
        List<Category> categories = workspaceCategories.getOrDefault(workspaceId, new ArrayList<>());
        List<WorkspaceMember> members = workspaceMembers.get(workspaceId);
        if (members == null || members.isEmpty()) return;
        
        BigDecimal[] amounts = {
            new BigDecimal("45.50"), new BigDecimal("120.00"), new BigDecimal("35.75"),
            new BigDecimal("85.25"), new BigDecimal("200.00"), new BigDecimal("12.50"),
            new BigDecimal("67.80"), new BigDecimal("150.00"), new BigDecimal("28.90"),
            new BigDecimal("95.40"), new BigDecimal("55.60"), new BigDecimal("180.00")
        };
        
        String[] notes = {
            "Weekly groceries", "Monthly utilities", "Dinner at restaurant",
            "Gas for car", "Rent payment", "Coffee with friends",
            "Concert tickets", "Shopping mall", "Lunch",
            "Cinema tickets", "Public transport", "Weekend trip"
        };
        
        for (int i = 0; i < amounts.length; i++) {
            Expense expense = new Expense();
            expense.setId(workspaceId + "-exp-" + (i + 1));
            if (!categories.isEmpty()) {
                Category cat = categories.get(i % categories.size());
                expense.setCategoryId(cat.getId());
                expense.setCategoryName(cat.getName());
                expense.setCategoryIcon(cat.getIcon());
                expense.setCategoryColor(cat.getColor());
            }
            expense.setAmount(amounts[i]);
            expense.setCurrency(workspaces.get(workspaceId).getCurrency());
            expense.setEffectiveDate(LocalDate.now().minusDays(amounts.length - i));
            expense.setNote(notes[i]);
            expense.setStatus(i < 8 ? "ACTIVE" : "SETTLED");
            WorkspaceMember paidBy = members.get(random.nextInt(members.size()));
            expense.setPaidByUserId(paidBy.getUserId());
            expense.setPaidByUserName(paidBy.getUser().getDisplayName());
            expenses.add(expense);
        }
        workspaceExpenses.put(workspaceId, expenses);
    }
    
    private void initializeWorkspacePayments(String workspaceId) {
        List<Payment> payments = new ArrayList<>();
        List<WorkspaceMember> members = workspaceMembers.get(workspaceId);
        if (members == null || members.size() < 2) return;
        
        for (int i = 0; i < 3; i++) {
            Payment payment = new Payment();
            payment.setId(workspaceId + "-pay-" + (i + 1));
            WorkspaceMember payee = members.get((i + 1) % members.size());
            WorkspaceMember payer = members.get(i % members.size());
            payment.setPayeeUserId(payee.getUserId());
            payment.setPayeeUserName(payee.getUser().getDisplayName());
            payment.setAmount(new BigDecimal("50.00"));
            payment.setCurrency(workspaces.get(workspaceId).getCurrency());
            payment.setEffectiveDate(LocalDate.now().minusDays(10 - i));
            payment.setNote("Settlement payment");
            payment.setStatus(i == 0 ? "PENDING" : "COMPLETED");
            payment.setPaidByUserId(payer.getUserId());
            payment.setPaidByUserName(payer.getUser().getDisplayName());
            payments.add(payment);
        }
        workspacePayments.put(workspaceId, payments);
    }
    
    private void initializeWorkspaceSettlements(String workspaceId) {
        List<Settlement> settlements = new ArrayList<>();
        List<WorkspaceMember> members = workspaceMembers.get(workspaceId);
        if (members == null || members.isEmpty()) return;
        
        Settlement settlement = new Settlement();
        settlement.setId(workspaceId + "-sett-1");
        settlement.setWorkspaceId(workspaceId);
        settlement.setCreatedByUserId(members.get(0).getUserId());
        settlement.setCreatedBy(members.get(0).getUser());
        settlement.setCreatedAt(OffsetDateTime.now().minusDays(7));
        settlements.add(settlement);
        workspaceSettlements.put(workspaceId, settlements);
    }
    
    // Public getters
    public Map<String, User> getUsers() {
        return users;
    }
    
    public Map<String, List<Workspace>> getUserWorkspaces() {
        return userWorkspaces;
    }
    
    public Map<String, Workspace> getWorkspaces() {
        return workspaces;
    }
    
    public Map<String, List<WorkspaceMember>> getWorkspaceMembers() {
        return workspaceMembers;
    }
    
    public Map<String, List<Category>> getWorkspaceCategories() {
        return workspaceCategories;
    }
    
    public List<Category> getGlobalCategories() {
        return new ArrayList<>(globalCategories);
    }
    
    public Map<String, List<Expense>> getWorkspaceExpenses() {
        return workspaceExpenses;
    }
    
    public Map<String, List<Payment>> getWorkspacePayments() {
        return workspacePayments;
    }
    
    public Map<String, List<Settlement>> getWorkspaceSettlements() {
        return workspaceSettlements;
    }
    
    public Map<String, List<Invite>> getWorkspaceInvites() {
        return workspaceInvites;
    }
    
    public Map<String, List<Notification>> getUserNotifications() {
        return userNotifications;
    }
    
    public List<Currency> getCurrencies() {
        return new ArrayList<>(currencies);
    }
}
