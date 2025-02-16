package com.ronial.app.conf;

import com.ronial.app.context.Context;
import com.ronial.app.exceptions.ConfigException;

import java.util.Optional;

@FunctionalInterface
public interface ConfigManagement extends Context {
    <T> Optional<T> value(String key) throws ConfigException;
}
