/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.morayl.custom_lint

import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

@Suppress("UnstableApiUsage")
class SerializableDetectorTest {
    @Test
    fun testData() {
        lint().allowMissingSdk().files(
            kotlin(
                """
                    package test.pkg

                    @Serializable
                    @Parcelable
                    data class TestClass1(
                        val s3: String? = null,
                        val s1: String,
                        val s2: String?,
                        val s4: String
                        = "aaa",
                    )
                    """
            ).indented()
        )
            .issues(SerializableDetector.ISSUE)
            .run()
            .expect(
                """
                    src/test/pkg/TestClass1.kt:5: Warning: This code mentions lint: Congratulations [ShortUniqueId]
                        private const val s2 : String = "Let's say it: lint"
                                                         ~~~~~~~~~~~~~~~~~~
                    0 errors, 1 warnings
                    """
            )
    }

//    @Test
//    fun testBasic() {
//        lint().allowMissingSdk().files(
//            kotlin(
//                """
//                    package test.pkg;
//                    class TestClass1 {
//                        // In a comment, mentioning "lint" has no effect
//                        private const val s1 : String = "Ignore non-word usages: linting"
//                        private const val s2 : String = "Let's say it: lint"
//                    }
//                    """
//            ).indented()
//        )
//            .issues(SampleCodeDetector.ISSUE)
//            .run()
//            .expect(
//                """
//                    src/test/pkg/TestClass1.kt:5: Warning: This code mentions lint: Congratulations [ShortUniqueId]
//                        private const val s2 : String = "Let's say it: lint"
//                                                         ~~~~~~~~~~~~~~~~~~
//                    0 errors, 1 warnings
//                    """
//            )
//    }
}
