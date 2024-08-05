package hcmus.alumni.event.config;

import org.springframework.core.convert.converter.Converter;

import hcmus.alumni.event.common.FetchEventMode;

public class StringToEnumConverter implements Converter<String, FetchEventMode> {
    @Override
    public FetchEventMode convert(String source) {
        try {
            return FetchEventMode.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            return FetchEventMode.NORMAL;
        }
    }
}