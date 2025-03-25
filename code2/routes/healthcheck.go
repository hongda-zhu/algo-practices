package routes

import (
	"net/http"

	"github.com/gin-gonic/gin"
)

const (
	HEALTHCHECK_ENDPOINT = "/health"
)

func AddHealthRoutes(rg *gin.RouterGroup) {
	ping := rg.Group(HEALTHCHECK_ENDPOINT)

	ping.GET("/", func(c *gin.Context) {
		c.JSON(http.StatusOK, "Alive!")
	})
}
