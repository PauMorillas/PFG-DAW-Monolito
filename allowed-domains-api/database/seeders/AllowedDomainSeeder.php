<?php

namespace Database\Seeders;

use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\DB;
use Carbon\Carbon;
use Schema;

class AllowedDomainSeeder extends Seeder
{
    public function run(): void
    {
        if (!Schema::hasTable('allowed_domains')) {
            $this->command->warn('Tabla "allowed_domains" no existe, se omite el seeder.');
            return;
        }

        if (!app()->environment(['local', 'development', 'testing'])) {
            $this->command->warn('Saltando seed de allowed_domains en entorno de producción.');
            return;
        }

        $now = Carbon::now();

        DB::table('allowed_domains')->delete();

        DB::table('allowed_domains')->insert([
            // FRONTEND Angular local
            [
                'dominio' => 'https://paumorillas.github.io/PFG-DAW-ANGULARFRONT',
                'activo' => 1,
                'descripcion' => 'Frontend Angular público.',
                'created_at' => $now,
                'updated_at' => $now,
            ],
            [
                'dominio' => 'http://localhost:4200',
                'activo' => 1,
                'descripcion' => 'Frontend Angular público.',
                'created_at' => $now,
                'updated_at' => $now,
            ],
            [
                'dominio' => 'https://paumorillas.github.io',
                'activo' => 1,
                'descripcion' => 'Frontend Angular público.',
                'created_at' => $now,
                'updated_at' => $now,
            ],
            // SPRING BOOT frontend público
            [
                'dominio' => 'https://embedbookapp.com',
                'activo' => 1,
                'descripcion' => 'Frontend público de Spring Boot (iframe host).',
                'created_at' => $now,
                'updated_at' => $now,
            ],
            // API Laravel local
            [
                'dominio' => 'http://localhost:8000',
                'activo' => 1,
                'descripcion' => 'API de dominios permitidos (Laravel).',
                'created_at' => $now,
                'updated_at' => $now,
            ],
            // Producción (ejemplo)
            [
                'dominio' => 'https://cliente-produccion.com',
                'activo' => 1,
                'descripcion' => 'Dominio principal de producción del cliente.',
                'created_at' => $now,
                'updated_at' => $now,
            ],
            // CDN para scripts y estilos
            [
                'dominio' => 'https://cdn.jsdelivr.net',
                'activo' => 1,
                'descripcion' => 'CDN público para scripts y estilos.',
                'created_at' => $now,
                'updated_at' => $now,
            ],

            // Para añadir más dominios estáticos, agregarlos aquí
        ]);
    }
}
