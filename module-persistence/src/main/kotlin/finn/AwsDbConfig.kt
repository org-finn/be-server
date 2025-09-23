package finn.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient

@Configuration
class AwsDbConfig {

    @Bean
    fun dynamoDbAsyncClient(): DynamoDbAsyncClient {
        return DynamoDbAsyncClient.builder()
            .region(Region.AP_NORTHEAST_2) // 사용할 AWS 리전
            .credentialsProvider(DefaultCredentialsProvider.create()) // 자격 증명 자동 감지
            .build()
    }

}