package finn.transaction

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Transactional (
    val readOnly: Boolean = false
)
