package com.hubspot.horizon;

import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Nonnull
@TypeQualifierDefault(value = ElementType.METHOD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface ReturnValuesAreNonnullByDefault {
}
