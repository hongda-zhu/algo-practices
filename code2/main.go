package main

import (
	"net/http"
	"net/http/httputil"

	"github.com/gin-gonic/gin"
	_config "github.com/thematrix97/leakyloki/config"
	_logger "github.com/thematrix97/leakyloki/logger"
	_routes "github.com/thematrix97/leakyloki/routes"
	"go.uber.org/zap"
)

const (
	HEALTHCHECK_ENDPOINT = "/health"
)

var (
	config       *_config.Config
	reverseProxy *httputil.ReverseProxy
)

func main() {
	var err error
	_logger.InitLogger()
	defer _logger.Logger.Sync()

	config, err = _config.NewConfig()
	if err != nil {
		_logger.Logger.Fatal("failed to load config", zap.String("error", err.Error()))
	}
	config.Print()

	targetUrl, err := config.TargetUrl()
	if err != nil {
		_logger.Logger.Fatal("failed to parse target URL", zap.String("error", err.Error()))
	}

	reverseProxy = &httputil.ReverseProxy{
		Director: func(request *http.Request) {
			request.URL = targetUrl
			request.RequestURI = ""
			request.Host = targetUrl.Host
		},
	}

	router := gin.Default()
	v1 := router.Group("/v1")
	_routes.AddHealthRoutes(v1)
	_routes.AddGetRoutes(v1, reverseProxy, config)
	router.Run(":8080")
}
