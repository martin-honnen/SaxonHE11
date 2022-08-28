package net.sf.saxon.transpile;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a Java class or interface with C# code that is to be injected literally into the body of the class
 * or interface when converting to C#.
 */

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)

public @interface CSharpInjectMembers {
    String[] code() default "";
}
