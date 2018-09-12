/*
 * Copyright 2017 Mediative
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

package rocks.muki.graphql

package object codegen {

  /**
    * Type alias for a processing result during a code generation step
    * @tparam T the success type
    */
  type Result[T] = Either[Failure, T]

  /**
    * Type alias for a graphql file pre-processing function.
    * _ The input is the raw graphql file content
    * _ The output is the transformed graphql file content or an error
    */
  type PreProcessor = String => Result[String]
}
