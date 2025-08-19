package finn.aop

import finn.transaction.Transactional
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.transactionManager
import org.springframework.stereotype.Component
import java.sql.Connection

@Aspect
@Component
class TransactionalAspect {

    @Around("@annotation(transactional)")
    fun manageTransaction(
        joinPoint: ProceedingJoinPoint,
        transactional: Transactional
    ): Any? {
        val isReadOnly = transactional.readOnly

        // 1. 현재 데이터베이스의 기본 격리 수준을 가져옵니다.
        val isolation =
            TransactionManager.currentOrNull()?.db?.transactionManager?.defaultIsolationLevel
                ?: Connection.TRANSACTION_READ_COMMITTED // 기본값이 없는 경우를 대비한 안전 장치

        // 2. transactionIsolation과 readOnly를 함께 전달합니다.
        return transaction(transactionIsolation = isolation, readOnly = isReadOnly) {
            joinPoint.proceed()
        }
    }
}