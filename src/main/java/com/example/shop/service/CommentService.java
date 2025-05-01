package com.example.shop.service;

import com.example.shop.model.Comment;
import com.example.shop.model.Product;
import com.example.shop.repository.CommentRepository;
import com.example.shop.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    private static final Logger logger = LoggerFactory.getLogger(CommentService.class); // Добавляем Logger


    private final CommentRepository commentRepository;
    private final ProductRepository productRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository, ProductRepository productRepository) {
        this.commentRepository = commentRepository;
        this.productRepository = productRepository;
    }

    public Comment saveComment(Long productId, String author, String text, String externalResourceUrl) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isPresent()) {
            Product product = productOptional.get();

            if (externalResourceUrl != null && !externalResourceUrl.isEmpty()) {
                String fetchedContent = fetchContentFromUrl(externalResourceUrl);
                logger.info("SSRF - Fetched content from Comment URL: {}", fetchedContent);
            }


            Comment comment = new Comment(text, author, externalResourceUrl, product);
            return commentRepository.save(comment);
        }
        return null;
    }

    public String fetchContentFromUrl(String urlString) {
        StringBuilder content = new StringBuilder();
        try {
            URL url = new URL(urlString);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\\n");
                }
            }
        } catch (Exception e) {
            logger.error("SSRF - Ошибка при загрузке контента из комментария: {}", e.getMessage()); // Логируем ошибку
            content.append("Ошибка при загрузке контента: ").append(e.getMessage());
        }
        return content.toString();
    }

    public List<Comment> getCommentsByProductId(Long productId) {
        return commentRepository.findByProductId(productId);
    }

    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }
}