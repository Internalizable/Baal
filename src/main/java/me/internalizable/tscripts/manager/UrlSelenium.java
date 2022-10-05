package me.internalizable.tscripts.manager;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

public class UrlSelenium {

   public static byte[] capture(String username, String tweetId) throws IOException {
       WebDriver driver = new ChromeDriver();

       driver.manage().window().setPosition(new Point(-2000, 0));
       driver.get("https://twitter.com/" + username + "/status/" + tweetId);

       By selector = By.cssSelector("article[data-testid=tweet]");

       WebElement webElement = driver.findElement(selector);
       byte[] data = webElement.getScreenshotAs(OutputType.BYTES);
       driver.close();

       return data;
   }

}
 