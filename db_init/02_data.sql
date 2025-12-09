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
INSERT INTO `gerente` (`id`, `nombre`, `correo_elec`, `pass_hash`, `telf`, `rol`)
VALUES
	('4', 'Manolito', 'morillashuertapau@gmail.com', '$2a$10$SGTiOsMK1dL.4uQ9LfkDHu51xf7p2Ow0jbZdnHYien8T7smLOmDWG', '666999666', 'GERENTE');

-- 2. INSERTAR NEGOCIOS
-- ====================================================================
-- Horario de apertura: 09:00:00 - 18:00:00 (9 horas)
INSERT INTO negocio (nombre, correo_elec, telf_contacto, hora_apertura, hora_cierre, dias_apertura, id_gerente) VALUES
(
    'Corte y Estilo Premium', 
    'info@corteestilo.com', 
    '910777888', 
    '09:00:00', 
    '18:00:00',
    '1,2,3,4,5',
    1 -- Gerente: Juan Pérez
),
(
    'Mecánica Rápida Express', 
    'citas@mecanicarapida.com', 
    '930999000', 
    '08:30:00', 
    '17:30:00', 
    '1,2,3,4,5',
    2 -- Gerente: Laura Gómez
),
(
    'Clínica Dental Sonrisa', 
    'recepcion@sonrisa.com', 
    '950111222', 
    '10:00:00', 
    '14:00:00',
    '1,2,3,4',
    3 -- Gerente: Carlos Ruiz
);

-- ====================================================================
-- NEGOCIOS DE PRUEBA PARA morillashuertapau@gmail.com
-- ====================================================================

-- 1. INSERTAR NEGOCIOS
INSERT INTO negocio (nombre, correo_elec, telf_contacto, hora_apertura, hora_cierre, id_gerente) 
VALUES ('Cafetería La Buena Onda', 'contacto@buenaonda.com', '611123456', '08:00:00', '20:00:00', 4);
SET @ID_NEGOCIO_1 = LAST_INSERT_ID();

-- Servicios de 'Cafetería La Buena Onda'
INSERT INTO servicio (titulo, descripcion, ubicacion, duracion_min, coste, id_negocio) VALUES
('Café Especial', 'Preparación de café gourmet.', 'Barra 1', 15, 1.50, @ID_NEGOCIO_1),
('Desayuno Completo', 'Tostadas, huevos y café.', 'Mesa 3', 45, 5.50, @ID_NEGOCIO_1);

-- 2. Insertar Academia de Yoga Zen
INSERT INTO negocio (nombre, correo_elec, telf_contacto, hora_apertura, hora_cierre, id_gerente) 
VALUES ('Academia de Yoga Zen', 'contacto@yogazen.com', '611234567', '07:00:00', '22:00:00', 4);
SET @ID_NEGOCIO_2 = LAST_INSERT_ID();

-- Servicios de 'Academia de Yoga Zen'
INSERT INTO servicio (titulo, descripcion, ubicacion, duracion_min, coste, id_negocio) VALUES
('Clase de Yoga Principiantes', 'Sesión introductoria para nuevos alumnos.', 'Sala A', 60, 10.00, @ID_NEGOCIO_2),
('Clase Avanzada', 'Yoga avanzado con posturas desafiantes.', 'Sala B', 75, 25.00, @ID_NEGOCIO_2),
('Meditación Guiada', '30 minutos de meditación para relajación total.', 'Sala C', 30, 35.00, @ID_NEGOCIO_2);

-- 3. Servicios de 'Mecánica Rápida Express'
INSERT INTO servicio (titulo, descripcion, ubicacion, duracion_min, coste, id_negocio) VALUES
('Cambio de Aceite', 'Aceite y filtros estándar.', 'Box 1', 60, 60.00, 2);
SET @ID_SERVICIO_ACEITE = LAST_INSERT_ID();

INSERT INTO servicio (titulo, descripcion, ubicacion, duracion_min, coste, id_negocio) VALUES
('Revisión Pre-ITV', 'Chequeo completo de seguridad.', 'Box 2', 120, 120.00, 2);
SET @ID_SERVICIO_ITV = LAST_INSERT_ID();

INSERT INTO `servicio` (`titulo`, `descripcion`, `ubicacion`, `coste`, `fecha_creacion`, `duracion_min`, `id_negocio`)
VALUES
  ('Cambio de Neumáticos', 'Sustitución de neumáticos y equilibrado.', 'Box 3', '80.00', NOW(), '45', 2),
  ('Diagnóstico Electrónico', 'Lectura de centralita y detección de fallos.', 'Box 4', '50.00', NOW(), '30', 2);


-- ====================================================================
-- 4. INSERTAR CLIENTES
-- ====================================================================
INSERT INTO cliente (nombre, correo_elec, pass_hash, telf, rol) VALUES
('Marta Torres', 'marta@cliente.es', @PASSWORD_HASH, '611000111', 'CLIENTE');
SET @ID_CLIENTE_1 = LAST_INSERT_ID();

INSERT INTO cliente (nombre, correo_elec, pass_hash, telf, rol) VALUES
('Alberto Vidal', 'alberto@cliente.es', @PASSWORD_HASH, '611222333', 'CLIENTE');
SET @ID_CLIENTE_2 = LAST_INSERT_ID();

INSERT INTO cliente (nombre, correo_elec, pass_hash, telf, rol) VALUES
('Sofía Ramos', 'sofia@cliente.es', @PASSWORD_HASH, '611444555', 'CLIENTE');
SET @ID_CLIENTE_3 = LAST_INSERT_ID();

-- ====================================================================
-- 5. INSERTAR RESERVAS (Citas) usando variables de cliente y servicio
-- ====================================================================

-- Lunes 10/11/2025 - Servicio Cambio de Aceite
INSERT INTO reserva (fecha_inicio, fecha_fin, estado, id_cliente, id_servicio) VALUES
('2025-11-24 09:00:00', '2025-11-10 10:00:00', 'ACTIVA', @ID_CLIENTE_1, @ID_SERVICIO_ACEITE),
('2025-11-24 10:30:00', '2025-11-10 11:30:00', 'ACTIVA', @ID_CLIENTE_2, @ID_SERVICIO_ACEITE);

-- Martes 11/11/2025 - Servicio Revisión Pre-ITV
INSERT INTO reserva (fecha_inicio, fecha_fin, estado, id_cliente, id_servicio) VALUES
('2025-11-25 15:00:00', '2025-11-11 17:00:00', 'ACTIVA', @ID_CLIENTE_3, @ID_SERVICIO_ITV);

-- Miércoles 12/11/2025 - Servicio Cambio de Aceite
INSERT INTO reserva (fecha_inicio, fecha_fin, estado, id_cliente, id_servicio) VALUES
('2025-11-25 11:30:00', '2025-11-12 12:30:00', 'ACTIVA', @ID_CLIENTE_2, @ID_SERVICIO_ACEITE);

-- Jueves 13/11/2025 - Servicio Revisión Pre-ITV (INACTIVA)
INSERT INTO reserva (fecha_inicio, fecha_fin, estado, id_cliente, id_servicio) VALUES
('2025-11-27 08:30:00', '2025-11-13 10:30:00', 'INACTIVA', @ID_CLIENTE_1, @ID_SERVICIO_ITV);

-- Viernes 14/11/2025 - Servicio Cambio de Aceite
INSERT INTO reserva (fecha_inicio, fecha_fin, estado, id_cliente, id_servicio) VALUES
('2025-11-21 10:00:00', '2025-11-14 11:00:00', 'ACTIVA', @ID_CLIENTE_3, @ID_SERVICIO_ACEITE),
('2025-11-21 16:30:00', '2025-11-14 17:30:00', 'ACTIVA', @ID_CLIENTE_2, @ID_SERVICIO_ACEITE);

-- === Reservas para los servicios de Academia de Yoga Zen ===
-- Lunes 10/11/2025 - Servicio Cambio de Aceite
INSERT INTO reserva (fecha_inicio, fecha_fin, estado, id_cliente, id_servicio) VALUES
('2025-11-24 09:00:00', '2025-11-10 10:00:00', 'ACTIVA', @ID_CLIENTE_1, 3),
('2025-11-24 10:30:00', '2025-11-10 11:30:00', 'ACTIVA', @ID_CLIENTE_2, 3);

-- Martes 11/11/2025 - Servicio Revisión Pre-ITV
INSERT INTO reserva (fecha_inicio, fecha_fin, estado, id_cliente, id_servicio) VALUES
('2025-11-25 15:00:00', '2025-11-11 17:00:00', 'ACTIVA', @ID_CLIENTE_3, 4);

-- Miércoles 12/11/2025 - Servicio Cambio de Aceite
INSERT INTO reserva (fecha_inicio, fecha_fin, estado, id_cliente, id_servicio) VALUES
('2025-11-25 11:30:00', '2025-11-12 12:30:00', 'ACTIVA', @ID_CLIENTE_2, 4);

-- Jueves 13/11/2025 - Servicio Revisión Pre-ITV (INACTIVA)
INSERT INTO reserva (fecha_inicio, fecha_fin, estado, id_cliente, id_servicio) VALUES
('2025-11-26 08:30:00', '2025-11-13 10:30:00', 'INACTIVA', @ID_CLIENTE_1, 4);

-- Viernes 14/11/2025 - Servicio Cambio de Aceite
INSERT INTO reserva (fecha_inicio, fecha_fin, estado, id_cliente, id_servicio) VALUES
('2025-11-27 10:00:00', '2025-11-14 11:00:00', 'ACTIVA', @ID_CLIENTE_3, 5),
('2025-11-28 16:30:00', '2025-11-14 17:30:00', 'ACTIVA', @ID_CLIENTE_2, 5);