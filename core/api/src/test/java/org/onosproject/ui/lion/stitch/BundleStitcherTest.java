/*
 * Copyright 2017-present Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.onosproject.ui.lion.stitch;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.onosproject.ui.AbstractUiTest;
import org.onosproject.ui.lion.LionBundle;

import java.util.Locale;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link BundleStitcher}.
 */
public class BundleStitcherTest extends AbstractUiTest {

    private static final String TEST_RESOURCE_BASE =
            "/org/onosproject/ui/lion/stitchtests";

    private static final String[] CARD_GAME_1_KEYS = {
            "of", "flush", "full_house", "pair", "three_oak",
            "ace", "king", "queen", "jack", "ten",
            "spades", "clubs",
    };

    private static final String[] CARD_GAME_1_ENGLISH = {
            "of", "Flush", "Full House", "Pair", "Three of a Kind",
            "Ace", "King", "Queen", "Jack", "Ten",
            "Spades", "Clubs",
    };

    // TODO: Andrea to Localize to Italian
    private static final String[] CARD_GAME_1_ITALIAN = {
            "of", "Flush", "Full House", "Pair", "Three of a Kind",
            "Ace", "King", "Queen", "Jack", "Ten",
            "Spades", "Clubs",
    };

    private static Locale systemLocale;

    private LionBundle lion;

    @BeforeClass
    public static void classSetup() {
        systemLocale = Locale.getDefault();
    }

    @AfterClass
    public static void classTeardown() {
        Locale.setDefault(systemLocale);
    }

    @Before
    public void testSetup() {
        // reset to a known default locale before starting each test
        Locale.setDefault(Locale.US);
    }


    private BundleStitcher testStitcher() {
        return new BundleStitcher(TEST_RESOURCE_BASE);
    }

    private void verifyItems(LionBundle lion, String[] values) {
        final int max = values.length;
        for (int i = 0; i < max; i++) {
            String key = CARD_GAME_1_KEYS[i];
            String expValue = values[i];
            String actValue = lion.getValue(key);
            assertEquals("wrong mapping", expValue, actValue);
        }
    }

    @Test
    public void cardGame1English() {
        title("cardGame1English");
        // use default locale (en_US)

        lion = testStitcher().stitch("CardGame1");
        print(lion);
        assertEquals("wrong key", "CardGame1", lion.id());
        assertEquals("bad key count", 12, lion.size());
        verifyItems(lion, CARD_GAME_1_ENGLISH);
    }

    /*
     * TODO: Andrea to localize
     * Under: ${ONOS_ROOT}/core/api/src/test/resources/
     *
     * Bundles to Localize:
     *    org/onosproject/ui/lion/stitchtests/app/Cards.properties
     *    org/onosproject/ui/lion/stitchtests/core/stuff/Rank.properties
     *    org/onosproject/ui/lion/stitchtests/core/stuff/Suit.properties
     */
    @Ignore("Andrea to localize bundles to Italian")
    @Test
    public void cardGame1Italian() {
        title("cardGame1Italian");
        Locale.setDefault(Locale.ITALIAN);

        lion = testStitcher().stitch("CardGame1");
        print(lion);
        assertEquals("wrong key", "CardGame1", lion.id());
        assertEquals("bad key count", 12, lion.size());
        verifyItems(lion, CARD_GAME_1_ITALIAN);
    }
}
