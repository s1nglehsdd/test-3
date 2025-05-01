package com.example.shop.controller;

import com.example.shop.model.Product;
import com.example.shop.model.Comment;
import com.example.shop.service.ProductService;
import com.example.shop.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.List;
import java.util.Optional;

@Controller
public class ProductController {

    private final ProductService productService;
    private final EntityManager entityManager;
    private final CommentService commentService;

    @Autowired
    public ProductController(ProductService productService, EntityManager entityManager, CommentService commentService) {
        this.productService = productService;
        this.entityManager = entityManager;
        this.commentService = commentService;
    }

    @GetMapping("/product/{id}")
    public String productDetails(@PathVariable Long id, Model model) {
        Optional<Product> productOptional = productService.getProductById(id);
        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            model.addAttribute("product", product);

            List<Comment> comments = commentService.getCommentsByProductId(id);
            model.addAttribute("comments", comments);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String && authentication.getPrincipal().equals("anonymousUser"))) {
                model.addAttribute("authenticatedUsername", authentication.getName());
            } else {
                model.addAttribute("authenticatedUsername", null);
            }


            return "product-details";
        } else {
            model.addAttribute("errorMessage", "Товар с ID " + id + " не найден.");
            return "error";
        }
    }


    @GetMapping("/catalog/unsafe")
    public String catalogUnsafe(@RequestParam(value = "query", required = false) String query, Model model) {
        try {
            String sql = "SELECT name, price FROM products WHERE name LIKE '%" + query + "%'";
            Query nativeQuery = entityManager.createNativeQuery(sql);
            List<Object[]> products = nativeQuery.getResultList();
            model.addAttribute("productsUnsafe", products);
            model.addAttribute("injectedResult", "Выполнен запрос: " + sql);
            return "catalog-unsafe"; // Убедитесь, что у вас есть шаблон catalog-unsafe.html
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка при выполнении запроса: " + e.getMessage());
            return "catalog-unsafe";
        }
    }

    @GetMapping("/product/details/unsafe")
    public String productDetailsUnsafe(@RequestParam("id") String id, Model model) {
        try {
            String sql = "SELECT p.name, p.description, p.price FROM products p WHERE p.id = " + id +
                    " UNION SELECT null, version(), null--";
            Query query = entityManager.createNativeQuery(sql);
            List<Object[]> results = query.getResultList();

            if (!results.isEmpty()) {
                Object[] productData = (Object[]) results.get(0);
                model.addAttribute("name", productData[0]);
                model.addAttribute("description", productData[1]);
                model.addAttribute("price", productData[2]);
                return "product-info";
            } else {
                model.addAttribute("errorMessage", "Товар не найден.");
                return "error";
            }
        } catch (Exception e) {
            //e.printStackTrace(); // В реальном приложении не следует выводить стектрейс пользователю
            model.addAttribute("errorMessage", "Произошла ошибка при получении информации о товаре: " + e.getMessage());
            return "error";
        }
    }


    @PostMapping("/product/{productId}/comment")
    public String addProductComment(@PathVariable Long productId,
                                    @RequestParam("author") String author,
                                    @RequestParam("text") String text,
                                    @RequestParam(value = "externalResourceUrl", required = false) String externalResourceUrl,
                                    RedirectAttributes redirectAttributes) {

        commentService.saveComment(productId, author, text, externalResourceUrl);

        redirectAttributes.addFlashAttribute("commentAdded", true);
        return "redirect:/product/" + productId;
    }
}