INSERT INTO users (name, email, password) VALUES
('Иван Иванов', 'ivan@example.com', 'password123'),
('Петр Петров', 'petr@example.com', 'password456'),
('Владелец', 'owner@example.com', ''),
('Админ', 'admin@example.com', 'admin123');

INSERT INTO roles (user_id, role, balance) VALUES
(1, 'USER', 1000.00),
(2, 'USER', 500.00),
(3, 'ADMIN', 110000),
(4, 'ADMIN', 1);