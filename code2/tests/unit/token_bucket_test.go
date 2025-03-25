package unit

import (
	"testing"
	"time"

	"github.com/stretchr/testify/assert"
	"github.com/thematrix97/leakyloki/limiter"
)

func TestTokenBucketLimiter(t *testing.T) {
	tests := []struct {
		name           string
		rateLimit      int
		volumeLimit    int64
		requestSize    int64
		requestCount   int
		expectedAllow  int
		expectedReject int
	}{
		{
			name:           "Basic rate limit",
			rateLimit:      5,
			volumeLimit:    0, // Sin límite de volumen
			requestSize:    100,
			requestCount:   10,
			expectedAllow:  5, // Debería permitir 5 (rateLimit)
			expectedReject: 5, // Debería rechazar el resto
		},
		{
			name:           "Volume limit",
			rateLimit:      10,
			volumeLimit:    500, // 500 bytes
			requestSize:    200, // Cada solicitud es de 200 bytes
			requestCount:   5,
			expectedAllow:  2, // Debería permitir solo 2 (volumeLimit/requestSize)
			expectedReject: 3, // Debería rechazar el resto
		},
		{
			name:           "No limits",
			rateLimit:      -1, // Sin límite de tasa
			volumeLimit:    0,  // Sin límite de volumen
			requestSize:    100,
			requestCount:   10,
			expectedAllow:  10, // Debería permitir todas
			expectedReject: 0,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			// Crear limitador con configuración de prueba
			limiter := limiter.NewTokenBucketLimiter(tt.rateLimit, tt.volumeLimit)

			// Contar solicitudes permitidas y rechazadas
			allowed := 0
			rejected := 0

			// Enviar solicitudes
			for i := 0; i < tt.requestCount; i++ {
				if limiter.Allow(tt.requestSize) {
					allowed++
				} else {
					rejected++
				}
			}

			// Verificar resultados
			assert.Equal(t, tt.expectedAllow, allowed, "Número incorrecto de solicitudes permitidas")
			assert.Equal(t, tt.expectedReject, rejected, "Número incorrecto de solicitudes rechazadas")
		})
	}
}

func TestTokenRefill(t *testing.T) {
	// Crear limitador con tasa baja
	limiter := limiter.NewTokenBucketLimiter(2, 0)

	// Agotar todos los tokens
	assert.True(t, limiter.Allow(10))
	assert.True(t, limiter.Allow(10))
	assert.False(t, limiter.Allow(10))

	// Esperar a que se rellenen tokens
	time.Sleep(1100 * time.Millisecond)

	// Debería haber al menos 2 nuevos tokens
	assert.True(t, limiter.Allow(10))
	assert.True(t, limiter.Allow(10))
	assert.False(t, limiter.Allow(10))
}
