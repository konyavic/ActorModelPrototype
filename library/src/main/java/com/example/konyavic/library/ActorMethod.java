package com.example.konyavic.library;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) // on method level
@Retention(RetentionPolicy.SOURCE) // not needed at runtime
public @interface ActorMethod {
}
