package com.mongoplus.aggregate;

import org.junit.Assert;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** 使用当前 JDK 验证真实绑定；显式的 Java 8 API 测试兼容旧版编译器参数。 */
final class JavaCompilerTestSupport {
    private JavaCompilerTestSupport() {
    }

    static List<String> java8Options(String... options) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        Assert.assertNotNull("动态编译测试需要完整 JDK 8 或以上版本", compiler);
        List<String> result = new ArrayList<>();
        if (compiler.isSupportedOption("--release") >= 0) {
            result.addAll(Arrays.asList("--release", "8"));
        } else {
            result.addAll(Arrays.asList("-source", "8", "-target", "8"));
        }
        result.addAll(Arrays.asList(options));
        return result;
    }

    static List<String> currentJdkOptions(String... options) {
        Assert.assertNotNull("动态编译测试需要完整 JDK 8 或以上版本", ToolProvider.getSystemJavaCompiler());
        return new ArrayList<>(Arrays.asList(options));
    }
}
