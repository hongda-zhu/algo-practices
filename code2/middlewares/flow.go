package middlewares

import (
	"bytes"
	"io"
	"net/http"
	"sync"
	"time"

	"github.com/gin-gonic/gin"
	_config "github.com/thematrix97/leakyloki/config"
	_limiter "github.com/thematrix97/leakyloki/limiter"
	_logger "github.com/thematrix97/leakyloki/logger"
	"go.uber.org/zap"
)

// Almacena un limitador por tenant
var tenantFlowLimiters sync.Map

// FlowLimiterConfig define la interfaz necesaria para configurar el limitador de flujo
type FlowLimiterConfig interface {
	// RateLimit devuelve el límite de solicitudes por segundo para un tenant
	RateLimit(tenantId string) (int, error)
	
	// VolumeLimit devuelve el límite de bytes por segundo para un tenant (opcional)
	VolumeLimit(tenantId string) (int64, error)
}

// getTenantFlowLimiter obtiene o crea un limitador para un tenant específico
func getTenantFlowLimiter(tenant string, cfg FlowLimiterConfig) (*_limiter.TokenBucketLimiter, error) {
	if tenant == "" {
		tenant = "default"
	}
	
	// Si ya existe, devolverlo
	if limiter, ok := tenantFlowLimiters.Load(tenant); ok {
		return limiter.(*_limiter.TokenBucketLimiter), nil
	}
	
	// Obtener configuración para este tenant
	rateLimit, err := cfg.RateLimit(tenant)
	if err != nil {
		return nil, err
	}
	
	// Intentar obtener límite de volumen (si está disponible)
	var volumeLimit int64 = 0 // Por defecto sin límite de volumen
	if volumeLimitFunc, ok := cfg.(interface{ VolumeLimit(string) (int64, error) }); ok {
		if vl, err := volumeLimitFunc.VolumeLimit(tenant); err == nil {
			volumeLimit = vl
		}
	}
	
	// Crear nuevo limitador
	newLimiter := _limiter.NewTokenBucketLimiter(rateLimit, volumeLimit)
	tenantFlowLimiters.Store(tenant, newLimiter)
	
	return newLimiter, nil
}

// bodyReadCloser es un wrapper para poder leer el cuerpo de la solicitud múltiples veces
type bodyReadCloser struct {
	io.ReadCloser
	buffer []byte
}

func (b *bodyReadCloser) Read(p []byte) (n int, err error) {
	n, err = b.ReadCloser.Read(p)
	if n > 0 {
		b.buffer = append(b.buffer, p[:n]...)
	}
	return n, err
}

// FlowLimiterMiddleware implementa un middleware que limita tanto la tasa de solicitudes
// como el volumen de datos que puede enviar cada tenant
func FlowLimiterMiddleware(cfg FlowLimiterConfig) gin.HandlerFunc {
	return func(c *gin.Context) {
		tenantId := c.Request.Header.Get(_config.TENANT_ID_HEADER_NAME)
		
		// Obtener el limitador para este tenant
		limiter, err := getTenantFlowLimiter(tenantId, cfg)
		if err != nil {
			_logger.Logger.Error("Error al obtener limitador para tenant", 
				zap.String("tenant", tenantId), 
				zap.Error(err))
			c.Next()
			return
		}
		
		// Si es una solicitud con cuerpo (POST, PUT), estimar su tamaño
		var requestSize int64 = 100 // Tamaño mínimo por defecto (encabezados, etc.)
		
		if c.Request.ContentLength > 0 {
			requestSize += c.Request.ContentLength
		} else if c.Request.Body != nil {
			// Si ContentLength no está disponible pero hay cuerpo, leerlo para obtener el tamaño
			bodyBytes, _ := io.ReadAll(c.Request.Body)
			requestSize += int64(len(bodyBytes))
			
			// Restaurar el cuerpo para que pueda ser leído de nuevo
			c.Request.Body = io.NopCloser(bytes.NewBuffer(bodyBytes))
		}
		
		// Verificar si se permite esta solicitud
		if !limiter.Allow(requestSize) {
			// Aplicar backpressure - esperar y reintentar una vez
			time.Sleep(100 * time.Millisecond)
			
			if !limiter.Allow(requestSize) {
				c.JSON(http.StatusTooManyRequests, gin.H{
					"error": "Flow limit exceeded",
					"detail": "Request rate or volume limit exceeded for this tenant",
				})
				_logger.Logger.Info("Límite de flujo excedido", 
					zap.String("tenant", tenantId),
					zap.Int64("requestSize", requestSize))
				c.Abort()
				return
			}
		}
		
		// Continuar con la solicitud
		c.Next()
	}
}