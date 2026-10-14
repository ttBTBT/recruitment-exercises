package openatom.basics;

import java.util.Scanner;

public class Toolbox {
    public static String gradeIf(int score) {
        checkScore(score);
        if (score >= 90) return "A";
        else if (score >= 80) return "B";
        else if (score >= 70) return "C";
        else if (score >= 60) return "D";
        else return "E";
    }

    public static String gradeSwitch(int score) {
        checkScore(score);
        switch (score / 10) {
            case 10: case 9: return "A";
            case 8: return "B";
            case 7: return "C";
            case 6: return "D";
            default: return "E";
        }
    }

    private static void checkScore(int score) {
        if (score < 0 || score > 100) throw new IllegalArgumentException("分数必须在0到100之间");
    }

    public static String pyramid(int levels) {
        if (levels < 1 || levels > 100) throw new IllegalArgumentException("层数必须在1到100之间");
        StringBuilder result = new StringBuilder();
        for (int row = 1; row <= levels; row++) {
            for (int space = 0; space < levels - row; space++) result.append(' ');
            for (int col = 1; col <= 2 * row - 1; col++) {
                result.append(row == 1 || row == levels || col == 1 || col == 2 * row - 1 ? '*' : ' ');
            }
            result.append(System.lineSeparator());
        }
        return result.toString();
    }

    public int calculateFactorial(int n) {
        // 题目指定 int 返回值，13! 已溢出，因此明确限制而不是返回错误结果。
        if (n < 0 || n > 12) throw new IllegalArgumentException("int阶乘只支持0到12");
        return n <= 1 ? 1 : n * calculateFactorial(n - 1);
    }

    public static void main(String[] args) {
        Toolbox toolbox = new Toolbox();
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("【欢迎使用多功能工具箱】");
            while (true) {
                System.out.println("1.成绩评定 2.空心金字塔 3.递归阶乘 4.退出系统");
                System.out.print("请选择功能：");
                if (!scanner.hasNextLine()) break;
                String selection = scanner.nextLine().trim();
                if (selection.equals("4")) { System.out.println("再见"); break; }
                if (!selection.matches("[123]")) { System.out.println("请输入1到4"); continue; }
                System.out.print(selection.equals("1") ? "请输入分数：" : selection.equals("2") ? "请输入金字塔层数：" : "计算阶乘：");
                if (!scanner.hasNextLine()) break;
                try {
                    int input = Integer.parseInt(scanner.nextLine().trim());
                    switch (selection) {
                        case "1":
                            System.out.println("成绩评定（if-else）：" + gradeIf(input));
                            System.out.println("成绩评定（switch）：" + gradeSwitch(input)); break;
                        case "2": System.out.print(pyramid(input)); break;
                        case "3": System.out.println("计算结果：" + toolbox.calculateFactorial(input)); break;
                        default: break;
                    }
                } catch (IllegalArgumentException exception) {
                    System.out.println("输入错误：" + exception.getMessage());
                }
            }
        }
    }
}
