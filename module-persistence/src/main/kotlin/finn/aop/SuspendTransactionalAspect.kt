package finn.aop

import finn.transaction.SuspendExposedTransactional
import kotlinx.coroutines.Dispatchers
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transactionManager
import org.springframework.stereotype.Component
import java.sql.Connection

@Aspect
@Component
class SuspendTransactionalAspect {

    @Around("@within(finn.transaction.SuspendExposedTransactional) || @annotation(finn.transaction.SuspendExposedTransactional)")
    suspend fun manageSuspendTransaction(joinPoint: ProceedingJoinPoint): Any? {
        val signature = joinPoint.signature as MethodSignature
        val method = signature.method

        val transactionalAnnotation = method.getAnnotation(SuspendExposedTransactional::class.java)
            ?: joinPoint.target::class.java.getAnnotation(SuspendExposedTransactional::class.java)


        val isReadOnly = transactionalAnnotation?.readOnly ?: false

        val isolation =
            TransactionManager.currentOrNull()?.db?.transactionManager?.defaultIsolationLevel
                ?: Connection.TRANSACTION_READ_COMMITTED

        // 2. newSuspendedTransaction을 사용합니다.
        return newSuspendedTransaction(Dispatchers.IO, transactionIsolation = isolation) {
            // 2. 블록 내부에서 현재 트랜잭션의 readOnly 속성을 직접 설정합니다.
            if (isReadOnly) {
                this.connection.readOnly = true
            }

            // 3. proceed() 호출 자체가 suspend 호출이 됩니다.
            joinPoint.proceed()
        }
    }
}