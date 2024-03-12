package com.hubspot.horizon.internal;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;

@Documented
@Nonnull
@TypeQualifierDefault(value = ElementType.METHOD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface ReturnValuesAreNonnullByDefault {
}
