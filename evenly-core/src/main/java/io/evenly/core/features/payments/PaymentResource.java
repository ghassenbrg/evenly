package io.evenly.core.features.payments;

import io.evenly.core.shared.common.PaginatedPayments;
import io.evenly.core.shared.exception.NotFoundException;
import io.evenly.core.shared.security.Authenticated;
import io.evenly.core.shared.security.SecurityContextProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import io.evenly.core.features.payments.dto.Payment;
import io.evenly.core.features.payments.dto.CreatePaymentRequest;
import io.evenly.core.features.payments.dto.UpdatePaymentRequest;

/**
 * Payment management endpoints.
 */
@Path("/api/workspaces/{workspaceId}")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
@Authenticated
public class PaymentResource {

    @Inject
    private PaymentService paymentService;

    @Inject
    private SecurityContextProvider securityContext;

    @GET
    @Path("/payments")
    public Response listPayments(@PathParam("workspaceId") String workspaceId,
                                  @QueryParam("startDate") LocalDate startDate,
                                  @QueryParam("endDate") LocalDate endDate,
                                  @QueryParam("status") String status,
                                  @QueryParam("page") @DefaultValue("0") int page,
                                  @QueryParam("size") @DefaultValue("20") int size,
                                  @QueryParam("sort") String sort) {
        PaginatedPayments paginated = paymentService.findForWorkspace(workspaceId, startDate, endDate, 
                                                                      status, page, size, sort);
        return Response.ok(paginated).build();
    }

    @POST
    @Path("/pay")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createPayment(@PathParam("workspaceId") String workspaceId,
                                  @Valid CreatePaymentRequest request) {
        String userId = securityContext.getUserId()
            .orElseThrow(() -> new SecurityException("User not authenticated"));
        
        Payment payment = paymentService.create(workspaceId, userId, request);
        Map<String, Object> response = new HashMap<>();
        response.put("data", payment);
        return Response.ok(response).build();
    }

    @GET
    @Path("/payments/{paymentId}")
    public Response getPayment(@PathParam("workspaceId") String workspaceId,
                               @PathParam("paymentId") String paymentId) {
        Optional<Payment> payment = paymentService.findById(paymentId);
        if (payment.isEmpty()) {
            throw new NotFoundException("Payment not found");
        }
        Map<String, Object> response = new HashMap<>();
        response.put("data", payment.get());
        return Response.ok(response).build();
    }

    @PUT
    @Path("/payments/{paymentId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updatePayment(@PathParam("workspaceId") String workspaceId,
                                  @PathParam("paymentId") String paymentId,
                                  @Valid UpdatePaymentRequest request) {
        Optional<Payment> existing = paymentService.findById(paymentId);
        if (existing.isEmpty()) {
            throw new NotFoundException("Payment not found");
        }
        Payment payment = paymentService.update(paymentId, request);
        Map<String, Object> response = new HashMap<>();
        response.put("data", payment);
        return Response.ok(response).build();
    }

    @DELETE
    @Path("/payments/{paymentId}")
    public Response deletePayment(@PathParam("workspaceId") String workspaceId,
                                  @PathParam("paymentId") String paymentId) {
        Optional<Payment> existing = paymentService.findById(paymentId);
        if (existing.isEmpty()) {
            throw new NotFoundException("Payment not found");
        }
        paymentService.delete(paymentId);
        return Response.noContent().build();
    }
}
