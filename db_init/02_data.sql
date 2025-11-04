-- ====================================================================
-- DATOS DE PRUEBA: INSERTS
-- ====================================================================

-- Contraseña para todos los gerentes: 'pass123'
-- Hash generado por BCrypt: $2a$10$wK1W6d.bUe7t9Xl0YgH2a.u5WfD9R0Jz4ZqXmFvF5zC0nI8HkS3I.
SET @PASSWORD_HASH = '$2a$10$wK1W6d.bUe7t9Xl0YgH2a.u5WfD9R0Jz4ZqXmFvF5zC0nI8HkS3I.';

-- 1. INSERTAR GERENTES
-- ====================================================================
INSERT INTO gerente (nombre, correo_elec, pass, telf) VALUES
('Juan Pérez', 'juan.perez@gestor.com', @PASSWORD_HASH, '600111222'),
('Laura Gómez', 'laura.gomez@gestor.com', @PASSWORD_HASH, '600333444'),
('Carlos Ruiz', 'carlos.ruiz@gestor.com', @PASSWORD_HASH, '600555666');


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


-- 4. INSERTAR CLIENTES_FINAL
-- ====================================================================
INSERT INTO cliente (nombre, correo_elec, telf) VALUES
('Marta Torres', 'marta@cliente.es', '611000111'),
('Alberto Vidal', 'alberto@cliente.es', '611222333'),
('Sofía Ramos', 'sofia@cliente.es', '611444555');


-- 5. INSERTAR RESERVAS (Citas)
-- ====================================================================
-- Reservas para 'Corte Caballero' (id_servicio=1, duración=30min)
INSERT INTO Reserva (fecha_inicio, estado, id_cliente, id_servicio) VALUES
('2025-09-27 10:00:00', 'ACTIVA', 1, 1), -- Cliente Marta, 10:00 - 10:30
('2025-09-27 10:30:00', 'ACTIVA', 2, 1); -- Cliente Alberto, 10:30 - 11:00

-- Reserva para 'Cambio de Aceite' (id_servicio=4, duración=60min)
INSERT INTO Reserva (fecha_inicio, estado, id_cliente, id_servicio) VALUES
('2025-09-27 09:30:00', 'ACTIVA', 3, 4); -- Cliente Sofía, 09:30 - 10:30

-- Reserva Cancelada (para pruebas de estado)
INSERT INTO Reserva (fecha_inicio, estado, id_cliente, id_servicio) VALUES
('2025-09-28 11:00:00', 'CANCELADA', 1, 1);