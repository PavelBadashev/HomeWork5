import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AutoTest {
    private WebDriver driver;

    private String getPropertyByName(String name) throws IOException {
        Properties properties = new Properties();
        InputStream input = ClassLoader.getSystemResourceAsStream("config.properties");
        properties.load(input);

        return properties.getProperty(name);
    }

    private String dateReformat(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date newDate = sdf.parse(date);
            sdf.applyPattern("yyyy-MM-dd");
            return sdf.format(newDate);
        } catch (ParseException e) {
            e.printStackTrace();
            Assertions.fail("Некорректный формат даты.");
        }

        return date;
    }

    private WebElement getElement(By condition) {
        WebElement result = driver.findElement(condition);
        WebDriverWait wait = new WebDriverWait(driver, 5);
        wait.until(ExpectedConditions.presenceOfElementLocated(condition));

        return result;
    }

    private void inputText(String key) throws IOException {
        String valueInput = getPropertyByName(key);
        WebElement value = getElement(By.id(key));
        value.sendKeys(valueInput);

        // Проверка ввода
        Assertions.assertEquals(
                valueInput,
                value.getAttribute("value")
        );
    }

    @BeforeAll
    public static void webDriverInstall() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    public void webDriverStart() throws IOException {
        driver = new ChromeDriver();
        driver.get(getPropertyByName("url"));
    }

    @AfterEach
    public void webDriverStop() {
        if (driver != null)
            driver.close();
    }

    @Test
    @DisplayName("Регистрация")
    public void registrationTest() throws IOException {
        inputText("username");
        inputText("email");
        // Проверка совпадения паролей
        if (!Objects.equals(getPropertyByName("password"), getPropertyByName("confirm_password"))) {
            Assertions.fail("Пароли не совпадают!");
        }
        inputText("password");
        inputText("confirm_password");

        // Дата рождения
        WebElement dateInput = driver.findElement(By.id("birthdate"));
        dateInput.sendKeys(getPropertyByName("birthdate"));

        // Уровень знания языка:
        Select selectDropDown = new Select(driver.findElement(By.id("language_level")));
        selectDropDown.selectByValue(getPropertyByName("language_level"));

        // Подтверждение введенных данных
        WebElement submit = driver.findElement(By.cssSelector("input[type=submit]"));
        submit.click();

        // Проверка вывода результата
        Assertions.assertEquals(
                String.format(
                        "Имя пользователя: %s\n" +
                        "Электронная почта: %s\n" +
                        "Дата рождения: %s\n" +
                        "Уровень языка: %s",
                        getPropertyByName("username"),
                        getPropertyByName("email"),
                        dateReformat(getPropertyByName("birthdate")),
                        getPropertyByName("language_level")
                        ),
                driver.findElement(By.id("output")).getText()
        );
    }
}
