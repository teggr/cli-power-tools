package dev.rebelcraft.cli;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FileNameUtilTest {
    @Test
    void testEscapeName_AllSafe() {
        assertEquals("my-app.1", FileNameUtil.escapeName("my-app.1"));
    }

    @Test
    void testEscapeName_UnsafeChars() {
        assertEquals("my_app_1_", FileNameUtil.escapeName("my/app:1?"));
    }

    @Test
    void testEscapeName_Empty() {
        assertEquals("", FileNameUtil.escapeName(""));
    }

    @Test
    void testEscapeName_OnlyUnsafe() {
        assertEquals("___", FileNameUtil.escapeName("!@#"));
    }
}
