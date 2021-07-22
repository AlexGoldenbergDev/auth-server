package com.goldenebrg.authserver.services;

import com.goldenebrg.authserver.rest.beans.UserDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;


@SpringBootTest
class UserServiceImplTest {

    private static final String PTN_NUMBERS = "Password must contain numbers";
    private static final String PTN_CAPITALS = "Password must contain capital letters";
    private static final String PTN_LOWERCASE = "Password must contain lower-case letters";
    private static final String PTN_SPECIAL_CHARS = "Password must contain special characters";

    private static final String PTN_CHARS = "Login must contain any letters";


    private final UserService userService;

    @Autowired
    UserServiceImplTest(UserService userService) {
        this.userService = userService;
    }

    @Test
    void When_PasswordMismatch_Then_ReturnMessage() {
        UserDto userDto = new UserDto();
        userDto.setPassword("012345");
        userDto.setMatchingPassword("01234");

        assertArrayEquals(new String[]{"Passwords doesn't match each other"}, userService.getPasswordValidationErrors(userDto).toArray());
    }

    @Test
    void When_PasswordNumbersOnly_Then_ReturnMessages() {
        passwordPatternTest("013456", PTN_CAPITALS, PTN_LOWERCASE, PTN_SPECIAL_CHARS);
    }

    @Test
    void When_PasswordCapitalsOnly_Then_ReturnMessages() {
        passwordPatternTest("ABCDEF", PTN_NUMBERS, PTN_LOWERCASE, PTN_SPECIAL_CHARS);
    }

    @Test
    void When_PasswordLowercaseOnly_Then_ReturnMessages() {
        passwordPatternTest("abcdef", PTN_NUMBERS, PTN_CAPITALS, PTN_SPECIAL_CHARS);
    }

    @Test
    void When_PasswordSpecialCharseOnly_Then_ReturnMessages() {
        passwordPatternTest("!@$$%^", PTN_NUMBERS, PTN_CAPITALS, PTN_LOWERCASE);
    }

    @Test
    void When_PasswordOkay_Then_ReturnNoMessages() {
        passwordPatternTest("P@ssw0rd");
    }

    private void passwordPatternTest(String password, String... messages) {
        UserDto userDto = new UserDto();
        userDto.setPassword(password);
        userDto.setMatchingPassword(password);

        assertArrayEquals(messages, userService.getPasswordValidationErrors(userDto).toArray());
    }


    @Test
    void When_LoginNumbersOnly_Then_ReturnMessages() {
        UserDto userDto = new UserDto();
        userDto.setLogin("123");

        assertArrayEquals(new String[]{PTN_CHARS}, userService.getLoginValidationErrors(userDto).toArray());
    }

    @Test
    void When_LoginOkay_Then_ReturnNoMessages() {
        UserDto userDto = new UserDto();
        userDto.setLogin("login");

        assertArrayEquals(new String[0], userService.getLoginValidationErrors(userDto).toArray());
    }

}