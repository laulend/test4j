package org.test4j.json.decoder.array;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.test4j.json.JSON;
import org.test4j.json.decoder.CollectionDecoder;
import org.test4j.json.decoder.ForTestType;
import org.test4j.json.encoder.beans.test.User;
import org.test4j.junit.Test4J;

@SuppressWarnings({ "rawtypes" })
public class CollectionDecoderTest extends Test4J {

    @Test
    public void testParseFromJSONArray() {
        String json = "[value1,value2,value3]";
        List list = JSON.toObject(json, ArrayList.class);
        want.collection(list).sizeEq(3).hasAllItems("value1", "value2", "value3");
    }

    @Test
    public void testDecode_RefValue() {
        String json = "[{#class:org.test4j.json.encoder.beans.test.User@12c8fa8,id:12,name:'darui.wu',age:0,salary:0,isFemale:false},{#refer:@12c8fa8}]";
        List<User> list = JSON.toObject(json, ArrayList.class);
        want.collection(list).sizeEq(2);
        User u1 = list.get(0);
        User u2 = list.get(1);
        want.object(u1).same(u2);
    }

    @Test
    public void testDecode() throws Exception {
        String json = "[{#class:@12c8fa8,id:12,name:'darui.wu',age:0,salary:0,isFemale:false},{#refer:@12c8fa8}]";
        Type type = ForTestType.getType("userList");
        List<User> list = JSON.toObject(json, type);
        want.collection(list).sizeEq(2);
        User u1 = list.get(0);
        User u2 = list.get(1);
        want.object(u1).same(u2);
    }

    @Test
    public void testAccept() throws Exception {
        Type type = ForTestType.getType("userList");
        boolean isAccepted = CollectionDecoder.toCOLLECTION.accept(type);
        want.bool(isAccepted).is(true);
    }

    @Test
    public void testGetComponent() throws Exception {
        Type toType = ForTestType.getType("objectList");
        TypeVariable type = (TypeVariable) reflector.invoke(CollectionDecoder.toCOLLECTION, "getComponentType", toType);
        want.object(type.toString()).isEqualTo("E");
    }

    @Test
    public void testGetComponent2() throws Exception {
        Type toType = ForTestType.getType("userList");
        Type type = reflector.invoke(CollectionDecoder.toCOLLECTION, "getComponentType", toType);
        want.object(type).isEqualTo(User.class);
    }
}
