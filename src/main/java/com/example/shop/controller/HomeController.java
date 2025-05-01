package com.example.shop.controller;

import com.example.shop.model.Product;
import com.example.shop.model.User;
import com.example.shop.service.ProductService;
import com.example.shop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {

    private final ProductService productService;
    private final UserService userService;

    @Autowired
    public HomeController(ProductService productService, UserService userService) {
        this.productService = productService;
        this.userService = userService;
    }

    @GetMapping("/")
    public String catalog(@RequestParam(value = "balanceInjectionId", required = false) Long balanceInjectionId,
                          Model model) {

        List<Product> products;
        products = productService.getAllProducts();

        model.addAttribute("products", products);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            String email = authentication.getName();
            Optional<User> userOptional = userService.findByEmail(email);
            userOptional.ifPresent(user -> {
                model.addAttribute("userId", user.getId());
                if (balanceInjectionId != null) {
                    Double injectedBalance = userService.getInjectedUserBalanceById(balanceInjectionId);
                    model.addAttribute("balance", injectedBalance);
                } else {
                    Double injectedBalance = userService.getInjectedUserBalanceById(user.getId());
                    model.addAttribute("balance", injectedBalance);
                }
            });
        } else {
            model.addAttribute("userId", null);
            model.addAttribute("balance", null);
        }

        return "index";
    }
}