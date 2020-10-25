package StareTesty.paymentsTests;

import TestHelpers.TestStatus;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.io.FileHandler;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public class CreateAccountAndPayment {
  private WebDriver driver;
  Actions actions;
  private WebDriverWait wait;

  @RegisterExtension
  TestStatus status = new TestStatus();

  @BeforeEach
  public void driverSetup() {
    WebDriverManager.chromedriver().setup();
    driver = new ChromeDriver();

    driver.manage().window().setSize(new Dimension(1295, 730));
    driver.manage().window().setPosition(new Point(10, 40));
    driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);

    driver.navigate().to("https://fakestore.testelka.pl/product-category/windsurfing/");

    actions = new Actions(driver);

    wait = new WebDriverWait(driver, 10);
  }

  @AfterEach
  public void driverQuit(TestInfo info) throws IOException {
    if (status.isFailed) {
      System.out.println("Test screenshot is available at: " + takeScreenShot(info));
    }
    driver.quit();
  }

  @Test
  public void buyAndCreateAccountTest() {
    chooseProductToBuy();
    beforeBuyProduct();

    driver.switchTo().frame(0);
    By cardNumberInput = By.cssSelector("input[name='cardnumber']");
    wait.until(ExpectedConditions.elementToBeClickable(cardNumberInput)).sendKeys("4242424242424242");

    driver.switchTo().defaultContent();
    driver.switchTo().frame(2);
    By cardCVCInput = By.cssSelector("input[name='cvc']");
    wait.until(ExpectedConditions.elementToBeClickable(cardCVCInput)).sendKeys("666");

    driver.switchTo().defaultContent();
    driver.switchTo().frame(1);
    By cardDateInput = By.cssSelector("input[name='exp-date']");
    wait.until(ExpectedConditions.elementToBeClickable(cardDateInput)).sendKeys("0125");

    driver.switchTo().defaultContent();

    String generatedString = RandomStringUtils.random(10, true, true);
    String email = "daria.testerska" + generatedString + "@aa.bb";

    fillOutRegistrationData("Daria", "Testerska", email,
            "Testerska 66", "85666", "Bydgoszcz", "000000666");

    By checkBox = By.cssSelector("input[id='terms']");
    driver.findElement(checkBox).click();

    By createAccountCheckBox = By.cssSelector("input[id='createaccount']");
    driver.findElement(createAccountCheckBox).click();
    driver.findElement(By.cssSelector("input[name='account_password']")).sendKeys(generatedString);

    submit(email);
  }

  private void beforeBuyProduct() {
    WebElement seeCart = driver.findElement(By.cssSelector("a[class='added_to_cart wc-forward']"));
    actions.click(seeCart).build().perform();
    WebElement goToCheckout = driver.findElement(By.cssSelector("a[class='checkout-button button alt wc-forward']"));
    actions.click(goToCheckout).build().perform();
    WebElement checkoutPageTitle = driver.findElement(By.cssSelector("h1[class='entry-title']"));
    Assertions.assertEquals(checkoutPageTitle.getText(), "Zamówienie");
  }

  private void chooseProductToBuy() {
    WebElement demoInfo = driver.findElement(By.cssSelector("a[class='woocommerce-store-notice__dismiss-link']"));
    actions.click(demoInfo).build().perform();

    WebElement submitButton = driver.findElement(By.cssSelector("a[href='?add-to-cart=393']"));
    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", submitButton);
    actions.click(submitButton).build().perform();
    WebElement seeCart = driver.findElement(By.cssSelector("a[class='added_to_cart wc-forward']"));
    Assertions.assertTrue(seeCart.isDisplayed(), "Products has not been added to the cart.");
  }

  private void submit(String login) {
    By orderButton = By.cssSelector("button[id='place_order']");
    driver.findElement(orderButton).click();
    By pageTitle = By.cssSelector("h1[class='entry-title']");
    wait.until(ExpectedConditions.textToBe(pageTitle, "Zamówienie otrzymane"));
    By activeUser = By.cssSelector("p[class='woocommerce-customer-details--email']");
    Assertions.assertEquals(driver.findElement(activeUser).getText(), login);
  }

  private void fillOutRegistrationData(String firstName, String lastName, String email, String street, String postCode, String city, String mobileNumber) {
    driver.findElement(By.cssSelector("input[name='billing_first_name']")).sendKeys(firstName);
    driver.findElement(By.cssSelector("input[name='billing_last_name']")).sendKeys(lastName);
    WebElement countrySelectionDropdown = driver.findElement(By.id("billing_country"));
    Select country = new Select(countrySelectionDropdown);
    country.selectByValue("PL");
    driver.findElement(By.cssSelector("input[name='billing_email']")).sendKeys(email);
    driver.findElement(By.cssSelector("input[name='billing_address_1']")).sendKeys(street);
    driver.findElement(By.cssSelector("input[name='billing_postcode']")).sendKeys(postCode);
    driver.findElement(By.cssSelector("input[name='billing_city']")).sendKeys(city);
    driver.findElement(By.cssSelector("input[name='billing_phone']")).sendKeys(mobileNumber);
  }

  private String takeScreenShot(TestInfo info) throws IOException {
    File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
    LocalDateTime timeNow = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyy HH-mm-ss");
    String path = "C:\\tests_screens\\" + info.getDisplayName() + formatter.format(timeNow) + ".png";
    FileHandler.copy(screenshot, new File(path));
    return path;
  }
}