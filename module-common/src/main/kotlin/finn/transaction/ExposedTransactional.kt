package finn.transaction

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ExposedTransactional (
    val readOnly: Boolean = false
)
