package com.aci.BinMasterCSM.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("file:./configuration/config.properties")
public class ExternalConfig {
}