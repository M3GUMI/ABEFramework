package algorithms;

import access_structure.AccessStructure;
import access_structure.matrix.LSSSMatrix;
import it.unisa.dia.gas.jpbc.*;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.a.TypeACurveGenerator;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Waters implements ABEAlgorithm{
    private Field G1;
    private Field Zr;
    private Field GT;
    private Pairing bp;
    private WatersPK pk;
    private WatersMSK msk;
    private final String curvePath = "src/main/resources/curves/waters.properties";

    class WatersPK extends PK {
        ElementPowPreProcessing g;
        Element egg_alpha;
        Element g_a;
        List<String> attributeNames;
        Map<String, ElementPowPreProcessing> hMap;

        public WatersPK(ElementPowPreProcessing g,
                        Element egg_alpha,
                        Element g_a,
                        List<String> attributeNames,
                        Map<String, ElementPowPreProcessing> hMap) {
            this.g = g;
            this.egg_alpha = egg_alpha;
            this.g_a = g_a;
            this.attributeNames = attributeNames;
            this.hMap = hMap;
        }
    }

    class WatersMSK extends MSK {
        Element g_alpha;

        public WatersMSK(Element g_alpha) {
            this.g_alpha = g_alpha;
        }
    }

    class WatersASK extends ASK{
        Element K;
        Element L;
        List<String> attributeNames;
        Map<String, Element> KxMap;

        public WatersASK(Element K,
                         Element L,
                         List<String> attributeNames,
                         Map<String, Element> KxMap) {
            this.K = K;
            this.L = L;
            this.attributeNames = attributeNames;
            this.KxMap = KxMap;
        }
    }

    class WatersCipher extends Cipher{
        Element C;
        Element C_;
        Map<String, Element> CMap;
        Map<String, Element> DMap;
        LSSSMatrix matrix;

        public WatersCipher(Element C,
                            Element C_,
                            Map<String, Element> CMap,
                            Map<String, Element> DMap,
                            LSSSMatrix matrix) {
            this.C = C;
            this.C_ = C_;
            this.CMap = CMap;
            this.DMap = DMap;
            this.matrix = matrix;
        }
    }

    public Waters(int size){
        File file = new File(curvePath);
        if (!file.exists()) {
            TypeACurveGenerator pg = new TypeACurveGenerator(size, size);
            PairingParameters typeAParams = pg.generate();
            try {
                FileWriter writer = new FileWriter(file);
                writer.write(typeAParams.toString());
                writer.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        Pairing pairing = PairingFactory.getPairing(curvePath);
        this.bp = pairing;
        this.G1 = pairing.getG1();
        this.GT = pairing.getGT();
        this.Zr = pairing.getZr();
    }

    @Override
    public void Setup(int U) {
        ElementPowPreProcessing g = G1.newRandomElement().getImmutable().getElementPowPreProcessing();
        Element alpha = Zr.newRandomElement().getImmutable();
        Element a = Zr.newRandomElement().getImmutable();

        Element egg_alpha = bp.pairing(g.pow(new BigInteger("1")),g.powZn(alpha)).getImmutable();
        Element g_a = g.powZn(a);
        Element g_alpha = g.powZn(alpha);

        Map<String, ElementPowPreProcessing> hMap = null;
        List<String> attributeSet = null;
        try {
            File file = new File("src/main/resources/attributesets.txt");
            BufferedReader reader = new BufferedReader(
                    new FileReader(file));
            hMap = new HashMap<>();
            String str;
            attributeSet = new ArrayList<>();
            while ((str = reader.readLine())!=null){
                attributeSet.add(str);
            }
            for (String s : attributeSet) {
                ElementPowPreProcessing h = G1.newRandomElement().getImmutable().getElementPowPreProcessing();
                hMap.put(s, h);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        pk = new WatersPK(g, egg_alpha, g_a, attributeSet, hMap);
        msk = new WatersMSK(g_alpha);
    }

    @Override
    public ASK KeyGen(List<String> attributes) {
        ElementPowPreProcessing g = pk.g;
        Element g_a = pk.g_a;
        Element g_alpha = msk.g_alpha;
        Element t = Zr.newRandomElement().getImmutable();
        Element K = g_alpha.mul(g_a.powZn(t));
        Element L = g.powZn(t);
        Map<String, ElementPowPreProcessing> hMap = pk.hMap;
        Map<String, Element> KxMap = new HashMap<>();
        for (String attribute : attributes) {
            ElementPowPreProcessing h = hMap.get(attribute);
            Element Kx = h.powZn(t);
            KxMap.put(attribute, Kx);
        }
        return new WatersASK(K, L, attributes, KxMap);
    }

    @Override
    public Cipher Enc(byte[] message, AccessStructure accessStructure) {
        if(!(accessStructure instanceof LSSSMatrix))
            return null;

        LSSSMatrix lsssMatrix = (LSSSMatrix) accessStructure;
        List<List<Integer>> matrix = lsssMatrix.getMatrix();
        int l = matrix.size();
        int n = matrix.get(0).size();
        ElementPowPreProcessing g = pk.g;
        Element egg_alpha = pk.egg_alpha;
        Element g_a = pk.g_a;
        Map<String, ElementPowPreProcessing> hMap = pk.hMap;

        List<Element> yVector = new ArrayList<>();
        List<Element> rVector = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            yVector.add(Zr.newRandomElement().getImmutable());
        }
        Element s = yVector.get(0);

        for (int i = 0; i < l; i++) {
            rVector.add(Zr.newRandomElement().getImmutable());
        }

        List<Element> gammaVector = new ArrayList<>();
        for (int i = 0; i < l; i++) {
            Element gamma = Zr.newZeroElement().getImmutable();
            for (int j = 0; j < n; j++) {
                if(matrix.get(i).get(j)==1){
                    gamma = gamma.add(yVector.get(j));
                }
                if(matrix.get(i).get(j)==-1){
                    gamma = gamma.sub(yVector.get(j));
                }
            }
            gammaVector.add(gamma);
        }

        Element C = egg_alpha.powZn(s);
        Element C_ = g.powZn(s);

        Map<String, Element> CMap = new HashMap<>();
        Map<String, Element> DMap = new HashMap<>();
        for (int i = 0; i < l; i++) {
            String attributeName = lsssMatrix.rho(i);
            Element gamma = gammaVector.get(i);
            Element ri = rVector.get(i);
            ElementPowPreProcessing h = hMap.get(attributeName);

            Element C_i = (g_a.powZn(gamma)).mul(h.powZn(ri.mul(-1)).getImmutable());
            Element D_i = g.powZn(ri).getImmutable();

            CMap.put(attributeName, C_i);
            DMap.put(attributeName, D_i);
        }

        return new WatersCipher(C, C_, CMap, DMap, lsssMatrix);
    }

    @Override
    public void Dec(Cipher cipher, AccessStructure accessStructure, ASK ask) {
        if(!(cipher instanceof WatersCipher))
            return;
        if(!(accessStructure instanceof LSSSMatrix))
            return;

        LSSSMatrix matrix = (LSSSMatrix) accessStructure;
        WatersCipher watersCipher = (WatersCipher)cipher;
        Element C = watersCipher.C;
        Element C_ = watersCipher.C_;
        Map<String, Element> CMap = watersCipher.CMap;
        Map<String, Element> DMap = watersCipher.DMap;

        WatersASK watersASK = (WatersASK) ask;
        Element K = watersASK.K;
        Element L = watersASK.L;
        Map<String, Element> KxMap = watersASK.KxMap;
        List<String> attributeList = watersASK.attributeNames;
        List<String> returnAttributes = matrix.returnAttributes(attributeList);

        Element element = bp.pairing(C_, K).getImmutable();
        for (String attribute : returnAttributes) {
            Element Ci = CMap.get(attribute);
            Element Di = DMap.get(attribute);
            Element Ki = KxMap.get(attribute);

            Element element1 = bp.pairing(Ci, L).getImmutable();
            Element element2 = bp.pairing(Di, Ki).getImmutable();
            Element element3 = element1.mul(element2);
            element = element.div(element3);
        }
    }
}
