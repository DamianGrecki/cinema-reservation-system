package pl.dgrecki.controllers;

import static pl.dgrecki.constants.Endpoints.BASKET_ENDPOINT;
import static pl.dgrecki.constants.Endpoints.BASKET_PRICING_ENDPOINT;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.dgrecki.models.responses.BasketResponse;
import pl.dgrecki.models.responses.basket.BasketPricingResponse;
import pl.dgrecki.services.BasketService;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class BasketController {

    private final BasketService basketService;

    @PostMapping(BASKET_ENDPOINT)
    public ResponseEntity<BasketResponse> addBasket() {
        return ResponseEntity.status(HttpStatus.CREATED).body(basketService.addBasket());
    }

    @GetMapping(BASKET_PRICING_ENDPOINT)
    public ResponseEntity<BasketPricingResponse> getBasketPricing(@PathVariable UUID basketId) {
        return ResponseEntity.ok(basketService.getBasketPricing(basketId));
    }
}
