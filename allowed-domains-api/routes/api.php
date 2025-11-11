<?php 

use App\Http\Controllers\DomainController;
use App\Http\Middleware\CheckApiToken;
use Illuminate\Support\Facades\Route;

// El prefijo '/api' es añadido automáticamente.
Route::middleware([CheckApiToken::class])->group(function () {
    
    // RUTA GET: Será accesible en /api/allowed-domains
    Route::get('allowed-domains', [DomainController::class, 'getDomains']);

    // RUTA POST: Será accesible en /api/save-domain
    Route::post('save-domain', [DomainController::class, 'saveDomain']);
});