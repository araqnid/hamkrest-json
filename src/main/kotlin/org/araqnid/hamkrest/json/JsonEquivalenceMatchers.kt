package org.araqnid.hamkrest.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.natpryce.hamkrest.MatchResult
import com.natpryce.hamkrest.Matcher
import java.io.IOException

private val strictMapper = ObjectMapper()

private val userInputMapper = ObjectMapper()
        .enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES)
        .enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES)
        .enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION)

fun equivalentJsonNode(expectedJsonSource: String): Matcher<JsonNode> =
        _equivalentTo(expectedJsonSource,
                userInputMapper::readTree,
                { it })

fun equivalentTo(expectedJsonSource: String): Matcher<String> =
        _equivalentTo(expectedJsonSource,
                userInputMapper::readTree,
                strictMapper::readTree)

fun equivalentTo(expectedJsonSource: ByteArray): Matcher<String> =
        _equivalentTo(expectedJsonSource,
                userInputMapper::readTree,
                strictMapper::readTree)

fun bytesEquivalentTo(expectedJsonSource: String): Matcher<ByteArray> =
        _equivalentTo(expectedJsonSource,
                userInputMapper::readTree,
                strictMapper::readTree)

fun bytesEquivalentTo(expectedJsonSource: ByteArray): Matcher<ByteArray> =
        _equivalentTo(expectedJsonSource,
                userInputMapper::readTree,
                strictMapper::readTree)

private fun <Expected, Actual> _equivalentTo(expectedJsonSource: Expected, expectedJsonParser: (Expected) -> JsonNode, actualJsonParser: (Actual) -> JsonNode) : Matcher<Actual> {
    val expectedJson = try {
        expectedJsonParser(expectedJsonSource)
    } catch (e: IOException) {
        throw IllegalArgumentException("Invalid reference JSON", e)
    }

    return object : Matcher.Primitive<Actual>() {
        override fun invoke(actual: Actual): MatchResult {
            val actualJson = try {
                actualJsonParser(actual)
            } catch (e: IOException) {
                return MatchResult.Mismatch("Invalid JSON: $e")
            }

            if (expectedJson == actualJson)
                return MatchResult.Match
            else
                return MatchResult.Mismatch("$actualJson")
        }

        override val description: String
            get() = "JSON $expectedJsonSource"
    }
}
