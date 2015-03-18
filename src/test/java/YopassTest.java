import junit.framework.TestCase;

public class YopassTest extends TestCase {

    public void testGetLifeTime() throws Exception {
        assertEquals(Yopass.getLifeTime("JEBUS"), 3600);
        assertEquals(Yopass.getLifeTime("1h"), 3600);
        assertEquals(Yopass.getLifeTime("1d"), 43200);
        assertEquals(Yopass.getLifeTime("1w"), 302400);
    }
}