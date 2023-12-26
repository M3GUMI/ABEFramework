package algorithms;

import accessStructure.AccessStructure;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;

import java.util.List;

public interface ABEAlgorithm {
    final String curvePath = "src/main/resources/curves/icn.properties";
    abstract public class PK{};
    abstract public class MSK{};
    abstract public class Cipher{};
    abstract public class ASK{};
    public enum PolicyType {
        Boolean,
        And,
        Threshold
    }
    public void Setup(int U);
    public ASK KeyGen(List<String> attributes);
    public PolicyType getPolicyType();
    public AccessStructure generateAccessStructure(String policy);
    public Cipher Enc(byte[] message, AccessStructure accessStructure);
    public void Dec(Cipher cipher, AccessStructure accessStructure, ASK ask);
}
