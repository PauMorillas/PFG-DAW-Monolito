#!/bin/bash

# --- PASO 1: Compilar la aplicación Spring Boot ---

echo "--- 📦 Limpiando y compilando la aplicación Java (Maven) ---"

# Ejecuta la limpieza de Maven, compila y salta los tests
# mvn clean package -DskipTests es la opción que pediste
mvn clean package -DskipTests

# Verificar si la compilación fue exitosa
if [ $? -ne 0 ]; then
    echo "❌ ERROR: La compilación de Maven falló. Deteniendo el script."
    exit 1
fi

echo "✅ Compilación exitosa. JAR listo."

# --- PASO 2: Limpieza Completa de Docker ---

echo "--- 🧹 Deteniendo y eliminando contenedores, redes y volumen de la BD ---"

# El comando 'down -v' es la clave para eliminar el volumen de datos persistentes.
# La opción '--remove-orphans' previene conflictos con recursos no gestionados.
docker compose down -v --remove-orphans

# Nota: El comando anterior no fallará incluso si los contenedores no estaban corriendo.
echo "✅ Limpieza de Docker completa."

# --- PASO 3: Construcción y Despliegue de los Servicios ---

echo "--- ⬆️ Desplegando la aplicación con las imágenes más recientes ---"

# 'up --build -d' construye la imagen con el nuevo JAR y levanta todo en background.
docker compose up --build -d

echo "--- ✨ Proceso de despliegue completado. ---"
echo ""
echo "💡 VERIFICACIÓN:"
echo "El contenedor 'pfg_daw_mysql_db' tardará unos segundos en estar HEALTHY."
echo "Puedes comprobar el estado con: docker ps"
echo "Una vez listo, accede a: http://localhost:8081/ (o el puerto que uses)"