package net.sf.saxon.transpile;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a Java class or interface to indicate whether the corresponding C# class should be implemented
 * as a delegate. By default, if there is an @FunctionalInterface annotation, then it becomes a delegate.
 */

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface CSharpDelegate {
    boolean value() default false;
}
