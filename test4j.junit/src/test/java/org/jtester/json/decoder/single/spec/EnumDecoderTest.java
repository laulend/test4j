package org.jtester.json.decoder.single.spec;

import org.jtester.junit.JTester;
import org.junit.Test;
import org.test4j.json.JSON;
import org.test4j.json.helper.JSONFeature;

public class EnumDecoderTest implements JTester {

    @Test
    public void testDecode() {
        String json = String.format("{'#class':'%s','#value':%s}", JSONFeature.class.getName(),
                JSONFeature.UnMarkClassFlag);

        Object o = JSON.toObject(json);
        want.object(o).clazIs(JSONFeature.class).isEqualTo(JSONFeature.UnMarkClassFlag);
    }

    @Test
    public void testDecode2() {
        String json = JSONFeature.UnMarkClassFlag.name();

        Object o = JSON.toObject(json, JSONFeature.class);
        want.object(o).clazIs(JSONFeature.class).isEqualTo(JSONFeature.UnMarkClassFlag);
    }
}