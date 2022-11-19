/*
 * Copyright (c) 2022 Institute of Software Chinese Academy of Sciences (ISCAS)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.eulix.platform.common.support.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The annotated element must be any of specified enum class values.
 * <p>
 * You need to provide a {@link ValueOfEnum#valueMethod()} to help get the values of
 * enum class which is also specified by {@link ValueOfEnum#enumClass()}.
 * By default, it will use {@link Enum#name()} as the value method. For the default
 * validation implementation, please refer to {@link ValueOfEnumValidator}.
 * <p>
 * Supported types are:
 * <ul>
 *     <li>all types from {@code java.lang.Enum<?>}</li>
 * </ul>
 * <p>
 * {@code null} elements are considered valid.
 *
 * @author Haibo Luo
 * @since 1.0
 */
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = ValueOfEnumValidator.class)
public @interface ValueOfEnum {
  /**
   * Used to specify enum class which must be derived from {@code java.lang.Enum<?>}.
   */
  Class<? extends Enum<?>> enumClass();
  String message() default "must be any of enum {enumClass} values";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};

  /**
   * Used to specify the value fetching method. By default, it
   * uses {@link Enum#name()}.
   */
  String valueMethod() default "name";
}