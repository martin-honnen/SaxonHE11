package net.sf.saxon.transpile;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a Java interface to reference an additional interface with method definitions that
 * will be present in the C# product. The value of the key="" parameter must be the fully-qualified
 * name of an interface definition.
 */

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)

public @interface CSharpExtendInterface {
    String key() default "";
}
