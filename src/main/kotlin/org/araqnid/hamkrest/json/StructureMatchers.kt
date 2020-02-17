package org.araqnid.hamkrest.json

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.DoubleNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.LongNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.NumericNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.natpryce.hamkrest.MatchResult
import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.anything
import com.natpryce.hamkrest.cast
import com.natpryce.hamkrest.closeTo
import com.natpryce.hamkrest.describe
import com.natpryce.hamkrest.equalTo
import java.io.IOException

private val mapper = ObjectMapper()
private fun parse(text: String): JsonNode? = mapper.readTree(text)
private fun parse(bytes: ByteArray): JsonNode? = mapper.readTree(bytes)

fun json(matcher: Matcher<JsonNode>): Matcher<String> {
    return object : Matcher.Primitive<String>() {
        override fun invoke(actual: String): MatchResult {
            val node: JsonNode = try {
                parse(actual)?.takeUnless { it.isMissingNode } ?: return MatchResult.Mismatch("Invalid JSON: $actual")
            } catch (e: IOException) {
                return MatchResult.Mismatch("Invalid JSON: $e")
            }
            return matcher(node)
        }

        override val description: String
            get() = "JSON ${describe(matcher)}"
    }
}

fun jsonBytes(matcher: Matcher<JsonNode>): Matcher<ByteArray> {
    return object : Matcher.Primitive<ByteArray>() {
        override fun invoke(actual: ByteArray): MatchResult {
            val node: JsonNode = try {
                parse(actual) ?: return MatchResult.Mismatch("Invalid JSON")
            } catch (e: IOException) {
                return MatchResult.Mismatch("Invalid JSON: $e")
            }
            return matcher(node)
        }

        override val description: String
            get() = "JSON ${describe(matcher)}"
    }
}

private fun MatchResult.prefixedWith(prefix: String): MatchResult {
    return if (this == MatchResult.Match)
        this
    else
        MatchResult.Mismatch("$prefix ${describe(this)}")
}

fun jsonAny(): Matcher<JsonNode> = anything
fun jsonNull(): Matcher<JsonNode> = equalTo(NullNode.instance)

fun jsonString(value: String) = jsonString(equalTo(value))
fun jsonString(valueMatcher: Matcher<String>): Matcher<JsonNode> {
    return cast(object : Matcher.Primitive<TextNode>() {
        override fun invoke(actual: TextNode): MatchResult {
            return valueMatcher(actual.asText()).prefixedWith("text value")
        }

        override val description: String
            get() = "text ${describe(valueMatcher)}"
    })
}

fun jsonNumber(value: Long) = jsonNumberLong(equalTo(value))
fun jsonNumber(value: Double) = jsonNumber(equalTo(value))
fun jsonNumberLong(valueMatcher: Matcher<Long>): Matcher<JsonNode> {
    return cast(object : Matcher.Primitive<NumericNode>() {
        override fun invoke(actual: NumericNode): MatchResult {
            return valueMatcher(actual.asLong()).prefixedWith("number value")
        }

        override val description: String
            get() = "number ${describe(valueMatcher)}"
    })
}
fun jsonNumber(valueMatcher: Matcher<Double>): Matcher<JsonNode> {
    return cast(object : Matcher.Primitive<NumericNode>() {
        override fun invoke(actual: NumericNode): MatchResult {
            return valueMatcher(actual.asDouble()).prefixedWith("number value")
        }

        override val description: String
            get() = "number ${describe(valueMatcher)}"
    })
}

fun jsonLong(n: Long) = jsonLong(equalTo(n))
fun jsonLong(matcher: Matcher<Long>): Matcher<JsonNode> {
    return cast(object : Matcher.Primitive<LongNode>() {
        override fun invoke(actual: LongNode): MatchResult {
            return matcher(actual.asLong()).prefixedWith("long value")
        }

        override val description: String
            get() = "long ${describe(matcher)}"
    })
}

fun jsonInt(n: Int) = jsonInt(equalTo(n))
fun jsonInt(matcher: Matcher<Int>): Matcher<JsonNode> {
    return cast(object : Matcher.Primitive<IntNode>() {
        override fun invoke(actual: IntNode): MatchResult {
            return matcher(actual.asInt()).prefixedWith("integer value")
        }

        override val description: String
            get() = "int ${describe(matcher)}"
    })
}

fun jsonDouble(n: Double) = jsonDouble(equalTo(n))
fun jsonDouble(n: Double, tolerance: Double) = jsonDouble(closeTo(n, tolerance))
fun jsonDouble(matcher: Matcher<Double>): Matcher<JsonNode> {
    return cast(object : Matcher.Primitive<DoubleNode>() {
        override fun invoke(actual: DoubleNode): MatchResult {
            return matcher(actual.asDouble()).prefixedWith("double value")
        }

        override val description: String
            get() = "double ${describe(matcher)}"
    })
}

fun jsonBoolean(n: Boolean) = jsonBoolean(equalTo(n))
fun jsonFalse() = jsonBoolean(false)
fun jsonTrue() = jsonBoolean(true)
fun jsonBoolean(matcher: Matcher<Boolean>): Matcher<JsonNode> {
    return cast(object : Matcher.Primitive<BooleanNode>() {
        override fun invoke(actual: BooleanNode): MatchResult {
            return matcher(actual.asBoolean()).prefixedWith("boolean value")
        }

        override val description: String
            get() = "boolean ${describe(matcher)}"
    })
}

fun jsonObject() = ObjectNodeMatcher()
fun jsonArray() = ArrayNodeMatcher()

class ObjectNodeMatcher(
        private val propertyMatchers: Map<String, Matcher<JsonNode>> = emptyMap(),
        private val failOnUnexpectedProperties: Boolean = true
) : Matcher.Primitive<JsonNode>() {

    override fun invoke(actual: JsonNode): MatchResult {
        if (actual !is ObjectNode)
            return MatchResult.Mismatch("not an object node: $actual")
        val remainingFieldNames = actual.fieldNames().asSequence().toMutableSet()
        propertyMatchers.entries.forEach { (name, matcher) ->
            if (!actual.has(name)) {
                return MatchResult.Mismatch("$name was not present")
            }
            val value: JsonNode = actual.get(name)
            val result = matcher(value)
            if (result != MatchResult.Match) {
                return result.prefixedWith("$name: ")
            }
            remainingFieldNames.remove(name)
        }

        if (failOnUnexpectedProperties && remainingFieldNames.isNotEmpty()) {
            return MatchResult.Mismatch("unexpected properties: $remainingFieldNames")
        }

        return MatchResult.Match
    }

    override val description: String
        get() {
            val parts = mutableListOf<String>()
            propertyMatchers.mapTo(parts) { (key, value) -> "$key: ${describe(value)}" }
            if (failOnUnexpectedProperties) parts += "/* others */"
            return parts.joinToString(", ", "{", "}")
        }

    fun withAnyOtherProperties() = ObjectNodeMatcher(propertyMatchers, false)

    fun <N : JsonNode> withProperty(key: String, matcher: Matcher<N>): ObjectNodeMatcher {
        require(!propertyMatchers.containsKey(key)) { "Property '$key' is specified multiple times" }
        @Suppress("UNCHECKED_CAST")
        return ObjectNodeMatcher(propertyMatchers + (key to matcher as Matcher<JsonNode>),
                failOnUnexpectedProperties)
    }

    fun withProperty(key: String, n: Int): ObjectNodeMatcher = withProperty(key,
            jsonInt(n))
    fun withProperty(key: String, n: Long): ObjectNodeMatcher = withProperty(key,
            jsonLong(n))
    fun withProperty(key: String, x: Double): ObjectNodeMatcher = withProperty(key,
            jsonDouble(x))
    fun withProperty(key: String, b: Boolean): ObjectNodeMatcher = withProperty(key,
            jsonBoolean(b))
    fun withProperty(key: String, str: String): ObjectNodeMatcher = withProperty(key,
            jsonString(str))
    fun withPropertyJSON(key: String, json: String): ObjectNodeMatcher = withProperty(key,
            equivalentJsonNode(json))
}

class ArrayNodeMatcher : Matcher.Primitive<JsonNode>() {
    override fun invoke(actual: JsonNode): MatchResult {
        if (actual !is ArrayNode) return MatchResult.Mismatch("not an array node: $actual")
        return if (actual.size() == 0) MatchResult.Match else MatchResult.Mismatch("array contains elements: $actual")
    }

    override val description: String
        get() = "empty array"

    fun of(vararg matchers: Matcher<JsonNode>): Matcher<JsonNode> {
        if (matchers.isEmpty())
            return this
        return cast(object : Matcher.Primitive<ArrayNode>() {
            override fun invoke(actual: ArrayNode): MatchResult {
                if (actual.size() != matchers.size) {
                    return MatchResult.Mismatch("array contains ${actual.size()} elements")
                }
                val matchList = matchers.toList().zip(actual.toList())
                matchList.forEachIndexed { index, (matcher, node) ->
                    val result = matcher(node)
                    if (result != MatchResult.Match) {
                        return MatchResult.Mismatch("$index: ${describe(result)}")
                    }
                }
                return MatchResult.Match
            }

            override val description: String
                get() = "array containing " + matchers.joinToString(", ") { describe(it) }
        })
    }

    fun inAnyOrder(vararg matchers: Matcher<JsonNode>): Matcher<JsonNode> {
        if (matchers.isEmpty())
            return this
        return cast(object : Matcher.Primitive<ArrayNode>() {
            override fun invoke(actual: ArrayNode): MatchResult {
                if (actual.size() != matchers.size) {
                    return MatchResult.Mismatch("array contains ${actual.size()} elements")
                }
                val remainingMatchers = matchers.toMutableList()
                actual.forEach { element ->
                    val matching = remainingMatchers.find { matcher -> matcher(element) == MatchResult.Match }
                    if (matching != null)
                        remainingMatchers.remove(matching)
                    else
                        return MatchResult.Mismatch("No match: ${describe(element)}")
                }
                return MatchResult.Match
            }

            override val description: String
                get() = "array containing " + matchers.joinToString(", ") { describe(it) } + " in any order"
        })
    }

    fun including(matcher: Matcher<JsonNode>): ArrayNodeIncluding {
        return ArrayNodeIncluding(matcher)
    }
}

class ArrayNodeIncluding(private val matcher: Matcher<JsonNode>) : Matcher.Primitive<JsonNode>() {
    override fun invoke(actual: JsonNode): MatchResult {
        if (actual !is ArrayNode) return MatchResult.Mismatch("not an array node: $actual")
        val matchResults = actual.toList().map { n -> matcher(n) }
        if (matchResults.contains(MatchResult.Match))
            return MatchResult.Match
        return MatchResult.Mismatch(matchResults.mapIndexed { index, matchResult -> "$index: ${describe(matchResult)}" }
                .joinToString("\n"))
    }

    override val description: String
        get() = "array containing ${describe(matcher)}"
}
