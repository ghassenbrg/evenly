package io.evenly.core.features.currencies;

import io.evenly.core.features.currencies.dto.Currency;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

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
        return Response.ok(currencies).build();
    }
}
