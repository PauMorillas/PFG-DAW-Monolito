#!/bin/bash

# --- PASO 1: Compilar la aplicación Spring Boot ---
echo "--- 📦 Limpiando y compilando la aplicación Java (Maven) ---"

cd ./gest-eventos-app || { echo "❌ No se encontró la carpeta gest-eventos-app"; exit 1; }
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ ERROR: La compilación de Maven falló. Deteniendo el script."
    exit 1
fi

echo "✅ Compilación exitosa. JAR listo."

# --- PASO 2: Limpieza completa de Podman ---
echo "--- 🧹 Deteniendo y eliminando contenedores, redes y volumen de la BD ---"

cd ..

# Usa podman-compose o podman compose directamente
podman compose down -v --remove-orphans || echo "⚠️ No había contenedores corriendo o error al detenerlos."

echo "✅ Limpieza de Podman completa."

# --- PASO 3: Construcción y despliegue de los servicios ---

echo "--- ⬆️ Desplegando la aplicación con las imágenes más recientes ---"

podman compose up --build -d || { echo "❌ Error al levantar los contenedores"; exit 1; }

echo "--- ✨ Proceso de despliegue completado. ---"
echo ""
echo "💡 VERIFICACIÓN:"
echo "El contenedor 'pfg_daw_mysql_db' tardará unos segundos en estar HEALTHY."
echo "Puedes comprobar el estado con: podman ps"
echo "Una vez listo, accede a: https://embedbookapp.com"