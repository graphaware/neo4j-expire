package com.graphaware.neo4j.strategies;

import java.util.InvalidPropertiesFormatException;

public class InvalidExpirationStrategyException extends InvalidPropertiesFormatException {

    public InvalidExpirationStrategyException(String message) {
        super(message);
    }
}
