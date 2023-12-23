package utils;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import utils.containers.Container;
import utils.containers.Tuple;

public class Util {
    public static <V> V getType(Object object) {
        return (V)object;
    }

    public static void main(String[] args) {
        Container container = new Tuple<Integer, Integer>(1, 2);
        Tuple tuple = container.getType();
        System.out.println(tuple.first);
    }

    public static Element getElementByFingerprint(String curvePath, String attributeName, boolean isImmutable) {
        if (isImmutable == false)
            return getElementByFingerprint(curvePath, attributeName);
        return getElementByFingerprint(curvePath, attributeName).getImmutable();
    }

    public static Element getElementByFingerprint(String curvePath, String attributeName) {
        Pairing bp = PairingFactory.getPairing(curvePath);
        Field G1 = bp.getG1();
        Element element = G1.newElementFromBytes(attributeName.getBytes());
        return element;
    }
}
