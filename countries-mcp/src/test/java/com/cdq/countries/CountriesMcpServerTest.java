package com.cdq.countries;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CountriesMcpServerTest {

    @Test
    void PortShouldBeTakenFromCommandLineArgument() {
        assertEquals(9090, CountriesMcpServer.resolvePort(new String[] {"9090"}));
    }

    @Test
    void IfNoPortInCommandLineUseDefault() {
        assertEquals(CountriesMcpServer.DEFAULT_PORT, CountriesMcpServer.resolvePort(new String[] {}));
    }
}
