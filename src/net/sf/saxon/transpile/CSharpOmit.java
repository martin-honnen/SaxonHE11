package net.sf.saxon.transpile;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a Java method to indicate that it is to be dropped from the C# generated code
 * (alternative to conditional exclusion using the preprocessor)
 */

@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD})

public @interface CSharpOmit {
    String code() default "";
}
