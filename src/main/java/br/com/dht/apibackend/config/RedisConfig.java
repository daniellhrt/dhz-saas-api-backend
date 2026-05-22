/**
 * Propósito: Configuração do cliente Redis para rate limiting distribuído.
 * Responsabilidade: Criar o ProxyManager Lettuce utilizado pelo Bucket4j para gerenciar buckets no Redis.
 * Papel na Arquitetura: Config / Infrastructure.
 */
package br.com.dht.apibackend.config;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {

    private static final Logger log = LoggerFactory.getLogger(RedisConfig.class);

    @Bean
    @ConditionalOnProperty(name = "spring.data.redis.host")
    public ProxyManager<byte[]> lettuceProxyManager(
            @Value("${spring.data.redis.host:localhost}") String host,
            @Value("${spring.data.redis.port:6379}") int port) {

        try {
            RedisURI redisUri = RedisURI.builder().withHost(host).withPort(port).build();
            RedisClient redisClient = RedisClient.create(redisUri);
            return LettuceBasedProxyManager.builderFor(redisClient).build();
        } catch (Exception e) {
            log.warn("Redis não disponível em {}:{}. Rate limiting usará fallback em memória.", host, port, e);
            return null;
        }
    }
}
