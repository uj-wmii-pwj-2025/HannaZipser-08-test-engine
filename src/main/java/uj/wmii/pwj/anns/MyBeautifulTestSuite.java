package uj.wmii.pwj.anns;

public class MyBeautifulTestSuite {

    @MyTest(
            params = {"5", "3"},
            expected = {"8"},
            type = int.class
    )
    public int add(int a, int b) {
        return a + b;
    }

    @MyTest(
            params = {"3.14", "2.86"},
            expected = {"6.0"},
            type = double.class,
            epsilon = 0.01
    )
    public double addDouble(double a, double b) {
        return a + b;
    }

    @MyTest(
            params = {"hello"},
            expected = {"HELLO"},
            type = String.class
    )
    public String toUpperCase(String s) {
        return s.toUpperCase();
    }

    @MyTest(
            params = {"5"},
            expected = {"true"},
            type = boolean.class
    )
    public boolean isPositive(int n) {
        return n > 0;
    }

    @MyTest(
            params = {"5"},
            expected = {"true"},
            type = boolean.class
    )
    public boolean withException(int a){
        throw new RuntimeException();
    }

    @MyTest(
            params = {"Hello World"},
            type = void.class
    )
    public void voidMethod(String msg) {
        msg += " ";
    }

    @MyTest(
            params = {"-5"},
            expected = {"true"},
            type = boolean.class
    )
    public boolean FailedTest(int n) {
        return n > 0;
    }

}

//EFFECT:
/*
==========================================
|           TEST ENGINE by Hania         |
==========================================

Testing class: uj.wmii.pwj.anns.MyBeautifulTestSuite

==================================================
Found 7 test method(s):
  1. add() - 1 test case(s)
  2. toUpperCase() - 1 test case(s)
  3. isPositive() - 1 test case(s)
  4. addDouble() - 1 test case(s)
  5. FailedTest() - 1 test case(s)
  6. voidMethod() - 1 test case(s)
  7. withException() - 1 test case(s)
==================================================

Running tests...

[PASS] add()
[PASS] toUpperCase()
[PASS] isPositive()
[PASS] addDouble()
[FAIL] FailedTest() -> false (expected: true)
[PASS] voidMethod()
[ERROR] withException() -> InvocationTargetException: null

==================================================
TEST SUMMARY
==================================================
Total tests:    7
PASSED:         5
FAILED:         1
ERRORS:         1
Success rate: 71.4%
==================================================
 */
