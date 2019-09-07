package org.araqnid.hamkrest.json

import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.closeTo
import com.natpryce.hamkrest.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class StructureMatchersTest {
    @get:Rule
    val thrown: ExpectedException = ExpectedException.none()

    @Test
    fun `matches integer`() {
        assertThat("123", json(jsonInt(123)))
    }

    @Test
    fun `bytes matches integer`() {
        assertThat("123".toByteArray(),
                jsonBytes(jsonInt(123)))
    }

    @Test
    fun `matches integer with matcher`() {
        assertThat("123",
                json(jsonInt(equalTo(123))))
    }

    @Test
    fun `matches long`() {
        assertThat("123123123123",
                json(jsonLong(123123123123)))
    }

    @Test
    fun `matches long with matcher`() {
        assertThat("123123123123",
                json(jsonLong(equalTo(123123123123))))
    }

    @Test
    fun `matches double`() {
        assertThat("1.5", json(jsonDouble(1.5)))
    }

    @Test
    fun `matches double approximately`() {
        assertThat("1.5",
                json(jsonDouble(1.4, 0.2)))
    }

    @Test
    fun `matches double with matcher`() {
        assertThat("1.5",
                json(jsonDouble(closeTo(1.4, 0.2))))
    }

    @Test
    fun `rejects floating point when matching integer`() {
        assertThat("1.5", !json(jsonInt(1)))
    }

    @Test
    fun `matches integer as numeric`() {
        assertThat("1", json(jsonNumber(1)))
    }

    @Test
    fun `matches double as numeric with matcher`() {
        assertThat("1.5",
                json(jsonNumber(closeTo(1.5,
                        0.01))))
    }

    @Test
    fun `matches integer as numeric with double matcher`() {
        assertThat("1",
                json(jsonNumber(closeTo(1.0,
                        0.01))))
    }

    @Test
    fun `matches boolean`() {
        assertThat("true", json(jsonBoolean(true)))
    }

    @Test
    fun `matches boolean with matcher`() {
        assertThat("true",
                json(jsonBoolean(equalTo(true))))
    }

    @Test
    fun `matches string`() {
        assertThat("\"foo\"",
                json(jsonString("foo")))
    }

    @Test
    fun `matches string with matcher`() {
        assertThat("\"foo\"",
                json(jsonString(equalTo("foo"))))
    }

    @Test
    fun `rejects empty string JSON`() {
        assertThat("", !json(jsonAny()))
    }

    @Test
    fun `rejects laxly-formatted input`() {
        assertThat("{ a : 1 }", !json(jsonAny()))
    }

    @Test
    fun `matches null`() {
        assertThat("null", json(jsonNull()))
    }

    @Test
    fun `matches empty object`() {
        assertThat("{}", json(jsonObject()))
    }

    @Test
    fun `traps unexpected property by default`() {
        assertThat("""{"a":1}""", !json(jsonObject()))
    }

    @Test
    fun `allows unexpected property if requested`() {
        assertThat("""{"a":1}""",
                json(jsonObject().withAnyOtherProperties()))
    }

    @Test
    fun `matches property with integer value`() {
        assertThat("""{"a":1}""",
                json(jsonObject().withProperty("a",
                        1)))
    }

    @Test
    fun `matches property with string value`() {
        assertThat("""{"a":"foo"}""",
                json(jsonObject().withProperty("a",
                        "foo")))
    }

    @Test
    fun `matches property with boolean value`() {
        assertThat("""{"a":true}""",
                json(jsonObject().withProperty("a",
                        true)))
    }

    @Test
    fun `matches property with double value`() {
        assertThat("""{"a":1.0}""",
                json(jsonObject().withProperty("a",
                        1.0)))
    }

    @Test
    fun `matches property with null value using matcher`() {
        assertThat("""{"a":null}""",
                json(jsonObject().withProperty("a",
                        jsonNull())))
    }

    @Test
    fun `matches property with null value using JSON equivalence`() {
        assertThat("""{"a":{"aa":1}}""",
                json(jsonObject().withPropertyJSON("a",
                        """{ "aa" : 1 }""")))
    }

    @Test
    fun `matches properties in any order`() {
        assertThat("""{"a":1,"b":2}""", json(
                jsonObject().withProperty("a", 1).withProperty("b", 2)
                        and jsonObject().withProperty("b", 2).withProperty("a", 1)
        ))
    }

    @Test
    fun `matches empty array`() {
        assertThat("""[]""", json(jsonArray()))
    }

    @Test
    fun `rejects array with unexpected contents`() {
        assertThat("""[1]""", !json(jsonArray()))
    }

    @Test
    fun `matches array of integers`() {
        assertThat("""[1,2,3]""",
                json(jsonArray().of(jsonInt(
                        1),
                        jsonInt(2),
                        jsonInt(3))))
    }

    @Test
    fun `rejects array of integers out of order`() {
        assertThat("""[2,1,3]""", !json(jsonArray().of(
                jsonInt(1),
                jsonInt(2),
                jsonInt(3))))
    }

    @Test
    fun `rejects short array`() {
        assertThat("""[1,2]""", !json(jsonArray().of(
                jsonInt(1),
                jsonInt(2),
                jsonInt(3))))
    }

    @Test
    fun `rejects long array`() {
        assertThat("""[1,2,3,4]""", !json(jsonArray().of(
                jsonInt(1),
                jsonInt(2),
                jsonInt(3))))
    }

    @Test
    fun `matches array of integers in any order`() {
        assertThat("""[2,1,3]""",
                json(jsonArray().inAnyOrder(jsonInt(
                        1),
                        jsonInt(2),
                        jsonInt(3))))
    }

    @Test
    fun `rejects array of integers in any order with unmatched item`() {
        assertThat("""[2,1,4]""", !json(jsonArray().inAnyOrder(
                jsonInt(1),
                jsonInt(2),
                jsonInt(3))))
    }

    @Test
    fun `matches array containing at least specified items`() {
        assertThat("""[2,1,3]""", json(
                jsonArray().including(jsonInt(1))
                        and jsonArray().including(jsonInt(
                        2))
                        and jsonArray().including(jsonInt(
                        3))
        ))
    }

    @Test
    fun `prohibits specifying the same property multiple times`() {
        thrown.expect(IllegalArgumentException::class.java)
        jsonObject().withProperty("a", 1).withProperty("a", 1)
    }
}
