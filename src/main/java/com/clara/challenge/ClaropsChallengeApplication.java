package com.clara.challenge;

import com.clara.challenge.config.SafeguardProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(SafeguardProperties.class)
public class ClaropsChallengeApplication {

  public static void main(String[] args) {
    SpringApplication.run(ClaropsChallengeApplication.class, args);
  }
}
