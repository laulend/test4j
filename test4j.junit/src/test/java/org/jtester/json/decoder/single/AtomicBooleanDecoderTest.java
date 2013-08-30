package org.jtester.json.decoder.single;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jtester.junit.JTester;
import org.junit.Test;
import org.test4j.json.JSON;
import org.test4j.json.helper.JSONFeature;
import org.test4j.json.helper.JSONMap;

public class AtomicBooleanDecoderTest implements JTester {
    @Test
    public void testAtomicBool() {
        AtomicBoolean expected = new AtomicBoolean(true);
        JSONMap json = new JSONMap() {
            private static final long serialVersionUID = 1L;

            {
                this.putJSON(JSONFeature.ValueFlag, 1);
            }
        };

        AtomicBoolean bool = JSON.toObject(json, AtomicBoolean.class, new HashMap<String, Object>());
        want.object(bool).reflectionEq(expected);
    }

    @Test
    public void testAtomicBool_JSONString() {
        AtomicBoolean expected = new AtomicBoolean(true);

        AtomicBoolean bool = JSON.toObject("{'" + JSONFeature.ValueFlag + "':1}", AtomicBoolean.class);
        want.object(bool).reflectionEq(expected);
    }
}