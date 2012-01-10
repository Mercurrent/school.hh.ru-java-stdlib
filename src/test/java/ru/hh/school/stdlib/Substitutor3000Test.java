package ru.hh.school.stdlib;

import org.junit.Assert;
import org.junit.Test;

public class Substitutor3000Test {
    @Test
    public void replacement() {
        Substitutor3000 sbst = new Substitutor3000();
        sbst.put("k1", "one");
        sbst.put("k2", "two");
        sbst.put("keys", "1: ${k1}, 2: ${k2}");

        Assert.assertEquals("1: one, 2: two", sbst.get("keys"));
    }

    @Test
    public void emptyReplacement() {
        Substitutor3000 sbst = new Substitutor3000();
        sbst.put("k", "bla-${inexistent}-bla");

        Assert.assertEquals("bla--bla", sbst.get("k"));
    }
    
    @Test
    public void openingWithoutEnding() {
        final Substitutor3000 target = new Substitutor3000();

        target.put("a", "a${bv");
        Assert.assertEquals("a${bv", target.get("a"));

        target.put("a", "a${");
        Assert.assertEquals("a${", target.get("a"));

        target.put("a", "${be-be");
        Assert.assertEquals("${be-be", target.get("a"));
    }
    
    @Test
    public void endingWithoutOpening() {
        final Substitutor3000 target = new Substitutor3000();
        
        target.put("b", "df}a");
        Assert.assertEquals("df}a", target.get("b"));
        
        target.put("n", "a${b}h}af");
        Assert.assertEquals("adf}ah}af", target.get("n"));
    }

    @Test
    public void nestedBraces() {
        final Substitutor3000 target = new Substitutor3000();
        
        target.put("a", "one");
        target.put("b", "a${a${b}}");
        Assert.assertEquals("a}", target.get("b"));
        
        target.put("a${b", "c");
        Assert.assertEquals("ac}", target.get("b"));
    }

    @Test
    public void recursion() {
        final Substitutor3000 target = new Substitutor3000();
        
        target.put("a", "c${a}");
        Assert.assertEquals("cc${a}", target.get("a"));
        
        target.put("a1", "b${a2}");
        target.put("a2", "c${a1}");
        Assert.assertEquals("bc${a1}", target.get("a1"));
    }
}
