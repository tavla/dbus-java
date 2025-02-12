package org.freedesktop.dbus.test.helper.interfaces;

import java.util.List;

import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.annotations.DBusMemberName;
import org.freedesktop.dbus.annotations.IntrospectionDescription;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.test.helper.SampleSerializable;
import org.freedesktop.dbus.test.helper.structs.SampleStruct;
import org.freedesktop.dbus.test.helper.structs.SampleTuple;
import org.freedesktop.dbus.types.Variant;

@IntrospectionDescription("An example remote interface")
@DBusInterfaceName("org.freedesktop.dbus.test.AlternateTestInterface")
public interface SampleRemoteInterface2 extends DBusInterface {
    @IntrospectionDescription("Test multiple return values and implicit variant parameters.")
    <A> SampleTuple<String, List<Integer>, Boolean> show(A in);

    @IntrospectionDescription("Test passing structs and explicit variants, returning implicit variants")
    <T> T dostuff(SampleStruct foo);

    @IntrospectionDescription("Test arrays, boxed arrays and lists.")
    List<Integer> sampleArray(List<String> l, Integer[] is, long[] ls);

    @IntrospectionDescription("Test passing objects as object paths.")
    DBusInterface getThis(DBusInterface t);

    @IntrospectionDescription("Test bools work")
    @DBusMemberName("checkbool")
    boolean check();

    @IntrospectionDescription("Test Serializable Object")
    SampleSerializable<String> testSerializable(byte b, SampleSerializable<String> s, int i);

    @IntrospectionDescription("Call another method on itself from within a call")
    String recursionTest();

    @IntrospectionDescription("Parameter-overloaded method (string)")
    int overload(String s);

    @IntrospectionDescription("Parameter-overloaded method (byte)")
    int overload(byte b);

    @IntrospectionDescription("Parameter-overloaded method (void)")
    int overload();

    @IntrospectionDescription("Nested List Check")
    List<List<Integer>> checklist(List<List<Integer>> lli);

    @IntrospectionDescription("Get new objects as object paths.")
    SampleNewInterface getNew();

    @IntrospectionDescription("Test Complex Variants")
    void complexv(Variant<? extends Object> v);

    @IntrospectionDescription("Test Introspect on a different interface")
    String Introspect();

    @IntrospectionDescription("Returns the given struct")
    SampleStruct returnSamplestruct( SampleStruct struct );
}
