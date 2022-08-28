package net.sf.saxon.transpile;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used by the Java-to-C# transpiler: it causes any exception named
 * in a catch clause within a Java method to be replaced by a different exception
 * in the generated C#
 */

@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})

public @interface CSharpReplaceException {
    String from();
    String to();
}
