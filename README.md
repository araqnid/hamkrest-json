JSON matchers for Hamkrest
==========================

[ ![Kotlin](https://img.shields.io/badge/kotlin-1.4.32-blue.svg)](http://kotlinlang.org)
[![build](https://github.com/araqnid/hamkrest-json/actions/workflows/gradle-build.yml/badge.svg)](https://github.com/araqnid/hamkrest-json/actions/workflows/gradle-build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/org.araqnid.hamkrest/hamkrest-json.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.araqnid.hamkrest%22%20AND%20a%3A%22hamkrest-json%22)

[What is Hamkrest?](https://github.com/npryce/hamkrest)

These matchers are for matching JSON documents: they use Jackson to parse JSON bytes/text to an
AST, and can match against that.

Equivalence matchers
--------------------

Match some text as JSON:

```kotlin
// matches despite spaces
assertThat("""{"a":1,"b":2}""", equivalentTo("""{ "a" : 1, "b" : 2 }"""))
```

```kotlin
// matches despite fields in other order
assertThat("""{"b":2,"a":1}""", equivalentTo("""{ "a" : 1, "b" : 2 }"""))
```

```kotlin
// pattern specified in matcher can be written more loosely
assertThat("""{"a":1,"b":"foo"}""", equivalentTo("""{ a: 1, b: 'foo' }"""))
```

Match a sequence of bytes:

```kotlin
assertThat("""{"a":1,"b":2}""".toByteArray(), bytesEquivalentTo("""{ "a" : 1, "b" : 2 }"""))
```

Structural matchers
-------------------

Match AST nodes by specifying either the expected value or a matcher:

```kotlin
assertThat("""42""", json(jsonInt(42)))
assertThat("""42""", json(jsonInt(equalTo(42))))
assertThat(""""xyzzy"""", json(jsonString("xyzzy")))
assertThat(""""xyzzy"""", json(jsonString(anything)))
```

### Objects

Build matchers for objects:

```kotlin
assertThat("""{ "a": 1, "b": 2 }""", json(jsonObject()
  .withProperty("a", 1)
  .withProperty("b", 2)
))
```

By default an object matcher will mismatch if the input contains additional properties, but
they can be allowed:

```kotlin
assertThat("""{ "a": 1, "b": 2, "foo": "bar" }""", json(jsonObject()
  .withProperty("a", 1)
  .withProperty("b", 2)
  .withAnyOtherProperties()
))
```

In general, `withProperty` accepts either values as the second argument (and infers a
likely node type) or a matcher of `JsonNode` (such as `jsonInt()` etc or another `jsonObject()`)

Shorthand for switching to an equivalence matcher:

```kotlin
assertThat("""{ "a": { "b" : 1 } }""", json(jsonObject()
  .withPropertyJSON("a", """{ b : 1}""")
))
```

### Arrays

By default, `jsonArray()` will match only an empty array (change in future?)

Match contents exactly:

```kotlin
assertThat("""[1, 2, 3]""", json(jsonArray().of(jsonInt(1), jsonInt(2), jsonInt(3))))
```

Match any element:

```kotlin
assertThat("""[1, 2, 3]""", json(jsonArray().including(jsonInt(2))))
```

Match elements in any order:

```kotlin
assertThat("""[1, 2, 3]""", json(jsonArray().inAnyOrder(jsonInt(2), jsonInt(1), jsonInt(3))))
```

Get the library
---------------

Hamkrest-JSON is published on [Maven Central](https://search.maven.org). You need something like this in
`build.gradle` or `build.gradle.kts`:

```kotlin
repositories {
    mavenCentral()
}
dependencies {
    testImplementation("org.araqnid.hamkrest:hamkrest-json:1.1.2")
}
```
