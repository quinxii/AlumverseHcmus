package hcmus.alumni.halloffame.config;

import org.springframework.core.convert.converter.Converter;

import hcmus.alumni.halloffame.common.FetchHofMode;

public class StringToEnumConverter implements Converter<String, FetchHofMode> {
    @Override
    public FetchHofMode convert(String source) {
        try {
            return FetchHofMode.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            return FetchHofMode.NORMAL;
        }
    }
}