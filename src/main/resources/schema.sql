CREATE TABLE users (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(255),
                       email VARCHAR(255) UNIQUE,
                       password VARCHAR(255)
);

CREATE TABLE roles (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       user_id INT,
                       role VARCHAR(255),
                       balance DECIMAL(10, 2),
                       FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    description TEXT,
    price DECIMAL(10, 2),
    quantity INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE orders (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        user_id INT,
                        product_id INT,
                        status VARCHAR(255),
                        order_date TIMESTAMP,
                        total_amount DECIMAL(10, 2),
                        FOREIGN KEY (user_id) REFERENCES users(id),
                        FOREIGN KEY (product_id) REFERENCES products(id)
);

 CREATE TABLE comments (
     id INT AUTO_INCREMENT PRIMARY KEY,
     text TEXT,
     author VARCHAR(255),
     product_id INT,
     FOREIGN KEY (product_id) REFERENCES products(id)
 );

ALTER TABLE comments ADD COLUMN external_resource_url VARCHAR(255);