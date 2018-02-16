package com.fisincorporated.exercisetracker.dagger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dagger.MapKey;

// https://github.com/Zorail/SubComponent/blob/master/app/src/main/java/zorail/rohan/com/subcomponent/Key/SubComponentKey.java
@MapKey
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SubComponentKey {
    Class<?> value();
}

