package edu.ucsal.fiadopay.annotation.merchant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import edu.ucsal.fiadopay.model.Merchant.Status;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)

public @interface MerchantStatus {
  Status value() default Status.ACTIVE;
}
