package limiter

import (
	"sync"
	"time"
)

// TokenBucketLimiter implementa un limitador de tasa basado en el algoritmo de cubo de tokens
// que permite controlar tanto solicitudes por segundo como volumen de datos por segundo
type TokenBucketLimiter struct {
	rateLimit       int       // Cantidad máxima de tokens por segundo
	currentTokens   int       // Tokens actuales en el cubo
	maxTokens       int       // Capacidad máxima de tokens
	lastRefillTime  time.Time // Última vez que se rellenaron tokens
	volumeLimit     int64     // Límite de bytes por segundo
	currentVolume   int64     // Volumen actual utilizado
	lastVolumeReset time.Time // Última vez que se reinició el contador de volumen
	mu              sync.Mutex
}

// NewTokenBucketLimiter crea un nuevo limitador con los límites especificados
// rateLimit es el número de solicitudes por segundo
// volumeLimit es el número de bytes por segundo (0 significa sin límite de volumen)
func NewTokenBucketLimiter(rateLimit int, volumeLimit int64) *TokenBucketLimiter {
	now := time.Now()
	return &TokenBucketLimiter{
		rateLimit:       rateLimit,
		currentTokens:   rateLimit, // Comenzar con cubo lleno
		maxTokens:       rateLimit,
		lastRefillTime:  now,
		volumeLimit:     volumeLimit,
		currentVolume:   0,
		lastVolumeReset: now,
	}
}

// Allow verifica si una solicitud con el volumen dado puede ser permitida
// Devuelve true si se permite, false si debe ser limitada
func (l *TokenBucketLimiter) Allow(requestSize int64) bool {
	// Si el rate limit está desactivado, permitir todo
	if l.rateLimit <= 0 {
		return true
	}

	l.mu.Lock()
	defer l.mu.Unlock()

	now := time.Now()

	// Rellenar tokens basados en el tiempo transcurrido
	l.refillTokens(now)

	// Reiniciar contador de volumen si ha pasado un segundo
	if now.Sub(l.lastVolumeReset) >= time.Second {
		l.currentVolume = 0
		l.lastVolumeReset = now
	}

	// Verificar límite de volumen si está activado
	if l.volumeLimit > 0 {
		if l.currentVolume+requestSize > l.volumeLimit {
			return false
		}
	}

	// Verificar si hay suficientes tokens
	if l.currentTokens <= 0 {
		return false
	}

	// Consumir un token y actualizar volumen
	l.currentTokens--
	l.currentVolume += requestSize

	return true
}

// refillTokens rellena los tokens en el cubo basado en el tiempo transcurrido
func (l *TokenBucketLimiter) refillTokens(now time.Time) {
	elapsed := now.Sub(l.lastRefillTime)
	
	// Calcular cuántos tokens rellenar basado en el tiempo transcurrido
	tokensToAdd := int(elapsed.Seconds() * float64(l.rateLimit))
	
	if tokensToAdd > 0 {
		l.currentTokens = min(l.maxTokens, l.currentTokens+tokensToAdd)
		l.lastRefillTime = now
	}
}

// min devuelve el mínimo de dos enteros
func min(a, b int) int {
	if a < b {
		return a
	}
	return b
}