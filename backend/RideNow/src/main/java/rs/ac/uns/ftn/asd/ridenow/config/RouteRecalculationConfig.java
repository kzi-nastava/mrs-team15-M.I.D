package rs.ac.uns.ftn.asd.ridenow.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rs.ac.uns.ftn.asd.ridenow.service.RouteRecalculationService;

@Configuration
public class RouteRecalculationConfig {

    private static final Logger logger = LoggerFactory.getLogger(RouteRecalculationConfig.class);

    @Autowired
    private RouteRecalculationService routeRecalculationService;

    @Value("${app.route.recalculation.enabled:true}")
    private boolean routeRecalculationEnabled;

    @Bean
    public ApplicationRunner recalculateAllRoutes() {
        return args -> {
            if (!routeRecalculationEnabled) {
                logger.info("Route recalculation is disabled by configuration");
                return;
            }

            logger.info("Starting route recalculation process...");
            try {
                routeRecalculationService.recalculateAllRoutes();
                logger.info("Route recalculation process completed!");
            } catch (Exception e) {
                logger.error("Route recalculation process failed: {}", e.getMessage());
            }
        };
    }
}
