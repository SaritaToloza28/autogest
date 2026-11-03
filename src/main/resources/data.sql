INSERT INTO car_model (brand, name) VALUES
                                        ('Mazda', '3'),
                                        ('Mazda', 'CX-5'),
                                        ('Chevrolet', 'Spark'),
                                        ('Renault', 'Logan'),
                                        ('Kia', 'Picanto'),
                                        ('Toyota', 'Corolla'),
                                        ('Nissan', 'Versa');

INSERT INTO spare_part (name, description, price, ean_code, stock, image_path) VALUES
                                                                                   ('Filtro de aceite', 'Filtro de aceite para motor a gasolina', 25000.00, '7701234000011', 30, NULL),
                                                                                   ('Pastillas de freno delanteras', 'Juego de 4 pastillas cerámicas', 120000.00, '7701234000028', 12, NULL),
                                                                                   ('Bujía de iridio', 'Bujía de larga duración', 35000.00, '7701234000035', 40, NULL),
                                                                                   ('Filtro de aire', 'Filtro de aire de motor', 45000.00, '7701234000042', 20, NULL),
                                                                                   ('Batería 12V 45Ah', 'Batería libre de mantenimiento', 380000.00, '7701234000059', 6, NULL),
                                                                                   ('Kit de correa de distribución', 'Correa con tensor', 290000.00, '7701234000066', 5, NULL),
                                                                                   ('Amortiguador delantero', 'Amortiguador a gas', 210000.00, '7701234000073', 8, NULL),
                                                                                   ('Disco de freno', 'Disco de freno ventilado', 160000.00, '7701234000080', 10, NULL),
                                                                                   ('Bomba de agua', 'Bomba de agua con empaque', 175000.00, '7701234000097', 4, NULL),
                                                                                   ('Plumillas limpiaparabrisas', 'Par de plumillas de 20 pulgadas', 40000.00, '7701234000103', 0, NULL);

INSERT INTO spare_part_car_model (spare_part_id, car_model_id)
SELECT sp.id, cm.id FROM spare_part sp, car_model cm
WHERE sp.ean_code = '7701234000011' AND cm.brand || ' ' || cm.name IN ('Mazda 3', 'Mazda CX-5', 'Toyota Corolla');

INSERT INTO spare_part_car_model (spare_part_id, car_model_id)
SELECT sp.id, cm.id FROM spare_part sp, car_model cm
WHERE sp.ean_code = '7701234000028' AND cm.brand || ' ' || cm.name IN ('Mazda 3', 'Kia Picanto');

INSERT INTO spare_part_car_model (spare_part_id, car_model_id)
SELECT sp.id, cm.id FROM spare_part sp, car_model cm
WHERE sp.ean_code = '7701234000035' AND cm.brand || ' ' || cm.name IN ('Chevrolet Spark', 'Kia Picanto', 'Renault Logan');

INSERT INTO spare_part_car_model (spare_part_id, car_model_id)
SELECT sp.id, cm.id FROM spare_part sp, car_model cm
WHERE sp.ean_code = '7701234000042' AND cm.brand || ' ' || cm.name IN ('Renault Logan', 'Nissan Versa');

INSERT INTO spare_part_car_model (spare_part_id, car_model_id)
SELECT sp.id, cm.id FROM spare_part sp, car_model cm
WHERE sp.ean_code = '7701234000059' AND cm.brand || ' ' || cm.name IN ('Mazda 3', 'Mazda CX-5', 'Toyota Corolla', 'Nissan Versa');

INSERT INTO spare_part_car_model (spare_part_id, car_model_id)
SELECT sp.id, cm.id FROM spare_part sp, car_model cm
WHERE sp.ean_code = '7701234000066' AND cm.brand || ' ' || cm.name IN ('Chevrolet Spark');

INSERT INTO spare_part_car_model (spare_part_id, car_model_id)
SELECT sp.id, cm.id FROM spare_part sp, car_model cm
WHERE sp.ean_code = '7701234000073' AND cm.brand || ' ' || cm.name IN ('Mazda CX-5');

INSERT INTO spare_part_car_model (spare_part_id, car_model_id)
SELECT sp.id, cm.id FROM spare_part sp, car_model cm
WHERE sp.ean_code = '7701234000080' AND cm.brand || ' ' || cm.name IN ('Toyota Corolla', 'Nissan Versa');

INSERT INTO spare_part_car_model (spare_part_id, car_model_id)
SELECT sp.id, cm.id FROM spare_part sp, car_model cm
WHERE sp.ean_code = '7701234000097' AND cm.brand || ' ' || cm.name IN ('Renault Logan', 'Chevrolet Spark');

INSERT INTO spare_part_car_model (spare_part_id, car_model_id)
SELECT sp.id, cm.id FROM spare_part sp, car_model cm
WHERE sp.ean_code = '7701234000103' AND cm.brand || ' ' || cm.name IN ('Mazda 3', 'Kia Picanto', 'Chevrolet Spark', 'Renault Logan');

INSERT INTO vehicle (license_plate, plate_color, owner_name, car_model_id) VALUES
                                                                               ('ABC-123', 'COLOR_YELLOW', 'Laura Gómez', (SELECT id FROM car_model WHERE brand = 'Mazda' AND name = '3')),
                                                                               ('JKL-456', 'COLOR_YELLOW', 'Andrés Martínez', (SELECT id FROM car_model WHERE brand = 'Chevrolet' AND name = 'Spark')),
                                                                               ('MNP-789', 'COLOR_YELLOW', 'Valentina Rojas', (SELECT id FROM car_model WHERE brand = 'Kia' AND name = 'Picanto')),
                                                                               ('TPU-321', 'COLOR_WHITE', 'Transportes El Sol S.A.S.', (SELECT id FROM car_model WHERE brand = 'Renault' AND name = 'Logan')),
                                                                               ('CD-1234', 'COLOR_BLUE', 'Embajada de Canadá', (SELECT id FROM car_model WHERE brand = 'Toyota' AND name = 'Corolla'));

-- Usuarios de prueba (solo desarrollo). La contraseña se guarda como hash BCrypt.
-- Contraseñas para iniciar sesión:
--   mecanico1 -> Mecanico1-2026
--   mecanico2 -> Mecanico2-2026
--   operador1 -> Operador1-2026
--   operador2 -> Operador2-2026
INSERT INTO app_user (username, password, full_name, role) VALUES
                                                               ('mecanico1', '$2a$10$Zsm7dl2oG1Vf5sBzYvs0Y.ZXMqxnbYVPcGaSPB860E4Hf2BiOiXt6', 'Carlos Ramírez', 'MECHANIC'),
                                                               ('mecanico2', '$2a$10$h7F6WQdb2FA2PymGNI7SmezyD3BQyaKfOiaVMyQeOMt.ZxiRADWc2', 'Diana Torres', 'MECHANIC'),
                                                               ('operador1', '$2a$10$625Vo1aPNVsBgziAF0szlOEheXbrCTy/L8QFa4pWJ4mUZ7G8gg/r6', 'Sofía Herrera', 'OPERATOR'),
                                                               ('operador2', '$2a$10$kpW2sUmgBwQ2VmMT7KBlC.GpJFLMCHOvhPkKWpVEm5QyUY9oaQjQS', 'Julián Castro', 'OPERATOR');
