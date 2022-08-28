package net.sf.saxon.transpile;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a Java method with C# code that is to be injected literally into the body of the method
 * when converting to C#, replacing the Java method body.
 */

@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})

public @interface CSharpReplaceBody {
    String code() default "";
}
