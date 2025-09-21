package finn.aop

import finn.transaction.SuspendExposedTransactional
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
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
    fun manageSuspendTransaction(joinPoint: ProceedingJoinPoint): Any? {
        val signature = joinPoint.signature as MethodSignature
        val method = signature.method

        val transactionalAnnotation = method.getAnnotation(SuspendExposedTransactional::class.java)
            ?: joinPoint.target::class.java.getAnnotation(SuspendExposedTransactional::class.java)


        val isReadOnly = transactionalAnnotation?.readOnly ?: false

        val isolation =
            TransactionManager.currentOrNull()?.db?.transactionManager?.defaultIsolationLevel
                ?: Connection.TRANSACTION_READ_COMMITTED

        // newSuspendedTransaction을 runBlocking(한 스레드를 블로킹)으로 감싸서 호출
        return runBlocking {
            newSuspendedTransaction(Dispatchers.IO, transactionIsolation = isolation) {
                // 블록 내부에서 현재 트랜잭션의 readOnly 속성을 직접 설정
                if (isReadOnly) {
                    this.connection.readOnly = true
                }
                joinPoint.proceed()
            }
        }
    }
}