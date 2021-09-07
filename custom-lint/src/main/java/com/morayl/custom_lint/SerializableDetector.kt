/*
 * Copyright (C) 2020 The Android Open Source Project
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

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.*
import com.android.tools.lint.detector.api.Detector.UastScanner
import org.jetbrains.kotlin.asJava.elements.KtLightField
import org.jetbrains.uast.*
import org.jetbrains.uast.kotlin.KotlinUClass
import org.jetbrains.uast.kotlin.KotlinUField

/**
 * Sample detector showing how to analyze Kotlin/Java code. This example
 * flags all string literals in the code that contain the word "lint".
 */
@Suppress("UnstableApiUsage")
class SerializableDetector : Detector(), UastScanner {
    override fun getApplicableUastTypes(): List<Class<out UElement?>> {
        return listOf(UField::class.java, UParameter::class.java)
    }

    override fun createUastHandler(context: JavaContext): UElementHandler {
        // Note: Visiting UAST nodes is a pretty general purpose mechanism;
        // Lint has specialized support to do common things like "visit every class
        // that extends a given super class or implements a given interface", and
        // "visit every call site that calls a method by a given name" etc.
        // Take a careful look at UastScanner and the various existing lint check
        // implementations before doing things the "hard way".
        // Also be aware of context.getJavaEvaluator() which provides a lot of
        // utility functionality.
        return object : UElementHandler() {

            override fun visitParameter(node: UParameter) {
                print(node.text)
            }
            override fun visitField(node: UField) {
                if (node !is KotlinUField) return
                val isSerializable = node.getContainingUClass()?.uAnnotations?.find { it.qualifiedName == "kotlinx.serialization.Serializable" } != null
                println(node.getContainingUClass()?.uAnnotations?.getOrNull(0)?.qualifiedName)
                println(node.text)
                if (!isSerializable) {
                    return
                }
                println(node.uAnnotations.getOrNull(0)?.qualifiedName) // org.jetbrains.annotations.Nullable
                val isNullable = node.text.matches(Regex("va[lr]\\s+.*:\\s*\\w+\\?.*"))
                println(node.name + ":" + isNullable)
                if (isNullable) {
                    val isInitialized = node.text.matches(Regex("va[lr]\\s+.*:\\s*\\w+\\?[\\\\n\\s]*=.*"))
                    println(node.name + ":" + isInitialized)
                    if (!isInitialized) {
                        context.report(
                            ISSUE, node, context.getNameLocation(node),
                            "Should initialize nullable parameter in Kotlinx Serializable Class"
                        )
                    }
                }
            }
            override fun visitClass(node: UClass) {
                if (node !is KotlinUClass) return
                val isSerializable = node.uAnnotations.find { it.qualifiedName == "kotlinx.serialization.Serializable" } != null
                println(node.uAnnotations.getOrNull(0)?.qualifiedName)
                if (!isSerializable) {
                    return
                }

                val fields = node
                    .allFields
                    .filterIsInstance<KtLightField>()

                checkFields(node, fields)
            }

            private fun checkFields(node: KotlinUClass, fields: List<KtLightField>) {
                fields.forEach {
                    val isNullable = it.text.matches(Regex("va[lr]\\s+.*:\\s*\\w+\\?.*"))
                    println(it.name + ":" + isNullable)
                    if (isNullable) {
                        val isInitialized = it.text.matches(Regex("va[lr]\\s+.*:\\s*\\w+\\?[\\\\n\\s]*=.*"))
                        println(it.name + ":" + isInitialized)
                        if (!isInitialized) {
                            context.report(
                                ISSUE, node, context.getNameLocation(node),
                                "Should initialize nullable constructor argument parameter in Kotlinx Serializable Class"
                            )
                        }
                    }
                }
                println(fields)
            }

            override fun visitLiteralExpression(node: ULiteralExpression) {
                val psi = node.sourcePsi
                val string = node.evaluateString() ?: return
                if (string.contains("lint") && string.matches(Regex(".*\\blint\\b.*"))) {
                    context.report(
                        ISSUE, node, context.getLocation(node),
                        "This code mentions `lint`: **Congratulations**"
                    )
                }
            }
        }
    }

    companion object {
        /**
         * Issue describing the problem and pointing to the detector
         * implementation.
         */
        @JvmField
        val ISSUE: Issue = Issue.create(
            // ID: used in @SuppressLint warnings etc
            id = "SerializableNullableFieldInitialize",
            // Title -- shown in the IDE's preference dialog, as category headers in the
            // Analysis results window, etc
            briefDescription = "Lint Mentions",
            // Full explanation of the issue; you can use some markdown markup such as
            // `monospace`, *italic*, and **bold**.
            explanation = """
                    This check highlights string literals in code which mentions the word `lint`. \
                    Blah blah blah.

                    Another paragraph here.
                    """, // no need to .trimIndent(), lint does that automatically
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.WARNING,
            implementation = Implementation(
                SerializableDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }
}
