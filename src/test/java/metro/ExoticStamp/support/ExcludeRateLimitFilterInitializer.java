package metro.ExoticStamp.support;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Removes RateLimitFilter bean definitions so SpringBoot ITs can load a context.
 * <p>
 * Production {@code SecurityConfig} calls {@code addFilterBefore(rateLimit, JwtAuthFilter.class)}
 * before {@code JwtAuthFilter} is registered on the chain, which throws
 * {@code IllegalArgumentException: JwtAuthFilter does not have a registered order}.
 * That is a production startup defect (reported in Batch B.1); this initializer is a
 * test-only workaround and must not be treated as a security policy change.
 */
public class ExcludeRateLimitFilterInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        applicationContext.addBeanFactoryPostProcessor(this::removeRateLimitFilterBeans);
    }

    private void removeRateLimitFilterBeans(ConfigurableListableBeanFactory beanFactory) {
        if (!(beanFactory instanceof BeanDefinitionRegistry registry)) {
            return;
        }
        removeIfPresent(registry, "rateLimitFilter");
        removeIfPresent(registry, "rateLimitFilterRegistration");
    }

    private static void removeIfPresent(BeanDefinitionRegistry registry, String name) {
        if (registry.containsBeanDefinition(name)) {
            registry.removeBeanDefinition(name);
        }
    }
}
