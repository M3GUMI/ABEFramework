package access_structure.matrix;


import access_structure.AccessStructure;
import access_structure.tree.AccessTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LSSSMatrix implements AccessStructure {
    private List<List<Integer>> matrix;
    private Map<Integer,String> rho;
    private Map<String, Integer> rho_inv;

    public LSSSMatrix(AccessTree structure){
        this.matrix = structure.getA();
        this.rho = structure.getRho();
        Map<String, Integer> rho_inv = new HashMap<>();
        for (Map.Entry<Integer, String> entry : rho.entrySet()) {
            String attributeName = entry.getValue();
            int row = entry.getKey();
            rho_inv.put(attributeName, row);
        }
        this.rho_inv = rho_inv;
    }

    public String rho(int i){
        return rho.get(i);
    }

    public List<String> returnAttributes(List<String> attributeNames) {
        List<Integer> rowNumList = new ArrayList<>();
        for (String attributeName : attributeNames) {
            if (rho_inv.containsKey(attributeName))
                rowNumList.add(rho_inv.get(attributeName));
        }

        return returnFingerprints(rowNumList);
    }

    /**
     * 得到符合策略的fingerprint集合，如果不符合就返回null
     * @return 符合策略的属性MD5值的集合
     */
    public List<String> returnFingerprints(List<Integer> rowNumList){
        float[][] augmentedMatrix = getAugmentedMatrix(rowNumList);
        List<Integer> result = new ArrayList<>();
        boolean hasResult = MatrixComputer.getResult(augmentedMatrix, result);
        if(!hasResult)
            return null;

        List<String> attributeNames = new ArrayList<>();
        for (int i = 0; i < result.size(); i++) {
            if(result.get(i)!=0){
                int rowNum = rowNumList.get(i);
                attributeNames.add(rho(rowNum));
            }
        }

        return attributeNames;
    }


    /**
     * 得到用于解密的增广矩阵
     * @return 增广矩阵
     */
    private float[][] getAugmentedMatrix(List<Integer> rowNumList){
        float[][] augmentedMatrix = new float[matrix.get(0).size()][rowNumList.size()+1];
        for (int i = 0; i<rowNumList.size(); i++) {
            for(int j=0; j<matrix.get(0).size(); j++){
                augmentedMatrix[j][i] = matrix.get(rowNumList.get(i)).get(j);
            }
        }
        int row  = augmentedMatrix.length;
        augmentedMatrix[0][augmentedMatrix[0].length-1] = 1;
        for (int i = 1; i < row; i++) {
            augmentedMatrix[i][augmentedMatrix[0].length-1] = 0;
        }
        return augmentedMatrix;
    }

    /**
     * 输出矩阵罢了
     */
    public void printMatrix() {
        for (int x = 0; x < matrix.size(); x++) {
            List<Integer> Ax = matrix.get(x);
            System.out.printf("%s: [", rho(x));
            for (Integer aAx : Ax) {
                System.out.print("  "+aAx);
            }
            System.out.print("]");
            System.out.println();
        }
    }

    public List<List<Integer>> getMatrix() {
        return matrix;
    }
}
