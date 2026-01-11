package io.evenly.core.features.notifications;

import io.evenly.core.features.notifications.dto.Notification;

import io.evenly.core.shared.security.Authenticated;
import io.evenly.core.shared.security.SecurityContextProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Notification endpoints.
 */
@Path("/api/notifications")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
@Authenticated
public class NotificationResource {

    @Inject
    private NotificationService notificationService;

    @Inject
    private SecurityContextProvider securityContext;

    @GET
    public Response listNotifications() {
        String userId = securityContext.getUserId()
            .orElseThrow(() -> new SecurityException("User not authenticated"));
        
        List<Notification> notifications = notificationService.findAllForUser(userId);
        Integer unreadCount = notificationService.getUnreadCount(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", notifications);
        response.put("unreadCount", unreadCount);
        return Response.ok(response).build();
    }

    @GET
    @Path("/unread-count")
    public Response getUnreadCount() {
        String userId = securityContext.getUserId()
            .orElseThrow(() -> new SecurityException("User not authenticated"));
        
        Integer count = notificationService.getUnreadCount(userId);
        Map<String, Object> response = new HashMap<>();
        Map<String, Integer> data = new HashMap<>();
        data.put("count", count);
        response.put("data", data);
        return Response.ok(response).build();
    }

    @POST
    @Path("/{notificationId}/mark-as-read")
    public Response markAsRead(@PathParam("notificationId") String notificationId) {
        notificationService.markAsRead(notificationId);
        return Response.noContent().build();
    }

    @POST
    @Path("/mark-all-as-read")
    public Response markAllAsRead() {
        String userId = securityContext.getUserId()
            .orElseThrow(() -> new SecurityException("User not authenticated"));
        
        notificationService.markAllAsRead(userId);
        return Response.noContent().build();
    }
}
