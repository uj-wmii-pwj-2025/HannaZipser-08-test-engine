package uj.wmii.pwj.anns;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MyTestEngine {

    private final String className;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please specify test class name");
            System.exit(-1);
        }
        String className = args[0].trim();
        printBanner(className);
        MyTestEngine engine = new MyTestEngine(className);
        engine.runTests();
    }

    public MyTestEngine(String className) {
        this.className = className;
    }

    public void runTests() {
        final Object unit = getObject(className);
        if (unit == null) {
            System.err.println("Failed to load test class");
            return;
        }

        List<Method> testMethods = getTestMethods(unit);

        if (testMethods.isEmpty()) {
            System.out.println("No test methods found");
            return;
        }

        printTestInfo(testMethods);
        System.out.println("\nRunning tests...\n");


        int passCount = 0;
        int failCount = 0;
        int errorCount = 0;

        for (Method m : testMethods) {
            TestResult result = launchSingleMethod(m, unit);
            switch (result) {
                case PASS:
                    passCount++;
                    break;
                case FAIL:
                    failCount++;
                    break;
                case ERROR:
                    errorCount++;
                    break;
            }
        }

        printSummary(testMethods.size(), passCount, failCount, errorCount);
    }

    private TestResult launchSingleMethod(Method m, Object unit) {
        MyTest annotation = m.getAnnotation(MyTest.class);
        String[] params = annotation.params();
        String[] expected = annotation.expected();
        Class<?> returnType = annotation.type();

        try {
            Object[] convertedParams = convertParameters(m, params);

            Object result = m.invoke(unit, convertedParams);

            if (returnType == void.class || expected.length == 0) {
                System.out.println("[PASS] " + m.getName() + "() ");
                return TestResult.PASS;
            }

            boolean matches = compareResults(result, expected[0], returnType, annotation.epsilon());

            if (matches) {
                System.out.printf("[PASS] %s()\n",
                        m.getName());
                return TestResult.PASS;
            } else {
                System.out.printf("[FAIL] %s() -> %s (expected: %s)\n",
                        m.getName(), result, expected[0]);
                return TestResult.FAIL;
            }

        } catch (Exception e) {
            System.out.printf("[ERROR] %s() -> %s: %s \n",
                    m.getName(), e.getClass().getSimpleName(), e.getMessage());
            return TestResult.ERROR;
        }
    }

    private static List<Method> getTestMethods(Object unit) {
        Method[] methods = unit.getClass().getDeclaredMethods();
        return Arrays.stream(methods).filter(
                m -> m.getAnnotation(MyTest.class) != null).collect(Collectors.toList());
    }

    private static Object getObject(String className) {
        try {
            Class<?> unitClass = Class.forName(className);
            return unitClass.getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return new Object();
        }
    }

    private Object[] convertParameters(Method m, String[] params) throws Exception {
        Class<?>[] paramTypes = m.getParameterTypes();
        Object[] converted = new Object[params.length];

        for (int i = 0; i < params.length; i++) {
            converted[i] = convertParameter(params[i], paramTypes[i]);
        }

        return converted;
    }

    private Object convertParameter(String value, Class<?> targetType) throws Exception {
        if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(value);
        } else if (targetType == double.class || targetType == Double.class) {
            return Double.parseDouble(value);
        } else if (targetType == float.class || targetType == Float.class) {
            return Float.parseFloat(value);
        } else if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(value);
        } else if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (targetType == String.class) {
            return value;
        } else if (targetType == char.class || targetType == Character.class) {
            return value.charAt(0);
        }
        return value;
    }

    private boolean compareResults(Object actual, String expected, Class<?> type, double epsilon) {
        if (actual == null) {
            return expected == null || expected.equals("null");
        }

        try {
            if (type == int.class || type == Integer.class) {
                return actual.equals(Integer.parseInt(expected));
            } else if (type == double.class || type == Double.class) {
                double actualVal = (Double) actual;
                double expectedVal = Double.parseDouble(expected);
                return Math.abs(actualVal - expectedVal) <= epsilon;
            } else if (type == float.class || type == Float.class) {
                float actualVal = (Float) actual;
                float expectedVal = Float.parseFloat(expected);
                return Math.abs(actualVal - expectedVal) <= epsilon;
            } else if (type == long.class || type == Long.class) {
                return actual.equals(Long.parseLong(expected));
            } else if (type == boolean.class || type == Boolean.class) {
                return actual.equals(Boolean.parseBoolean(expected));
            } else if (type == String.class) {
                return actual.equals(expected);
            } else if (type == char.class || type == Character.class) {
                return actual.equals(expected.charAt(0));
            }

            return String.valueOf(actual).equals(expected);

        } catch (Exception e) {
            return false;
        }
    }


    private void printSummary(int total, int pass, int fail, int errors) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("TEST SUMMARY");
        System.out.println("=".repeat(50));
        System.out.printf("Total tests:    %d\n", total);
        System.out.printf("PASSED:         %d \n", pass);
        System.out.printf("FAILED:         %d \n", fail);
        System.out.printf("ERRORS:         %d \n", errors);

        if (total > 0) {
            double successRate = (pass * 100.0) / total;
            System.out.printf("Success rate: %.1f%%\n", successRate);
        }
        System.out.println("=".repeat(50));

        if (errors == 0 && fail == 0) {
            System.out.println("\n All tests passed! ");
        }
    }

    private void printTestInfo(List<Method> testMethods) {
        System.out.println("\n" + "=".repeat(50));
        System.out.printf("Found %d test method(s):\n", testMethods.size());
        for (int i = 0; i < testMethods.size(); i++) {
            Method m = testMethods.get(i);
            MyTest annotation = m.getAnnotation(MyTest.class);
            int testCases = annotation.params().length > 0
                    ? annotation.params().length / Math.max(1, m.getParameterCount())
                    : 1;
            System.out.printf("  %d. %s() - %d test case(s)\n",
                    i + 1, m.getName(), testCases);
        }
        System.out.println("=".repeat(50));
    }

    private static void printBanner(String className) {
        System.out.println("==========================================");
        System.out.println("|           TEST ENGINE by Hania         |");
        System.out.println("==========================================");
        System.out.println();
        System.out.printf("Testing class: %s\n", className);
    }
}
