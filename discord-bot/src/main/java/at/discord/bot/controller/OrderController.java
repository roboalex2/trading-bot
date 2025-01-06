package at.discord.bot.controller;

import at.discord.bot.api.OrderControllerApi;
import at.discord.bot.api.model.ProductPage;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class OrderController implements OrderControllerApi {


    /**
     * GET /example
     * Get all products with pagination
     *
     * @param page   Page number (optional)
     * @param amount Amount of products per page (optional)
     * @return List of products (status code 200)
     * or Bad request (status code 400)
     * or Internal error (status code 500)
     */
    @Override
    public ResponseEntity<ProductPage> exampleGet(Integer page, Integer amount) {
        return null;
    }
}
