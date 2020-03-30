/*
 * MIT License
 *
 * Copyright (c) 2020 Artipie
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.artipie.http.hm;

import com.artipie.http.Response;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.rs.RsWithBody;
import com.artipie.http.rs.RsWithHeaders;
import com.artipie.http.rs.RsWithStatus;
import io.reactivex.Flowable;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.cactoos.map.MapEntry;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests for {@link RsHasHeaders}.
 *
 * @since 0.8
 */
class RsHasHeadersTest {

    @Test
    void shouldMatchHeaders() {
        final MapEntry<String, String> type = new MapEntry<>(
            "Content-Type", "application/json"
        );
        final MapEntry<String, String> length = new MapEntry<>(
            "Content-Length", "123"
        );
        final Response response = new RsWithHeaders(
            new RsWithStatus(RsStatus.OK),
            Arrays.asList(type, length)
        );
        final RsHasHeaders matcher = new RsHasHeaders(Arrays.asList(length, type));
        MatcherAssert.assertThat(
            matcher.matches(response),
            new IsEqual<>(true)
        );
    }

    @Test
    void shouldNotMatchNotMatchingHeaders() {
        final Response response = new RsWithStatus(RsStatus.OK);
        final RsHasHeaders matcher = new RsHasHeaders(
            Matchers.containsInAnyOrder(new MapEntry<>("X-My-Header", "value"))
        );
        MatcherAssert.assertThat(
            matcher.matches(response),
            new IsEqual<>(false)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"data", "chunk1,chunk2"})
    void shouldMatchResponseTwice(final String chunks) {
        final String[] elements = chunks.split(",");
        final byte[] data = String.join("", elements).getBytes();
        final Response response = new RsWithBody(
            Flowable.fromIterable(
                Stream.of(elements)
                    .map(String::getBytes)
                    .map(ByteBuffer::wrap)
                    .collect(Collectors.toList())
            )
        );
        new RsHasBody(data).matches(response);
        MatcherAssert.assertThat(
            new RsHasBody(data).matches(response),
            new IsEqual<>(true)
        );
    }
}
