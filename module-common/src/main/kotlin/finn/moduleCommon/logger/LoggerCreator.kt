package finn.moduleCommon.logger

import org.slf4j.LoggerFactory

/**
 * abstract로 선언한 이유? => 사용하는 측에서 인스턴스로 생성하지 않게 방지하기 위한 일종의 편책(abstract를 인스턴스 생성이 아닌 companion object로 자연스럽게 접근하도록 유도)
 */
abstract class LoggerCreator {
    val log = LoggerFactory.getLogger(this.javaClass)!!
}