<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;
use Log;
use Symfony\Component\HttpFoundation\Response;

class CheckApiToken
{
    /**
     * Handle an incoming request.
     *
     * @param  \Closure(\Illuminate\Http\Request): (\Symfony\Component\HttpFoundation\Response)  $next
     */
    public function handle(Request $request, Closure $next): Response
    {
        if ($request->is('api/*')) {
            $tokenEsperado = config('api.secret') ?? 'NO-SET';
            $tokenRecibido = $request->bearerToken() ?? 'NO-SET';

            if (!$tokenRecibido || $tokenRecibido !== $tokenEsperado) {
                return response()->json([
                    'error' => 'Unauthorized',
                ], 401);
            }
        }

        return $next($request);
    }
}
