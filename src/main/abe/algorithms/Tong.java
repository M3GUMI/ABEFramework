package algorithms;

import accessStructure.AccessStructure;
import accessStructure.tree.AccessTree;
import accessStructure.tree.node.PolicyAttribute;
import accessStructure.tree.node.TreeNode;
import it.unisa.dia.gas.jpbc.*;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.a1.TypeA1CurveGenerator;
import it.unisa.dia.gas.plaf.jpbc.util.ElementUtils;
import utils.Util;
import utils.containers.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Tong implements ABEAlgorithm{
    public Field G1;
    public Field Zr;
    public Field GT;
    public Pairing bp;
    public Element g;
    public TongPK pk;
    public TongMSK msk;
    private final String curvePath = "src/main/resources/curves/tong.properties";
    public PolicyType policyType = PolicyType.Boolean;

    class TongPK extends PK {
        int d;
        Element g_p;
        Element g_r;
        Element lambda;
        Element mu;
        ElementPowPreProcessing phi;
        ElementPowPreProcessing phi_bar;
        Element h;
        Element J;
        Element epsilon;
        Element eta;
        List<Element> H1List;
        List<Element> H2List;
        int Z = 4000;

        public TongPK(int d,
                      Element g_p,
                      Element g_r,
                      Element lambda,
                      Element mu,
                      ElementPowPreProcessing phi,
                      ElementPowPreProcessing phi_bar,
                      Element h,
                      Element J,
                      Element epsilon,
                      Element eta,
                      List<Element> h1List,
                      List<Element> h2List) {
            this.d = d;
            this.g_p = g_p;
            this.g_r = g_r;
            this.lambda = lambda;
            this.mu = mu;
            this.phi = phi;
            this.phi_bar = phi_bar;
            this.h = h;
            this.J = J;
            this.epsilon = epsilon;
            this.eta = eta;
            this.H1List = h1List;
            this.H2List = h2List;
        }
    }

    class TongMSK extends MSK {
        Element g_q;
        Element alpha;
        Element beta;
        List<Element> h1List;
        List<Element> h2List;

        public TongMSK(Element g_q,
                       Element alpha,
                       Element beta,
                       List<Element> h1List,
                       List<Element> h2List){
            this.g_q = g_q;
            this.alpha = alpha;
            this.beta = beta;
            this.h1List = h1List;
            this.h2List = h2List;
        }
    }

    class TongCipher extends Cipher {
        Element C;
        Element C1;
        Map<String, Container> CMap;

        public TongCipher(Element C,
                          Element C1,
                          Map<String, Container> CMap) {
            this.C = C;
            this.C1 = C1;
            this.CMap = CMap;
        }
    }

    class TongASK extends ASK {
        Element K;
        Map<String, Container> KList;
        Element K_bar_1;
        Element K_bar_2;
        Element K_bar_3;
        Element K_bar_4;
        Element K_bar;
        List<Element> K1_barList;
        List<Element> K2_barList;
        List<String> attributeNames;
        Map<String, Triad> attributes;

        public TongASK(Element K,
                       Map<String, Container> KList,
                       Element k_bar_1,
                       Element k_bar_2,
                       Element k_bar_3,
                       Element k_bar_4,
                       Element k_bar,
                       List<Element> k1_barList,
                       List<Element> k2_barList,
                       List<String> attributeNames,
                       Map<String, Triad> attributes) {
            this.K = K;
            this.KList = KList;
            this.K_bar_1 = k_bar_1;
            this.K_bar_2 = k_bar_2;
            this.K_bar_3 = k_bar_3;
            this.K_bar_4 = k_bar_4;
            this.K_bar = k_bar;
            this.K1_barList = k1_barList;
            this.K2_barList = k2_barList;
            this.attributeNames = attributeNames;
            this.attributes = attributes;
        }
    }

    public Tong() {
        File file = new File(curvePath);
        if (!file.exists()) {
            TypeA1CurveGenerator pg = new TypeA1CurveGenerator(3, 256);
            PairingParameters typeAParams = pg.generate();

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
        this.g = G1.newRandomElement().getImmutable();
    }

    public Tong(int size) {
        File file = new File(curvePath);
        if (!file.exists()) {
            TypeA1CurveGenerator pg = new TypeA1CurveGenerator(3, size);
            PairingParameters typeAParams = pg.generate();

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
        this.g = G1.newRandomElement().getImmutable();
    }

    @Override
    public void Setup(int d) {
        Element alpha = Zr.newRandomElement().getImmutable();
        Element beta = Zr.newRandomElement().getImmutable();
        Element lambda = Zr.newRandomElement().getImmutable();
        Element mu = Zr.newRandomElement().getImmutable();
        Element theta = Zr.newRandomElement().getImmutable();

        Element g_p = getGpGenerator(g);
        Element g_q = getGqGenerator(g);
        Element g_r = getGrGenerator(g);
        Element R_0 = getGrGenerator(g);
        ElementPowPreProcessing phi = G1.newRandomElement().getImmutable().getElementPowPreProcessing();
        ElementPowPreProcessing phi_bar = G1.newRandomElement().getImmutable().getElementPowPreProcessing();

        Element h = g_p.powZn(theta).mul(g_r.powZn(theta));
        Element J = g_q.mul(R_0);
        Element epsilon = bp.pairing(g_p, h).powZn(alpha).getImmutable();
        Element eta = g_p.powZn(beta);

        List<Element> h1List = new ArrayList<>();
        List<Element> h2List = new ArrayList<>();
        List<Element> H1List = new ArrayList<>();
        List<Element> H2List = new ArrayList<>();
        for (int i = 0; i < d; i++) {
            Element h1 = getGpGenerator(g);
            Element h2 = getGpGenerator(g);
            Element R1 = getGpGenerator(g);
            Element R2 = getGpGenerator(g);

            Element H1 = h1.mul(R1);
            Element H2 = h2.mul(R2);

            h1List.add(h1);
            h2List.add(h2);
            H1List.add(H1);
            H2List.add(H2);
        }

        this.pk = new TongPK(d, g_p, g_r, lambda, mu, phi, phi_bar, h, J, epsilon, eta, H1List, H2List);
        this.msk = new TongMSK(g_q, alpha, beta, h1List, h2List);
    }

    private Element getGpGenerator(Element g){
        if (!g.isImmutable())
            g = g.getImmutable();
        PairingParameters type1Params = PairingFactory.getPairingParameters(curvePath);
        Element element = ElementUtils.getGenerator(bp, g.duplicate(), type1Params, 0, 3).getImmutable();
        return element;
    }

    private Element getGqGenerator(Element g){
        if (!g.isImmutable())
            g = g.getImmutable();
        PairingParameters type1Params = PairingFactory.getPairingParameters(curvePath);
        Element element = ElementUtils.getGenerator(bp, g.duplicate(), type1Params, 1, 3).getImmutable();
        return element;
    }

    private Element getGrGenerator(Element g){
        if (!g.isImmutable())
            g = g.getImmutable();
        PairingParameters type1Params = PairingFactory.getPairingParameters(curvePath);
        Element element = ElementUtils.getGenerator(bp, g.duplicate(), type1Params, 2, 3).getImmutable();
        return element;
    }

    @Override
    public ASK KeyGen(List<String> attributes) {
        Element a = Zr.newRandomElement();
        Element alpha = msk.alpha;
        Element beta = msk.beta;
        List<Element> h1List = msk.h1List;
        List<Element> h2List = msk.h2List;
        Element g_q = msk.g_q;

        Element h = pk.h;
        Element g_p = pk.g_p;
        Element lambda = pk.lambda;
        Element mu = pk.mu;
        ElementPowPreProcessing phi = pk.phi;
        ElementPowPreProcessing phi_bar = pk.phi_bar;
        int d = pk.d;
        Element K = h.powZn((alpha.sub(a)).div(beta));

        Map<String, Container> KMap = new HashMap<>();
        Map<String, Triad> attributeMap = new HashMap<>();
        for (String attribute : attributes) {
            Triad<String, Integer, Integer> triad = parseAttribute(attribute);
            if (triad == null)
                return null;
            String attributeName = triad.first;
            Element a_j = Zr.newRandomElement();
            Element H_att = Util.getElementByFingerprint(curvePath, attributeName, true);
            Element K1 = g_p.powZn(a).mul(H_att.powZn(a_j));
            Element K2 = h.powZn(a_j);
            if (triad.second != null) {
                int t1 = triad.second;
                int t2 = triad.third;

                Element K3 = getV_t1(t1).powZn(a_j);
                Element K4 = getV_bar_t2(t2).powZn(a_j);
                Quaternion<Element, Element, Element, Element> quaternion = new Quaternion<>(K1, K2, K3, K4);
                KMap.put(attributeName, quaternion);
            } else {
                Tuple<Element, Element> tuple = new Tuple<>(K1, K2);
                KMap.put(attributeName, tuple);
            }
            attributeMap.put(attribute, triad);
        }

        Element K1_bar = G1.newOneElement().getImmutable();
        Element K2_bar = G1.newOneElement().getImmutable();
        List<Element> Kl1_bar_list = new ArrayList<>();
        List<Element> Kl2_bar_list = new ArrayList<>();
        for (int i = 0; i < d; i++) {
            Element r1 = Zr.newRandomElement().getImmutable();
            Element r2 = Zr.newRandomElement().getImmutable();
            Element h1 = h1List.get(i);
            Element h2 = h2List.get(i);
            K1_bar = K1_bar.mul(h1.powZn(r1.mul(-1)).mul(h2.powZn(r2.mul(-1))));
            K2_bar = K2_bar.mul(h1.powZn(r1.mul(-1)).mul(h2.powZn(r2.mul(-1))));

            Element Kl1_bar = g_p.powZn(r1);
            Element Kl2_bar = g_p.powZn(r2);
            Kl1_bar_list.add(Kl1_bar);
            Kl2_bar_list.add(Kl2_bar);
        }
        Element tau_1 = Zr.newRandomElement().getImmutable();
        Element tau_2 = Zr.newRandomElement().getImmutable();
        Element K3_bar = g_q.powZn(tau_1);
        Element K4_bar = g_q.powZn(tau_2);
        Element R_1 = getGrGenerator(g);
        Element K_bar = R_1.mul(h.powZn(a));
        return new TongASK(K, KMap, K1_bar, K2_bar, K3_bar, K4_bar, K_bar, Kl1_bar_list, Kl2_bar_list, attributes, attributeMap);
    }

    @Override
    public AccessStructure generateAccessStructure(String policy) {
        return AccessTree.buildFromPolicy(policy);
    }

    @Override
    public PolicyType getPolicyType() {
        return policyType;
    }

    private Triad<String, Integer, Integer> parseAttribute(String attribute) {
        if (attribute.matches("[a-z,A-Z,0-9]*:[0-9]*-[0-9]*")) {
            int index1 = attribute.indexOf(":");
            int index2 = attribute.indexOf("-");
            String attributeName = attribute.substring(0, index1);
            String attributeLowerLimit = attribute.substring(index1 + 1, index2);
            String attributeUpperLimit = attribute.substring(index2 + 1);

            Integer t1 = Integer.parseInt(attributeLowerLimit);
            Integer t2 = Integer.parseInt(attributeUpperLimit);

            return new Triad<String, Integer, Integer>(attributeName, t1, t2);

        } else if (attribute.matches("[a-z,A-Z,0-9]*")) {
            String attributeName = attribute;
            return new Triad<String, Integer, Integer>(attributeName, null, null);
        }
        return null;
    }

    @Override
    public Cipher Enc(byte[] message, AccessStructure accessStructure) {
        if (!(accessStructure instanceof AccessTree))
            return null;
        AccessTree tree = (AccessTree) accessStructure;
        TreeNode root = tree.policyTree;
        Element s = Zr.newRandomElement().getImmutable();
        Map<String, Container> CMap = new HashMap<>();
        getCMap(CMap, root, s);
        Element epsilon = pk.epsilon;
        Element eta = pk.eta;
        Element C = epsilon.powZn(s);
        Element C1 = eta.powZn(s);

        return new TongCipher(C, C1, CMap);
    }

    public void getCMap(Map<String, Container> CMap, TreeNode current, Element secret) {
        if (current == null)
            return;

        if (current.getName() == "and") {
            Element sRight = Zr.newRandomElement().getImmutable();
            Element sLeft = secret.sub(sRight);
            getCMap(CMap, current.left, sLeft);
            getCMap(CMap, current.right, sRight);
        } else if (current.getName() == "or") {
            getCMap(CMap, current.left, secret);
            getCMap(CMap, current.right, secret);
        } else {
            Element h = pk.h;
            ElementPowPreProcessing phi = pk.phi;
            ElementPowPreProcessing phi_bar = pk.phi_bar;
            PolicyAttribute policyAttribute = (PolicyAttribute) current;
            String attributeName = current.getName();
            Element H = Util.getElementByFingerprint(curvePath, attributeName, true);
            if (policyAttribute.hasSpan) {
                Element y_ = Zr.newRandomElement().getImmutable();
                Element y__ = secret.sub(y_);
                int t1 = policyAttribute.ti;
                int t2 = policyAttribute.tj;

                Element C1 = h.powZn(y_);
                Element C2 = H.powZn(y_);
                Element C3 = getV_t1(t1).powZn(y_);
                Element C4 = h.powZn(y__);
                Element C5 = H.powZn(y__);
                Element C6 = getV_bar_t2(t2).powZn(y__);
                CMap.put(attributeName, new Hexadecimal<>(C1, C2, C3, C4, C5, C6));
            } else {
                Element C1 = h.powZn(secret);
                Element C2 = H.powZn(secret);
                CMap.put(attributeName, new Tuple<>(C1, C2));
            }
        }
    }

    private Element getV_t1(int t1) {
        ElementPowPreProcessing phi = pk.phi;
        Element lambda = pk.lambda;
        Element pow = lambda.powZn(Zr.newElement(t1));
        return phi.powZn(pow);
    }

    private Element getV_bar_t2(int t2) {
        ElementPowPreProcessing phi_bar = pk.phi_bar;
        int Z = pk.Z;
        Element mu = pk.mu;
        Element pow = mu.powZn(Zr.newElement(Z-t2));
        return phi_bar.powZn(pow);
    }

    @Override
    public void Dec(Cipher cipher, AccessStructure accessStructure, ASK ask) {
        if(!(cipher instanceof TongCipher && accessStructure instanceof AccessTree && ask instanceof TongASK))
            return;
        TongCipher tongCipher = (TongCipher) cipher;
        AccessTree accessTree = (AccessTree) accessStructure;
        TongASK tongASK = (TongASK) ask;
        List<String> attributeNames = tongASK.attributeNames;
        Map<String, Triad> attributes = tongASK.attributes;
        TreeNode root = accessTree.policyTree;
        Map<String, Container> CMap = tongCipher.CMap;
        Element F_root = computeRoot(tongCipher, root, tongASK);
        Element e1 = G1.newRandomElement().getImmutable();
        Element e2 = G1.newRandomElement().getImmutable();
        Element e3 = Zr.newRandomElement().getImmutable();
        bp.pairing(e1, e1).mul(F_root).powZn(e3.invert()).mul(tongCipher.C);
    }

    public Element computeRoot(TongCipher tongCipher, TreeNode current, TongASK tongASK) {
        if (current == null)
            return null;

        List<String> attributeNames = tongASK.attributeNames;
        Map<String, Triad> attributes = tongASK.attributes;
        Map<String, Container> CMap = tongCipher.CMap;
        Element g_p = pk.g_p;
        Element h = pk.h;

        if (current.getName() == "or") {
            Element Fx;
            if ((Fx = computeRoot(tongCipher, current.left, tongASK)) != null)
                return Fx;
            return computeRoot(tongCipher, current.right, tongASK);
        } else if (current.getName() == "and") {
            Element Fx1 = computeRoot(tongCipher, current.left, tongASK);
            Element Fx2 = computeRoot(tongCipher, current.right, tongASK);
            return Fx1.mul(Fx2);
        } else {
            PolicyAttribute leafNode = (PolicyAttribute)current;
            String attributeName = leafNode.attributeName;
            if (attributes.containsKey(attributeName))
                return null;
//            if (leafNode.hasSpan) {
//                Triad<String, Integer, Integer> userAttribute = attributes.get(attributeName);
//                int ta = userAttribute.second;
//                int tb = userAttribute.third;
//                int t1 = leafNode.ti;
//                int t2 = leafNode.tj;
//                if (ta < t1 || tb < t2)
//                    return null;
//
//            }
            Element zr1 = Zr.newRandomElement().getImmutable();
            Element zr2 = Zr.newRandomElement().getImmutable();
            Element v1 = getV_t1(30).powZn(zr1.mul(zr2));
            Element v2 = getV_bar_t2(30).powZn(zr1.mul(zr2));
            Element H = Util.getElementByFingerprint(curvePath, attributeName, true);
            Element h1 = G1.newRandomElement();
            g_p.powZn(zr1.mul(zr2)).mul(H.powZn(zr1.mul(zr2)));
            h.mul(v1).powZn(zr1.mul(zr2));
            h.mul(v2).powZn(zr1.mul(zr2));
            Element a = bp.pairing(H, h1);
            Element b = bp.pairing(H, h1);
            bp.pairing(H, h1);
            bp.pairing(H, h1);
            return a.mul(b);
        }
    }
}
