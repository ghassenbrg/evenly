package io.evenly.core.features.currencies;

import io.evenly.core.features.currencies.dto.Currency;

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
 * Currency endpoints (no authentication required).
 */
@Path("/api/currencies")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class CurrencyResource {

    @Inject
    private CurrencyService currencyService;

    @GET
    public Response listCurrencies() {
        List<Currency> currencies = currencyService.findAll();
        Map<String, Object> response = new HashMap<>();
        response.put("data", currencies);
        return Response.ok(response).build();
    }
}
