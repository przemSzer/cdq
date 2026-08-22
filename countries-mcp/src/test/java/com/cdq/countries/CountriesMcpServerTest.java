package com.cdq.countries;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CountriesMcpServerTest {

    @Test
    void resolvePortPrefersCommandLineArgument() {
        assertEquals(9090, CountriesMcpServer.resolvePort(new String[] {"9090"}));
    }

    @Test
    void resolvePortFallsBackToDefault() {
        assertEquals(CountriesMcpServer.DEFAULT_PORT, CountriesMcpServer.resolvePort(new String[] {}));
    }
}
