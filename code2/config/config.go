package config

import (
	_ "embed"
	"fmt"
	"net/url"
	"strconv"
	"strings"
	"time"

	"github.com/spf13/viper"
	_utils "github.com/thematrix97/leakyloki/utils"
)

//go:embed config.yaml
var defaultConfig string

type LimitsConfig struct {
	MaxQueryRange   string `mapstructure:"max_query_range"`
	RateLimit       int    `mapstructure:"rate_limit"`
	MaxLookbackTime string `mapstructure:"max_lookback_time"`
	VolumeLimit     string `mapstructure:"volume_limit"`
}

type Config struct {
	Target struct {
		Url string `mapstructure:"url"`
	} `mapstructure:"target"`
	LimitsConfig LimitsConfig            `mapstructure:"limits_config"`
	Overrides    map[string]LimitsConfig `mapstructure:"overrides"`
}

func NewConfig() (*Config, error) {
	var config Config
	var err error

	viper.AutomaticEnv()
	// these will resolve to config/config.yaml
	viper.SetConfigType("yaml")
	viper.SetConfigName("config")
	viper.AddConfigPath("config")
	// end
	viper.SetEnvKeyReplacer(strings.NewReplacer(".", "_"))
	viper.ReadConfig(strings.NewReader(defaultConfig))

	if err = viper.MergeInConfig(); err != nil {
		if _, ok := err.(viper.ConfigFileNotFoundError); !ok {
			return nil, err
		}
	}

	if err := viper.Unmarshal(&config); err != nil {
		return nil, err
	}

	return &config, nil
}

func (config *Config) Print() {
	fmt.Printf("config: %+v\n", config)
}

func (config *Config) TargetUrl() (*url.URL, error) {
	var err error

	var targetUrl *url.URL
	if targetUrl, err = url.Parse(config.Target.Url); err != nil {
		return nil, err
	}

	return targetUrl, nil
}

func (config *Config) RateLimit(tenantId string) (int, error) {
	if override, exists := config.Overrides[tenantId]; exists {
		return override.RateLimit, nil
	}
	return config.LimitsConfig.RateLimit, nil //Default limits override
}

func (config *Config) MaxQueryRange(tenantId string) (time.Duration, error) {
	var r string
	if override, exists := config.Overrides[tenantId]; exists {
		r = override.MaxQueryRange
	} else {
		r = config.LimitsConfig.MaxQueryRange // Defaults maxquery range
	}
	return _utils.ParseCustomDuration(r)
}

func (config *Config) MaxLookbackTime(tenantId string) (time.Duration, error) {
	var r string
	if override, exists := config.Overrides[tenantId]; exists {
		r = override.MaxLookbackTime
	} else {
		r = config.LimitsConfig.MaxLookbackTime // Defaults MaxLookbackDays
	}
	return _utils.ParseCustomDuration(r)
}

// VolumeLimit devuelve el límite de volumen para un tenant específico en bytes por segundo
func (config *Config) VolumeLimit(tenantId string) (int64, error) {
	var volumeStr string
	
	// Verificar si hay override para este tenant
	if override, exists := config.Overrides[tenantId]; exists {
		volumeStr = override.VolumeLimit
	} else {
		volumeStr = config.LimitsConfig.VolumeLimit
	}
	
	return ParseVolumeLimit(volumeStr)
}

// ParseVolumeLimit convierte una string de límite de volumen a bytes por segundo
// Formatos aceptados: "500B", "1.5MB", "10KB", etc.
func ParseVolumeLimit(volumeStr string) (int64, error) {
	if volumeStr == "" {
		return 0, nil // Sin límite de volumen
	}
	
	// Determinar unidad
	var unit int64 = 1 // Bytes
	var valueStr string
	
	if strings.HasSuffix(volumeStr, "MB") {
		unit = 1024 * 1024
		valueStr = volumeStr[:len(volumeStr)-2]
	} else if strings.HasSuffix(volumeStr, "KB") {
		unit = 1024
		valueStr = volumeStr[:len(volumeStr)-2]
	} else if strings.HasSuffix(volumeStr, "B") {
		unit = 1
		valueStr = volumeStr[:len(volumeStr)-1]
	} else {
		// Asumir bytes si no se especifica unidad
		valueStr = volumeStr
	}
	
	// Soportar valores decimales
	if strings.Contains(valueStr, ".") {
		floatVal, err := strconv.ParseFloat(valueStr, 64)
		if err != nil {
			return 0, err
		}
		return int64(floatVal * float64(unit)), nil
	}
	
	// Procesar valor entero
	intVal, err := strconv.ParseInt(valueStr, 10, 64)
	if err != nil {
		return 0, err
	}
	
	return intVal * unit, nil
}