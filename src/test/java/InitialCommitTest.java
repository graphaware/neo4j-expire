import org.junit.Test;

import static org.junit.Assert.*;

public class InitialCommitTest {

    @Test
    public void add_should_add_numbers() {
        assertEquals(InitialCommit.add(1, 1), 2);
    }

}