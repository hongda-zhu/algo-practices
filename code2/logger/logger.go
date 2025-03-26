package logger

import (
	"go.uber.org/zap"
	"go.uber.org/zap/zapcore"
)

var Logger *zap.Logger

func InitLogger() {
	Logger, _ = zap.NewProduction()
}

// DisableLogsForTests silences the logger during tests
func DisableLogsForTests() {
	config := zap.NewProductionConfig()
	config.Level = zap.NewAtomicLevelAt(zapcore.FatalLevel) // Only log fatal errors
	Logger, _ = config.Build()
}
