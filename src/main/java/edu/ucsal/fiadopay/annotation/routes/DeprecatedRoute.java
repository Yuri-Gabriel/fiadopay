package edu.ucsal.fiadopay.annotation.routes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//DeprecatedRoute - Marca rotas antigas para futuro descontinuação.

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface DeprecatedRoute {

    String replacedBy() default "";

    String sinceVersion() default "1.0";

    String removalVersion() default "UNDEFINED";

    String reason() default "Rota legada será descontinuada";

    boolean rejectRequests() default false;
}