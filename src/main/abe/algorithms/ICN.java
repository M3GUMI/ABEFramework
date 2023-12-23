package algorithms;

import access_structure.AccessStructure;
import access_structure.clause.ICNClause;
import it.unisa.dia.gas.jpbc.*;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.a1.TypeA1CurveGenerator;
import it.unisa.dia.gas.plaf.jpbc.util.ElementUtils;
import org.junit.Test;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ICN implements ABEAlgorithm{
    public Field G1;
    public Field Zr;
    public Field GT;
    public Pairing bp;
    public ICNPK pk;
    public ICNMSK msk;
    private final String curvePath = "src/main/resources/curves/icn.properties";

    public class ICNPK extends PK {
        ElementPowPreProcessing phi;
        ElementPowPreProcessing psi;
        Element phi_beta;
        Element ephipsi_alpha;
        List<String> attributeNames;
        Map<String, Element> IMap;
        Map<String, Element> kMap;
        Map<String, ElementPowPreProcessing> TMap;

        public ICNPK (ElementPowPreProcessing phi,
                      ElementPowPreProcessing psi,
                      Element phi_beta,
                      Element ephipsi_alpha,
                      List<String> attributeNames,
                      Map<String, Element> IMap,
                      Map<String, Element> kMap,
                      Map<String, ElementPowPreProcessing> TMap){
            this.phi = phi;
            this.psi = psi;
            this.phi_beta = phi_beta;
            this.ephipsi_alpha = ephipsi_alpha;
            this.attributeNames = attributeNames;
            this.IMap = IMap;
            this.kMap = kMap;
            this.TMap = TMap;
        }
    }

    class ICNMSK extends MSK {
        Element beta;
        Element psi_alpha;
        Element alpha;
        Map<String, ElementPowPreProcessing> SMap;

        public ICNMSK(Element beta,
                      Element psi_alpha,
                      Element alpha,
                      Map<String, ElementPowPreProcessing> SMap) {
            this.beta = beta;
            this.psi_alpha = psi_alpha;
            this.alpha = alpha;
            this.SMap = SMap;
        }
    }

    class ICNASK extends ASK {
        List<String> attributes;
        Element D_UID;
        Map<String, Element> XMap;
        Map<String, Element> YMap;
        Map<String, Element> ZMap;
        Map<String, String> invZmap;

        public ICNASK(List<String> attributes,
                      Element D_UID,
                      Map<String, Element> XMap,
                      Map<String, Element> YMap,
                      Map<String, Element> ZMap) {
            this.attributes = attributes;
            this.D_UID = D_UID;
            this.XMap = XMap;
            this.YMap = YMap;
            this.ZMap = ZMap;
            this.invZmap = new HashMap<>();
            for (Map.Entry<String, Element> entry : ZMap.entrySet()) {
                String attribute = entry.getKey();
                String point = entry.getValue().toString();
                invZmap.put(point, attribute);
            }
        }
    }

    class ICNCipher extends Cipher {
        Element C;
        Element C_;
        Map<String, Element> C1Map;
        Map<String, Element> C2Map;
        Map<String, Element> C3Map;

        public ICNCipher (Element C,
                          Element C_,
                          Map<String, Element> C1Map,
                          Map<String, Element> C2Map,
                          Map<String, Element> C3Map) {
            this.C = C;
            this.C_ = C_;
            this.C1Map = C1Map;
            this.C2Map = C2Map;
            this.C3Map = C3Map;
        }
    }

    public ICN(int size){
        File file = new File(curvePath);
        if (!file.exists()) {
            TypeA1CurveGenerator pg = new TypeA1CurveGenerator(4, size);
            PairingParameters typeAParams = pg.generate();
//            BigInteger n0 = typeAParams.getBigInteger("n0");
//            BigInteger n1 = typeAParams.getBigInteger("n1");
//            BigInteger n2 = typeAParams.getBigInteger("n2");
//            BigInteger n3 = typeAParams.getBigInteger("n3");
//            n0 = n0.multiply(n1);
//            n1 = n2.multiply(n3);
//
//            String param = "type a1\n" +
//                    "p " + typeAParams.getString("p") + "\n" +
//                    "n " + typeAParams.getString("n") + "\n" +
//                    "n0 " + n0 + "\n"+
//                    "n1 " + n1 + "\n"+
//                    "l " + typeAParams.getString("l") + "\n";

            try {
                file.createNewFile();
                FileWriter writer = new FileWriter(curvePath);
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

    private ElementPowPreProcessing getGsGenerator(Element generator) {
        PairingParameters type1Params = PairingFactory.getPairingParameters(curvePath);
        return ElementUtils.getGenerator(bp, generator.duplicate(), type1Params, 0, 2).getImmutable().getElementPowPreProcessing();

    }

    private ElementPowPreProcessing getGtGenerator(Element generator) {
        PairingParameters type1Params = PairingFactory.getPairingParameters(curvePath);
        return ElementUtils.getGenerator(bp, generator.duplicate(), type1Params, 1, 2).getImmutable().getElementPowPreProcessing();
    }

    @Override
    public void Setup(int U) {
        ElementPowPreProcessing phi = G1.newRandomElement().getImmutable().getElementPowPreProcessing();
        ElementPowPreProcessing psi = G1.newRandomElement().getImmutable().getElementPowPreProcessing();
        Element alpha = Zr.newRandomElement().getImmutable();
        Element beta = Zr.newRandomElement().getImmutable();
        Element phi_beta = phi.powZn(beta);
        Element ephipsi_alpha = bp.pairing(phi.pow(new BigInteger("1")), psi.powZn(alpha)).getImmutable();
        Element psi_alpha = psi.powZn(alpha);

        Map<String, Element> IMap = new HashMap<>();
        Map<String, Element> kMap = new HashMap<>();
        Map<String, ElementPowPreProcessing> SMap = new HashMap<>();
        Map<String, ElementPowPreProcessing> TMap = new HashMap<>();
        List<String> attributeSet = null;
        try {
            File file = new File("src/main/resources/attributesets.txt");
            BufferedReader reader = new BufferedReader(
                    new FileReader(file));
            String str;
            attributeSet = new ArrayList<>();
            while ((str = reader.readLine())!=null){
                attributeSet.add(str);
            }
            attributeSet.add("PUB");

            for (String s : attributeSet) {
                Element I = Zr.newRandomElement().getImmutable();
                Element k = Zr.newRandomElement().getImmutable();
                Element h = Zr.newRandomElement().getImmutable();
                ElementPowPreProcessing S = phi.powZn(h).getElementPowPreProcessing();
                ElementPowPreProcessing T = psi.powZn(h).getElementPowPreProcessing();

                IMap.put(s, I);
                kMap.put(s, k);
                SMap.put(s, S);
                TMap.put(s, T);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        pk = new ICNPK(phi, psi, phi_beta, ephipsi_alpha, attributeSet, IMap, kMap, TMap);
        msk = new ICNMSK(beta, psi_alpha, alpha, SMap);
    }

    @Override
    public ASK KeyGen(List<String> attributes) {
        Map<String, Element> IMap = pk.IMap;
        Map<String, Element> kMap = pk.kMap;
        Map<String, ElementPowPreProcessing> TMap = pk.TMap;
        ElementPowPreProcessing phi = pk.phi;
        ElementPowPreProcessing psi = pk.psi;
        Element psi_alpha = msk.psi_alpha;
        Element beta = msk.beta;
        Element alpha = msk.alpha;
        Map<String, ElementPowPreProcessing> SMap = msk.SMap;

        Element r_UID = Zr.newRandomElement().getImmutable();
        Element D_UID = psi.powZn((alpha.add(r_UID)).div(beta));

        attributes.add("PUB");
        Map<String, Element> XMap = new HashMap<>();
        Map<String, Element> YMap = new HashMap<>();
        Map<String, Element> ZMap = new HashMap<>();
        for (String attribute : attributes) {
            Element r = Zr.newRandomElement().getImmutable();
            ElementPowPreProcessing S = SMap.get(attribute);
            Element I = IMap.get(attribute);

            Element X = phi.powZn(r_UID).mul(S.powZn(r));
            Element Y = phi.powZn(r);
            Element Z = bp.pairing(phi.pow(new BigInteger("1")), psi.pow(new BigInteger("1"))).powZn(r_UID.mul(I));

            XMap.put(attribute, X);
            YMap.put(attribute, Y);
            ZMap.put(attribute, Z);
        }

        return new ICNASK(attributes, D_UID, XMap, YMap, ZMap);
    }

    @Override
    public Cipher Enc(byte[] message, AccessStructure accessStructure) {
        if (!(accessStructure instanceof ICNClause))
            return null;
        ICNClause ICNClause = (ICNClause) accessStructure;

        ElementPowPreProcessing phi = pk.phi;
        ElementPowPreProcessing psi = pk.psi;
        Element ephipsi_alpha = pk.ephipsi_alpha;
        Element phi_beta = pk.phi_beta;
        Map<String, Element> IMap = pk.IMap;
        Map<String, Element> kMap = pk.kMap;
        Map<String, ElementPowPreProcessing> TMap = pk.TMap;

        List<String> attributeList = ICNClause.clauses;

        Element s = Zr.newRandomElement().getImmutable();
        Element C = ephipsi_alpha.powZn(s);
        Element C_ = phi_beta.powZn(s);
        List<Element> Ilist = new ArrayList<>();
        Ilist.add(s);

        Map<String, Element> C1Map = new HashMap<>();
        Map<String, Element> C2Map = new HashMap<>();
        Map<String, Element> C3Map = new HashMap<>();
        for (int i = 0; i < attributeList.size(); i++) {
            String attribute = attributeList.get(i);
            Element I = IMap.get(attribute);
            Element I_minus_1 = Ilist.get(Ilist.size() - 1);
            Ilist.add(I);
            Element k = kMap.get(attribute);
            Element l = Zr.newRandomElement().getImmutable();
            ElementPowPreProcessing T = TMap.get(attribute);

            Element element = (I_minus_1.sub(I)).mul(l);
            Element C1 = psi.powZn(element);
            Element C2 = T.powZn(element);
            Element C3 = (k.mul(l)).invert();

            C1Map.put(attribute, C1);
            C2Map.put(attribute, C2);
            C3Map.put(attribute, C3);
        }

        return new ICNCipher(C, C_, C1Map, C2Map, C3Map);
    }

    @Override
    public void Dec(Cipher cipher, AccessStructure accessStructure, ASK ask) {
        if (!(cipher instanceof ICNCipher) || !(accessStructure instanceof ICNClause) || !(ask instanceof ICNASK))
            return;

        ICNCipher icnCipher = (ICNCipher) cipher;
        ICNASK icnask = (ICNASK) ask;

        Map<String, Element> C1Map = icnCipher.C1Map;
        Map<String, Element> C2Map = icnCipher.C2Map;
        Map<String, Element> C3Map = icnCipher.C3Map;
        Map<String, Element> XMap = icnask.XMap;
        Map<String, Element> YMap = icnask.YMap;
        Map<String, Element> ZMap = icnask.ZMap;
        Map<String, Element> kMap = pk.kMap;
        Map<String, String> ZMap_inv = icnask.invZmap;

        String curAttribute = "PUB";
        Element curPair = null;
        while (true) {
            Element X = XMap.get(curAttribute);
            Element Y = YMap.get(curAttribute);
            Element Z = ZMap.get(curAttribute);
            Element C1 = C1Map.get(curAttribute);
            Element C2 = C2Map.get(curAttribute);
            Element C3 = C3Map.get(curAttribute);
            Element k = kMap.get(curAttribute);

            Element element1 = bp.pairing(X, C1.powZn(k.mul(C3))).getImmutable();
            Element element2 = bp.pairing(Y, C2.powZn(k.mul(C3))).getImmutable();
            curPair = Z.mul(element1).div(element2).getImmutable();
            if (!ZMap_inv.containsKey(curPair.toString()))
                break;
            curAttribute = ZMap_inv.get(curPair.toString());
        }

        Element C = icnCipher.C;
        Element C_ = icnCipher.C_;
        Element D_UID = icnask.D_UID;
        Element res = C.div(bp.pairing(C_, D_UID).div(curPair).getImmutable());
    }

    @Test
    public void a(){
        TypeA1CurveGenerator pg = new TypeA1CurveGenerator(4, 32);
        PairingParameters typeAParams = pg.generate();
        System.out.println(typeAParams);
    }

    public static void main(String[] args) throws Exception{
        ICN icn = new ICN(32);
        icn.Setup(2);
//        File file = new File("src/main/resources/curves/test.properties");
//        FileWriter writer = new FileWriter(file);
//        writer.write("1234");
//        writer.flush();
        Map<String, Integer> map = new HashMap<>();
        Element r = icn.Zr.newRandomElement().getImmutable();
        Element r_ = r.duplicate();
        Element g = icn.G1.newRandomElement().getImmutable();
        Element g_r = g.powZn(r);
        Element g__r = g.powZn(r_);
        System.out.println(g__r.isEqual(g_r));
        map.put(g_r.toString(), 123);
        System.out.println(map.get(g__r.toString()));
    }
}