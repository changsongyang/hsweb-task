package org.hswebframework.task.worker.executor.supports;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class TestClass {

    public static boolean staticJob() {

        return true;
    }

    private static void privateStaticMethod() {

    }

    public void returnVoidJob() {

    }

    public Object parameterMethod(Object param1, Object param2) {

        return param1 + "" + param2;
    }

    private boolean privateMethod() {

        return true;
    }
}
