package config

import (
	_ "embed"
	"fmt"
	"net/url"
	"strings"
	"time"

	"github.com/spf13/viper"
	_utils "github.com/thematrix97/leakyloki/utils"
)

//go:embed config.yaml
var defaultConfig string

// ConfigInterface defines the interface for configuration
type ConfigInterface interface {
	RateLimit(tenantId string) (int, error)
	MaxQueryRange(tenantId string) (time.Duration, error)
	MaxLookbackTime(tenantId string) (time.Duration, error)
	TargetUrl() (*url.URL, error)
	Print()
}

type LimitsConfig struct {
	MaxQueryRange   string `mapstructure:"max_query_range"`
	RateLimit       int    `mapstructure:"rate_limit"`
	MaxLookbackTime string `mapstructure:"max_lookback_time"`
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

func (config *Config) MaxLookbackTime(tenantId string) (time.Duration, error) {
	var r string
	if override, exists := config.Overrides[tenantId]; exists {
		r = override.MaxLookbackTime
	} else {
		r = config.LimitsConfig.MaxLookbackTime // Defaults MaxLookbackDays
	}
	return _utils.ParseCustomDuration(r)
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
