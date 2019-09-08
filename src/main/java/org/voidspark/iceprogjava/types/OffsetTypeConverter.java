package org.voidspark.iceprogjava.types;

import static java.lang.String.format;

import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.TypeConversionException;

public final class OffsetTypeConverter implements ITypeConverter<Integer> {
    @Override
    public Integer convert(String value) throws Exception {
        if (value.matches("\\d+")) {
            return Integer.parseInt(value);
        } else if (value.matches("\\d+k")) {
            return Integer.parseInt(value.substring(0, value.length() - 1)) * 1024;
        } else if (value.matches("\\d+M")) {
            return Integer.parseInt(value.substring(0, value.length() - 1)) * 1024 * 1024;
        } else {
            throw new TypeConversionException(format("`%s' is not a valid offset", value));
        }
    }
}
