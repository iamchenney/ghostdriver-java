package eu.fryc.ghostdriver;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.net.PortProber;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;

import eu.fryc.phantomjs.PhantomJS;

public class PhantomJSDriver extends RemoteWebDriver {

    private Process process;
    
    /**
     * TODO
     * 
     * @param externalDriverUrl
     * @param desireCapabilities
     */
    public PhantomJSDriver(URL externalDriverUrl, Capabilities desireCapabilities) {
        super(externalDriverUrl, desireCapabilities);
        process = null;
    }

    /**
     * TODO
     * 
     * @param desiredCapabilities
     */
    public PhantomJSDriver(Capabilities desiredCapabilities) {
        super((CommandExecutor) null, desiredCapabilities);

        // NOTE: At this point, given that there is not a Command Executor set,
        // the status of the Driver is inconsistent.
        // We will create the Executor as part of the "PhantomJSDriver#startSession(Capabilities)"
        // call that the RemoteWebDriver constructor will make.
    }
    
    public PhantomJSDriver() {
        super(getDefaultCapabilities());
    }
    
    private static Capabilities getDefaultCapabilities() {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setBrowserName("phantomjs");
        return capabilities;
    }

    /**
     * TODO
     * 
     * @param desiredCapabilities
     * @param requiredCapabilities
     * @throws WebDriverException
     */
    @Override
    protected void startSession(Capabilities desiredCapabilities, Capabilities requiredCapabilities) throws WebDriverException {
        // Will launch a PhantomJS WebDriver process ONLY if this driver is not already using an external one

        // Read the Proxy configuration
        Proxy proxy = (Proxy) desiredCapabilities.getCapability(CapabilityType.PROXY);

        // Find a free port to launch PhantomJS WebDriver on
        String port = Integer.toString(PortProber.findFreePort());

        // parameters
        List<String> paramList = new LinkedList<String>();
        if (proxy != null) {
            paramList.add("--proxy=" + proxy.getHttpProxy());
        }
        paramList.add(port);
        String[] parameters = paramList.toArray(new String[paramList.size()]);

        log(getSessionId(), "About to launch PhantomJS WebDriver", null, When.BEFORE);
        try {
            PhantomJS phantomJS = new PhantomJS();
            GhostDriverScript script = new GhostDriverScript();

            process = phantomJS.execute(script.getGhostDriver(), parameters);

            // PhantomJS is ready to serve.
            // Setting the HTTP Command Executor that this RemoteWebDriver will use
            setCommandExecutor(new HttpCommandExecutor(new URL("http://localhost:" + port)));
        } catch (IOException ioe) {
            // Log exception & Cleanup
            log(getSessionId(), null, ioe, When.EXCEPTION);
            stopClient();
            throw new WebDriverException("PhantomJSDriver: " + ioe.getMessage());
        }
        log(getSessionId(), "PhantomJS WebDriver ready", null, When.AFTER);

        // We are ready to let the RemoteDriver do its job from here
        super.startSession(desiredCapabilities, requiredCapabilities);
    }

    /**
     * TODO
     */
    @Override
    protected void stopClient() {
        // Shutdown the PhantomJS process
        log(getSessionId(), "Shutting down PhantomJS WebDriver", null, When.BEFORE);
        process.destroy();
    }
}