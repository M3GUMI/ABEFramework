package algorithms;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class ABEAlgorithmFactory {
    public static ABEAlgorithm getAlgorithm(String algorithmName) {
        algorithmName = "algorithms." + algorithmName;
        ABEAlgorithm algorithmInstance = null;
        try {
            Class<ABEAlgorithm> algorithmClass = (Class<ABEAlgorithm>) Class.forName(algorithmName);
            algorithmInstance = (ABEAlgorithm) algorithmClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return algorithmInstance;
    }

    static ABEAlgorithm getAlgorithm(String algorithmName, int size) {
        algorithmName = "algorithms." + algorithmName;
        ABEAlgorithm algorithmInstance = null;
        try {
            Class<ABEAlgorithm> algorithmClass = (Class<ABEAlgorithm>) Class.forName(algorithmName);
            Constructor<ABEAlgorithm> constructor = algorithmClass.getConstructor(int.class);
            algorithmInstance = constructor.newInstance(size);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return algorithmInstance;
    }
}
