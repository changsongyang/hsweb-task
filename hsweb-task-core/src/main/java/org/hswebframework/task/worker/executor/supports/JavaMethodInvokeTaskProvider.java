package org.hswebframework.task.worker.executor.supports;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Slf4j
public class JavaMethodInvokeTaskProvider implements RunnableTaskBuilderProvider {
    private ClassLoader classLoader;

    private Function<Class, Object> instanceGetter = (clazz) -> {

        try {
            return clazz.newInstance();
        } catch (Exception e) {
            log.error("can not new instance for {}", clazz);
        }
        return null;
    };

    public JavaMethodInvokeTaskProvider() {
        this(JavaMethodInvokeTaskProvider.class.getClassLoader());
    }

    public JavaMethodInvokeTaskProvider(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void setInstanceGetter(Function<Class, Object> instanceGetter) {
        this.instanceGetter = instanceGetter;
    }

    @Override
    public String getSupportTaskType() {
        return "java-method";
    }

    @Override
    @SuppressWarnings("all")
    public TaskRunner build(String content) throws Exception {
        String className = content.substring(0, content.lastIndexOf("."));
        String methodName = content.substring(content.lastIndexOf(".") + 1);
        Class clazz = classLoader.loadClass(className);
        Method method;
        try {
            method = clazz.getMethod(methodName);
        } catch (Exception e) {
            try {
                method = clazz.getDeclaredMethod(methodName);
            } catch (Exception e2) {
                method = Stream.concat(Stream.of(clazz.getMethods()), Stream.of(clazz.getDeclaredMethods()))
                        .filter(m -> m.getName().equals(methodName))
                        .findFirst()
                        .orElseThrow(() -> new NoSuchMethodException(content));
            }
        }

        if (!Modifier.isPublic(method.getModifiers())) {
            method.setAccessible(true);
        }
        Object instance = Modifier.isStatic(method.getModifiers()) ? null : instanceGetter.apply(clazz);
        Method finaleMethod = method;
        int parameterCount = method.getParameterCount();
        Object[] invokeParameter = new Object[parameterCount];
        Class[] methodTypes = method.getParameterTypes();
        return (context) -> {
            for (int i = 0; i < parameterCount; i++) {
                invokeParameter[i] = context.getParameter(String.valueOf(i), methodTypes[i]);
            }
            return finaleMethod.invoke(instance, (Object[]) invokeParameter);
        };

    }
}
