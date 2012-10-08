package eu.fryc.ghostdriver;

import org.junit.Test;
import org.openqa.selenium.WebDriver;

public class TestPhantomJSDriver {

    @Test
    public void test() {
        WebDriver browser = new PhantomJSDriver();
        
        browser.get("http://www.google.com/");
        
        browser.quit();
    }
}
