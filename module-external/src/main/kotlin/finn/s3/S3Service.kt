package finn.s3

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import java.net.URL
import java.time.Duration
import java.util.*

@Service
class S3Service(
    private val s3Client: S3Client,
    private val s3Presigner: S3Presigner
) {
    companion object {
        private val log = KotlinLogging.logger {}
    }

    /**
     * 특정 티커에 대해 가장 최신 날짜 폴더에 있는 모든 객체에 대한
     * 시간과 Presigned URL의 맵을 반환합니다.
     * @param bucketName S3 버킷 이름
     * @param tickerId 조회할 티커의 UUID
     * @return Map<String, URL> (Key: "HH-MM-SS", Value: Presigned URL)
     */
    fun getLatestPresignedUrlsForTicker(bucketName: String, tickerId: UUID): Map<String, URL> {
        // 1. 버킷에서 가장 최신 날짜 폴더(prefix)를 자동으로 찾습니다. (예: "2025/09/02/")
        val latestDayPrefix = findLatestDayPrefix(bucketName)
            ?: run {
                log.warn { "No date folders found in bucket: $bucketName" }
                return emptyMap()
            }
        log.debug { "$latestDayPrefix!!" }

        // 2. 찾은 날짜 폴더와 tickerId를 조합하여 최종 경로를 만듭니다.
        val finalPrefix = "$latestDayPrefix$tickerId/"
        log.info { "Listing objects with final prefix: $finalPrefix" }

        // 3. 해당 경로의 모든 객체 목록을 가져옵니다.
        val listRequest = ListObjectsV2Request.builder()
            .bucket(bucketName)
            .prefix(finalPrefix)
            .build()

        // ⭐️ 크기가 0보다 큰 실제 파일만 필터링합니다.
        val objectsInFolder = s3Client.listObjectsV2(listRequest).contents()
            .filter { it.size() > 0 }

        if (objectsInFolder.isEmpty()) {
            log.warn { "No actual objects found for prefix: $finalPrefix" }
            return emptyMap()
        }

        // 4. 각 객체에 대해 (시간, Presigned URL) 쌍을 생성하고 맵으로 변환합니다.
        return objectsInFolder
            .associate { s3Object ->
                // 1. prefix에서 날짜 부분("YYYY/MM/DD/") 추출 후 "/"를 "-"로 변경
                // 예: "2025/09/01/" -> "2025-09-01"
                val datePart = latestDayPrefix.removeSuffix("/").replace("/", "-")

                // 2. 파일명에서 시간 부분("HH-MM-SS") 추출 후 "-"를 ":"로 변경
                // 예: "09-30-00_stock_prices.json" -> "09:30:00"
                val timePart = s3Object.key().split("/").last().split("_").first().replace("-", ":")

                // 3. 날짜와 시간을 합쳐 최종 키 생성
                // 예: "2025-09-01 09:30:00"
                val fullTimestampKey = "$datePart $timePart"

                val getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Object.key())
                    .build()

                val presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10)) // URL 유효 시간
                    .getObjectRequest(getObjectRequest)
                    .build()

                val presignedUrl = s3Presigner.presignGetObject(presignRequest).url()

                // fullTimestampKey를 Key로, presignedUrl을 Value로 하는 Pair 생성
                fullTimestampKey to presignedUrl
            }
    }

    /**
     * S3 Prefix를 사용하여 YYYY/MM/DD/ 형태의 가장 최신 경로를 재귀적으로 찾습니다.
     */
    private fun findLatestDayPrefix(
        bucketName: String,
        prefix: String = "",
        depth: Int = 0
    ): String? {
        // 재귀 깊이가 3에 도달하면, 현재 prefix가 YYYY/MM/DD/ 이므로 탐색을 종료하고 반환
        if (depth == 3) {
            return if (prefix.endsWith("/")) prefix else null
        }

        val listRequest =
            ListObjectsV2Request.builder().bucket(bucketName).prefix(prefix).delimiter("/").build()
        val listResponse = s3Client.listObjectsV2(listRequest)

        if (listResponse.hasCommonPrefixes()) {
            val latestSubFolder =
                listResponse.commonPrefixes().maxByOrNull { it.prefix() }?.prefix()
            return latestSubFolder?.let { findLatestDayPrefix(bucketName, it, depth + 1) }
        }

        return null
    }
}