package pl.dgrecki.controllers;

import static pl.dgrecki.constants.Endpoints.BASKET_ENDPOINT;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.dgrecki.models.responses.BasketResponse;
import pl.dgrecki.services.BasketService;

@RestController
@RequestMapping(BASKET_ENDPOINT)
@RequiredArgsConstructor
public class BasketController {

    private final BasketService basketService;

    @PostMapping
    public ResponseEntity<BasketResponse> addBasket() {
        return ResponseEntity.status(HttpStatus.CREATED).body(basketService.addBasket());
    }
}
