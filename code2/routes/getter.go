package routes

import (
	"net/http/httputil"

	"github.com/gin-gonic/gin"
	_config "github.com/thematrix97/leakyloki/config"
	_middlewares "github.com/thematrix97/leakyloki/middlewares"
)

const (
	GET_ENDPOINT = "/loki/api/v1"
)

func AddGetRoutes(rg *gin.RouterGroup, rp *httputil.ReverseProxy, cfg *_config.Config) {
	api := rg.Group(GET_ENDPOINT, _middlewares.FlowLimiterMiddleware(cfg), _middlewares.RateLimiterMiddleware(cfg), _middlewares.RangeTimeLimiterMiddleware(cfg))

	/* Only Query + Range endopoints are considered */
	proxyResponse := func(c *gin.Context) {
		rp.ServeHTTP(c.Writer, c.Request)
	}

	api.GET("/query", proxyResponse)

	api.GET("/query_range", proxyResponse)
	
	// Endpoint para push de logs (entrada de datos)
	api.POST("/push", proxyResponse)
}
