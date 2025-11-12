#!/bin/sh
set -e

echo "⏳ Waiting for database..."

# Esperar a que MySQL esté listo
/usr/local/bin/php -r "
    \$host = getenv('DB_HOST') ?: 'mysql_db';
    \$db = getenv('DB_DATABASE');
    \$user = getenv('DB_USERNAME');
    \$pass = getenv('DB_PASSWORD');
    while (true) {
        try {
            new PDO('mysql:host='.\$host.';dbname='.\$db, \$user, \$pass);
            break;
        } catch (PDOException \$e) {
            echo 'DB not ready, waiting...\\n';
            sleep(2);
        }
    }
"

echo "✅ Database ready. Running migrations..."
/usr/local/bin/php artisan migrate --path=database/migrations --seed --force || true


echo "🚀 Starting Laravel server..."
exec /usr/local/bin/php artisan serve --host 0.0.0.0 --port=8000