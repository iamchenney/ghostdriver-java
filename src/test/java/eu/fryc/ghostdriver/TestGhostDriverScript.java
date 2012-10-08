package eu.fryc.ghostdriver;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

public class TestGhostDriverScript {

    private GhostDriverScript script = new GhostDriverScript();

    @Test
    public void testGhostDriverResource() {
        assertNotNull(script.getResourceStream());
    }
    
    @Test
    public void testExtractGhostDriver() {
        File file = script.getGhostDriver();
        
        assertNotNull(file);
        assertTrue(file.exists());
    }
}
