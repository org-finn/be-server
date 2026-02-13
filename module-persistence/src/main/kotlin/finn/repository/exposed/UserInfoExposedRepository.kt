package finn.repository.exposed

import finn.entity.UserInfoExposed
import finn.exception.CriticalDataPollutedException
import finn.exception.DomainPolicyViolationException
import finn.exception.NotFoundDataException
import finn.queryDto.FavoriteTickerQueryDto
import finn.table.OAuthUserTable
import finn.table.TickerTable
import finn.table.UserInfoTable
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.selectAll
import org.springframework.stereotype.Repository
import java.time.Clock
import java.time.LocalDateTime
import java.util.*

@Repository
class UserInfoExposedRepository(
    private val clock: Clock,
) {
    companion object {
        private val log = KotlinLogging.logger {}
    }

    fun save(
        oAuthUserId: UUID,
        nickname: String,
        imageUrl: String?,
        role: String,
        status: String
    ): UserInfoExposed {
        return UserInfoExposed.new {
            this.oauthUserId = oAuthUserId
            this.nickname = nickname
            this.imageUrl = imageUrl
            this.role = role
            this.status = status
            this.createdAt = LocalDateTime.now(clock)
            this.updatedAt = LocalDateTime.now(clock)
        }
    }

    fun findById(id: UUID): UserInfoExposed {
        return UserInfoExposed.findById(id)
            ?: run {
                log.error { "${id}의 userInfo가 존재하지 않습니다." }
                throw CriticalDataPollutedException("auth 관련 로직 중 문제가 발생하였습니다.")
            }
    }

    fun findByProviderId(providerId: String): UserInfoExposed? {
        val row = UserInfoTable.join(
            OAuthUserTable,
            JoinType.INNER,
            UserInfoTable.oauthUserId,
            OAuthUserTable.id
        )
            .selectAll()
            .where { OAuthUserTable.providerId eq providerId }
            .singleOrNull()

        return row?.let { UserInfoExposed.wrapRow(it) }
    }

    fun nonExistNickname(nickname: String): Boolean {
        return UserInfoTable
            .select(UserInfoTable.nickname)
            .where { UserInfoTable.nickname eq nickname }
            .empty()
    }

    fun updateNickname(nickname: String, userId: UUID) {
        if (!nonExistNickname(nickname)) { // 중복 선제 검사
            throw DomainPolicyViolationException("이미 존재하는 닉네임입니다.")
        }
        UserInfoExposed.findByIdAndUpdate(userId) {
            it.nickname = nickname
        } ?: throw NotFoundDataException("존재하지 않는 사용자입니다.")
    }

    fun findFavoriteTickersByUserId(userId: UUID): List<FavoriteTickerQueryDto> {
        val tickers = getFavoriteTickers(userId)

        if (tickers.isNullOrBlank()) {
            return emptyList()
        }

        val tickerCodes = tickers.split(",")

        return TickerTable.select(
            TickerTable.id,
            TickerTable.code,
            TickerTable.shortCompanyName
        )
            .where { TickerTable.code inList tickerCodes }
            .map { row ->
                FavoriteTickerQueryDto(
                    tickerId = row[TickerTable.id].value,
                    tickerCode = row[TickerTable.code],
                    shortCompanyName = row[TickerTable.shortCompanyName]
                )
            }
    }

    fun updateFavoriteTickers(userId: UUID, tickerCodes: List<String>) {
        UserInfoExposed.findByIdAndUpdate(userId) {
            it.favoriteTickers = tickerCodes.joinToString(",")
        }
    }

    fun addFavoriteTicker(userId: UUID, tickerCode: String) {
        val tickers = getFavoriteTickers(userId)

        val tickerList = mutableSetOf<String>()

        if (!tickers.isNullOrBlank()) {
            tickerList.addAll(tickers.split(","))
        }
        tickerList.add(tickerCode)

        UserInfoExposed.findByIdAndUpdate(userId) {
            it.favoriteTickers = tickerList.joinToString(",")
        }
    }

    fun removeFavoriteTicker(userId: UUID, tickerCode: String) {
        val tickers = getFavoriteTickers(userId)

        val tickerList = mutableSetOf<String>()

        if (!tickers.isNullOrBlank()) {
            tickerList.addAll(tickers.split(","))
        }
        tickerList.remove(tickerCode)

        UserInfoExposed.findByIdAndUpdate(userId) {
            it.favoriteTickers = tickerList.joinToString(",")
        }
    }

    fun delete(userId: UUID, status: String) {
        UserInfoExposed.findByIdAndUpdate(userId) {
            it.status = status // 탈퇴 상태로 변경
            it.deletedAt = LocalDateTime.now() // deletedAt 표시하고 추후 row 배치 삭제
        }
    }

    fun existFavorite(userId: UUID, tickerCodes: List<String>): Map<String, Boolean> {
        val favoriteTickers = getFavoriteTickers(userId)?.split(",")
        val map = mutableMapOf<String, Boolean>()
        // favoriteTickers에 존재하는 tickerCodes만 true
        tickerCodes.forEach {
            map[it] = favoriteTickers?.any { it2 ->
                it2 == it
            } ?: false

        }
        return map
    }

    private fun getFavoriteTickers(userId: UUID): String? {
        val tickers = UserInfoTable.select(UserInfoTable.favoriteTickers)
            .where { UserInfoTable.id eq userId }
            .map { it[UserInfoTable.favoriteTickers] }
            .singleOrNull()
        return tickers
    }
}