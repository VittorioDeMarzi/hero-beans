-- HeroBeans Sample Data
-- Members: 1 Admin + 1 User
INSERT INTO member (name, email, password, role, created_at, last_updated_at) VALUES
      ('Admin User', 'admin@herobeans.com', '$2a$10$y3rEiacoc/0F1Qh0mVweo.rYAAuyCbOGhuPI/fk3XnC20irt21.nm', 'ADMIN', NOW(), NOW()),
      ('John Coffee', 'john@example.com', '$2a$10$y3rEiacoc/0F1Qh0mVweo.rYAAuyCbOGhuPI/fk3XnC20irt21.nm', 'USER', NOW(), NOW());

-- Coffee Products with diverse profiles and characteristics
INSERT INTO coffee (name, body, sweetness, acidity, taste, brew_recommendation, origin, processing_method, roast_level, description, image_url, created_at, updated_at) VALUES
    -- Ethiopian Premium Selection
    ('Ethiopian Yirgacheffe', 'MEDIUM', 'HIGH', 'HIGHEST', 'Floral, citrus, and bright tea-like finish', 'FILTER', 'ETHIOPIA', 'WASHED_PROCESS', 'LIGHT_ROAST', 'A stunning single-origin coffee with complex floral notes and bright acidity. Perfect for pour-over brewing methods.', 'https://example.com/images/ethiopian-yirgacheffe.jpg', NOW(), NOW()),

    ('Ethiopian Sidamo Natural', 'HIGH', 'HIGHEST', 'HIGH', 'Blueberry, wine-like, fruity explosion', 'FILTER', 'ETHIOPIA', 'NATURAL_PROCESS', 'MEDIUM_ROAST', 'Natural processed coffee with intense fruit flavors and wine-like complexity. A true Ethiopian treasure.', 'https://example.com/images/ethiopian-sidamo.jpg', NOW(), NOW()),

    -- Colombian Varieties
    ('Colombian Huila Supremo', 'HIGH', 'MEDIUM', 'MEDIUM', 'Chocolate, caramel, nutty undertones', 'ESPRESSO', 'COLOMBIA', 'WASHED_PROCESS', 'MEDIUM_ROAST', 'Classic Colombian profile with perfect balance of sweetness and body. Ideal for espresso preparations.', 'https://example.com/images/colombian-huila.jpg', NOW(), NOW()),

    ('Colombian Geisha Pink Bourbon', 'MEDIUM', 'HIGHEST', 'HIGH', 'Jasmine, tropical fruits, silky texture', 'FILTER', 'COLOMBIA', 'HONEY_PROCESS', 'LIGHT_ROAST', 'Rare Pink Bourbon variety with exceptional cup quality and unique flavor profile. Limited production.', 'https://example.com/images/colombian-geisha.jpg', NOW(), NOW()),

    -- Brazilian Excellence
    ('Brazilian Cerrado Pulped Natural', 'HIGHEST', 'HIGH', 'LOW', 'Chocolate, nuts, creamy body', 'ESPRESSO', 'BRAZIL', 'SEMI_WASHED_PROCESS', 'MEDIUM_DARK_ROAST', 'Full-bodied Brazilian coffee with rich chocolate notes. Perfect base for espresso blends.', 'https://example.com/images/brazilian-cerrado.jpg', NOW(), NOW()),

    ('Brazilian Yellow Bourbon', 'HIGH', 'HIGHEST', 'MEDIUM', 'Honey, vanilla, orange zest', 'FILTER', 'BRAZIL', 'NATURAL_PROCESS', 'MEDIUM_ROAST', 'Sweet and complex Yellow Bourbon variety with natural processing that enhances its inherent sweetness.', 'https://example.com/images/brazilian-bourbon.jpg', NOW(), NOW()),

    -- Indonesian Specialties
    ('Indonesian Sumatra Mandheling', 'HIGHEST', 'LOW', 'LOWEST', 'Earthy, herbal, full-bodied intensity', 'FRENCH_PRESS', 'INDONESIA', 'SEMI_WASHED_PROCESS', 'DARK_ROAST', 'Bold and earthy Sumatran coffee with unique processing that creates its distinctive flavor profile.', 'https://example.com/images/sumatra-mandheling.jpg', NOW(), NOW()),

    ('Indonesian Java Estate', 'HIGHEST', 'MEDIUM', 'LOW', 'Dark chocolate, tobacco, rustic charm', 'MOKA', 'INDONESIA', 'WASHED_PROCESS', 'MEDIUM_DARK_ROAST', 'Historic Java estate coffee with traditional flavor profile and substantial body.', 'https://example.com/images/java-estate.jpg', NOW(), NOW()),

    -- Vietnamese Innovation
    ('Vietnamese Robusta Premium', 'HIGHEST', 'LOW', 'MEDIUM', 'Bold, chocolatey, strong caffeine kick', 'ESPRESSO', 'VIETNAM', 'NATURAL_PROCESS', 'DARK_ROAST', 'High-quality Robusta with exceptional cup quality and intense flavor. Perfect for espresso lovers.', 'https://example.com/images/vietnamese-robusta.jpg', NOW(), NOW()),

    ('Vietnamese Arabica Cau Dat', 'MEDIUM', 'HIGH', 'HIGH', 'Floral, sweet, clean finish', 'FILTER', 'VIETNAM', 'WASHED_PROCESS', 'MEDIUM_ROAST', 'Rare Vietnamese Arabica from high altitude regions with surprising complexity and sweetness.', 'https://example.com/images/vietnamese-arabica.jpg', NOW(), NOW()),

    -- Honduran Gems
    ('Honduran Marcala SHG', 'HIGH', 'MEDIUM', 'MEDIUM', 'Caramel, apple, balanced acidity', 'FILTER', 'HONDURAS', 'WASHED_PROCESS', 'MEDIUM_ROAST', 'Strictly High Grown coffee from Marcala region with excellent balance and clean cup profile.', 'https://example.com/images/honduran-marcala.jpg', NOW(), NOW()),

    ('Honduran Pacas Anaerobic', 'MEDIUM', 'HIGHEST', 'HIGH', 'Tropical fruits, fermented complexity', 'FILTER', 'HONDURAS', 'ANAEROBIC_FERMENTATION', 'LIGHT_ROAST', 'Innovative anaerobic fermentation process creates unique flavor compounds and exceptional cup quality.', 'https://example.com/images/honduran-pacas.jpg', NOW(), NOW()),

    -- Indian Monsoon
    ('Indian Monsooned Malabar', 'HIGHEST', 'LOW', 'LOWEST', 'Spicy, woody, unique processing character', 'FRENCH_PRESS', 'INDIA', 'NATURAL_PROCESS', 'MEDIUM_DARK_ROAST', 'Traditional monsooning process creates distinctive flavor profile unlike any other coffee in the world.', 'https://example.com/images/indian-monsoon.jpg', NOW(), NOW()),

    ('Indian Plantation AA', 'HIGH', 'MEDIUM', 'MEDIUM', 'Spices, chocolate, medium body', 'ESPRESSO', 'INDIA', 'WASHED_PROCESS', 'MEDIUM_ROAST', 'High-grade plantation coffee from Indian estates with balanced profile and subtle spice notes.', 'https://example.com/images/indian-plantation.jpg', NOW(), NOW()),

    -- Experimental Processing
    ('Colombian Carbonic Maceration', 'MEDIUM', 'HIGHEST', 'HIGHEST', 'Wine-like, complex fermentation notes', 'FILTER', 'COLOMBIA', 'CARBONIC_MACERATION', 'LIGHT_ROAST', 'Innovative carbonic maceration process borrowed from winemaking creates unprecedented flavor complexity.', 'https://example.com/images/colombian-carbonic.jpg', NOW(), NOW()),

    ('Brazilian Double Fermentation', 'HIGH', 'HIGH', 'MEDIUM', 'Fruity, clean, enhanced sweetness', 'FILTER', 'BRAZIL', 'DOUBLE_FERMENTATION', 'MEDIUM_ROAST', 'Double fermentation process amplifies natural fruit flavors while maintaining clean cup characteristics.', 'https://example.com/images/brazilian-double.jpg', NOW(), NOW()),

    -- Specialty Blends
    ('Heritage Dark Roast Blend', 'HIGHEST', 'MEDIUM', 'LOW', 'Bold, smoky, traditional coffeehouse', 'FRENCH_PRESS', 'BRAZIL', 'NATURAL_PROCESS', 'DARK_ROAST', 'Traditional dark roast blend perfect for those who prefer bold, full-bodied coffee with minimal acidity.', 'https://example.com/images/heritage-blend.jpg', NOW(), NOW()),

    ('Morning Glory Light Roast', 'LOW', 'HIGH', 'HIGHEST', 'Bright, clean, morning perfection', 'FILTER', 'ETHIOPIA', 'WASHED_PROCESS', 'LIGHT_ROAST', 'Perfect morning coffee with bright acidity and clean finish. Designed to start your day with energy.', 'https://example.com/images/morning-glory.jpg', NOW(), NOW()),

    -- Premium Single Origins
    ('Indonesian Kopi Luwak Alternative', 'HIGH', 'MEDIUM', 'LOW', 'Smooth, refined, ethical luxury', 'ESPRESSO', 'INDONESIA', 'WASHED_PROCESS', 'MEDIUM_ROAST', 'Ethically produced alternative to traditional kopi luwak with similar smooth, refined characteristics.', 'https://example.com/images/ethical-luxury.jpg', NOW(), NOW()),

    ('Honduran Honey Process Exclusive', 'MEDIUM', 'HIGHEST', 'HIGH', 'Honey, tropical, exclusive sweetness', 'FILTER', 'HONDURAS', 'HONEY_PROCESS', 'MEDIUM_ROAST', 'Exclusive honey processed coffee with exceptional sweetness and complex tropical fruit notes.', 'https://example.com/images/honey-exclusive.jpg', NOW(), NOW());

-- Package Options for each coffee (varied weights and prices)
-- Note: Using ordinal values from Grams enum (0=G250, 1=G500, 2=G1000)
INSERT INTO package_option (coffee_id, weight, quantity, price) VALUES
-- Ethiopian Yirgacheffe (Premium pricing)
(1, 0, 45, 18.90),
(1, 1, 32, 35.80),
(1, 2, 18, 68.50),

-- Ethiopian Sidamo Natural
(2, 0, 38, 16.50),
(2, 1, 25, 31.00),
(2, 2, 12, 58.90),

-- Colombian Huila Supremo
(3, 0, 55, 15.90),
(3, 1, 42, 29.80),
(3, 2, 28, 56.00),

-- Colombian Geisha Pink Bourbon (Premium)
(4, 0, 15, 35.00),
(4, 1, 8, 68.00),
(4, 2, 3, 130.00),

-- Brazilian Cerrado
(5, 0, 67, 12.90),
(5, 1, 48, 24.50),
(5, 2, 35, 46.80),

-- Brazilian Yellow Bourbon
(6, 0, 41, 14.70),
(6, 1, 29, 27.90),
(6, 2, 19, 52.00),

-- Indonesian Sumatra Mandheling
(7, 0, 33, 16.20),
(7, 1, 24, 30.50),
(7, 2, 15, 57.90),

-- Indonesian Java Estate
(8, 0, 29, 17.50),
(8, 1, 18, 33.00),
(8, 2, 11, 62.00),

-- Vietnamese Robusta Premium
(9, 0, 52, 11.90),
(9, 1, 38, 22.80),
(9, 2, 26, 43.50),

-- Vietnamese Arabica Cau Dat
(10, 0, 22, 19.90),
(10, 1, 15, 37.50),
(10, 2, 8, 71.00),

-- Honduran Marcala SHG
(11, 0, 44, 13.90),
(11, 1, 31, 26.50),
(11, 2, 22, 50.00),

-- Honduran Pacas Anaerobic (Premium)
(12, 0, 18, 24.90),
(12, 1, 12, 47.50),
(12, 2, 6, 89.00),

-- Indian Monsooned Malabar
(13, 0, 36, 15.50),
(13, 1, 23, 29.00),
(13, 2, 16, 55.50),

-- Indian Plantation AA
(14, 0, 48, 14.20),
(14, 1, 34, 26.90),
(14, 2, 21, 51.00),

-- Colombian Carbonic Maceration (Premium)
(15, 0, 12, 28.90),
(15, 1, 7, 55.00),
(15, 2, 4, 105.00),

-- Brazilian Double Fermentation
(16, 0, 26, 21.50),
(16, 1, 17, 40.90),
(16, 2, 9, 77.50),

-- Heritage Dark Roast Blend
(17, 0, 89, 10.90),
(17, 1, 67, 20.50),
(17, 2, 45, 38.90),

-- Morning Glory Light Roast
(18, 0, 72, 11.90),
(18, 1, 54, 22.50),
(18, 2, 38, 42.00),

-- Indonesian Kopi Luwak Alternative (Luxury)
(19, 0, 8, 45.00),
(19, 1, 4, 87.50),
(19, 2, 2, 165.00),

-- Honduran Honey Process Exclusive (Premium)
(20, 0, 14, 26.90),
(20, 1, 9, 51.50),
(20, 2, 5, 97.00);
