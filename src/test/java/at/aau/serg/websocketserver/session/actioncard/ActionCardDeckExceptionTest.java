package at.aau.serg.websocketserver.session.actioncard;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ActionCardDeckExceptionTest {


    @Test
    void testConstructorWithMessageAndCause() {
        // Arrange
        String errorMessage = "Test error message";
        Throwable cause = new RuntimeException("Original cause");

        // Act
        ActionCardDeckException exception = new ActionCardDeckException(errorMessage, cause);

        // Assert
        assertEquals(errorMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
