package tests;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import pages.ClaimsPage;
import io.github.bonigarcia.wdm.WebDriverManager;

public class ClaimsTest {
    WebDriver driver;
    ClaimsPage claimsPage;

    @BeforeClass
    public void setUp() {
        // Setup Chrome driver using WebDriverManager
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();

        // Navigate to claims application
        driver.get("http://localhost:8080/claims");
        driver.manage().window().maximize();

        // Initialize ClaimsPage
        claimsPage = new ClaimsPage(driver);
    }

    @Test
    public void testSubmitClaimWithValidPolicyId() {
        // Test submitting a claim with valid policy ID
        claimsPage.fillClaim("POL-2026-001");

        // Verify success message or page state
        Assert.assertTrue(driver.getPageSource().contains("submitted"),
                "Claim should be submitted successfully");
    }

    @Test
    public void testMultipleClaimSubmissions() {
        String[] policyIds = { "POL-2026-001", "POL-2026-002", "POL-2026-003" };

        for (String policyId : policyIds) {
            claimsPage.fillClaim(policyId);
            // Add verification logic here
        }
    }

    @AfterClass
    public void tearDown() {
        // Close browser
        if (driver != null) {
            driver.quit();
        }
    }
}
