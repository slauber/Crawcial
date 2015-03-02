package de.crawcial.database;

import org.junit.Test;

/**
 * Created by Sebastian Lauber on 01.03.15.
 */
public class CornerCaseTest {

    @Test(expected = NullPointerException.class)
    public void persistNullElement() {
        DatabaseService.getInstance().persist(null);
    }
/** Does not work due to threading
 @Test public void nullUrlAttachment() {
 new Thread(new DatabaseAttachment("123123asdmklasd", "asnödq3ö3n", new JsonObject())).start();
 }

 @Test public void invalidKey(){
 new Thread(new DatabaseAttachment("123123asdmklasd", "asnödq3ö3n", DbPerfTest.generateSampleData(1).get(0)));
 }
 **/
}
