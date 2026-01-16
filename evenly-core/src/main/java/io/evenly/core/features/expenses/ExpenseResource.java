package io.evenly.core.features.expenses;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.evenly.core.domain.repository.ExpenseRepository;
import io.evenly.core.domain.repository.WorkspaceMemberRepository;
import io.evenly.core.features.expenses.dto.CreateExpenseRequest;
import io.evenly.core.features.expenses.dto.Expense;
import io.evenly.core.features.expenses.dto.UpdateExpenseRequest;
import io.evenly.core.shared.common.PaginatedExpenses;
import io.evenly.core.shared.exception.NotFoundException;
import io.evenly.core.shared.security.Authenticated;
import io.evenly.core.shared.security.SecurityContextProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import java.util.UUID;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Expense management endpoints.
 */
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
@Authenticated
public class ExpenseResource {

    @Inject
    private ExpenseService expenseService;

    @Inject
    private SecurityContextProvider securityContext;

    @Inject
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Inject
    private ExpenseRepository expenseRepository;

    @GET
    @Path("/workspaces/{workspaceId}/expenses")
    public Response listExpenses(@PathParam("workspaceId") String workspaceId,
            @QueryParam("startDate") LocalDate startDate,
            @QueryParam("endDate") LocalDate endDate,
            @QueryParam("categoryId") String categoryId,
            @QueryParam("status") String status,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sort") String sort) {
        String userId = securityContext.getUserId()
                .orElseThrow(() -> new SecurityException("User not authenticated"));
        
        // Check workspace membership
        UUID workspaceUuid = UUID.fromString(workspaceId);
        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceUuid, userId)) {
            throw new SecurityException("Access denied: User is not a member of this workspace");
        }
        
        PaginatedExpenses paginated = expenseService.findForWorkspace(workspaceId, startDate, endDate,
                categoryId, status, page, size, sort);
        return Response.ok(paginated).build();
    }

    @POST
    @Path("/workspaces/{workspaceId}/expenses")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createExpense(@PathParam("workspaceId") String workspaceId,
            @Valid CreateExpenseRequest request) {
        String userId = securityContext.getUserId()
                .orElseThrow(() -> new SecurityException("User not authenticated"));

        // Check workspace membership
        UUID workspaceUuid = UUID.fromString(workspaceId);
        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceUuid, userId)) {
            throw new SecurityException("Access denied: User is not a member of this workspace");
        }

        Expense expense = expenseService.create(workspaceId, userId, request);
        Map<String, Object> response = new HashMap<>();
        response.put("data", expense);
        return Response.ok(response).build();
    }

    @GET
    @Path("/workspaces/{workspaceId}/expenses/{expenseId}")
    public Response getExpense(@PathParam("workspaceId") String workspaceId,
            @PathParam("expenseId") String expenseId) {
        String userId = securityContext.getUserId()
                .orElseThrow(() -> new SecurityException("User not authenticated"));
        
        // Check workspace membership
        UUID workspaceUuid = UUID.fromString(workspaceId);
        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceUuid, userId)) {
            throw new SecurityException("Access denied: User is not a member of this workspace");
        }
        
        // Verify expense exists and belongs to the workspace
        UUID expenseUuid = UUID.fromString(expenseId);
        io.evenly.core.domain.Expense domainExpense = expenseRepository.findById(expenseUuid)
                .orElseThrow(() -> new NotFoundException("Expense not found"));
        
        if (!domainExpense.getWorkspaceId().equals(workspaceUuid)) {
            throw new SecurityException("Access denied: Expense does not belong to this workspace");
        }
        
        Optional<Expense> expense = expenseService.findById(expenseId);
        Map<String, Object> response = new HashMap<>();
        response.put("data", expense.get());
        return Response.ok(response).build();
    }

    @PUT
    @Path("/workspaces/{workspaceId}/expenses/{expenseId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateExpense(@PathParam("workspaceId") String workspaceId,
            @PathParam("expenseId") String expenseId,
            @Valid UpdateExpenseRequest request) {
        String userId = securityContext.getUserId()
                .orElseThrow(() -> new SecurityException("User not authenticated"));
        
        // Check workspace membership
        UUID workspaceUuid = UUID.fromString(workspaceId);
        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceUuid, userId)) {
            throw new SecurityException("Access denied: User is not a member of this workspace");
        }
        
        // Verify expense exists and belongs to the workspace
        UUID expenseUuid = UUID.fromString(expenseId);
        io.evenly.core.domain.Expense domainExpense = expenseRepository.findById(expenseUuid)
                .orElseThrow(() -> new NotFoundException("Expense not found"));
        
        if (!domainExpense.getWorkspaceId().equals(workspaceUuid)) {
            throw new SecurityException("Access denied: Expense does not belong to this workspace");
        }
        
        Expense expense = expenseService.update(expenseId, userId, request);
        Map<String, Object> response = new HashMap<>();
        response.put("data", expense);
        return Response.ok(response).build();
    }

    @DELETE
    @Path("/workspaces/{workspaceId}/expenses/{expenseId}")
    public Response deleteExpense(@PathParam("workspaceId") String workspaceId,
            @PathParam("expenseId") String expenseId) {
        String userId = securityContext.getUserId()
                .orElseThrow(() -> new SecurityException("User not authenticated"));
        
        // Check workspace membership
        UUID workspaceUuid = UUID.fromString(workspaceId);
        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceUuid, userId)) {
            throw new SecurityException("Access denied: User is not a member of this workspace");
        }
        
        // Verify expense exists and belongs to the workspace
        UUID expenseUuid = UUID.fromString(expenseId);
        io.evenly.core.domain.Expense domainExpense = expenseRepository.findById(expenseUuid)
                .orElseThrow(() -> new NotFoundException("Expense not found"));
        
        if (!domainExpense.getWorkspaceId().equals(workspaceUuid)) {
            throw new SecurityException("Access denied: Expense does not belong to this workspace");
        }
        
        expenseService.delete(expenseId, userId);
        return Response.noContent().build();
    }
}
