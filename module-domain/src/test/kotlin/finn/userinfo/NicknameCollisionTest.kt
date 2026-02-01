import com.aventrix.jnanoid.jnanoid.NanoIdUtils
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.util.*

class NicknameCollisionTest : StringSpec({

    // 테스트할 닉네임 생성 함수 (제공해주신 코드)
    fun createUniqueNickname(): String {
        val alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray()
        return NanoIdUtils.randomNanoId(Random(), alphabet, 10)
    }

    "하루 2000명씩 2년 동안 가입해도(약 150만 건) 닉네임 충돌은 발생하지 않아야 한다" {
        // Given
        val usersPerDay = 2000
        val days = 365 * 2 // 2년
        val totalGenerations = usersPerDay * days // 1,460,000 건

        // 중복 체크를 위한 Set (O(1) 조회 성능)
        // 메모리 효율을 위해 초기 용량 설정
        val nicknameStore = HashSet<String>(totalGenerations)

        println("시뮬레이션 시작: 총 ${totalGenerations}개의 닉네임을 생성합니다...")
        val startTime = System.currentTimeMillis()

        // When
        var collisionCount = 0
        repeat(totalGenerations) {
            val nickname = createUniqueNickname()

            // add 메서드는 이미 존재하면 false를 반환함
            if (!nicknameStore.add(nickname)) {
                collisionCount++
                println("충돌 발생! (로또 맞을 확률): $nickname")
            }
        }

        val endTime = System.currentTimeMillis()
        println("시뮬레이션 종료. 소요 시간: ${(endTime - startTime)}ms")

        // Then
        // 1. 충돌 횟수는 0이어야 한다.
        collisionCount shouldBe 0

        // 2. Set에 저장된 개수가 시도 횟수와 정확히 일치해야 한다.
        nicknameStore.size shouldBe totalGenerations
    }
})