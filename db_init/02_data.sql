USE gest_eventos;
-- ====================================================================
-- DATOS DE PRUEBA: INSERTS
-- ====================================================================

-- Contraseña para todos los gerentes: 'pass123'
-- Hash generado por BCrypt: $2a$10$wK1W6d.bUe7t9Xl0YgH2a.u5WfD9R0Jz4ZqXmFvF5zC0nI8HkS3I.
SET @PASSWORD_HASH = '$2a$10$wK1W6d.bUe7t9Xl0YgH2a.u5WfD9R0Jz4ZqXmFvF5zC0nI8HkS3I.';

-- 1. INSERTAR GERENTES
-- ====================================================================
INSERT INTO gerente (nombre, correo_elec, pass_hash, telf, rol) VALUES
('Juan Pérez', 'juan.perez@gestor.com', @PASSWORD_HASH, '600111222', 'GERENTE'),
('Laura Gómez', 'laura.gomez@gestor.com', @PASSWORD_HASH, '600333444', 'GERENTE'),
('Carlos Ruiz', 'carlos.ruiz@gestor.com', @PASSWORD_HASH, '600555666', 'GERENTE');

-- 2. INSERTAR NEGOCIOS
-- ====================================================================
-- Horario de apertura: 09:00:00 - 18:00:00 (9 horas)
INSERT INTO negocio (nombre, correo_elec, telf_contacto, hora_apertura, hora_cierre, id_gerente) VALUES
(
    'Corte y Estilo Premium', 
    'info@corteestilo.com', 
    '910777888', 
    '09:00:00', 
    '18:00:00', 
    1 -- Gerente: Juan Pérez
),
(
    'Mecánica Rápida Express', 
    'citas@mecanicarapida.com', 
    '930999000', 
    '08:30:00', 
    '17:30:00', 
    2 -- Gerente: Laura Gómez
),
(
    'Clínica Dental Sonrisa', 
    'recepcion@sonrisa.com', 
    '950111222', 
    '10:00:00', 
    '14:00:00', 
    3 -- Gerente: Carlos Ruiz
);


-- 3. INSERTAR SERVICIOS (Plantillas)
-- ====================================================================
-- Servicios de 'Corte y Estilo Premium' (id_negocio=1)
INSERT INTO servicio (titulo, descripcion, ubicacion, duracion_min, id_negocio) VALUES
('Corte Caballero', 'Corte y lavado básico.', 'Sillón 1', 30, 1),
('Tinte Completo', 'Aplicación de tinte y secado.', 'Sillón 3', 90, 1),
('Peinado Dama', 'Peinado para evento.', 'Sillón 2', 45, 1);

-- Servicios de 'Mecánica Rápida Express' (id_negocio=2)
INSERT INTO servicio (titulo, descripcion, ubicacion, duracion_min, id_negocio) VALUES
('Cambio de Aceite', 'Aceite y filtros estándar.', 'Box 1', 60, 2),
('Revisión Pre-ITV', 'Chequeo completo de seguridad.', 'Box 2', 120, 2);

-- Servicios de 'Clínica Dental Sonrisa' (id_negocio=3)
INSERT INTO servicio (titulo, descripcion, ubicacion, duracion_min, id_negocio) VALUES
('Revisión Anual', 'Consulta y limpieza simple.', 'Consulta 1', 45, 3);

-- 4. INSERTAR CLIENTES
-- ====================================================================
INSERT INTO cliente (nombre, correo_elec, telf, rol) VALUES
('Marta Torres', 'marta@cliente.es', '611000111', 'CLIENTE'),
('Alberto Vidal', 'alberto@cliente.es', '611222333', 'CLIENTE'),
('Sofía Ramos', 'sofia@cliente.es', '611444555', 'CLIENTE');

-- 5. INSERTAR RESERVAS (Citas)
-- ====================================================================
-- Lunes 10/11/2025
-- Servicio 4 (Cambio de Aceite, 60min)
INSERT INTO reserva (fecha_inicio, fecha_fin, estado, id_cliente, id_servicio) VALUES
('2025-11-10 09:00:00', '2025-11-10 10:00:00', 'ACTIVA', 1, 4), -- Cliente Marta, 09:00 - 10:00
('2025-11-10 10:30:00', '2025-10-10 11:30:00', 'ACTIVA', 2, 4); -- Cliente Alberto, 10:30 - 11:30

-- Martes 11/11/2025
-- Servicio 5 (Revisión Pre-ITV, 120min)
INSERT INTO reserva (fecha_inicio, fecha_fin, estado, id_cliente, id_servicio) VALUES
('2025-11-11 15:00:00', '2025-11-11 17:00:00', 'ACTIVA', 3, 5); -- Cliente Sofía, 15:00 - 17:00 (Ocupa las últimas 2h)

-- Miércoles 12/11/2025
-- Servicio 4 (Cambio de Aceite, 60min)
INSERT INTO reserva (fecha_inicio, fecha_fin, estado, id_cliente, id_servicio) VALUES
('2025-11-12 11:30:00', '2025-11-12 12:30:00', 'ACTIVA', 2, 4); -- Cliente Alberto, 11:30 - 12:30

-- Jueves 13/11/2025
-- Servicio 5 (Revisión Pre-ITV, 120min) - Estado 'INACTIVA' para variar
INSERT INTO reserva (fecha_inicio, fecha_fin, estado, id_cliente, id_servicio) VALUES
('2025-11-13 08:30:00', '2025-11-13 10:30:00', 'INACTIVA', 1, 5); -- Cliente Marta, 08:30 - 10:30

-- Viernes 14/11/2025
-- Servicio 4 (Cambio de Aceite, 60min)
INSERT INTO reserva (fecha_inicio, fecha_fin, estado, id_cliente, id_servicio) VALUES
('2025-11-14 10:00:00', '2025-11-14 11:00:00', 'ACTIVA', 3, 4), -- Cliente Sofía, 10:00 - 11:00
('2025-11-14 16:30:00', '2025-11-14 17:30:00', 'ACTIVA', 2, 4); -- Cliente Alberto, 16:30 - 17:30 (Reserva justo antes del cierre)