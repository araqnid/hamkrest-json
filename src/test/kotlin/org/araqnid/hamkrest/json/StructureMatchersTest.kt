package org.araqnid.hamkrest.json

import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.closeTo
import com.natpryce.hamkrest.equalTo
import org.junit.Test

class StructureMatchersTest {
    @Test
    fun `matches integer`() {
        assert.that("123", json(jsonInt(123)))
    }

    @Test
    fun `bytes matches integer`() {
        assert.that("123".toByteArray(),
                jsonBytes(jsonInt(123)))
    }

    @Test
    fun `matches integer with matcher`() {
        assert.that("123",
                json(jsonInt(equalTo(123))))
    }

    @Test
    fun `matches long`() {
        assert.that("123123123123",
                json(jsonLong(123123123123)))
    }

    @Test
    fun `matches long with matcher`() {
        assert.that("123123123123",
                json(jsonLong(equalTo(123123123123))))
    }

    @Test
    fun `matches double`() {
        assert.that("1.5", json(jsonDouble(1.5)))
    }

    @Test
    fun `matches double approximately`() {
        assert.that("1.5",
                json(jsonDouble(1.4, 0.2)))
    }

    @Test
    fun `matches double with matcher`() {
        assert.that("1.5",
                json(jsonDouble(closeTo(1.4, 0.2))))
    }

    @Test
    fun `rejects floating point when matching integer`() {
        assert.that("1.5", !json(jsonInt(1)))
    }

    @Test
    fun `matches integer as numeric`() {
        assert.that("1", json(jsonNumber(1)))
    }

    @Test
    fun `matches double as numeric with matcher`() {
        assert.that("1.5",
                json(jsonNumber(closeTo(1.5,
                        0.01))))
    }

    @Test
    fun `matches integer as numeric with double matcher`() {
        assert.that("1",
                json(jsonNumber(closeTo(1.0,
                        0.01))))
    }

    @Test
    fun `matches boolean`() {
        assert.that("true", json(jsonBoolean(true)))
    }

    @Test
    fun `matches boolean with matcher`() {
        assert.that("true",
                json(jsonBoolean(equalTo(true))))
    }

    @Test
    fun `matches string`() {
        assert.that("\"foo\"",
                json(jsonString("foo")))
    }

    @Test
    fun `matches string with matcher`() {
        assert.that("\"foo\"",
                json(jsonString(equalTo("foo"))))
    }

    @Test
    fun `rejects empty string JSON`() {
        assert.that("", !json(jsonAny()))
    }

    @Test
    fun `rejects laxly-formatted input`() {
        assert.that("{ a : 1 }", !json(jsonAny()))
    }

    @Test
    fun `matches null`() {
        assert.that("null", json(jsonNull()))
    }

    @Test
    fun `matches empty object`() {
        assert.that("{}", json(jsonObject()))
    }

    @Test
    fun `traps unexpected property by default`() {
        assert.that("""{"a":1}""", !json(jsonObject()))
    }

    @Test
    fun `allows unexpected property if requested`() {
        assert.that("""{"a":1}""",
                json(jsonObject().withAnyOtherProperties()))
    }

    @Test
    fun `matches property with integer value`() {
        assert.that("""{"a":1}""",
                json(jsonObject().withProperty("a",
                        1)))
    }

    @Test
    fun `matches property with string value`() {
        assert.that("""{"a":"foo"}""",
                json(jsonObject().withProperty("a",
                        "foo")))
    }

    @Test
    fun `matches property with boolean value`() {
        assert.that("""{"a":true}""",
                json(jsonObject().withProperty("a",
                        true)))
    }

    @Test
    fun `matches property with double value`() {
        assert.that("""{"a":1.0}""",
                json(jsonObject().withProperty("a",
                        1.0)))
    }

    @Test
    fun `matches property with null value using matcher`() {
        assert.that("""{"a":null}""",
                json(jsonObject().withProperty("a",
                        jsonNull())))
    }

    @Test
    fun `matches property with null value using JSON equivalence`() {
        assert.that("""{"a":{"aa":1}}""",
                json(jsonObject().withPropertyJSON("a",
                        """{ "aa" : 1 }""")))
    }

    @Test
    fun `matches properties in any order`() {
        assert.that("""{"a":1,"b":2}""", json(
                jsonObject().withProperty("a", 1).withProperty("b", 2)
                        and jsonObject().withProperty("b", 2).withProperty("a", 1)
        ))
    }

    @Test
    fun `matches empty array`() {
        assert.that("""[]""", json(jsonArray()))
    }

    @Test
    fun `rejects array with unexpected contents`() {
        assert.that("""[1]""", !json(jsonArray()))
    }

    @Test
    fun `matches array of integers`() {
        assert.that("""[1,2,3]""",
                json(jsonArray().of(jsonInt(
                        1),
                        jsonInt(2),
                        jsonInt(3))))
    }

    @Test
    fun `rejects array of integers out of order`() {
        assert.that("""[2,1,3]""", !json(jsonArray().of(
                jsonInt(1),
                jsonInt(2),
                jsonInt(3))))
    }

    @Test
    fun `rejects short array`() {
        assert.that("""[1,2]""", !json(jsonArray().of(
                jsonInt(1),
                jsonInt(2),
                jsonInt(3))))
    }

    @Test
    fun `rejects long array`() {
        assert.that("""[1,2,3,4]""", !json(jsonArray().of(
                jsonInt(1),
                jsonInt(2),
                jsonInt(3))))
    }

    @Test
    fun `matches array of integers in any order`() {
        assert.that("""[2,1,3]""",
                json(jsonArray().inAnyOrder(jsonInt(
                        1),
                        jsonInt(2),
                        jsonInt(3))))
    }

    @Test
    fun `rejects array of integers in any order with unmatched item`() {
        assert.that("""[2,1,4]""", !json(jsonArray().inAnyOrder(
                jsonInt(1),
                jsonInt(2),
                jsonInt(3))))
    }

    @Test
    fun `matches array containing at least specified items`() {
        assert.that("""[2,1,3]""", json(
                jsonArray().including(jsonInt(1))
                        and jsonArray().including(jsonInt(
                        2))
                        and jsonArray().including(jsonInt(
                        3))
        ))
    }
}
