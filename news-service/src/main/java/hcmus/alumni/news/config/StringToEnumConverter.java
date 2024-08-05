package hcmus.alumni.news.config;

import org.springframework.core.convert.converter.Converter;

import hcmus.alumni.news.common.FetchNewsMode;

public class StringToEnumConverter implements Converter<String, FetchNewsMode> {
    @Override
    public FetchNewsMode convert(String source) {
        try {
            return FetchNewsMode.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            return FetchNewsMode.NORMAL;
        }
    }
}