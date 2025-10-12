package gymbo.gymbo.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Validated
@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "telegram.bot")
public class BotConfig {
    private String token;
}
