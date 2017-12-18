package io.buck.parser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by buck on 2017/12/18
 */

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface Message {
    String value();
}
