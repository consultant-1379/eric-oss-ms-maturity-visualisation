/*******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 ******************************************************************************/
package com.ericsson.oss.ci.domain.converter;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SetToStringConverterTest {
    private final SetToStringConverter converter = new SetToStringConverter();

    @Test
    void convertToDatabaseColumnNull() {
        assertNull(converter.convertToDatabaseColumn(null));
        assertNull(converter.convertToDatabaseColumn(Collections.emptySet()));
    }

    @Test
    void convertToDatabaseColumn() {
        final String expected = "A;B;C";
        final Set<String> set = new HashSet<>();
        set.add("A");
        set.add("B");
        set.add("C");
        assertEquals(expected, converter.convertToDatabaseColumn(set));
    }

    @Test
    void convertToEntityAttributeNull() {
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void convertToEntityAttribute() {
        final Set<String> expected = Set.of("A", "B", "C");
        final Set<String> result = converter.convertToEntityAttribute("A;B;C");
        assertEquals(expected.size(), result.size());
        assertTrue(CollectionUtils.isEqualCollection(expected, result));
    }
}