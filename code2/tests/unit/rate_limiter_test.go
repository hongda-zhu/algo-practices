package unit

import (
	"net/http"
	"net/http/httptest"
	"sync"
	"testing"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"
	_config "github.com/thematrix97/leakyloki/config"
	_middlewares "github.com/thematrix97/leakyloki/middlewares"
	_logger "github.com/thematrix97/leakyloki/logger"
)

func init() {
    _logger.InitLogger() 
}

// Estructura para simular la configuración
type MockRateConfig struct {
	rateLimit int
}

func (m *MockRateConfig) RateLimit(tenantId string) (int, error) {
	return m.rateLimit, nil
}

func TestRateLimiterMiddleware(t *testing.T) {
	// Configurar el modo de prueba para Gin
	gin.SetMode(gin.TestMode)

	tests := []struct {
		name            string
		rateLimit       int
		requestsToSend  int
		expectedSuccess int
		expectedFailure int
	}{
		{
			name:            "No rate limit",
			rateLimit:       -1,
			requestsToSend:  10,
			expectedSuccess: 10,
			expectedFailure: 0,
		},
		{
			name:            "Low rate limit",
			rateLimit:       3,
			requestsToSend:  10,
			expectedSuccess: 3,  // Primeras solicitudes deberían pasar
			expectedFailure: 7,  // El resto deberían fallar por exceder el límite
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			// Configurar middleware con mock config
			cfg := &MockRateConfig{rateLimit: tt.rateLimit}
			middleware := _middlewares.RateLimiterMiddleware(cfg)

			// Contadores para respuestas
			successCount := 0
			failureCount := 0

			// Crear un router Gin con el middleware
			router := gin.New()
			router.GET("/test", middleware, func(c *gin.Context) {
				c.Status(http.StatusOK)
			})

			// Enviar múltiples solicitudes rápidamente
			var wg sync.WaitGroup
			for i := 0; i < tt.requestsToSend; i++ {
				wg.Add(1)
				go func() {
					defer wg.Done()
					
					w := httptest.NewRecorder()
					req, _ := http.NewRequest("GET", "/test", nil)
					req.Header.Set(_config.TENANT_ID_HEADER_NAME, "test-tenant")
					
					router.ServeHTTP(w, req)
					
					if w.Code == http.StatusOK {
						successCount++
					} else if w.Code == http.StatusTooManyRequests {
						failureCount++
					}
				}()
			}
			
			// Esperar a que todas las goroutines terminen
			wg.Wait()
			
			// Verificar resultados
			assert.LessOrEqual(t, successCount, tt.expectedSuccess, "Demasiadas solicitudes exitosas")
			if tt.rateLimit > 0 {
				assert.GreaterOrEqual(t, failureCount, tt.expectedFailure, "No se detectaron suficientes solicitudes con exceso de límite")
			}
		})
	}
}

func TestMultipleTenants(t *testing.T) {
	// Configurar middleware con rate limit bajo
	cfg := &MockRateConfig{rateLimit: 2}
	middleware := _middlewares.RateLimiterMiddleware(cfg)
	
	// Configurar router
	gin.SetMode(gin.TestMode)
	router := gin.New()
	router.GET("/test", middleware, func(c *gin.Context) {
		c.Status(http.StatusOK)
	})
	

	// Probar con diferentes tenants
	tenant1Responses := []int{}
	tenant2Responses := []int{}

	// Enviar solicitudes para tenant1
	for i := 0; i < 3; i++ {
		w := httptest.NewRecorder()
		req, _ := http.NewRequest("GET", "/test", nil)
		req.Header.Set(_config.TENANT_ID_HEADER_NAME, "tenant1")
		router.ServeHTTP(w, req)
		tenant1Responses = append(tenant1Responses, w.Code)
		time.Sleep(10 * time.Millisecond)
	}

	// Enviar solicitudes para tenant2
	for i := 0; i < 3; i++ {
		w := httptest.NewRecorder()
		req, _ := http.NewRequest("GET", "/test", nil)
		req.Header.Set(_config.TENANT_ID_HEADER_NAME, "tenant2")
		router.ServeHTTP(w, req)
		tenant2Responses = append(tenant2Responses, w.Code)
		time.Sleep(10 * time.Millisecond)
	}

	// Verificar que cada tenant tiene su propio limitador
	assert.Contains(t, tenant1Responses, http.StatusOK, "Tenant1 debería tener al menos una solicitud exitosa")
	assert.Contains(t, tenant1Responses, http.StatusTooManyRequests, "Tenant1 debería tener al menos una solicitud rechazada")
	assert.Contains(t, tenant2Responses, http.StatusOK, "Tenant2 debería tener al menos una solicitud exitosa")
	assert.Contains(t, tenant2Responses, http.StatusTooManyRequests, "Tenant2 debería tener al menos una solicitud rechazada")
}