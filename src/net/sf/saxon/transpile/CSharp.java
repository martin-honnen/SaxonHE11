package net.sf.saxon.transpile;

/**
 * This class contains dummy methods which, if called, have no effect; but calls
 * on these methods are detected by the Java-to-C# converter and affect the C# code
 * that is generated.
 */

public class CSharp {

    /**
     * CSharp.emitCode("goto label") causes the converter to include
     * the code "goto label" in the generated C# code. The argument must
     * be supplied as a string literal. At run time, the method has no effect.
     * @param code the C# code to be emitted.
     */

    public static void emitCode(String code) {
        // do nothing at run time
    }
}

