/*
 * Copyright 2014-2016 Peng Wan <phylame@163.com>
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

package pw.phylame.hongceicei

import java.io.BufferedReader
import java.io.InputStream
import java.io.Reader
import java.nio.charset.Charset

const val DEFAULT_ENCODING = "UTF-8"

fun InputStream.reader(encoding: String = DEFAULT_ENCODING): Reader =
        reader(Charset.forName(encoding))

fun InputStream.bufferedReader(encoding: String = DEFAULT_ENCODING): BufferedReader =
        bufferedReader(Charset.forName(encoding))
