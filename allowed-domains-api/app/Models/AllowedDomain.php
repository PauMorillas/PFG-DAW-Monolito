<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class AllowedDomain extends Model
{
    protected $table = 'allowed_domains';

    protected $fillable = [
        'dominio',
        'activo',
        'descripcion'
    ];

    /**
     * Para obtener solo los dominios activos.
     */
    public function scopeActive($query)
    {
        return $query->where('activo', true);
    }
}
