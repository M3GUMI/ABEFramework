package access_structure.matrix;

import java.util.ArrayList;
import java.util.List;

public class MatrixComputer {
    /**
     * 获取增广矩阵的解
     * @param matrix LSSS矩阵
     * @param resultList 返回的解（唯一解或者无穷解的其中一个）
     * @return 是否有接
     */
    public static boolean getResult(float[][] matrix, List<Integer> resultList) {
        int count = 0; //非零行的数量
        List<Integer> nonZeroRowIndex = new ArrayList<Integer>(); //记录非零行的位置

        //首先计算一次高斯消元
        boolean hasResult = computeMatrix(matrix, 0);
        if (!hasResult) return false;

        int[] result = new int[matrix[0].length-1];
        //记录非零行的位置和数量
        for (int i = 0; i < matrix.length; i++) {
            if (nonZeroIndex(matrix[i], 0, matrix[0].length - 1) != -1) {
                nonZeroRowIndex.add(i);
                count++;
            }
        }
        //找到解
        for (int i : nonZeroRowIndex) {
            result[nonZeroIndex(matrix[i], 0, count)] = (int)(matrix[i][matrix[0].length - 1] / matrix[i][nonZeroIndex(matrix[i], 0, count)]);
        }
        for(int i=0; i<result.length; i++){
            resultList.add(result[i]);
        }
        return true;
    }


    /**
     * @param list  一维数组
     * @param start 扫描最开始的位置
     * @param end   扫描结束的位置
     * @return 返回非零首元的位置，如果没有非零首元则返回-1
     */
    private static int nonZeroIndex(float[] list, int start, int end) {
        int index = -1;
        for (int i = start; i < end; i++) {
            if (list[i] > 0.000001 || list[i] < -0.000001) {
                index = i;
                break;
            }
        }
        return index;
    }

    /**
     * @param matrix 矩阵
     * @param start  开始计算的列的位置
     * @return 返回是否有解
     */
    private static boolean computeMatrix(float[][] matrix, int start) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < i; j++) {
                int index = nonZeroIndex(matrix[j], start, matrix[0].length - 1);
                if (index != -1) {
                    float times = matrix[i][index] / matrix[j][index];
                    for (int k = start; k < matrix[0].length; k++) {
                        matrix[i][k] = matrix[i][k] - matrix[j][k] * times;
                    }
                    //如果不符合策略
                    if (nonZeroIndex(matrix[i], 0, matrix[0].length) == matrix[0].length - 1) {
                        for (float v : matrix[i]) {
                            System.out.print(v+" ");
                        }
                        System.out.println("属性不符合策略");
                        return false;
                    }
                }
            }
        }

        for (int i = matrix.length - 1; i >= 0; i--) {
            for (int j = matrix.length - 1; j > i; j--) {
                int index = nonZeroIndex(matrix[j], start, matrix[0].length - 1);
                if (index != -1) {
                    float times = matrix[i][index] / matrix[j][index];
                    for (int k = start; k < matrix[0].length; k++) {
                        matrix[i][k] = matrix[i][k] - matrix[j][k] * times;
                    }
                    //如果不符合策略
                    if (nonZeroIndex(matrix[i], 0, matrix[0].length) == matrix[0].length - 1) {
                        System.out.println("属性不符合策略");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * 方便调试罢了
     * @param matrix 矩阵
     */
    private static void printMatrix(float[][] matrix) {
        for (float[] floats : matrix) {
            for (int j = 0; j < matrix[0].length; j++) {
                System.out.print(floats[j] + "   ");
            }
            System.out.println();
        }
    }
}
