package org.hswebframework.task.utils

import spock.lang.Specification

/**
 * @author zhouhao
 * @since 1.0.0
 */
class IdUtilsTest extends Specification {

    def "测试ID生成"() {
        given:
        def id = IdUtils.newUUID();
        expect:
        id != null
        id.length() <= 32
        !id.contains("-")
    }
}
