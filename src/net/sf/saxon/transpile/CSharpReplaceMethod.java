package net.sf.saxon.transpile;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a Java method, constructor, or field with C# code that is to replace the entire Java method.
 */

@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR})

public @interface CSharpReplaceMethod {
    String code() default "";
}
