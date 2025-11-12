FROM php:8.3-cli

# Dependencias del sistema
RUN apt-get update && apt-get install -y \
    git unzip libzip-dev libicu-dev libonig-dev libxml2-dev \
    default-mysql-client nodejs npm \
 && docker-php-ext-install pdo pdo_mysql mbstring zip bcmath \
 && apt-get clean && rm -rf /var/lib/apt/lists/*

COPY --from=composer:2 /usr/bin/composer /usr/bin/composer

WORKDIR /var/www/html
COPY . .

RUN composer install --no-dev --prefer-dist --no-interaction --optimize-autoloader \
 && npm ci \
 && npm run build \
 && php artisan key:generate || true \
 && php artisan config:cache || true \
 && php artisan route:cache || true

COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

EXPOSE 8000
ENTRYPOINT ["docker-entrypoint.sh"]