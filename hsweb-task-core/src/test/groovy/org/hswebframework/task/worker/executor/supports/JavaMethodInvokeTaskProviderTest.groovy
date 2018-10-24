package org.hswebframework.task.worker.executor.supports

import org.hswebframework.task.worker.executor.ExecuteContext

/**
 * @author zhouhao
 * @since 1.0.0
 * @see JavaMethodInvokeTaskProvider
 */
class JavaMethodInvokeTaskProviderTest extends spock.lang.Specification {

    def emptyContext = createMapContext([:])

    def createMapContext(map) {
        return new ExecuteContext() {
            @Override
            Map<String, Object> getParameters() {
                return map
            }

            @Override
            def getParameter(String key, Class type) {
                return type.cast(map.get(key));
            }
        }
    }

    def "测试调用静态方法"() {
        def provider = new JavaMethodInvokeTaskProvider();
        given:
        def callStatic = provider.build("org.hswebframework.task.worker.executor.supports.TestClass.staticJob");
        when:
        callStatic != null
        then:
        def result = callStatic.run(emptyContext);
        expect:
        result == true
    }

    def "测试调用void方法"() {
        def provider = new JavaMethodInvokeTaskProvider();
        provider.setInstanceGetter({ type -> type.newInstance() })
        given:
        def callStatic = provider.build("org.hswebframework.task.worker.executor.supports.TestClass.returnVoidJob");
        when:
        callStatic != null
        then:
        def result = callStatic.run(emptyContext);
        expect:
        result == null
    }

    def "测试调用private方法"() {
        def provider = new JavaMethodInvokeTaskProvider();
        provider.setInstanceGetter({ type -> type.newInstance() })
        given:
        def callStatic = provider.build("org.hswebframework.task.worker.executor.supports.TestClass.privateMethod");
        when:
        callStatic != null
        then:
        def result = callStatic.run(emptyContext);
        expect:
        result == true
    }

    def "测试调用static private方法"() {
        def provider = new JavaMethodInvokeTaskProvider();
        given:
        def callStatic = provider.build("org.hswebframework.task.worker.executor.supports.TestClass.privateStaticMethod");
        when:
        callStatic != null
        then:
        def result = callStatic.run(emptyContext);
        expect:
        result == null
    }

    def "测试调用带参数的方法"() {
        def provider = new JavaMethodInvokeTaskProvider();
        given:
        def callStatic = provider.build("org.hswebframework.task.worker.executor.supports.TestClass.parameterMethod");
        when:
        callStatic != null
        then:
        def result = callStatic.run(createMapContext(["0": "param1", "1": "param2"]));
        expect:
        result == "param1" + "param2"
    }

}
