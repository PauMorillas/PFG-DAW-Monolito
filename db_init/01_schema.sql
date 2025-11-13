-- Creación de las bases de datos
DROP DATABASE IF EXISTS gest_eventos;
CREATE DATABASE gest_eventos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Creación del Usuario y Asignación de Permisos
-- El usuario se crea con el host '%' para permitir conexiones desde otros contenedores
-- (que tienen nombres de host diferentes, pero están en la misma red Docker).
CREATE USER IF NOT EXISTS 'usuariodemo'@'%' IDENTIFIED BY 'IesCampMorvedre01%';

-- Otorgar permisos completos al usuario en ambas bases de datos
GRANT ALL PRIVILEGES ON gest_eventos.* TO 'usuariodemo'@'%';
GRANT ALL PRIVILEGES ON allowed_domains_api.* TO 'usuariodemo'@'%';

FLUSH PRIVILEGES;

USE gest_eventos;

-- ====================================================================
-- TABLA GERENTE (Representa al Dueño de la Tienda)
-- ====================================================================
CREATE TABLE gerente(
	id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(50),
    correo_elec VARCHAR(100) UNIQUE NOT NULL,
	pass_hash VARCHAR(125) NOT NULL,
	telf CHAR(9),
    rol ENUM('CLIENTE', 'GERENTE') NOT NULL DEFAULT 'GERENTE'
);

-- ====================================================================
-- TABLA NEGOCIO (La Tienda o Empresa física)
-- ====================================================================
CREATE TABLE negocio(
    id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(50),
    correo_elec VARCHAR(100) UNIQUE NOT NULL,
    telf_contacto CHAR(9),
    hora_apertura TIME,
    hora_cierre TIME,
    
    id_gerente INT NOT NULL,
    
    CONSTRAINT FK_UsuarioNegocio FOREIGN KEY (id_gerente)
    REFERENCES gerente(id)
);

-- ====================================================================
-- TABLA CLIENTE (Clientes de nuestros clientes - NO inician sesión)
-- ====================================================================
CREATE TABLE cliente(
    id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    correo_elec VARCHAR(100), -- No es único aquí, ya que dos negocios pueden tener clientes con el mismo email.
    pass_hash VARCHAR(125),
    telf CHAR(9),
    rol ENUM('CLIENTE', 'GERENTE') NOT NULL DEFAULT 'CLIENTE'
);

-- ====================================================================
-- TABLA SERVICIO (La Plantilla del Servicio a ofrecer)
-- ====================================================================
CREATE TABLE servicio(
    id INT PRIMARY KEY AUTO_INCREMENT,
    titulo VARCHAR(75) NOT NULL,
    descripcion VARCHAR(125),
    ubicacion VARCHAR(125),
    
    fecha_creacion DATETIME DEFAULT NOW(),
    duracion_min INT NOT NULL, -- Duración estándar del servicio en minutos.
    
    id_negocio INT NOT NULL, -- FK: Negocio (CREADOR) dueño del servicio.
    
    CONSTRAINT FK_NegocioServicio FOREIGN KEY (id_negocio)
    REFERENCES negocio(id)
);

-- ====================================================================
-- TABLA RESERVA (La Cita Concreta Agendada)
-- ====================================================================
CREATE TABLE reserva(
    id INT PRIMARY KEY AUTO_INCREMENT,
    fecha_inicio DATETIME NOT NULL DEFAULT NOW(), -- Fecha y hora de inicio de la CITA REAL.
	fecha_fin DATETIME NOT NULL DEFAULT NOW(),
    estado ENUM('ACTIVA', 'INACTIVA','CANCELADA') NOT NULL,
    
    id_cliente INT NOT NULL, -- FK: El CLIENTE que hace la reserva.
    id_servicio INT NOT NULL, -- La plantilla de servicio reservada
    
    CONSTRAINT FK_ClienteReserva FOREIGN KEY (id_cliente)
    REFERENCES cliente(id),
    
    CONSTRAINT FK_ServicioReserva FOREIGN KEY (id_servicio)
    REFERENCES servicio(id)
);

-- ====================================================================
-- TABLA PRE_RESERVA (Entidad encargada de gestionar la info de una reserva antes de ser confirmada)
-- ====================================================================
CREATE TABLE pre_reserva (
    id INT NOT NULL AUTO_INCREMENT,
    token VARCHAR(255) NOT NULL UNIQUE,
    fecha_expiracion DATETIME NOT NULL,
    id_servicio INT NOT NULL,
    fecha_inicio DATETIME NOT NULL DEFAULT NOW(),
    fecha_fin DATETIME NOT NULL DEFAULT NOW(),
    nombre_cliente VARCHAR(255) NOT NULL,
    correo_elec VARCHAR(255) NOT NULL,
    telf CHAR(9),
    pass_hash VARCHAR(125), -- Para almacenar el hash de la contraseña
    
    PRIMARY KEY (id),

    -- DEFINICIÓN DE LA CLAVE FORÁNEA (OBLIGATORIA)
    CONSTRAINT FK_PreReservaServicio 
        FOREIGN KEY (id_servicio) 
        REFERENCES servicio(id)
        -- La reserva debe eliminarse si el servicio se borra
        ON DELETE CASCADE
        ON UPDATE CASCADE
);