<?php

// database/seeders/AllowedDomainSeeder.php

namespace Database\Seeders;

use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\DB;
use Carbon\Carbon; 
use Schema;// Usamos Carbon para las timestamps

class AllowedDomainSeeder extends Seeder
{
    /**
     * Run the database seeds.
     */
    public function run(): void
    {
        // Limpiamos la tabla para que no haya duplicados
        // Solo correr si la tabla existe
        if (!Schema::hasTable('allowed_domains')) {
            $this->command->warn('Tabla "allowed_domains" no existe, se omite el seeder.');
            return;
        }

        // Solo limpiar e insertar si estamos en entorno local o de desarrollo
        if (!app()->environment(['local', 'development', 'testing'])) {
            $this->command->warn('Saltando seed de allowed_domains en entorno de producción.');
            return;
        }

        $now = Carbon::now();

        // Limpieza controlada (sin TRUNCATE)
        DB::table('allowed_domains')->delete();

        DB::table('allowed_domains')->insert([
            [
                'dominio' => 'https://cliente-produccion.com',
                'activo' => 1, // 1 = true
                'descripcion' => 'Dominio principal del cliente XYZ para iFrame.',
                'created_at' => $now,
                'updated_at' => $now,
            ],
            [
                'dominio' => 'https://otro-microservicio.net',
                'activo' => 1,
                'descripcion' => 'Dominio de otro microservicio interno.',
                'created_at' => $now,
                'updated_at' => $now,
            ],
            [
                'dominio' => 'http://localhost:3000',
                'activo' => 1,
                'descripcion' => 'Dominio de desarrollo local del frontend.',
                'created_at' => $now,
                'updated_at' => $now,
            ],
            [
                'dominio' => 'https://antiguo-dominio.com',
                'activo' => 0, // 0 = false (inactivo)
                'descripcion' => 'Dominio antiguo, marcado como inactivo para pruebas.',
                'created_at' => $now,
                'updated_at' => $now,
            ],
        ]);
    }
}
