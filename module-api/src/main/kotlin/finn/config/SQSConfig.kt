package finn.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import java.time.Duration

@Configuration
class SqsConfig {
    @Bean
    fun sqsAsyncClient(): SqsAsyncClient {
        return SqsAsyncClient.builder()
            .region(Region.AP_NORTHEAST_2)
            .httpClientBuilder(
                NettyNioAsyncHttpClient.builder()
                    .maxConcurrency(500)
                    .connectionAcquisitionTimeout(Duration.ofSeconds(60))
            )
            .build()
    }
}