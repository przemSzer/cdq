package com.cdq.countries;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CountriesMCPPropertiesTest {

    @Test
    void requireApiKeyRejectsMissingValue() {
        assertThrows(IllegalStateException.class, () -> CountriesMCPProperties.requireApiKey(null));
        assertThrows(IllegalStateException.class, () -> CountriesMCPProperties.requireApiKey("  "));
    }

    @Test
    void bearerAddsPrefixWhenMissing() {
        assertEquals("Bearer rc_live_demo", CountriesMCPProperties.bearer("rc_live_demo"));
    }

    @Test
    void bearerKeepsExistingPrefix() {
        assertEquals("Bearer rc_live_demo", CountriesMCPProperties.bearer("Bearer rc_live_demo"));
    }
}
