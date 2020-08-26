package org.openstreetmap.atlas.checks.validation.tag;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Tests fir {@link IncorrectTagCheck}
 *
 * @author v-brjor
 */
public class IncorrectTagCheckTest
{

    @Rule
    public IncorrectTagCheckTestRule setup = new IncorrectTagCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    private final IncorrectTagCheck check = new IncorrectTagCheck(ConfigurationResolver
        .inlineConfiguration("{\"IncorrectTagCheck\": {}}"));

    @Test
    public void testTagValueContainsCapitalLetter()
    {
        this.verifier.actual(this.setup, this.check);
        this.verifier.verifyNotEmpty();
    }

    @Test
    public void testTagValueContainsSpace()
    {

    }

    @Test
    public void testTagValueIsNotAlphanumeric()
    {

    }
}
