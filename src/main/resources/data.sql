-- Seed products only if table is empty
-- This runs every startup because spring.sql.init.mode=always
-- The WHERE NOT EXISTS guard prevents duplicate inserts

INSERT INTO products (name, description, price, image_url)
SELECT 'Mechanical Keyboard',
       'TKL layout, red switches, RGB backlit. Ideal for developers.',
       2999.00,
       'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=400'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Mechanical Keyboard');

INSERT INTO products (name, description, price, image_url)
SELECT 'USB-C Hub 7-in-1',
       'HDMI 4K, 3x USB 3.0, SD card, PD charging. Compact aluminium build.',
       1499.00,
       'https://images.unsplash.com/photo-1625842268584-8f3296236761?w=400'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'USB-C Hub 7-in-1');

INSERT INTO products (name, description, price, image_url)
SELECT 'Laptop Stand',
       'Adjustable aluminium stand. Improves posture, reduces neck strain.',
       899.00,
       'https://images.unsplash.com/photo-1593642632559-0c6d3fc62b89?w=400'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Laptop Stand');

INSERT INTO products (name, description, price, image_url)
SELECT 'Wireless Mouse',
       'Ergonomic design, 2.4GHz, 1-year battery life. Silent click.',
       799.00,
       'https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=400'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Wireless Mouse');

INSERT INTO products (name, description, price, image_url)
SELECT 'LED Desk Lamp',
       'Touch dimmer, 3 colour modes, USB charging port on base.',
       649.00,
       'https://images.unsplash.com/photo-1573297617698-b48e7fbd3e84?w=400'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'LED Desk Lamp');