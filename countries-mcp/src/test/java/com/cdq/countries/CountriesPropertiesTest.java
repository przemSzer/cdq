package com.cdq.countries;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CountriesPropertiesTest {

    @Test
    void requireApiKeyRejectsMissingValue() {
        assertThrows(IllegalStateException.class, () -> CountriesProperties.requireApiKey(null));
        assertThrows(IllegalStateException.class, () -> CountriesProperties.requireApiKey("  "));
    }

    @Test
    void bearerAddsPrefixWhenMissing() {
        assertEquals("Bearer rc_live_demo", CountriesProperties.bearer("rc_live_demo"));
    }

    @Test
    void bearerKeepsExistingPrefix() {
        assertEquals("Bearer rc_live_demo", CountriesProperties.bearer("Bearer rc_live_demo"));
    }
}
