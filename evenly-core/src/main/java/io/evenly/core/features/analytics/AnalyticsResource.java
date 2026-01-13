package io.evenly.core.features.analytics;

import io.evenly.core.domain.repository.WorkspaceMemberRepository;
import io.evenly.core.features.expenses.dto.Expense;
import io.evenly.core.features.expenses.ExpenseService;
import io.evenly.core.shared.security.Authenticated;
import io.evenly.core.shared.security.SecurityContextProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import io.evenly.core.features.analytics.dto.BalanceSummary;
import io.evenly.core.features.analytics.dto.ExpenseSnapshotResponse;
import io.evenly.core.features.analytics.dto.ExpenseSummary;

/**
 * Analytics endpoints.
 */
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
@Authenticated
public class AnalyticsResource {

    @Inject
    private AnalyticsService analyticsService;

    @Inject
    private ExpenseService expenseService;

    @Inject
    private SecurityContextProvider securityContext;

    @Inject
    private WorkspaceMemberRepository workspaceMemberRepository;

    @GET
    @Path("/workspaces/{workspaceId}/analytics/balance-summary")
    public Response getBalanceSummary(@PathParam("workspaceId") String workspaceId,
                                      @QueryParam("startDate") LocalDate startDate,
                                      @QueryParam("endDate") LocalDate endDate) {
        String userId = securityContext.getUserId()
            .orElseThrow(() -> new SecurityException("User not authenticated"));
        
        BalanceSummary summary = analyticsService.getBalanceSummary(workspaceId, userId, startDate, endDate);
        return Response.ok(summary).build();
    }

    @GET
    @Path("/workspaces/{workspaceId}/analytics/expenses-snapshot")
    public Response getExpensesSnapshot(@PathParam("workspaceId") String workspaceId,
                                        @QueryParam("startDate") LocalDate startDate,
                                        @QueryParam("endDate") LocalDate endDate,
                                        @QueryParam("size") @DefaultValue("0") int size) {
        String userId = securityContext.getUserId()
                .orElseThrow(() -> new SecurityException("User not authenticated"));
        
        // Check workspace membership
        UUID workspaceUuid = UUID.fromString(workspaceId);
        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceUuid, userId)) {
            throw new SecurityException("Access denied: User is not a member of this workspace");
        }
        
        ExpenseSnapshotResponse snapshot = analyticsService.getExpensesSnapshot(workspaceId, startDate, endDate, size);
        return Response.ok(snapshot).build();
    }

    @GET
    @Path("/workspaces/{workspaceId}/analytics/recent-expenses")
    public Response getRecentExpenses(@PathParam("workspaceId") String workspaceId,
                                      @QueryParam("size") @DefaultValue("5") int size) {
        String userId = securityContext.getUserId()
                .orElseThrow(() -> new SecurityException("User not authenticated"));
        
        // Check workspace membership
        UUID workspaceUuid = UUID.fromString(workspaceId);
        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceUuid, userId)) {
            throw new SecurityException("Access denied: User is not a member of this workspace");
        }
        
        List<Expense> expenses = expenseService.findRecentForWorkspace(workspaceId, size);
        Map<String, Object> response = new HashMap<>();
        response.put("data", expenses);
        return Response.ok(response).build();
    }

    @GET
    @Path("/workspaces/{workspaceId}/analytics/expenses-summary")
    public Response getExpensesSummary(@PathParam("workspaceId") String workspaceId,
                                        @QueryParam("startDate") LocalDate startDate,
                                        @QueryParam("endDate") LocalDate endDate) {
        String userId = securityContext.getUserId()
                .orElseThrow(() -> new SecurityException("User not authenticated"));
        
        // Check workspace membership
        UUID workspaceUuid = UUID.fromString(workspaceId);
        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceUuid, userId)) {
            throw new SecurityException("Access denied: User is not a member of this workspace");
        }
        
        ExpenseSummary summary = analyticsService.getExpensesSummary(workspaceId, startDate, endDate);
        return Response.ok(summary).build();
    }
}
