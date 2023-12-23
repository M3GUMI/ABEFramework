package algorithms;

import access_structure.AccessStructure;
import access_structure.matrix.LSSSMatrix;

import java.io.FileNotFoundException;
import java.util.List;

public interface ABEAlgorithm {
    abstract class PK{};
    abstract class MSK{};
    abstract class Cipher{};
    abstract class ASK{};
    public void Setup(int U);
    public ASK KeyGen(List<String> attributes);
    public Cipher Enc(byte[] message, AccessStructure accessStructure);
    public void Dec(Cipher cipher, AccessStructure accessStructure, ASK ask);
}
