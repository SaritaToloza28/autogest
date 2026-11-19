INSERT INTO car_model (brand, name) VALUES
                                        ('Mazda', '3'),
                                        ('Mazda', 'CX-5'),
                                        ('Chevrolet', 'Spark'),
                                        ('Renault', 'Logan'),
                                        ('Kia', 'Picanto'),
                                        ('Toyota', 'Corolla'),
                                        ('Nissan', 'Versa'),
                                        ('Renault', 'Duster'),
                                        ('Renault', 'Sandero'),
                                        ('Renault', 'Stepway'),
                                        ('Renault', 'Kwid'),
                                        ('Chevrolet', 'Onix'),
                                        ('Chevrolet', 'Sail'),
                                        ('Chevrolet', 'Tracker'),
                                        ('Chevrolet', 'N300'),
                                        ('Mazda', '2'),
                                        ('Mazda', 'BT-50'),
                                        ('Kia', 'Rio'),
                                        ('Kia', 'Sportage'),
                                        ('Hyundai', 'Accent'),
                                        ('Hyundai', 'Tucson'),
                                        ('Toyota', 'Hilux'),
                                        ('Toyota', 'Fortuner'),
                                        ('Nissan', 'March'),
                                        ('Nissan', 'Frontier'),
                                        ('Suzuki', 'Swift'),
                                        ('Suzuki', 'Vitara'),
                                        ('Volkswagen', 'Gol'),
                                        ('Ford', 'Ranger'),
                                        ('Ford', 'EcoSport');

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
                                                                                   ('Plumillas limpiaparabrisas', 'Par de plumillas de 20 pulgadas', 40000.00, '7701234000103', 0, NULL),
                                                                                   ('Aceite de motor 10W-30', 'Aceite semisintético para motor a gasolina, 1 galón', 68000.00, '7701234000110', 25, NULL),
                                                                                   ('Líquido de frenos DOT 4', 'Botella de 500 ml', 22000.00, '7701234000127', 30, NULL),
                                                                                   ('Refrigerante', 'Refrigerante listo para usar, 1 galón', 38000.00, '7701234000134', 18, NULL),
                                                                                   ('Kit de embrague', 'Disco, prensa y balinera', 520000.00, '7701234000141', 3, NULL),
                                                                                   ('Pastillas de freno traseras', 'Juego de 4 pastillas semimetálicas', 98000.00, '7701234000158', 10, NULL),
                                                                                   ('Bandas de freno traseras', 'Juego de bandas para freno de tambor', 85000.00, '7701234000165', 9, NULL),
                                                                                   ('Rodamiento de rueda delantero', 'Rodamiento sellado con sensor ABS', 115000.00, '7701234000172', 7, NULL),
                                                                                   ('Terminal de dirección', 'Terminal exterior de dirección', 65000.00, '7701234000189', 14, NULL),
                                                                                   ('Rótula de suspensión', 'Rótula inferior de tijera', 72000.00, '7701234000196', 12, NULL),
                                                                                   ('Filtro de combustible', 'Filtro en línea para inyección', 48000.00, '7701234000202', 22, NULL),
                                                                                   ('Filtro de cabina', 'Filtro de aire acondicionado con carbón activado', 42000.00, '7701234000219', 15, NULL),
                                                                                   ('Alternador', 'Alternador 12V 90A reconstruido', 650000.00, '7701234000226', 0, NULL),
                                                                                   ('Motor de arranque', 'Motor de arranque 12V', 480000.00, '7701234000233', 2, NULL),
                                                                                   ('Termostato', 'Termostato con empaque, 82 °C', 55000.00, '7701234000240', 11, NULL),
                                                                                   ('Sensor de oxígeno', 'Sonda lambda de 4 cables', 210000.00, '7701234000257', 6, NULL),
                                                                                   ('Bobina de encendido', 'Bobina individual por cilindro', 160000.00, '7701234000264', 8, NULL),
                                                                                   ('Bombillo H4', 'Bombillo halógeno para farola, 60/55 W', 18000.00, '7701234000271', 50, NULL),
                                                                                   ('Espejo retrovisor lateral', 'Espejo lateral manual, lado conductor', 135000.00, '7701234000288', 4, NULL),
                                                                                   ('Correa de accesorios', 'Correa poly-V para alternador y aire acondicionado', 62000.00, '7701234000295', 16, NULL),
                                                                                   ('Empaque de culata', 'Empaque multicapa de acero', 145000.00, '7701234000301', 5, NULL);

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

-- Insumos generales: aceite, líquido de frenos, refrigerante y bombillo H4 sirven para todos los modelos
INSERT INTO spare_part_car_model (spare_part_id, car_model_id)
SELECT sp.id, cm.id FROM spare_part sp, car_model cm
WHERE sp.ean_code = '7701234000110';

INSERT INTO spare_part_car_model (spare_part_id, car_model_id)
SELECT sp.id, cm.id FROM spare_part sp, car_model cm
WHERE sp.ean_code = '7701234000127';

INSERT INTO spare_part_car_model (spare_part_id, car_model_id)
SELECT sp.id, cm.id FROM spare_part sp, car_model cm
WHERE sp.ean_code = '7701234000134';

INSERT INTO spare_part_car_model (spare_part_id, car_model_id)
SELECT sp.id, cm.id FROM spare_part sp, car_model cm
WHERE sp.ean_code = '7701234000271';

-- Repuestos por modelo
INSERT INTO spare_part_car_model (spare_part_id, car_model_id)
SELECT sp.id, cm.id FROM spare_part sp, car_model cm
WHERE sp.ean_code = '7701234000141' AND cm.brand || ' ' || cm.name IN ('Chevrolet Spark', 'Chevrolet Sail', 'Chevrolet Onix', 'Renault Logan', 'Renault Sandero', 'Renault Stepway', 'Kia Picanto', 'Suzuki Swift', 'Volkswagen Gol');

INSERT INTO spare_part_car_model (spare_part_id, car_model_id)
SELECT sp.id, cm.id FROM spare_part sp, car_model cm
WHERE sp.ean_code = '7701234000158' AND cm.brand || ' ' || cm.name IN ('Mazda 3', 'Mazda CX-5', 'Kia Sportage', 'Hyundai Tucson', 'Toyota Corolla', 'Toyota Fortuner', 'Nissan Frontier', 'Ford Ranger');

INSERT INTO spare_part_car_model (spare_part_id, car_model_id)
SELECT sp.id, cm.id FROM spare_part sp, car_model cm
WHERE sp.ean_code = '7701234000165' AND cm.brand || ' ' || cm.name IN ('Chevrolet Spark', 'Chevrolet Sail', 'Chevrolet N300', 'Renault Kwid', 'Renault Logan', 'Renault Sandero', 'Kia Picanto', 'Kia Rio', 'Hyundai Accent', 'Nissan March', 'Nissan Versa', 'Volkswagen Gol', 'Suzuki Swift', 'Mazda 2');

-- Por marca: comparten plataforma o medidas
INSERT INTO spare_part_car_model (spare_part_id, car_model_id)
SELECT sp.id, cm.id FROM spare_part sp, car_model cm
WHERE sp.ean_code = '7701234000172' AND cm.brand IN ('Renault', 'Nissan');

INSERT INTO spare_part_car_model (spare_part_id, car_model_id)
SELECT sp.id, cm.id FROM spare_part sp, car_model cm
WHERE sp.ean_code = '7701234000189' AND cm.brand IN ('Chevrolet', 'Kia', 'Hyundai');

INSERT INTO spare_part_car_model (spare_part_id, car_model_id)
SELECT sp.id, cm.id FROM spare_part sp, car_model cm
WHERE sp.ean_code = '7701234000196' AND cm.brand IN ('Toyota', 'Mazda', 'Ford');

INSERT INTO spare_part_car_model (spare_part_id, car_model_id)
SELECT sp.id, cm.id FROM spare_part sp, car_model cm
WHERE sp.ean_code = '7701234000202' AND cm.brand IN ('Chevrolet', 'Renault', 'Kia', 'Hyundai', 'Suzuki', 'Volkswagen');

INSERT INTO spare_part_car_model (spare_part_id, car_model_id)
SELECT sp.id, cm.id FROM spare_part sp, car_model cm
WHERE sp.ean_code = '7701234000219' AND cm.brand || ' ' || cm.name IN ('Mazda 2', 'Mazda 3', 'Mazda CX-5', 'Kia Sportage', 'Hyundai Tucson', 'Toyota Corolla', 'Renault Duster', 'Chevrolet Tracker', 'Ford EcoSport', 'Suzuki Vitara');

INSERT INTO spare_part_car_model (spare_part_id, car_model_id)
SELECT sp.id, cm.id FROM spare_part sp, car_model cm
WHERE sp.ean_code = '7701234000226' AND cm.brand || ' ' || cm.name IN ('Renault Duster', 'Renault Logan', 'Renault Sandero', 'Nissan Versa');

INSERT INTO spare_part_car_model (spare_part_id, car_model_id)
SELECT sp.id, cm.id FROM spare_part sp, car_model cm
WHERE sp.ean_code = '7701234000233' AND cm.brand IN ('Toyota');

INSERT INTO spare_part_car_model (spare_part_id, car_model_id)
SELECT sp.id, cm.id FROM spare_part sp, car_model cm
WHERE sp.ean_code = '7701234000240' AND cm.brand IN ('Mazda', 'Kia', 'Hyundai');

INSERT INTO spare_part_car_model (spare_part_id, car_model_id)
SELECT sp.id, cm.id FROM spare_part sp, car_model cm
WHERE sp.ean_code = '7701234000257' AND cm.brand IN ('Chevrolet', 'Suzuki', 'Volkswagen', 'Ford');

INSERT INTO spare_part_car_model (spare_part_id, car_model_id)
SELECT sp.id, cm.id FROM spare_part sp, car_model cm
WHERE sp.ean_code = '7701234000264' AND cm.brand IN ('Mazda', 'Toyota', 'Nissan');

INSERT INTO spare_part_car_model (spare_part_id, car_model_id)
SELECT sp.id, cm.id FROM spare_part sp, car_model cm
WHERE sp.ean_code = '7701234000288' AND cm.brand || ' ' || cm.name IN ('Chevrolet Spark', 'Chevrolet Sail', 'Renault Kwid', 'Kia Picanto');

INSERT INTO spare_part_car_model (spare_part_id, car_model_id)
SELECT sp.id, cm.id FROM spare_part sp, car_model cm
WHERE sp.ean_code = '7701234000295' AND cm.brand IN ('Renault', 'Chevrolet', 'Nissan', 'Suzuki');

INSERT INTO spare_part_car_model (spare_part_id, car_model_id)
SELECT sp.id, cm.id FROM spare_part sp, car_model cm
WHERE sp.ean_code = '7701234000301' AND cm.brand IN ('Chevrolet', 'Kia', 'Hyundai', 'Renault');

INSERT INTO vehicle (license_plate, plate_color, owner_name, owner_phone, car_model_id) VALUES
                                                                               ('ABC-123', 'COLOR_YELLOW', 'Laura Gómez', '3001234567', (SELECT id FROM car_model WHERE brand = 'Mazda' AND name = '3')),
                                                                               ('JKL-456', 'COLOR_YELLOW', 'Andrés Martínez', '3109876543', (SELECT id FROM car_model WHERE brand = 'Chevrolet' AND name = 'Spark')),
                                                                               ('MNP-789', 'COLOR_YELLOW', 'Valentina Rojas', '3155550123', (SELECT id FROM car_model WHERE brand = 'Kia' AND name = 'Picanto')),
                                                                               ('TPU-321', 'COLOR_WHITE', 'Transportes El Sol S.A.S.', '3204567890', (SELECT id FROM car_model WHERE brand = 'Renault' AND name = 'Logan')),
                                                                               ('CD-1234', 'COLOR_BLUE', 'Embajada de Canadá', '3012223344', (SELECT id FROM car_model WHERE brand = 'Toyota' AND name = 'Corolla')),
                                                                               ('KMR-214', 'COLOR_YELLOW', 'Camila Pérez', '3014567812', (SELECT id FROM car_model WHERE brand = 'Renault' AND name = 'Duster')),
                                                                               ('HTX-905', 'COLOR_YELLOW', 'Jorge Sánchez', '3127788990', (SELECT id FROM car_model WHERE brand = 'Chevrolet' AND name = 'Onix')),
                                                                               ('BQL-638', 'COLOR_YELLOW', 'Natalia Vargas', '3168904512', (SELECT id FROM car_model WHERE brand = 'Mazda' AND name = '2')),
                                                                               ('FZN-417', 'COLOR_YELLOW', 'Ricardo López', '3005512398', (SELECT id FROM car_model WHERE brand = 'Kia' AND name = 'Sportage')),
                                                                               ('GVA-552', 'COLOR_YELLOW', 'Paola Moreno', '3187654321', (SELECT id FROM car_model WHERE brand = 'Hyundai' AND name = 'Accent')),
                                                                               ('WPD-260', 'COLOR_YELLOW', 'Felipe Ortiz', '3209981234', (SELECT id FROM car_model WHERE brand = 'Suzuki' AND name = 'Swift')),
                                                                               ('UYS-781', 'COLOR_YELLOW', 'Daniela Cárdenas', '3043216547', (SELECT id FROM car_model WHERE brand = 'Volkswagen' AND name = 'Gol')),
                                                                               ('SXT-482', 'COLOR_WHITE', 'Cooperativa de Taxis de Tunja', '3113456789', (SELECT id FROM car_model WHERE brand = 'Chevrolet' AND name = 'Sail')),
                                                                               ('OFB-139', 'COLOR_WHITE', 'Alcaldía de Tunja', '3176549870', (SELECT id FROM car_model WHERE brand = 'Toyota' AND name = 'Hilux')),
                                                                               ('CC-5821', 'COLOR_BLUE', 'Consulado de España', '3021118899', (SELECT id FROM car_model WHERE brand = 'Nissan' AND name = 'Frontier'));

-- Roles (sincronizados con el enum RoleName). Se insertan antes que los usuarios.
INSERT INTO app_role (name) VALUES ('ROLE_ADMIN') ON CONFLICT DO NOTHING;
INSERT INTO app_role (name) VALUES ('ROLE_OPERATOR') ON CONFLICT DO NOTHING;
INSERT INTO app_role (name) VALUES ('ROLE_MECHANIC') ON CONFLICT DO NOTHING;

-- Usuarios de prueba (solo desarrollo). La contraseña se guarda como hash BCrypt.
-- Contraseñas para iniciar sesión:
--   mecanico1 -> Mecanico1-2026
--   mecanico2 -> Mecanico2-2026
--   mecanico3 -> Mecanico3-2026
--   mecanico4 -> Mecanico4-2026
--   operador1 -> Operador1-2026
--   operador2 -> Operador2-2026
--   operador3 -> Operador3-2026
INSERT INTO app_user (username, password, full_name, is_active, role_id) VALUES
    ('mecanico1', '$2a$10$Zsm7dl2oG1Vf5sBzYvs0Y.ZXMqxnbYVPcGaSPB860E4Hf2BiOiXt6', 'Carlos Ramírez', TRUE, (SELECT role_id FROM app_role WHERE name = 'ROLE_MECHANIC')),
    ('mecanico2', '$2a$10$h7F6WQdb2FA2PymGNI7SmezyD3BQyaKfOiaVMyQeOMt.ZxiRADWc2', 'Diana Torres', TRUE, (SELECT role_id FROM app_role WHERE name = 'ROLE_MECHANIC')),
    ('operador1', '$2a$10$625Vo1aPNVsBgziAF0szlOEheXbrCTy/L8QFa4pWJ4mUZ7G8gg/r6', 'Sofía Herrera', TRUE, (SELECT role_id FROM app_role WHERE name = 'ROLE_OPERATOR')),
    ('operador2', '$2a$10$kpW2sUmgBwQ2VmMT7KBlC.GpJFLMCHOvhPkKWpVEm5QyUY9oaQjQS', 'Julián Castro', TRUE, (SELECT role_id FROM app_role WHERE name = 'ROLE_OPERATOR')),
    ('mecanico3', '$2a$10$E1uRvfIdEKuixClzWp5RyeEXdejhmsQCWI2SfiTHkZHJI4oFGi/H2', 'Miguel Ángel Suárez', TRUE, (SELECT role_id FROM app_role WHERE name = 'ROLE_MECHANIC')),
    ('mecanico4', '$2a$10$UiQSGQ/nBsZKg7Vb4.ELUeWTk6A.ozNztAe6u6lXKnIBwIM7j2.Za', 'Paula Jiménez', TRUE, (SELECT role_id FROM app_role WHERE name = 'ROLE_MECHANIC')),
    ('operador3', '$2a$10$XAo2U79cWU6FLk0LcIhsmebJ9tasQe75Sm6sg0oJOxSAmVXeW2fjG', 'Esteban Rincón', TRUE, (SELECT role_id FROM app_role WHERE name = 'ROLE_OPERATOR'));

-- Reparaciones de ejemplo en todas las etapas del flujo:
-- 2 pendientes, 3 en progreso, 2 completadas, 2 entregadas y 1 cancelada (3 a domicilio)
INSERT INTO repair (repair_date, reason, status, service_type, latitude, longitude, vehicle_id, mechanic_id) VALUES
    ('2026-10-20 09:30:00', 'Revisión de frenos: ruido al frenar', 'PENDING', 'SERVICE_INTERNAL', NULL, NULL,
     (SELECT id FROM vehicle WHERE license_plate = 'ABC-123'), (SELECT id FROM app_user WHERE username = 'mecanico1')),
    ('2026-10-18 14:00:00', 'Cambio de bujías', 'IN_PROGRESS', 'SERVICE_INTERNAL', NULL, NULL,
     (SELECT id FROM vehicle WHERE license_plate = 'JKL-456'), (SELECT id FROM app_user WHERE username = 'mecanico1')),
    ('2026-10-10 08:00:00', 'Cambio de filtro de aire', 'DELIVERED', 'SERVICE_EXTERNAL', 4.711000, -74.072100,
     (SELECT id FROM vehicle WHERE license_plate = 'TPU-321'), (SELECT id FROM app_user WHERE username = 'mecanico2')),
    ('2026-11-12 10:15:00', 'Ruido en la suspensión delantera al pasar huecos', 'IN_PROGRESS', 'SERVICE_INTERNAL', NULL, NULL,
     (SELECT id FROM vehicle WHERE license_plate = 'KMR-214'), (SELECT id FROM app_user WHERE username = 'mecanico2')),
    ('2026-11-14 08:00:00', 'Cambio de embrague: patina al acelerar', 'IN_PROGRESS', 'SERVICE_INTERNAL', NULL, NULL,
     (SELECT id FROM vehicle WHERE license_plate = 'SXT-482'), (SELECT id FROM app_user WHERE username = 'mecanico3')),
    ('2026-11-05 09:00:00', 'Mantenimiento de 20.000 km: aceite, filtros y revisión general', 'COMPLETED', 'SERVICE_INTERNAL', NULL, NULL,
     (SELECT id FROM vehicle WHERE license_plate = 'HTX-905'), (SELECT id FROM app_user WHERE username = 'mecanico3')),
    ('2026-11-03 07:30:00', 'No enciende: revisión del sistema de arranque', 'COMPLETED', 'SERVICE_EXTERNAL', 5.544915, -73.357230,
     (SELECT id FROM vehicle WHERE license_plate = 'OFB-139'), (SELECT id FROM app_user WHERE username = 'mecanico4')),
    ('2026-10-25 11:00:00', 'Cambio de pastillas traseras y líquido de frenos', 'DELIVERED', 'SERVICE_INTERNAL', NULL, NULL,
     (SELECT id FROM vehicle WHERE license_plate = 'FZN-417'), (SELECT id FROM app_user WHERE username = 'mecanico2')),
    ('2026-10-28 15:30:00', 'Recalentamiento del motor: el cliente decidió no hacer la reparación', 'CANCELLED', 'SERVICE_INTERNAL', NULL, NULL,
     (SELECT id FROM vehicle WHERE license_plate = 'GVA-552'), (SELECT id FROM app_user WHERE username = 'mecanico4')),
    ('2026-11-16 16:00:00', 'Revisión de luces y cambio de bombillos', 'PENDING', 'SERVICE_EXTERNAL', 5.531870, -73.361540,
     (SELECT id FROM vehicle WHERE license_plate = 'CC-5821'), (SELECT id FROM app_user WHERE username = 'mecanico4'));

-- Repuestos usados (compatibles con el modelo de cada vehículo)
INSERT INTO repair_spare_part (repair_id, spare_part_id, quantity) VALUES
    ((SELECT r.id FROM repair r JOIN vehicle v ON r.vehicle_id = v.id WHERE v.license_plate = 'JKL-456'),
     (SELECT id FROM spare_part WHERE ean_code = '7701234000035'), 4),
    ((SELECT r.id FROM repair r JOIN vehicle v ON r.vehicle_id = v.id WHERE v.license_plate = 'TPU-321'),
     (SELECT id FROM spare_part WHERE ean_code = '7701234000042'), 1),
    ((SELECT r.id FROM repair r JOIN vehicle v ON r.vehicle_id = v.id WHERE v.license_plate = 'KMR-214'),
     (SELECT id FROM spare_part WHERE ean_code = '7701234000172'), 2),
    ((SELECT r.id FROM repair r JOIN vehicle v ON r.vehicle_id = v.id WHERE v.license_plate = 'SXT-482'),
     (SELECT id FROM spare_part WHERE ean_code = '7701234000141'), 1),
    ((SELECT r.id FROM repair r JOIN vehicle v ON r.vehicle_id = v.id WHERE v.license_plate = 'SXT-482'),
     (SELECT id FROM spare_part WHERE ean_code = '7701234000110'), 1),
    ((SELECT r.id FROM repair r JOIN vehicle v ON r.vehicle_id = v.id WHERE v.license_plate = 'HTX-905'),
     (SELECT id FROM spare_part WHERE ean_code = '7701234000110'), 1),
    ((SELECT r.id FROM repair r JOIN vehicle v ON r.vehicle_id = v.id WHERE v.license_plate = 'HTX-905'),
     (SELECT id FROM spare_part WHERE ean_code = '7701234000202'), 1),
    ((SELECT r.id FROM repair r JOIN vehicle v ON r.vehicle_id = v.id WHERE v.license_plate = 'OFB-139'),
     (SELECT id FROM spare_part WHERE ean_code = '7701234000233'), 1),
    ((SELECT r.id FROM repair r JOIN vehicle v ON r.vehicle_id = v.id WHERE v.license_plate = 'FZN-417'),
     (SELECT id FROM spare_part WHERE ean_code = '7701234000158'), 1),
    ((SELECT r.id FROM repair r JOIN vehicle v ON r.vehicle_id = v.id WHERE v.license_plate = 'FZN-417'),
     (SELECT id FROM spare_part WHERE ean_code = '7701234000127'), 1);

-- Se descuenta del stock lo que se usó, igual que hace la app
-- (la orden cancelada no descuenta: al cancelar, los repuestos vuelven al stock)
UPDATE spare_part SET stock = stock - 4 WHERE ean_code = '7701234000035';
UPDATE spare_part SET stock = stock - 1 WHERE ean_code = '7701234000042';
UPDATE spare_part SET stock = stock - 2 WHERE ean_code = '7701234000172';
UPDATE spare_part SET stock = stock - 1 WHERE ean_code = '7701234000141';
UPDATE spare_part SET stock = stock - 2 WHERE ean_code = '7701234000110';
UPDATE spare_part SET stock = stock - 1 WHERE ean_code = '7701234000202';
UPDATE spare_part SET stock = stock - 1 WHERE ean_code = '7701234000233';
UPDATE spare_part SET stock = stock - 1 WHERE ean_code = '7701234000158';
UPDATE spare_part SET stock = stock - 1 WHERE ean_code = '7701234000127';