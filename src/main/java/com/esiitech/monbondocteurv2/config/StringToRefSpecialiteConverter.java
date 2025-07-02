package com.esiitech.monbondocteurv2.config;

import com.esiitech.monbondocteurv2.model.RefSpecialite;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToRefSpecialiteConverter implements Converter<String, RefSpecialite> {
    @Override
    public RefSpecialite convert(String source) {
        return RefSpecialite.valueOf(source.toUpperCase());
    }
}
