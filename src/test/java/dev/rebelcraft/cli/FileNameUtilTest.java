package dev.rebelcraft.cli;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FileNameUtilTest {
    @Test
    void testEscapeName_AllSafe() {
        // given: a safe name
        String input = "my-app.1";

        // when: escaping the name
        String result = FileNameUtil.escapeName(input);

        // then: the result should be unchanged
        assertEquals("my-app.1", result);
    }

    @Test
    void testEscapeName_UnsafeChars() {
        // given: a name with unsafe characters
        String input = "my/app:1?";

        // when: escaping the name
        String result = FileNameUtil.escapeName(input);

        // then: unsafe characters should be replaced with underscores
        assertEquals("my_app_1_", result);
    }

    @Test
    void testEscapeName_Empty() {
        // given: an empty string
        String input = "";

        // when: escaping the name
        String result = FileNameUtil.escapeName(input);

        // then: the result should be empty
        assertEquals("", result);
    }

    @Test
    void testEscapeName_OnlyUnsafe() {
        // given: a string with only unsafe characters
        String input = "!@#";

        // when: escaping the name
        String result = FileNameUtil.escapeName(input);

        // then: all characters should be replaced with underscores
        assertEquals("___", result);
    }
}
