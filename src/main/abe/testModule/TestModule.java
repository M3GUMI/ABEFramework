package testModule;

import accessStructure.AccessStructure;
import algorithms.ABEAlgorithm;
import algorithms.ABEAlgorithmFactory;
import utils.containers.Triad;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestModule {
    static String booleanPolicyTimeTestInstancePath = "src/main/resources/TestInstance/TimeTestInstance/booleanTestInstancePath.txt";
    static String andPolicyTimeTestInstancePath = "src/main/resources/TestInstance/TimeTestInstance/andTestInstancePath.txt";
    static int attrInc = 5;
    static int policyNumPerInc = 10;
    static int IncNum = 8;
    static String booleanPolicyCorrTestInstancePath = "src/main/resources/TestInstance/CorrTestInstance/booleanTestInstancePath.txt";
    static String andPolicyCorrTestInstancePath = "src/main/resources/TestInstance/CorrTestInstance/andTestInstancePath.txt";
    static String attributeSetPath = "src/main/resources/attributesets.txt";
    static List<String> attributeSet;

    static {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(attributeSetPath));
            String attribute;
            attributeSet = new ArrayList<>();
            while ((attribute = reader.readLine()) != null)
                attributeSet.add(attribute);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static public void timeTest(ABEAlgorithm algorithm) {
        timeTest(algorithm, 256);
    }

    static Triad<List<Long>, List<Long>, List<Long>> timeTest(ABEAlgorithm algorithm, int size) {
        BufferedReader reader = null;
        List<Long> keyGenTime = null;
        List<Long> encTime = null;
        List<Long> decTime = null;
        try {
            switch (algorithm.getPolicyType()) {
                case And:
                    reader = new BufferedReader(new FileReader(andPolicyTimeTestInstancePath));
                    break;
                case Boolean:
                    reader = new BufferedReader(new FileReader(booleanPolicyTimeTestInstancePath));
                    break;
                case Threshold:
                    break;
            }

            keyGenTime = new ArrayList<>();
            encTime = new ArrayList<>();
            decTime = new ArrayList<>();
            algorithm.Setup(size);
            for (int i = 0; i < IncNum; i++) {
                long keyGenSum = 0;
                long encSum = 0;
                long decSum = 0;
                for (int j = 0; j < policyNumPerInc; j++) {
                    String testInstance = reader.readLine();
                    List<String> attribute = new ArrayList<>();
                    String[] instance = testInstance.split(", ");
                    String policy = instance[0];
                    String[] attributes = instance[1].split(" ");
                    List<String> attributeNames = new ArrayList<>(attribute);
                    long stime, etime;

                    stime = System.currentTimeMillis();
                    ABEAlgorithm.ASK ask = algorithm.KeyGen(attribute);
                    etime = System.currentTimeMillis();
                    keyGenSum += etime - stime;

                    stime = System.currentTimeMillis();
                    AccessStructure accessStructure = algorithm.generateAccessStructure(policy);
                    ABEAlgorithm.Cipher cipher = algorithm.Enc(null, accessStructure);
                    etime = System.currentTimeMillis();
                    encSum += etime - stime;

                    stime = System.currentTimeMillis();
                    algorithm.Dec(cipher, accessStructure, ask);
                    etime = System.currentTimeMillis();
                    decSum += etime - stime;
                }
                keyGenTime.add(keyGenSum / policyNumPerInc);
                encTime.add(encSum / policyNumPerInc);
                decTime.add(decSum / policyNumPerInc);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("-----------------KeyGenTime-----------------");
        for (int i = 0; i < IncNum; i++) {
            System.out.println("KeyGen[attNum = " + (i+1) * attrInc + "]: " + keyGenTime.get(i) + "ms");
        }
        System.out.println("-----------------EncTime-----------------");
        for (int i = 0; i < IncNum; i++) {
            System.out.println("Enc[attNum = " + (i+1) * attrInc + "]: " + encTime.get(i) + "ms");
        }
        System.out.println("-----------------DecTime-----------------");
        for (int i = 0; i < IncNum; i++) {
            System.out.println("Dec[attNum = " + (i+1) * attrInc + "]: " + decTime.get(i) + "ms");
        }

        return new Triad<>(keyGenTime, encTime, decTime);
    }

    static public void testInstanceGen() throws IOException {
        FileWriter booleanTimeWriter = new FileWriter(booleanPolicyTimeTestInstancePath);
        FileWriter andTimeWriter = new FileWriter(andPolicyTimeTestInstancePath);
        StringBuilder buffer = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < IncNum; i++) {
            for (int j = 0; j < policyNumPerInc; j++) {
                List<String> attribute = new ArrayList<>();
                for (int k = 0; k < attrInc * (i+1); k++) {
                    int randomNumber = random.nextInt(100);
                    String attributeName = "user" + randomNumber;
                    buffer.append(attributeName);
                    attribute.add(attributeName);
                    if (k != attrInc * (i+1)-1) {
                        buffer.append(" and ");
                    }
                }
                buffer.append(", ");
                for (String att : attribute) {
                    buffer.append(att).append(" ");
                }
                buffer.append("\n");
            }
        }
        booleanTimeWriter.write(buffer.toString());
        booleanTimeWriter.flush();
        andTimeWriter.write(buffer.toString());
        andTimeWriter.flush();
        booleanTimeWriter.close();
        andTimeWriter.close();
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ABEAlgorithm algorithm = ABEAlgorithmFactory.getAlgorithm("LaiDengLi");
        timeTest(algorithm);
//        System.out.println(Class.forName("algorithms.Waters"));
    }
}
