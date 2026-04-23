package pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class ClaimsPage {
    WebDriver driver;
    
    @FindBy(id = "policyId")
    WebElement policyField;
    
    @FindBy(id = "submit")
    WebElement submitBtn;

    public ClaimsPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public void fillClaim(String id) {
        policyField.sendKeys(id);
        submitBtn.click();
    }
}
