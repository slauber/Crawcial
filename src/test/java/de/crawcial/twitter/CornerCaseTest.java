package de.crawcial.twitter;

import org.junit.Test;

import java.io.IOException;

/**
 * Created by Sebastian Lauber on 01.03.15.
 */
public class CornerCaseTest {
    @Test(expected = NullPointerException.class)
    public void boundaryTests() throws IOException {
        // Should crash due to no valid config file
        Utils.loadParams("NONSENSE", false);
    }

}
