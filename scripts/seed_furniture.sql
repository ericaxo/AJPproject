SET FOREIGN_KEY_CHECKS=0;
USE tabonfurniture;

-- Ensure categories exist
INSERT IGNORE INTO categories (name, description, icon, is_active, created_at, updated_at) VALUES
('Living Room','Sofas, sectionals, coffee tables, and TV units','fas fa-couch',1,NOW(),NOW()),
('Bedroom','Beds, mattresses, wardrobes, bedside tables','fas fa-bed',1,NOW(),NOW()),
('Dining','Dining tables, chairs, sideboards, bar stools','fas fa-utensils',1,NOW(),NOW()),
('Office','Desks, office chairs, bookcases, storage','fas fa-briefcase',1,NOW(),NOW()),
('Outdoor','Patio sets, loungers, outdoor accessories','fas fa-umbrella-beach',1,NOW(),NOW()),
('Storage','Cabinets, shelves, organizers','fas fa-box',1,NOW(),NOW()),
('Kids','Furniture for kids and nurseries','fas fa-child',1,NOW(),NOW()),
('Decor','Rugs, lamps, mirrors, decor accents','fas fa-lightbulb',1,NOW(),NOW());

-- Insert 20 furniture products
INSERT INTO products (name, description, price, category, dimensions, color, material, image_url, stock_quantity, is_active, created_at) VALUES
('Modern Fabric Sofa','3-seater sofa with durable fabric upholstery',699.00,'Living Room','3-Seater','Gray','Fabric & Wood','https://images.unsplash.com/photo-1501045661006-fcebe0257c3f?w=400&h=400&fit=crop',40,1,NOW()),
('Solid Wood Coffee Table','Minimalist oak wood coffee table',249.00,'Living Room','120x60x45 cm','Walnut','Solid Wood','https://images.unsplash.com/photo-1484101403633-562f891dc89a?w=400&h=400&fit=crop',60,1,NOW()),
('Queen Size Bed Frame','Sturdy bed frame with headboard',499.00,'Bedroom','Queen','Natural','Wood & Veneer','https://images.unsplash.com/photo-1505691723518-36a5ac3b2bb3?w=400&h=400&fit=crop',30,1,NOW()),
('Memory Foam Mattress','Medium-firm 8-inch mattress',399.00,'Bedroom','Queen','White','Memory Foam','https://images.unsplash.com/photo-1505691938895-1758d7feb511?w=400&h=400&fit=crop',50,1,NOW()),
('Extendable Dining Table','Seats up to 8 people',549.00,'Dining','160-210x90x75 cm','Oak','Engineered Wood','https://images.unsplash.com/photo-1524758631624-e2822e304c36?w=400&h=400&fit=crop',20,1,NOW()),
('Ergonomic Office Chair','Adjustable lumbar support',199.00,'Office','Standard','Black','Mesh & Metal','https://images.unsplash.com/photo-1582582621959-48d233a81f49?w=400&h=400&fit=crop',80,1,NOW()),
('Outdoor Lounge Set','Weather-resistant with cushions',899.00,'Outdoor','4-Piece','Beige','Rattan & Aluminum','https://images.unsplash.com/photo-1505691938895-1758d7feb511?w=400&h=400&fit=crop',15,1,NOW()),
('Bookshelf with Cabinets','Tall bookshelf with storage',279.00,'Storage','90x35x200 cm','Walnut','Engineered Wood','https://images.unsplash.com/photo-1484101403633-562f891dc89a?w=400&h=400&fit=crop',40,1,NOW()),
('Kids Study Desk','Height-adjustable study desk',159.00,'Kids','Small','White','MDF & Steel','https://images.unsplash.com/photo-1519710164239-da123dc03ef4?w=400&h=400&fit=crop',50,1,NOW()),
('Decorative Floor Lamp','Modern lamp with fabric shade',89.00,'Decor','Standard','Black','Metal & Fabric','https://images.unsplash.com/photo-1481277542470-605612bd2d61?w=400&h=400&fit=crop',120,1,NOW()),
('Sectional Sofa','Comfortable L-shaped sectional',999.00,'Living Room','Large','Blue','Fabric & Wood','https://images.unsplash.com/photo-1493666438817-866a91353ca9?w=400&h=400&fit=crop',25,1,NOW()),
('TV Console Unit','Low-profile TV unit with storage',329.00,'Living Room','180x45x50 cm','Oak','Engineered Wood','https://images.unsplash.com/photo-1540574163026-643ea20ade25?w=400&h=400&fit=crop',35,1,NOW()),
('Wardrobe 3-Door','Spacious 3-door wardrobe',599.00,'Bedroom','200x60x220 cm','Walnut','Engineered Wood','https://images.unsplash.com/photo-1616597098258-8eca1dc101aa?w=400&h=400&fit=crop',22,1,NOW()),
('Bedside Table Pair','Pair of bedside tables',149.00,'Bedroom','45x35x50 cm','Natural','Solid Wood','https://images.unsplash.com/photo-1519710164239-da123dc03ef4?w=400&h=400&fit=crop',70,1,NOW()),
('Dining Chair Set (4)','Set of 4 upholstered chairs',299.00,'Dining','Standard','Beige','Fabric & Wood','https://images.unsplash.com/photo-1524758631624-e2822e304c36?w=400&h=400&fit=crop',55,1,NOW()),
('Bar Stool Pair','Adjustable height bar stools (2)',129.00,'Dining','Adjustable','Black','Metal & PU','https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=400&h=400&fit=crop',90,1,NOW()),
('Office Desk','Spacious work desk with drawers',349.00,'Office','160x70x75 cm','White','MDF & Steel','https://images.unsplash.com/photo-1519710164239-da123dc03ef4?w=400&h=400&fit=crop',40,1,NOW()),
('Bookshelf Ladder','Open ladder-style bookshelf',189.00,'Office','70x35x180 cm','Oak','Engineered Wood','https://images.unsplash.com/photo-1540574163026-643ea20ade25?w=400&h=400&fit=crop',65,1,NOW()),
('Patio Umbrella','Large tilting patio umbrella',119.00,'Outdoor','3m Diameter','Green','Polyester & Aluminum','https://images.unsplash.com/photo-1505691938895-1758d7feb511?w=400&h=400&fit=crop',75,1,NOW()),
('Wall Mirror','Framed rectangular wall mirror',99.00,'Decor','80x60 cm','Gold','Glass & Metal','https://images.unsplash.com/photo-1505691723518-36a5ac3b2bb3?w=400&h=400&fit=crop',110,1,NOW());

SET FOREIGN_KEY_CHECKS=1;
SET FOREIGN_KEY_CHECKS=0;
USE tabonfurniture;

-- Ensure categories exist
INSERT IGNORE INTO categories (name, description, icon, is_active, created_at, updated_at) VALUES
('Living Room','Sofas, sectionals, coffee tables, and TV units','fas fa-couch',1,NOW(),NOW()),
('Bedroom','Beds, mattresses, wardrobes, bedside tables','fas fa-bed',1,NOW(),NOW()),
('Dining','Dining tables, chairs, sideboards, bar stools','fas fa-utensils',1,NOW(),NOW()),
('Office','Desks, office chairs, bookcases, storage','fas fa-briefcase',1,NOW(),NOW()),
('Outdoor','Patio sets, loungers, outdoor accessories','fas fa-umbrella-beach',1,NOW(),NOW()),
('Storage','Cabinets, shelves, organizers','fas fa-box',1,NOW(),NOW()),
('Kids','Furniture for kids and nurseries','fas fa-child',1,NOW(),NOW()),
('Decor','Rugs, lamps, mirrors, decor accents','fas fa-lightbulb',1,NOW(),NOW());

-- Insert 20 furniture products
INSERT INTO products (name, description, price, category, dimensions, color, material, image_url, stock_quantity, is_active, created_at) VALUES
('Modern Fabric Sofa','3-seater sofa with durable fabric upholstery',699.00,'Living Room','3-Seater','Gray','Fabric & Wood','https://images.unsplash.com/photo-1501045661006-fcebe0257c3f?w=400&h=400&fit=crop',40,1,NOW()),
('Solid Wood Coffee Table','Minimalist oak wood coffee table',249.00,'Living Room','120x60x45 cm','Walnut','Solid Wood','https://images.unsplash.com/photo-1484101403633-562f891dc89a?w=400&h=400&fit=crop',60,1,NOW()),
('Queen Size Bed Frame','Sturdy bed frame with headboard',499.00,'Bedroom','Queen','Natural','Wood & Veneer','https://images.unsplash.com/photo-1505691723518-36a5ac3b2bb3?w=400&h=400&fit=crop',30,1,NOW()),
('Memory Foam Mattress','Medium-firm 8-inch mattress',399.00,'Bedroom','Queen','White','Memory Foam','https://images.unsplash.com/photo-1505691938895-1758d7feb511?w=400&h=400&fit=crop',50,1,NOW()),
('Extendable Dining Table','Seats up to 8 people',549.00,'Dining','160-210x90x75 cm','Oak','Engineered Wood','https://images.unsplash.com/photo-1524758631624-e2822e304c36?w=400&h=400&fit=crop',20,1,NOW()),
('Ergonomic Office Chair','Adjustable lumbar support',199.00,'Office','Standard','Black','Mesh & Metal','https://images.unsplash.com/photo-1582582621959-48d233a81f49?w=400&h=400&fit=crop',80,1,NOW()),
('Outdoor Lounge Set','Weather-resistant with cushions',899.00,'Outdoor','4-Piece','Beige','Rattan & Aluminum','https://images.unsplash.com/photo-1505691938895-1758d7feb511?w=400&h=400&fit=crop',15,1,NOW()),
('Bookshelf with Cabinets','Tall bookshelf with storage',279.00,'Storage','90x35x200 cm','Walnut','Engineered Wood','https://images.unsplash.com/photo-1484101403633-562f891dc89a?w=400&h=400&fit=crop',40,1,NOW()),
('Kids Study Desk','Height-adjustable study desk',159.00,'Kids','Small','White','MDF & Steel','https://images.unsplash.com/photo-1519710164239-da123dc03ef4?w=400&h=400&fit=crop',50,1,NOW()),
('Decorative Floor Lamp','Modern lamp with fabric shade',89.00,'Decor','Standard','Black','Metal & Fabric','https://images.unsplash.com/photo-1481277542470-605612bd2d61?w=400&h=400&fit=crop',120,1,NOW()),
('Sectional Sofa','Comfortable L-shaped sectional',999.00,'Living Room','Large','Blue','Fabric & Wood','https://images.unsplash.com/photo-1493666438817-866a91353ca9?w=400&h=400&fit=crop',25,1,NOW()),
('TV Console Unit','Low-profile TV unit with storage',329.00,'Living Room','180x45x50 cm','Oak','Engineered Wood','https://images.unsplash.com/photo-1540574163026-643ea20ade25?w=400&h=400&fit=crop',35,1,NOW()),
('Wardrobe 3-Door','Spacious 3-door wardrobe',599.00,'Bedroom','200x60x220 cm','Walnut','Engineered Wood','https://images.unsplash.com/photo-1616597098258-8eca1dc101aa?w=400&h=400&fit=crop',22,1,NOW()),
('Bedside Table Pair','Pair of bedside tables',149.00,'Bedroom','45x35x50 cm','Natural','Solid Wood','https://images.unsplash.com/photo-1519710164239-da123dc03ef4?w=400&h=400&fit=crop',70,1,NOW()),
('Dining Chair Set (4)','Set of 4 upholstered chairs',299.00,'Dining','Standard','Beige','Fabric & Wood','https://images.unsplash.com/photo-1524758631624-e2822e304c36?w=400&h=400&fit=crop',55,1,NOW()),
('Bar Stool Pair','Adjustable height bar stools (2)',129.00,'Dining','Adjustable','Black','Metal & PU','https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=400&h=400&fit=crop',90,1,NOW()),
('Office Desk','Spacious work desk with drawers',349.00,'Office','160x70x75 cm','White','MDF & Steel','https://images.unsplash.com/photo-1519710164239-da123dc03ef4?w=400&h=400&fit=crop',40,1,NOW()),
('Bookshelf Ladder','Open ladder-style bookshelf',189.00,'Office','70x35x180 cm','Oak','Engineered Wood','https://images.unsplash.com/photo-1540574163026-643ea20ade25?w=400&h=400&fit=crop',65,1,NOW()),
('Patio Umbrella','Large tilting patio umbrella',119.00,'Outdoor','3m Diameter','Green','Polyester & Aluminum','https://images.unsplash.com/photo-1505691938895-1758d7feb511?w=400&h=400&fit=crop',75,1,NOW()),
('Wall Mirror','Framed rectangular wall mirror',99.00,'Decor','80x60 cm','Gold','Glass & Metal','https://images.unsplash.com/photo-1505691723518-36a5ac3b2bb3?w=400&h=400&fit=crop',110,1,NOW());

SET FOREIGN_KEY_CHECKS=1;
