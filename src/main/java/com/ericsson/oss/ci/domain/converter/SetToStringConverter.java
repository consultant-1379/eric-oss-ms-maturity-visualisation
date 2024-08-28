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

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Set;

@Converter
public class SetToStringConverter implements AttributeConverter<Set<String>, String> {
    private static final String SEPARATOR = ";";

    @Override
    public String convertToDatabaseColumn(final Set<String> attribute) {
        return CollectionUtils.isEmpty(attribute) ? null : String.join(SEPARATOR, attribute);
    }

    @Override
    public Set<String> convertToEntityAttribute(String dbData) {
        return dbData == null ? null : Set.of(dbData.split(SEPARATOR));
    }
}
