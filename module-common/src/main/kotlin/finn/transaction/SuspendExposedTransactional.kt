package finn.transaction

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class SuspendExposedTransactional(
    val readOnly: Boolean = false
)
