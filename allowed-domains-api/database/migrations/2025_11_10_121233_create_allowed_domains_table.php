<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::create('allowed_domains', function (Blueprint $table) {
        $table->id();
        $table->string('dominio')->unique();  // unique porque no quieres dominios duplicados
        $table->boolean('activo')->default(true);
        $table->text('descripcion')->nullable();
        $table->timestamps();
    });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('allowed_domains');
    }
};
