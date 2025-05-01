package com.example.shop.controller;

import com.example.shop.model.Order;
import com.example.shop.model.Product;
import com.example.shop.model.User;
import com.example.shop.repository.OrderRepository;
import com.example.shop.service.OrderService;
import com.example.shop.service.UserService;
import com.example.shop.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final UserService userService;
    private final OrderService orderService;
    private final ProductService productService;
    private final OrderRepository orderRepository;


    @Autowired
    public AdminController(UserService userService, OrderService orderService, ProductService productService,
                           OrderRepository orderRepository) {
        this.userService = userService;
        this.orderService = orderService;
        this.productService = productService;
        this.orderRepository = orderRepository;
    }

    @GetMapping
    public String adminPanel() {
        return "admin_index";
    }

    @GetMapping("/users")
    public String manageUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin_users";
    }

    @GetMapping("/orders")
    public String manageOrders(Model model) {
        List<Order> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);

        model.addAttribute("totalOrders", orderService.getTotalOrderCount());
        model.addAttribute("newOrdersToday", orderService.getNewOrdersCount(LocalDate.now(), LocalDate.now()));
        model.addAttribute("completedOrdersCount", orderService.getTotalCompletedOrderCount());
        model.addAttribute("completedOrdersPercentage", String.format("%.2f", orderService.getCompletedOrderPercentage()));
        model.addAttribute("orderStatusCounts", orderService.getOrderStatusCounts());
        model.addAttribute("popularProducts", orderRepository.findMostPopularProductsWithNames(5));
        model.addAttribute("activeCustomers", orderRepository.findMostActiveCustomersWithNames(5));


        return "admin_orders";
    }

    @GetMapping("/products")
    public String manageProducts(Model model) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "product-list";
    }

    @PostMapping("/products/update-quantity")
    public String updateProductQuantity(@RequestParam("productId") Long productId,
                                        @RequestParam("quantity") int quantity) {
        logger.info("Updating quantity for product ID {}. New quantity: {}", productId, quantity);
        Optional<Product> productOptional = productService.getProductById(productId);
        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            product.setQuantity(quantity);
            productService.save(product);
            logger.info("Product quantity updated successfully.");
        } else {
            logger.warn("Product with ID {} not found for quantity update.", productId);
        }
        return "redirect:/admin/products";
    }

    @Transactional
    @PostMapping("/users/edit_balance")
    public String editBalanceSubmit(@RequestParam("id") Long userId, @RequestParam("balance") double balance) {
        logger.info("Entering editBalanceSubmit with user ID: {} and new balance: {}", userId, balance);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentAdminEmail = authentication.getName();
        Optional<User> currentAdminOptional = userService.findByEmail(currentAdminEmail);

        if (currentAdminOptional.isPresent()) {
            User currentAdmin = currentAdminOptional.get();

            if (userId.equals(currentAdmin.getId()) &&
                    currentAdmin.getRoles().stream().anyMatch(role -> "ADMIN".equals(role.getRole())))
            {
                logger.warn("Admin with ID {} attempted to change their own balance.", currentAdmin.getId());
                return "redirect:/admin/users?error=cannot_edit_own_balance";
            }

            Optional<User> userToEditOptional = userService.getUserById(userId);
            if (userToEditOptional.isPresent()) {
                try {
                    userService.updateUserBalanceById(userId, balance);
                    logger.info("Balance updated successfully for user ID: {}", userId);
                } catch (UserService.UserNotFoundException e) {
                    logger.warn("Balance not updated: User with ID {} not found", userId);
                    return "redirect:/admin/users?error=user_not_found&userId=" + userId;
                } catch (ResponseStatusException e) {
                    logger.warn("Balance not updated: User with ID {} has no roles. Error: {}", userId, e.getMessage());
                    return "redirect:/admin/users?error=user_has_no_roles&userId=" + userId;
                }
            } else {
                logger.warn("User with ID {} not found for balance update.", userId);
                return "redirect:/admin/users?error=user_not_found&userId=" + userId;
            }

        } else {
            logger.error("Current admin user not found.");
            return "redirect:/admin/users?error=admin_not_found";
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/orders/{id}/edit-status")
    @Transactional
    public String updateOrderStatus(@PathVariable Long id, @RequestParam("status") String status) {
        logger.info("Updating status for order ID {}. New status: {}", id, status);
        try {
            orderService.updateOrderStatus(id, status);
            logger.info("Order status updated successfully.");
        } catch (Exception e) {
            logger.error("Failed to update order status for ID {}: {}", id, e.getMessage());
        }
        return "redirect:/admin/orders";
    }

    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        logger.info("Deleting product with ID: {}", id);
        productService.deleteProduct(id);
        logger.info("Product with ID {} deleted successfully.", id);
        return "redirect:/admin/products";
    }
}