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
        initializeNotifications();
    }
    
    private void initializeUsers() {
        // Primary demo user: gbargougui
        createUser("gbargougui", "gbargougui@example.com", "gbargougui", "Ghassen Bargougui");
        // Additional users for testing multi-user scenarios
        createUser("user1", "alice@example.com", "alice", "Alice Johnson");
        createUser("user2", "bob@example.com", "bob", "Bob Smith");
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
        String[] categoryIcons = {"fa-solid fa-pizza-slice", "fa-solid fa-car", "fa-solid fa-cart-shopping", "fa-solid fa-film", "fa-solid fa-lightbulb", "fa-solid fa-hospital", "fa-solid fa-plane", "fa-solid fa-box"};
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
            category.setCreatedAt(OffsetDateTime.now().minusMonths(3).minusDays(random.nextInt(30)));
            category.setUpdatedAt(OffsetDateTime.now().minusMonths(3).minusDays(random.nextInt(30)));
            globalCategories.add(category);
        }
    }
    
    private void initializeWorkspaces() {
        // Workspace 1: Shared apartment (primary demo workspace for gbargougui)
        Workspace ws1 = createWorkspace("ws1", "Shared Apartment", "EQUAL", "USD", 2000.0, false);
        userWorkspaces.computeIfAbsent("gbargougui", k -> new ArrayList<>()).add(ws1);
        userWorkspaces.computeIfAbsent("user1", k -> new ArrayList<>()).add(ws1);
        userWorkspaces.computeIfAbsent("user2", k -> new ArrayList<>()).add(ws1);
        
        List<WorkspaceMember> members1 = new ArrayList<>();
        members1.add(createMember("gbargougui", "OWNER", users.get("gbargougui"), 33.34));
        members1.add(createMember("user1", "MEMBER", users.get("user1"), 33.33));
        members1.add(createMember("user2", "MEMBER", users.get("user2"), 33.33));
        workspaceMembers.put("ws1", members1);
        
        initializeWorkspaceCategories("ws1");
        initializeWorkspaceExpenses("ws1");
        initializeWorkspacePayments("ws1");
        initializeWorkspaceSettlements("ws1");
        
        // Workspace 2: Personal workspace for gbargougui
        Workspace ws2 = createWorkspace("ws2", "Personal", "EQUAL", "USD", null, true);
        userWorkspaces.computeIfAbsent("gbargougui", k -> new ArrayList<>()).add(ws2);
        List<WorkspaceMember> members2 = new ArrayList<>();
        members2.add(createMember("gbargougui", "OWNER", users.get("gbargougui"), 100.0));
        workspaceMembers.put("ws2", members2);
        initializeWorkspaceCategories("ws2");
        initializeWorkspaceExpenses("ws2");
        
        // Workspace 3: Vacation trip (additional workspace)
        Workspace ws3 = createWorkspace("ws3", "Summer Trip", "EQUAL", "EUR", 3000.0, false);
        userWorkspaces.computeIfAbsent("gbargougui", k -> new ArrayList<>()).add(ws3);
        userWorkspaces.computeIfAbsent("user4", k -> new ArrayList<>()).add(ws3);
        List<WorkspaceMember> members3 = new ArrayList<>();
        members3.add(createMember("gbargougui", "OWNER", users.get("gbargougui"), 50.0));
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
        // Workspaces created 3-4 months ago to match expense data timeline
        workspace.setCreatedAt(OffsetDateTime.now().minusMonths(3).minusDays(random.nextInt(30)));
        workspace.setUpdatedAt(OffsetDateTime.now().minusDays(random.nextInt(10)));
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
        
        // Expanded category list with more variety for richer data
        String[] names;
        String[] icons;
        String[] colors;
        
        if (workspaceId.equals("ws1")) {
            // Shared apartment - comprehensive categories
            names = new String[]{
                "Groceries", "Utilities", "Rent", "Restaurants", "Transport",
                "Entertainment", "Shopping", "Health", "Bills", "Travel",
                "Home & Garden", "Subscriptions", "Personal Care", "Insurance", "Education"
            };
            icons = new String[]{
                "fa-solid fa-cart-shopping", "fa-solid fa-lightbulb", "fa-solid fa-house", 
                "fa-solid fa-utensils", "fa-solid fa-train-subway", "fa-solid fa-film",
                "fa-solid fa-bag-shopping", "fa-solid fa-hospital", "fa-solid fa-file-invoice-dollar",
                "fa-solid fa-plane", "fa-solid fa-seedling", "fa-solid fa-tv", "fa-solid fa-spa",
                "fa-solid fa-shield", "fa-solid fa-graduation-cap"
            };
            colors = new String[]{
                "#FF6B6B", "#4ECDC4", "#45B7D1", "#FFA07A", "#98D8C8",
                "#F7DC6F", "#BB8FCE", "#85C1E2", "#F1948A", "#52BE80",
                "#F8B739", "#5DADE2", "#EC7063", "#A569BD", "#48C9B0"
            };
        } else if (workspaceId.equals("ws2")) {
            // Personal workspace - personal-focused categories
            names = new String[]{
                "Food & Dining", "Transport", "Shopping", "Entertainment", 
                "Health & Fitness", "Subscriptions", "Personal Care", "Hobbies",
                "Education", "Gifts", "Travel"
            };
            icons = new String[]{
                "fa-solid fa-utensils", "fa-solid fa-car", "fa-solid fa-bag-shopping",
                "fa-solid fa-film", "fa-solid fa-dumbbell", "fa-solid fa-tv",
                "fa-solid fa-spa", "fa-solid fa-palette", "fa-solid fa-graduation-cap",
                "fa-solid fa-gift", "fa-solid fa-plane"
            };
            colors = new String[]{
                "#FF6B6B", "#4ECDC4", "#45B7D1", "#FFA07A",
                "#98D8C8", "#F7DC6F", "#BB8FCE", "#85C1E2",
                "#48C9B0", "#F1948A", "#52BE80"
            };
        } else {
            // Vacation/trip workspace - travel-focused categories
            names = new String[]{
                "Accommodation", "Food & Dining", "Transport", "Activities",
                "Shopping", "Entertainment", "Miscellaneous", "Tours", "Souvenirs"
            };
            icons = new String[]{
                "fa-solid fa-hotel", "fa-solid fa-utensils", "fa-solid fa-plane",
                "fa-solid fa-ticket", "fa-solid fa-bag-shopping", "fa-solid fa-film",
                "fa-solid fa-box", "fa-solid fa-map", "fa-solid fa-gift"
            };
            colors = new String[]{
                "#45B7D1", "#FF6B6B", "#4ECDC4", "#FFA07A",
                "#98D8C8", "#F7DC6F", "#BB8FCE", "#52BE80", "#F1948A"
            };
        }
        
        for (int i = 0; i < names.length; i++) {
            Category category = new Category();
            category.setId(workspaceId + "-cat-" + (i + 1));
            category.setWorkspaceId(workspaceId);
            category.setName(names[i]);
            category.setSlug(names[i].toLowerCase().replace(" ", "-").replace("&", "and"));
            category.setIcon(icons[i]);
            category.setColor(colors[i]);
            category.setIsActive(true);
            category.setSortOrder(i + 1);
            category.setCreatedAt(OffsetDateTime.now().minusMonths(3).minusDays(random.nextInt(30)));
            category.setUpdatedAt(OffsetDateTime.now().minusMonths(3).minusDays(random.nextInt(30)));
            categories.add(category);
        }
        workspaceCategories.put(workspaceId, categories);
    }
    
    private void initializeWorkspaceExpenses(String workspaceId) {
        List<Expense> expenses = new ArrayList<>();
        List<Category> categories = workspaceCategories.getOrDefault(workspaceId, new ArrayList<>());
        List<WorkspaceMember> members = workspaceMembers.get(workspaceId);
        if (members == null || members.isEmpty()) return;
        
        // Generate expenses over 4-5 months (approximately 150 days) for more data
        int daysBack = 150;
        int expenseCount = 0;
        LocalDate today = LocalDate.now();
        String currency = workspaces.get(workspaceId).getCurrency();
        
        // Category-specific expense templates with realistic amounts and notes
        Map<String, ExpenseTemplate[]> categoryTemplates = new HashMap<>();
        
        // Groceries - frequent, moderate amounts
        categoryTemplates.put("Groceries", new ExpenseTemplate[]{
            new ExpenseTemplate(new BigDecimal("45.50"), "Weekly grocery shopping"),
            new ExpenseTemplate(new BigDecimal("62.30"), "Supermarket run"),
            new ExpenseTemplate(new BigDecimal("38.75"), "Quick grocery trip"),
            new ExpenseTemplate(new BigDecimal("89.20"), "Monthly bulk shopping"),
            new ExpenseTemplate(new BigDecimal("52.10"), "Weekend groceries"),
            new ExpenseTemplate(new BigDecimal("71.40"), "Grocery shopping"),
            new ExpenseTemplate(new BigDecimal("43.25"), "Food shopping"),
            new ExpenseTemplate(new BigDecimal("95.60"), "Big grocery haul")
        });
        
        // Utilities - monthly, larger amounts
        categoryTemplates.put("Utilities", new ExpenseTemplate[]{
            new ExpenseTemplate(new BigDecimal("120.00"), "Electricity bill"),
            new ExpenseTemplate(new BigDecimal("85.50"), "Water bill"),
            new ExpenseTemplate(new BigDecimal("95.25"), "Gas bill"),
            new ExpenseTemplate(new BigDecimal("110.75"), "Internet bill"),
            new ExpenseTemplate(new BigDecimal("78.40"), "Phone bill"),
            new ExpenseTemplate(new BigDecimal("135.20"), "Combined utilities")
        });
        
        // Rent - monthly, large amounts
        categoryTemplates.put("Rent", new ExpenseTemplate[]{
            new ExpenseTemplate(new BigDecimal("800.00"), "Monthly rent"),
            new ExpenseTemplate(new BigDecimal("750.00"), "Rent payment"),
            new ExpenseTemplate(new BigDecimal("850.00"), "Rent - shared apartment")
        });
        
        // Restaurants - frequent, varied amounts
        categoryTemplates.put("Restaurants", new ExpenseTemplate[]{
            new ExpenseTemplate(new BigDecimal("35.75"), "Dinner at Italian restaurant"),
            new ExpenseTemplate(new BigDecimal("28.50"), "Lunch with colleagues"),
            new ExpenseTemplate(new BigDecimal("42.30"), "Brunch on Sunday"),
            new ExpenseTemplate(new BigDecimal("67.80"), "Date night dinner"),
            new ExpenseTemplate(new BigDecimal("19.90"), "Quick lunch"),
            new ExpenseTemplate(new BigDecimal("55.40"), "Dinner out"),
            new ExpenseTemplate(new BigDecimal("38.20"), "Restaurant meal"),
            new ExpenseTemplate(new BigDecimal("72.60"), "Fine dining"),
            new ExpenseTemplate(new BigDecimal("24.75"), "Coffee and breakfast"),
            new ExpenseTemplate(new BigDecimal("48.90"), "Dinner with friends")
        });
        
        // Transport - frequent, smaller to moderate amounts
        categoryTemplates.put("Transport", new ExpenseTemplate[]{
            new ExpenseTemplate(new BigDecimal("45.25"), "Gas for car"),
            new ExpenseTemplate(new BigDecimal("12.50"), "Uber ride"),
            new ExpenseTemplate(new BigDecimal("28.00"), "Public transport pass"),
            new ExpenseTemplate(new BigDecimal("35.75"), "Taxi fare"),
            new ExpenseTemplate(new BigDecimal("18.30"), "Parking fee"),
            new ExpenseTemplate(new BigDecimal("52.40"), "Gas station"),
            new ExpenseTemplate(new BigDecimal("15.20"), "Metro card"),
            new ExpenseTemplate(new BigDecimal("22.60"), "Bus tickets"),
            new ExpenseTemplate(new BigDecimal("38.90"), "Car maintenance"),
            new ExpenseTemplate(new BigDecimal("9.50"), "Train ticket")
        });
        
        // Entertainment - moderate amounts
        categoryTemplates.put("Entertainment", new ExpenseTemplate[]{
            new ExpenseTemplate(new BigDecimal("25.00"), "Movie tickets"),
            new ExpenseTemplate(new BigDecimal("45.50"), "Concert tickets"),
            new ExpenseTemplate(new BigDecimal("18.75"), "Streaming subscription"),
            new ExpenseTemplate(new BigDecimal("32.40"), "Event tickets"),
            new ExpenseTemplate(new BigDecimal("15.90"), "Game purchase"),
            new ExpenseTemplate(new BigDecimal("28.60"), "Theater tickets"),
            new ExpenseTemplate(new BigDecimal("55.00"), "Concert event"),
            new ExpenseTemplate(new BigDecimal("22.50"), "Entertainment venue")
        });
        
        // Shopping - varied amounts
        categoryTemplates.put("Shopping", new ExpenseTemplate[]{
            new ExpenseTemplate(new BigDecimal("65.00"), "Clothing"),
            new ExpenseTemplate(new BigDecimal("45.30"), "Electronics"),
            new ExpenseTemplate(new BigDecimal("28.50"), "Household items"),
            new ExpenseTemplate(new BigDecimal("85.75"), "Online shopping"),
            new ExpenseTemplate(new BigDecimal("35.20"), "Gifts"),
            new ExpenseTemplate(new BigDecimal("52.40"), "Shopping mall"),
            new ExpenseTemplate(new BigDecimal("42.60"), "Retail purchase"),
            new ExpenseTemplate(new BigDecimal("38.90"), "Shopping trip")
        });
        
        // Health - moderate to large amounts
        categoryTemplates.put("Health", new ExpenseTemplate[]{
            new ExpenseTemplate(new BigDecimal("35.00"), "Pharmacy"),
            new ExpenseTemplate(new BigDecimal("120.00"), "Doctor visit"),
            new ExpenseTemplate(new BigDecimal("45.50"), "Vitamins"),
            new ExpenseTemplate(new BigDecimal("85.00"), "Dental checkup"),
            new ExpenseTemplate(new BigDecimal("25.75"), "Health products"),
            new ExpenseTemplate(new BigDecimal("95.00"), "Medical appointment")
        });
        
        // Bills - larger amounts, monthly
        categoryTemplates.put("Bills", new ExpenseTemplate[]{
            new ExpenseTemplate(new BigDecimal("95.00"), "Phone bill"),
            new ExpenseTemplate(new BigDecimal("65.50"), "Internet bill"),
            new ExpenseTemplate(new BigDecimal("45.25"), "Insurance payment"),
            new ExpenseTemplate(new BigDecimal("125.00"), "Combined bills"),
            new ExpenseTemplate(new BigDecimal("78.00"), "Utility bill"),
            new ExpenseTemplate(new BigDecimal("110.00"), "Monthly bills")
        });
        
        // Travel - larger amounts
        categoryTemplates.put("Travel", new ExpenseTemplate[]{
            new ExpenseTemplate(new BigDecimal("250.00"), "Hotel booking"),
            new ExpenseTemplate(new BigDecimal("180.00"), "Flight tickets"),
            new ExpenseTemplate(new BigDecimal("95.50"), "Travel expenses"),
            new ExpenseTemplate(new BigDecimal("150.00"), "Vacation costs"),
            new ExpenseTemplate(new BigDecimal("320.00"), "Travel package")
        });
        
        // Home & Garden - moderate amounts
        categoryTemplates.put("Home & Garden", new ExpenseTemplate[]{
            new ExpenseTemplate(new BigDecimal("45.00"), "Home supplies"),
            new ExpenseTemplate(new BigDecimal("28.50"), "Garden tools"),
            new ExpenseTemplate(new BigDecimal("65.75"), "Furniture"),
            new ExpenseTemplate(new BigDecimal("35.20"), "Decorations"),
            new ExpenseTemplate(new BigDecimal("52.40"), "Home improvement"),
            new ExpenseTemplate(new BigDecimal("38.90"), "Garden supplies")
        });
        
        // Subscriptions - smaller, recurring amounts
        categoryTemplates.put("Subscriptions", new ExpenseTemplate[]{
            new ExpenseTemplate(new BigDecimal("15.99"), "Streaming service"),
            new ExpenseTemplate(new BigDecimal("9.99"), "Music subscription"),
            new ExpenseTemplate(new BigDecimal("12.50"), "Software subscription"),
            new ExpenseTemplate(new BigDecimal("19.99"), "Gym membership"),
            new ExpenseTemplate(new BigDecimal("8.99"), "News subscription"),
            new ExpenseTemplate(new BigDecimal("24.99"), "Premium service")
        });
        
        // Personal Care - smaller amounts
        categoryTemplates.put("Personal Care", new ExpenseTemplate[]{
            new ExpenseTemplate(new BigDecimal("25.00"), "Haircut"),
            new ExpenseTemplate(new BigDecimal("18.50"), "Skincare products"),
            new ExpenseTemplate(new BigDecimal("35.75"), "Spa treatment"),
            new ExpenseTemplate(new BigDecimal("22.40"), "Personal care items"),
            new ExpenseTemplate(new BigDecimal("28.00"), "Beauty products"),
            new ExpenseTemplate(new BigDecimal("32.50"), "Personal care service")
        });
        
        // Insurance - larger amounts, periodic
        categoryTemplates.put("Insurance", new ExpenseTemplate[]{
            new ExpenseTemplate(new BigDecimal("150.00"), "Health insurance"),
            new ExpenseTemplate(new BigDecimal("85.00"), "Car insurance"),
            new ExpenseTemplate(new BigDecimal("120.00"), "Home insurance"),
            new ExpenseTemplate(new BigDecimal("95.50"), "Insurance premium")
        });
        
        // Education - moderate to large amounts
        categoryTemplates.put("Education", new ExpenseTemplate[]{
            new ExpenseTemplate(new BigDecimal("125.00"), "Course fee"),
            new ExpenseTemplate(new BigDecimal("45.00"), "Books"),
            new ExpenseTemplate(new BigDecimal("85.00"), "Training"),
            new ExpenseTemplate(new BigDecimal("65.50"), "Educational materials"),
            new ExpenseTemplate(new BigDecimal("200.00"), "Workshop")
        });
        
        // Accommodation - large amounts (for travel workspace)
        categoryTemplates.put("Accommodation", new ExpenseTemplate[]{
            new ExpenseTemplate(new BigDecimal("120.00"), "Hotel night"),
            new ExpenseTemplate(new BigDecimal("85.50"), "Hostel"),
            new ExpenseTemplate(new BigDecimal("200.00"), "Resort stay"),
            new ExpenseTemplate(new BigDecimal("150.00"), "Airbnb"),
            new ExpenseTemplate(new BigDecimal("95.00"), "Lodging")
        });
        
        // Activities - moderate amounts (for travel workspace)
        categoryTemplates.put("Activities", new ExpenseTemplate[]{
            new ExpenseTemplate(new BigDecimal("45.00"), "Tour tickets"),
            new ExpenseTemplate(new BigDecimal("35.50"), "Activity booking"),
            new ExpenseTemplate(new BigDecimal("28.75"), "Event entry"),
            new ExpenseTemplate(new BigDecimal("55.00"), "Adventure activity"),
            new ExpenseTemplate(new BigDecimal("42.00"), "Activity pass")
        });
        
        // Food & Dining - for personal workspace
        categoryTemplates.put("Food & Dining", new ExpenseTemplate[]{
            new ExpenseTemplate(new BigDecimal("35.75"), "Restaurant meal"),
            new ExpenseTemplate(new BigDecimal("28.50"), "Takeout"),
            new ExpenseTemplate(new BigDecimal("42.30"), "Dinner out"),
            new ExpenseTemplate(new BigDecimal("19.90"), "Quick meal"),
            new ExpenseTemplate(new BigDecimal("48.90"), "Dining out"),
            new ExpenseTemplate(new BigDecimal("32.40"), "Food delivery")
        });
        
        // Health & Fitness - for personal workspace
        categoryTemplates.put("Health & Fitness", new ExpenseTemplate[]{
            new ExpenseTemplate(new BigDecimal("65.00"), "Gym membership"),
            new ExpenseTemplate(new BigDecimal("45.50"), "Fitness equipment"),
            new ExpenseTemplate(new BigDecimal("35.00"), "Supplements"),
            new ExpenseTemplate(new BigDecimal("28.75"), "Fitness class"),
            new ExpenseTemplate(new BigDecimal("52.00"), "Personal trainer"),
            new ExpenseTemplate(new BigDecimal("38.90"), "Fitness gear")
        });
        
        // Hobbies - moderate amounts
        categoryTemplates.put("Hobbies", new ExpenseTemplate[]{
            new ExpenseTemplate(new BigDecimal("45.00"), "Hobby supplies"),
            new ExpenseTemplate(new BigDecimal("28.50"), "Craft materials"),
            new ExpenseTemplate(new BigDecimal("65.75"), "Equipment purchase"),
            new ExpenseTemplate(new BigDecimal("35.20"), "Hobby items"),
            new ExpenseTemplate(new BigDecimal("52.40"), "Hobby tools")
        });
        
        // Gifts - moderate amounts
        categoryTemplates.put("Gifts", new ExpenseTemplate[]{
            new ExpenseTemplate(new BigDecimal("45.00"), "Birthday gift"),
            new ExpenseTemplate(new BigDecimal("35.50"), "Gift purchase"),
            new ExpenseTemplate(new BigDecimal("65.00"), "Special occasion gift"),
            new ExpenseTemplate(new BigDecimal("28.75"), "Gift card"),
            new ExpenseTemplate(new BigDecimal("52.40"), "Gift item")
        });
        
        // Tours - moderate amounts (for travel workspace)
        categoryTemplates.put("Tours", new ExpenseTemplate[]{
            new ExpenseTemplate(new BigDecimal("85.00"), "Guided tour"),
            new ExpenseTemplate(new BigDecimal("65.50"), "Tour booking"),
            new ExpenseTemplate(new BigDecimal("95.00"), "Day tour"),
            new ExpenseTemplate(new BigDecimal("120.00"), "Multi-day tour")
        });
        
        // Souvenirs - smaller amounts (for travel workspace)
        categoryTemplates.put("Souvenirs", new ExpenseTemplate[]{
            new ExpenseTemplate(new BigDecimal("25.00"), "Souvenir purchase"),
            new ExpenseTemplate(new BigDecimal("18.50"), "Local crafts"),
            new ExpenseTemplate(new BigDecimal("32.40"), "Memorabilia"),
            new ExpenseTemplate(new BigDecimal("22.00"), "Gift shop")
        });
        
        // Miscellaneous - varied amounts
        categoryTemplates.put("Miscellaneous", new ExpenseTemplate[]{
            new ExpenseTemplate(new BigDecimal("25.00"), "Misc expense"),
            new ExpenseTemplate(new BigDecimal("18.50"), "Other items"),
            new ExpenseTemplate(new BigDecimal("35.75"), "Various expenses"),
            new ExpenseTemplate(new BigDecimal("28.00"), "Miscellaneous purchase")
        });
        
        // Generate expenses with realistic distribution over time
        for (int dayOffset = daysBack; dayOffset >= 0; dayOffset--) {
            LocalDate expenseDate = today.minusDays(dayOffset);
            
            // Skip weekends less frequently (40% chance on weekends vs 80% on weekdays)
            boolean isWeekend = expenseDate.getDayOfWeek().getValue() >= 6;
            if (isWeekend && random.nextDouble() > 0.4) {
                continue;
            }
            
            // Determine number of expenses per day (0-3, weighted toward 1-2 for more data)
            int expensesToday = 0;
            double rand = random.nextDouble();
            if (rand < 0.45) {
                expensesToday = 1;
            } else if (rand < 0.75) {
                expensesToday = 2;
            } else if (rand < 0.90) {
                expensesToday = 0;
            } else {
                expensesToday = 3; // Occasionally 3 expenses in a day
            }
            
            for (int e = 0; e < expensesToday; e++) {
                // Select category (weighted - groceries and restaurants more frequent)
                Category selectedCategory;
                if (categories.isEmpty()) {
                    continue;
                }
                
                double categoryRand = random.nextDouble();
                if (categoryRand < 0.30) {
                    // 30% chance for Groceries
                    selectedCategory = categories.stream()
                        .filter(c -> c.getName().equals("Groceries"))
                        .findFirst()
                        .orElse(categories.get(random.nextInt(categories.size())));
                } else if (categoryRand < 0.55) {
                    // 25% chance for Restaurants
                    selectedCategory = categories.stream()
                        .filter(c -> c.getName().equals("Restaurants"))
                        .findFirst()
                        .orElse(categories.get(random.nextInt(categories.size())));
                } else if (categoryRand < 0.70) {
                    // 15% chance for Transport
                    selectedCategory = categories.stream()
                        .filter(c -> c.getName().equals("Transport"))
                        .findFirst()
                        .orElse(categories.get(random.nextInt(categories.size())));
                } else if (categoryRand < 0.85 && expenseDate.getDayOfMonth() <= 5) {
                    // 15% chance for Rent (only early in month)
                    selectedCategory = categories.stream()
                        .filter(c -> c.getName().equals("Rent"))
                        .findFirst()
                        .orElse(categories.get(random.nextInt(categories.size())));
                } else if (categoryRand < 0.92 && expenseDate.getDayOfMonth() >= 25) {
                    // 7% chance for Utilities (late in month)
                    selectedCategory = categories.stream()
                        .filter(c -> c.getName().equals("Utilities"))
                        .findFirst()
                        .orElse(categories.get(random.nextInt(categories.size())));
                } else {
                    // Random category
                    selectedCategory = categories.get(random.nextInt(categories.size()));
                }
                
                // Get template for category or use default
                ExpenseTemplate template;
                ExpenseTemplate[] templates = categoryTemplates.get(selectedCategory.getName());
                if (templates != null && templates.length > 0) {
                    template = templates[random.nextInt(templates.length)];
                } else {
                    // Default template
                    BigDecimal amount = new BigDecimal(String.format("%.2f", 20 + random.nextDouble() * 100));
                    template = new ExpenseTemplate(amount, "Expense");
                }
                
                // Add some variation to amounts (±20%)
                double variation = 0.8 + (random.nextDouble() * 0.4);
                BigDecimal amount = template.amount.multiply(BigDecimal.valueOf(variation))
                    .setScale(2, java.math.RoundingMode.HALF_UP);
                
                // Create expense
                Expense expense = new Expense();
                expense.setId(workspaceId + "-exp-" + (++expenseCount));
                expense.setCategoryId(selectedCategory.getId());
                expense.setCategoryName(selectedCategory.getName());
                expense.setCategoryIcon(selectedCategory.getIcon());
                expense.setCategoryColor(selectedCategory.getColor());
                expense.setAmount(amount);
                expense.setCurrency(currency);
                expense.setEffectiveDate(expenseDate);
                expense.setNote(template.note);
                
                // Older expenses (more than 30 days) are more likely to be SETTLED
                boolean isOld = dayOffset > 30;
                expense.setStatus((isOld && random.nextDouble() < 0.7) ? "SETTLED" : "ACTIVE");
                
                // Select payer (prefer gbargougui for ws1, otherwise random)
                WorkspaceMember paidBy;
                if (workspaceId.equals("ws1") && random.nextDouble() < 0.6) {
                    paidBy = members.stream()
                        .filter(m -> "gbargougui".equals(m.getUserId()))
                        .findFirst()
                        .orElse(members.get(random.nextInt(members.size())));
                } else {
                    paidBy = members.get(random.nextInt(members.size()));
                }
                expense.setPaidByUserId(paidBy.getUserId());
                expense.setPaidByUserName(paidBy.getUser().getDisplayName());
                
                expenses.add(expense);
            }
        }
        
        // Sort by date descending (most recent first)
        expenses.sort((e1, e2) -> e2.getEffectiveDate().compareTo(e1.getEffectiveDate()));
        
        workspaceExpenses.put(workspaceId, expenses);
    }
    
    // Helper class for expense templates
    private static class ExpenseTemplate {
        final BigDecimal amount;
        final String note;
        
        ExpenseTemplate(BigDecimal amount, String note) {
            this.amount = amount;
            this.note = note;
        }
    }
    
    private void initializeWorkspacePayments(String workspaceId) {
        List<Payment> payments = new ArrayList<>();
        List<WorkspaceMember> members = workspaceMembers.get(workspaceId);
        if (members == null || members.size() < 2) return;
        
        // Find gbargougui member if exists
        WorkspaceMember gbargouguiMember = members.stream()
            .filter(m -> "gbargougui".equals(m.getUserId()))
            .findFirst()
            .orElse(null);
        
        // Generate 18-25 payments over the last 4-5 months for more data
        int paymentCount = 18 + random.nextInt(8);
        LocalDate today = LocalDate.now();
        String currency = workspaces.get(workspaceId).getCurrency();
        
        // Payment amounts (realistic settlement amounts)
        BigDecimal[] paymentAmounts = {
            new BigDecimal("45.50"), new BigDecimal("120.00"), new BigDecimal("85.25"),
            new BigDecimal("67.80"), new BigDecimal("150.00"), new BigDecimal("95.40"),
            new BigDecimal("55.60"), new BigDecimal("180.00"), new BigDecimal("125.30"),
            new BigDecimal("78.90"), new BigDecimal("200.00"), new BigDecimal("92.15"),
            new BigDecimal("110.75"), new BigDecimal("65.40"), new BigDecimal("135.20")
        };
        
        String[] paymentNotes = {
            "Settlement payment", "Balance settlement", "Monthly settlement",
            "Partial payment", "Settlement for expenses", "Balance payment",
            "Settlement", "Reimbursement", "Balance due"
        };
        
        for (int i = 0; i < paymentCount; i++) {
            Payment payment = new Payment();
            payment.setId(workspaceId + "-pay-" + (i + 1));
            
            // For demo consistency, involve gbargougui in payments when possible
            WorkspaceMember payee, payer;
            if (gbargouguiMember != null && workspaceId.equals("ws1")) {
                // For ws1, make gbargougui the payer 60% of the time
                if (random.nextDouble() < 0.6) {
                    payer = gbargouguiMember;
                    payee = members.stream()
                        .filter(m -> !"gbargougui".equals(m.getUserId()))
                        .findFirst()
                        .orElse(members.get((i + 1) % members.size()));
                } else {
                    // Sometimes gbargougui receives payment
                    payee = gbargouguiMember;
                    payer = members.stream()
                        .filter(m -> !"gbargougui".equals(m.getUserId()))
                        .findFirst()
                        .orElse(members.get(i % members.size()));
                }
            } else {
                // Random assignment for other workspaces
                int payerIdx = random.nextInt(members.size());
                int payeeIdx = (payerIdx + 1) % members.size();
                payer = members.get(payerIdx);
                payee = members.get(payeeIdx);
            }
            
            payment.setPayeeUserId(payee.getUserId());
            payment.setPayeeUserName(payee.getUser().getDisplayName());
            payment.setAmount(paymentAmounts[i % paymentAmounts.length]);
            payment.setCurrency(currency);
            
            // Spread payments over last 150 days, more frequent in recent weeks
            int daysBack;
            if (i < paymentCount / 3) {
                // Recent payments (last 2 weeks)
                daysBack = random.nextInt(14);
            } else if (i < 2 * paymentCount / 3) {
                // Middle period (2-8 weeks ago)
                daysBack = 14 + random.nextInt(42);
            } else {
                // Older payments (8-20 weeks ago)
                daysBack = 56 + random.nextInt(94);
            }
            payment.setEffectiveDate(today.minusDays(daysBack));
            payment.setNote(paymentNotes[random.nextInt(paymentNotes.length)]);
            
            // Recent payments more likely to be PENDING
            payment.setStatus((daysBack < 7 && random.nextDouble() < 0.3) ? "PENDING" : "COMPLETED");
            payment.setPaidByUserId(payer.getUserId());
            payment.setPaidByUserName(payer.getUser().getDisplayName());
            payments.add(payment);
        }
        
        // Sort by date descending
        payments.sort((p1, p2) -> p2.getEffectiveDate().compareTo(p1.getEffectiveDate()));
        
        workspacePayments.put(workspaceId, payments);
    }
    
    private void initializeWorkspaceSettlements(String workspaceId) {
        List<Settlement> settlements = new ArrayList<>();
        List<WorkspaceMember> members = workspaceMembers.get(workspaceId);
        if (members == null || members.isEmpty()) return;
        
        // Generate 6-10 settlements over the last 4-5 months for more data
        int settlementCount = 6 + random.nextInt(5);
        OffsetDateTime now = OffsetDateTime.now();
        
        for (int i = 0; i < settlementCount; i++) {
            // Prefer gbargougui as the creator for demo consistency, but mix it up
            WorkspaceMember creator;
            if (workspaceId.equals("ws1") && random.nextDouble() < 0.7) {
                creator = members.stream()
                    .filter(m -> "gbargougui".equals(m.getUserId()))
                    .findFirst()
                    .orElse(members.get(0));
            } else {
                creator = members.get(random.nextInt(members.size()));
            }
            
            Settlement settlement = new Settlement();
            settlement.setId(workspaceId + "-sett-" + (i + 1));
            settlement.setWorkspaceId(workspaceId);
            settlement.setCreatedByUserId(creator.getUserId());
            settlement.setCreatedBy(creator.getUser());
            
            // Spread settlements over last 150 days, more recent ones more frequent
            int daysBack;
            if (i < settlementCount / 2) {
                // Recent settlements (last 3 weeks)
                daysBack = random.nextInt(21);
            } else {
                // Older settlements (3-20 weeks ago)
                daysBack = 21 + random.nextInt(129);
            }
            settlement.setCreatedAt(now.minusDays(daysBack));
            settlements.add(settlement);
        }
        
        // Sort by date descending
        settlements.sort((s1, s2) -> s2.getCreatedAt().compareTo(s1.getCreatedAt()));
        
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
    
    public List<Currency> getCurrencies() {
        return new ArrayList<>(currencies);
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
    
    private void initializeNotifications() {
        OffsetDateTime now = OffsetDateTime.now();
        
        // Generate notifications for gbargougui (primary demo user)
        List<Notification> gbargouguiNotifications = new ArrayList<>();
        
        // Notification types and templates
        String[] notificationTypes = {
            "EXPENSE_ADDED", "EXPENSE_UPDATED", "EXPENSE_SETTLED", 
            "PAYMENT_RECEIVED", "PAYMENT_SENT", "SETTLEMENT_CREATED",
            "MEMBER_ADDED", "MEMBER_REMOVED", "WORKSPACE_UPDATED",
            "INVITE_ACCEPTED", "BALANCE_REMINDER", "BUDGET_WARNING"
        };
        
        // Generate 25-35 notifications over the last 60 days
        int notificationCount = 25 + random.nextInt(11);
        
        for (int i = 0; i < notificationCount; i++) {
            Notification notification = new Notification();
            notification.setId("notif-" + (i + 1));
            
            // Select notification type
            String type = notificationTypes[random.nextInt(notificationTypes.length)];
            notification.setType(type);
            
            // Generate content based on type
            String content = generateNotificationContent(type, i);
            notification.setContent(content);
            
            // Spread notifications over last 60 days, more recent ones more frequent
            int daysBack;
            if (i < notificationCount / 3) {
                // Recent notifications (last week)
                daysBack = random.nextInt(7);
            } else if (i < 2 * notificationCount / 3) {
                // Middle period (1-3 weeks ago)
                daysBack = 7 + random.nextInt(14);
            } else {
                // Older notifications (3-8 weeks ago)
                daysBack = 21 + random.nextInt(39);
            }
            
            int hoursBack = random.nextInt(24);
            int minutesBack = random.nextInt(60);
            notification.setTimestamp(now.minusDays(daysBack).minusHours(hoursBack).minusMinutes(minutesBack));
            
            // Assign to a workspace (prefer ws1)
            String[] workspaceIds = {"ws1", "ws2", "ws3"};
            String workspaceId = workspaceIds[random.nextInt(workspaceIds.length)];
            if (random.nextDouble() < 0.6) {
                workspaceId = "ws1"; // Prefer ws1 60% of the time
            }
            notification.setWorkspaceId(workspaceId);
            
            // Recent notifications more likely to be unread (70% unread if < 7 days, 30% if older)
            boolean isRecent = daysBack < 7;
            notification.setRead(isRecent ? (random.nextDouble() < 0.3) : (random.nextDouble() < 0.7));
            
            gbargouguiNotifications.add(notification);
        }
        
        // Sort by timestamp descending (most recent first)
        gbargouguiNotifications.sort((n1, n2) -> n2.getTimestamp().compareTo(n1.getTimestamp()));
        
        userNotifications.put("gbargougui", gbargouguiNotifications);
        
        // Generate some notifications for other users too
        List<Notification> user1Notifications = new ArrayList<>();
        for (int i = 0; i < 8 + random.nextInt(7); i++) {
            Notification notification = new Notification();
            notification.setId("notif-user1-" + (i + 1));
            notification.setType(notificationTypes[random.nextInt(notificationTypes.length)]);
            notification.setContent(generateNotificationContent(notification.getType(), i));
            
            int daysBack = random.nextInt(30);
            int hoursBack = random.nextInt(24);
            notification.setTimestamp(now.minusDays(daysBack).minusHours(hoursBack));
            notification.setWorkspaceId("ws1");
            notification.setRead(random.nextDouble() < 0.5);
            
            user1Notifications.add(notification);
        }
        user1Notifications.sort((n1, n2) -> n2.getTimestamp().compareTo(n1.getTimestamp()));
        userNotifications.put("user1", user1Notifications);
        
        List<Notification> user2Notifications = new ArrayList<>();
        for (int i = 0; i < 6 + random.nextInt(5); i++) {
            Notification notification = new Notification();
            notification.setId("notif-user2-" + (i + 1));
            notification.setType(notificationTypes[random.nextInt(notificationTypes.length)]);
            notification.setContent(generateNotificationContent(notification.getType(), i));
            
            int daysBack = random.nextInt(30);
            int hoursBack = random.nextInt(24);
            notification.setTimestamp(now.minusDays(daysBack).minusHours(hoursBack));
            notification.setWorkspaceId("ws1");
            notification.setRead(random.nextDouble() < 0.6);
            
            user2Notifications.add(notification);
        }
        user2Notifications.sort((n1, n2) -> n2.getTimestamp().compareTo(n1.getTimestamp()));
        userNotifications.put("user2", user2Notifications);
    }
    
    private String generateNotificationContent(String type, int index) {
        switch (type) {
            case "EXPENSE_ADDED":
                String[] expenseMessages = {
                    "Alice Johnson added a new expense: $45.50 for Groceries",
                    "Bob Smith added a new expense: $28.90 for Restaurants",
                    "A new expense of $67.80 was added to Shared Apartment",
                    "Expense added: $95.40 for Transport",
                    "New expense: $52.10 for Utilities",
                    "Alice Johnson added $35.75 for Groceries",
                    "Bob Smith added an expense: $120.00 for Rent"
                };
                return expenseMessages[index % expenseMessages.length];
                
            case "EXPENSE_UPDATED":
                String[] updateMessages = {
                    "Alice Johnson updated an expense in Shared Apartment",
                    "An expense was updated: $45.50 → $52.30",
                    "Expense updated in Shared Apartment workspace",
                    "Bob Smith modified an expense"
                };
                return updateMessages[index % updateMessages.length];
                
            case "EXPENSE_SETTLED":
                String[] settledMessages = {
                    "Expense settlement completed for Shared Apartment",
                    "All expenses have been settled in Shared Apartment",
                    "Settlement processed: $245.50 total",
                    "Expenses marked as settled"
                };
                return settledMessages[index % settledMessages.length];
                
            case "PAYMENT_RECEIVED":
                String[] receivedMessages = {
                    "You received $50.00 from Alice Johnson",
                    "Payment received: $85.25 from Bob Smith",
                    "You received a payment of $120.00",
                    "Payment of $67.80 received from Alice Johnson"
                };
                return receivedMessages[index % receivedMessages.length];
                
            case "PAYMENT_SENT":
                String[] sentMessages = {
                    "Payment of $50.00 sent to Alice Johnson",
                    "You sent $85.25 to Bob Smith",
                    "Payment sent: $120.00",
                    "Payment of $67.80 completed"
                };
                return sentMessages[index % sentMessages.length];
                
            case "SETTLEMENT_CREATED":
                String[] settlementMessages = {
                    "A new settlement was created in Shared Apartment",
                    "Settlement created: $245.50 to be distributed",
                    "New settlement available in Shared Apartment",
                    "Settlement created by Alice Johnson"
                };
                return settlementMessages[index % settlementMessages.length];
                
            case "MEMBER_ADDED":
                String[] memberAddedMessages = {
                    "Alice Johnson joined Shared Apartment",
                    "New member added to Shared Apartment workspace",
                    "Bob Smith joined your workspace",
                    "A new member was added to Shared Apartment"
                };
                return memberAddedMessages[index % memberAddedMessages.length];
                
            case "MEMBER_REMOVED":
                return "A member was removed from Shared Apartment";
                
            case "WORKSPACE_UPDATED":
                String[] workspaceMessages = {
                    "Shared Apartment workspace settings were updated",
                    "Workspace settings changed",
                    "Shared Apartment was updated",
                    "Workspace configuration updated"
                };
                return workspaceMessages[index % workspaceMessages.length];
                
            case "INVITE_ACCEPTED":
                String[] inviteMessages = {
                    "Alice Johnson accepted your workspace invitation",
                    "Invitation accepted: Bob Smith joined Shared Apartment",
                    "Your invite was accepted",
                    "New member joined via invitation"
                };
                return inviteMessages[index % inviteMessages.length];
                
            case "BALANCE_REMINDER":
                String[] balanceMessages = {
                    "Reminder: You owe $45.50 in Shared Apartment",
                    "Balance reminder: $120.00 outstanding",
                    "You have an outstanding balance of $67.80",
                    "Balance update: $95.40 owed"
                };
                return balanceMessages[index % balanceMessages.length];
                
            case "BUDGET_WARNING":
                String[] budgetMessages = {
                    "Budget warning: Shared Apartment is at 85% of monthly limit",
                    "You're approaching your budget limit in Shared Apartment",
                    "Budget alert: 90% of monthly limit reached",
                    "Budget warning: $1,800 of $2,000 spent"
                };
                return budgetMessages[index % budgetMessages.length];
                
            default:
                return "New notification";
        }
    }
}
