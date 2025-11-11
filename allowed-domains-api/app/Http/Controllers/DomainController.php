<?php

namespace App\Http\Controllers;

use App\Models\AllowedDomain;
use Illuminate\Http\Request;
use Validator;

class DomainController extends Controller
{
    public function getDomains()
    {   
        // Obtener los dominios activos y unirlos en una cadena separada por espacios
       $domains = AllowedDomain::active()
        ->pluck('dominio')
        ->join(' ');
    
    return response($domains)
        ->header('Content-Type', 'text/plain');
    }

    public function saveDomain(Request $request)
    {
        
        // 1. VALIDACIÓN DE DATOS
        $validator = Validator::make($request->all(), [
            // El campo 'dominio' es requerido, debe ser un formato de URL, 
            // debe tener máximo 255 caracteres, y debe ser único en la tabla.
            'dominio' => 'required|url|max:255|unique:allowed_domains,dominio',
            'descripcion' => 'nullable|string|max:500', // Campo opcional
            'activo' => 'boolean' // Opcional, si se envía
        ]);

        if ($validator->fails()) {
            // Devolver errores de validación con código 422 (Unprocessable Entity)
            return response()->json([
                'message' => 'Validation Failed',
                'errors' => $validator->errors()
            ], 422);
        }

        // 2. CREACIÓN DEL DOMINIO
        $validated = $validator->validated();

        $domain = new AllowedDomain();
        $domain->dominio = $validated['dominio'];
        $domain->descripcion = $validated['descripcion'] ?? null; // Usa null si no se envió
        $domain->activo = $validated['activo'] ?? true; // Por defecto, es activo (true)

        try {
            $domain->save();

            // 3. RESPUESTA DE ÉXITO
            return response()->json([
                'message' => 'Dominio guardado correctamente!',
                'domain' => $domain
            ], 201); // 201 Created
            
        } catch (\Exception $e) {
            // Manejo de errores de base de datos
            return response()->json([
                'message' => 'Error al guardar el dominio.',
                'error_detail' => $e->getMessage()
            ], 500);
        }
    }
}
