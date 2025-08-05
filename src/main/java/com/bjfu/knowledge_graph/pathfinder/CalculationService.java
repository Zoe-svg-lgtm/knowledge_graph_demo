package com.bjfu.knowledge_graph.pathfinder;

import com.bjfu.knowledge_graph.bean.nodes.layer1.Formula;
import com.bjfu.knowledge_graph.bean.nodes.layer1.PhysicalQuantity;
import com.bjfu.knowledge_graph.bean.solver.CalculationResult;
import com.bjfu.knowledge_graph.bean.solver.KnownVariable;
import com.bjfu.knowledge_graph.bean.solver.SolvingStep;
import com.bjfu.knowledge_graph.bean.solver.steps.FindFormulaStep;
import com.bjfu.knowledge_graph.repository.FormulaRepository;
import com.bjfu.knowledge_graph.repository.PhysicalQuantityRepository;
import com.bjfu.knowledge_graph.service.PhysicsSymbolMappingService;
import lombok.extern.slf4j.Slf4j;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.interfaces.IExpr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class CalculationService {

    @Autowired
    private FormulaRepository formulaRepository;
    @Autowired
    private PhysicalQuantityRepository quantityRepository;

    @Autowired
    private PhysicsSymbolMappingService symbolMappingService;
    // Symja表达式求值器 - 线程安全
    private final ExprEvaluator evaluator = new ExprEvaluator();

    /**
     * 根据求解路径和初始已知量，执行数值计算。
     * @param path 求解路径
     * @param initialKnowns 初始已知量
     * @return 最终计算结果
     */
    public CalculationResult executeCalculation(List<SolvingStep> path, List<KnownVariable> initialKnowns) {
        // 存储所有已知和新计算出的物理量的值，Key是物理量ID，Value是数值
        Map<String, Double> calculatedValues = new HashMap<>();

        // 1. 初始化已知量
        for (KnownVariable known : initialKnowns) {
            // 需要从物理量名称映射到ID
            String id = quantityRepository.findQuantityNodeIdByName(known.getName());
            calculatedValues.put(id, known.getValue());
        }

        CalculationResult lastResult = null;

        // 2. 顺序遍历路径进行计算
        for (SolvingStep step : path) {
            if (step instanceof FindFormulaStep) {
                FindFormulaStep findStep = (FindFormulaStep) step;

                // 获取当前步骤要使用的公式实体
                Formula formula = formulaRepository.findById(Long.valueOf(findStep.getFormulaNodeId()))
                        .orElseThrow(() -> new IllegalStateException("Formula not found: " + findStep.getFormulaNodeId()));

                // 获取要求解的目标物理量实体
                String targetId = findStep.getOutputNodeId();
                PhysicalQuantity targetQuantity = quantityRepository.findById(Long.valueOf(targetId))
                        .orElseThrow(() -> new IllegalStateException("Quantity not found: " + targetId));

                // 进行单步计算
                double resultValue = calculateSingleStep(formula, targetQuantity, calculatedValues);

                // 更新值集合
                calculatedValues.put(targetId, resultValue);

                // 记录本次计算结果，循环结束后最后一个就是最终结果
                lastResult = new CalculationResult(targetQuantity.getName(), resultValue, targetQuantity.getUnit());

                // 计算结果添加到SolvingStep的描述中，丰富返回给前端的信息
                step.setDescription(step.getDescription() + String.format(" -> Calculated %s = %.2f %s",
                    targetQuantity.getName(), resultValue, targetQuantity.getUnit()));
            }
        }

        if (lastResult == null) {
            throw new IllegalStateException("Calculation path was empty or invalid, no result produced.");
        }

        return lastResult;
    }

    /**
     * 执行单步计算
     * @param formula 使用的公式
     * @param targetQuantity 要求解的物理量
     * @param knownValues 当前所有已知的值
     * @return 计算出的新值
     */
    private double calculateSingleStep(Formula formula, PhysicalQuantity targetQuantity, Map<String, Double> knownValues) {
        log.info("=== 开始单步计算 ===");
        log.info("公式: {}", formula.getExpression());
        log.info("目标变量: {} ({})", targetQuantity.getName(), targetQuantity.getSymbol());
        log.info("当前已知值: {}", knownValues);

        // 1. 获取公式的数学表达式
        String expressionStr = formula.getExpression();

        // 2. 标准化物理符号
        String normalizedExpression = symbolMappingService.normalizePhysicsFormula(expressionStr);
        log.info("标准化表达式: {}", normalizedExpression);

        // 3. 处理公式变形
        String targetSymbol = symbolMappingService.normalizePhysicsFormula(targetQuantity.getSymbol());
        String solvableExpression = adaptFormulaForTarget(normalizedExpression, targetSymbol);
        log.info("求解表达式: {} = {}", targetSymbol, solvableExpression);

        // 4. 获取公式中的所有变量符号并建立映射
        Set<String> variableSymbols = new HashSet<>();
        Map<String, String> symbolToIdMap = new HashMap<>(); // 符号到ID的映射
        Map<String, String> idToSymbolMap = new HashMap<>(); // ID到符号的映射

        for (PhysicalQuantity pq : formula.getQuantities()) {
            String originalSymbol = pq.getSymbol();
            String standardSymbol = symbolMappingService.normalizePhysicsFormula(originalSymbol);
            String quantityId = String.valueOf(pq.getId());

            variableSymbols.add(standardSymbol);
            symbolToIdMap.put(standardSymbol, quantityId);
            idToSymbolMap.put(quantityId, standardSymbol);

            log.debug("符号映射: {} -> {} (ID: {})", originalSymbol, standardSymbol, quantityId);
        }

        // 5. 构建表达式
        ExpressionBuilder builder = new ExpressionBuilder(solvableExpression)
                .variables(variableSymbols);
        Expression expression = builder.build();


        // 为变量赋值
        log.info("开始为变量赋值:");
        int assignedCount = 0;

        for (String symbol : variableSymbols) {
            String quantityId = symbolToIdMap.get(symbol);

            if (quantityId != null && knownValues.containsKey(quantityId)) {
                double value = knownValues.get(quantityId);
                expression.setVariable(symbol, value);
                assignedCount++;
                log.info("  {} = {} (来自ID: {})", symbol, value, quantityId);
            } else {
                log.warn("  {} = ??? (未找到值，ID: {})", symbol, quantityId);
            }
        }

        // 计算结果
        try {
            double result = expression.evaluate();
            log.info("计算成功: {} = {}", targetSymbol, result);
            return result;
        } catch (Exception e) {
            log.error("表达式求值失败: {}", e.getMessage());
            log.error("表达式: {}", solvableExpression);
            log.error("变量值:");
            for (String var : variableSymbols) {
                try {
                    log.error("  {} =", var);
                } catch (Exception ex) {
                    log.error("  {} = 未赋值", var);
                }
            }
            throw new RuntimeException("计算表达式失败: " + solvableExpression, e);
        }
    }

    /**
     * 使用Symja进行公式变形，求解目标变量
     * @param fullEquation 完整的等式，如 "F = m * a"
     * @param targetSymbol 目标变量符号，如 "a"
     * @return 变形后的表达式，如 "F / m"
     */
    private String adaptFormulaForTarget(String fullEquation, String targetSymbol) {
        try {
            // 1. 预处理公式，标准化格式
            String normalizedEquation = preprocessEquation(fullEquation);

            // 2. 将等式转换为 Symja 的方程格式 (左边 - 右边 == 0)
            String[] parts = normalizedEquation.split("=");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid equation format: " + fullEquation);
            }

            String leftSide = parts[0].trim();
            String rightSide = parts[1].trim();

            // 构造方程：leftSide - rightSide == 0
            String equation = String.format("%s - (%s) == 0", leftSide, rightSide);

            // 3. 使用Symja的Solve函数求解
            String solveCommand = String.format("Solve[%s, %s]", equation, targetSymbol);
            log.info("Solving equation: {}", solveCommand);

            IExpr result = evaluator.eval(solveCommand);
            log.info("Symja raw result: {}", result);

            // 4. 解析Symja的结果
            String solvedExpression = parseSymjaResult(result, targetSymbol);

            // 5. 后处理，转换为exp4j兼容格式
            String finalExpression = postprocessExpression(solvedExpression);

            log.info("Equation '{}' solved for '{}': {}", fullEquation, targetSymbol, finalExpression);
            return finalExpression;

        } catch (Exception e) {
            log.warn("Failed to solve equation '{}' for variable '{}': {}", fullEquation, targetSymbol, e.getMessage());

            // 如果符号计算失败，回退到预定义规则
            return solveWithFallbackRules(fullEquation, targetSymbol);
        }
    }

    /**
     * 预处理公式，转换为Symja可识别的格式
     */
    private String preprocessEquation(String equation) {
        String processed = equation
                .replaceAll("\\s+", "") // 移除所有空格
                .replace("==", "=")     // 标准化等号
                .replace("π", "Pi")     // 转换π为Pi
                .replace("^", "^")      // 幂运算符号
                .replace("sqrt", "Sqrt") // 平方根函数
                .replace("sin", "Sin")   // 三角函数
                .replace("cos", "Cos")
                .replace("tan", "Tan")
                .replace("ln", "Log")    // 自然对数
                .replace("log", "Log10") // 常用对数
                .replace("exp", "Exp");  // 指数函数

        // 添加隐式乘法符号 - 改进版本
        processed = addImplicitMultiplication(processed);

        return processed;
    }

    /**
     * 添加隐式乘法符号 - 改进版本
     * 将 "ma" 转换为 "m*a", "2x" 转换为 "2*x" 等
     */
    private String addImplicitMultiplication(String expression) {
        // 先处理函数调用，避免误处理
        Set<String> functions = Set.of("Sin", "Cos", "Tan", "Log", "Log10", "Exp", "Sqrt", "Pi");

        // 处理连续的字母（如 ma -> m*a），但跳过函数名
        StringBuilder result = new StringBuilder();
        char[] chars = expression.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            result.append(chars[i]);

            // 检查是否需要添加乘法符号
            if (i < chars.length - 1) {
                char current = chars[i];
                char next = chars[i + 1];

                // 数字后跟字母：2a -> 2*a
                if (Character.isDigit(current) && Character.isLetter(next)) {
                    // 检查是否是已知函数的开始
                    if (!startsWithFunction(expression, i + 1, functions)) {
                        result.append("*");
                    }
                }
                // 字母后跟数字：a2 -> a*2
                else if (Character.isLetter(current) && Character.isDigit(next)) {
                    result.append("*");
                }
                // 字母后跟字母：ma -> m*a
                else if (Character.isLetter(current) && Character.isLetter(next)) {
                    // 检查是否是已知函数的开始
                    if (!startsWithFunction(expression, i + 1, functions)) {
                        result.append("*");
                    }
                }
                // 字母/数字后跟左括号：2( -> 2*(
                else if ((Character.isLetterOrDigit(current)) && next == '(') {
                    result.append("*");
                }
                // 右括号后跟字母/数字：)a -> )*a
                else if (current == ')' && Character.isLetterOrDigit(next)) {
                    result.append("*");
                }
            }
        }

        return result.toString();
    }

    /**
     * 检查指定位置是否以某个函数名开始
     */
    private boolean startsWithFunction(String expression, int startIndex, Set<String> functions) {
        for (String func : functions) {
            if (expression.regionMatches(startIndex, func, 0, func.length())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 解析Symja返回的结果 - 改进版本
     */
    private String parseSymjaResult(IExpr result, String targetSymbol) {
        String resultStr = result.toString();
        log.info("Symja raw result: {}", resultStr);

        // 检查是否求解成功
        if (resultStr.equals("{}") || resultStr.isEmpty()) {
            throw new UnsupportedOperationException("No solution found");
        }

        // Symja的Solve函数返回格式通常是 {{variable -> expression}}
        if (resultStr.startsWith("{{") && resultStr.endsWith("}}")) {
            // 移除外层大括号
            String content = resultStr.substring(2, resultStr.length() - 2);
            return extractSolution(content, targetSymbol);
        }

        // 如果是 {variable -> expression} 格式
        if (resultStr.startsWith("{") && resultStr.endsWith("}")) {
            String content = resultStr.substring(1, resultStr.length() - 1);
            return extractSolution(content, targetSymbol);
        }

        // 如果直接是 variable -> expression 格式
        if (resultStr.contains("->")) {
            return extractSolution(resultStr, targetSymbol);
        }

        // 如果Symja返回的是表达式本身（某些简单情况）
        if (!resultStr.contains("Solve") && !resultStr.contains("->")) {
            return resultStr;
        }

        // 如果Symja无法求解，返回原始结果
        throw new UnsupportedOperationException("Cannot parse Symja result: " + resultStr);
    }

    /**
     * 从解答字符串中提取目标变量的解
     */
    private String extractSolution(String content, String targetSymbol) {
        // 处理多个解的情况，用逗号分隔
        String[] solutions = content.split(",");

        for (String solution : solutions) {
            solution = solution.trim();
            if (solution.contains(targetSymbol + "->")) {
                String[] parts = solution.split("->");
                if (parts.length >= 2) {
                    return parts[1].trim();
                }
            }
        }

        throw new UnsupportedOperationException("Target variable " + targetSymbol + " not found in solution");
    }

    /**
     * 后处理表达式，转换为exp4j兼容格式
     */
    private String postprocessExpression(String expression) {
        return expression
                .replace("Pi", "π")           // 转换回π
                .replace("Sqrt[", "sqrt(")    // 转换平方根函数
                .replace("Sin[", "sin(")      // 转换三角函数
                .replace("Cos[", "cos(")
                .replace("Tan[", "tan(")
                .replace("Log[", "log(")      // 转换对数函数
                .replace("Log10[", "log10(")
                .replace("Exp[", "exp(")      // 转换指数函数
                .replaceAll("\\[", "(")       // 转换括号
                .replaceAll("\\]", ")")
                .replace("*", "*")            // 确保乘法符号
                .replaceAll("\\s+", "");      // 移除空格
    }

    /**
     * 回退方案：使用预定义规则 - 改进版本
     */
    private String solveWithFallbackRules(String fullEquation, String targetSymbol) {
        log.info("Using fallback rules for equation: {}", fullEquation);

        String[] parts = fullEquation.split("=");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Unsupported formula format: " + fullEquation);
        }

        String leftPart = parts[0].trim().replaceAll("\\s+", "");
        String rightPart = parts[1].trim().replaceAll("\\s+", "");

        // 如果目标变量已经在左边，直接返回右边
        if (leftPart.equals(targetSymbol)) {
            return rightPart;
        }

        // 如果目标变量在右边，交换左右
        if (rightPart.equals(targetSymbol)) {
            return leftPart;
        }

        // 预定义的常见物理公式变形规则
        return solveWithPredefinedRules(leftPart, rightPart, targetSymbol);
    }

    /**
     * 预定义的公式变形规则 - 扩展版本
     */
    private String solveWithPredefinedRules(String leftExpr, String rightExpr, String targetSymbol) {
        String formulaKey = leftExpr + "=" + rightExpr;

        // 创建规则映射
        Map<String, Map<String, String>> rules = createFormulaRules();

        // 标准化公式格式并匹配
        String normalizedFormula = normalizeFormula(formulaKey);

        // 尝试正向和反向匹配
        for (Map.Entry<String, Map<String, String>> entry : rules.entrySet()) {
            String ruleKey = entry.getKey();
            Map<String, String> targetRules = entry.getValue();

            // 正向匹配
            if (normalizeFormula(ruleKey).equals(normalizedFormula)) {
                if (targetRules.containsKey(targetSymbol)) {
                    return targetRules.get(targetSymbol);
                }
            }

            // 反向匹配（交换等号左右）
            String[] ruleParts = ruleKey.split("=");
            if (ruleParts.length == 2) {
                String reversedRule = ruleParts[1] + "=" + ruleParts[0];
                if (normalizeFormula(reversedRule).equals(normalizedFormula)) {
                    if (targetRules.containsKey(targetSymbol)) {
                        return targetRules.get(targetSymbol);
                    }
                }
            }
        }

        // 尝试简单的代数变形
        return trySimpleAlgebraicTransformation(leftExpr, rightExpr, targetSymbol);
    }

    /**
     * 尝试简单的代数变形
     */
    private String trySimpleAlgebraicTransformation(String leftExpr, String rightExpr, String targetSymbol) {
        // 简单的乘法除法变形
        // 例如：A = B * C，求解 B -> A / C

        // 检查右边是否是乘法表达式
        if (rightExpr.contains("*")) {
            String[] factors = rightExpr.split("\\*");
            if (factors.length == 2) {
                String factor1 = factors[0].trim();
                String factor2 = factors[1].trim();

                if (factor1.equals(targetSymbol)) {
                    return leftExpr + "/" + factor2;
                }
                if (factor2.equals(targetSymbol)) {
                    return leftExpr + "/" + factor1;
                }
            }
        }

        // 检查左边是否是乘法表达式
        if (leftExpr.contains("*")) {
            String[] factors = leftExpr.split("\\*");
            if (factors.length == 2) {
                String factor1 = factors[0].trim();
                String factor2 = factors[1].trim();

                if (factor1.equals(targetSymbol)) {
                    return rightExpr + "/" + factor2;
                }
                if (factor2.equals(targetSymbol)) {
                    return rightExpr + "/" + factor1;
                }
            }
        }

        throw new UnsupportedOperationException(
                String.format("No transformation rule found for solving '%s' in '%s=%s'",
                        targetSymbol, leftExpr, rightExpr));
    }

    /**
     * 创建预定义的公式变形规则 - 扩展版本
     */
    private Map<String, Map<String, String>> createFormulaRules() {
        Map<String, Map<String, String>> rules = new HashMap<>();

        // 牛顿第二定律: F = m * a
        addFormulaRule(rules, "F", "m*a", "F", "m", "a");

        // 欧姆定律: V = I * R
        addFormulaRule(rules, "V", "I*R", "V", "I", "R");

        // 功率公式: P = V * I
        addFormulaRule(rules, "P", "V*I", "P", "V", "I");

        // 动能公式: E = 0.5 * m * v^2
        Map<String, String> kinetic = new HashMap<>();
        kinetic.put("E", "0.5*m*v^2");
        kinetic.put("m", "E/(0.5*v^2)");
        kinetic.put("v", "sqrt(E/(0.5*m))");
        rules.put("E=0.5*m*v^2", kinetic);

        // 圆面积: A = π * r^2
        Map<String, String> circleArea = new HashMap<>();
        circleArea.put("A", "π*r^2");
        circleArea.put("r", "sqrt(A/π)");
        rules.put("A=π*r^2", circleArea);

        // 速度公式: v = s / t
        addFormulaRule(rules, "v", "s/t", "v", "s", "t");

        // 加速度公式: a = v / t
        addFormulaRule(rules, "a", "v/t", "a", "v", "t");

        return rules;
    }

    /**
     * 辅助方法：为简单的三元素公式添加变形规则
     */
    private void addFormulaRule(Map<String, Map<String, String>> rules,
                                String result, String expression,
                                String var1, String var2, String var3) {
        Map<String, String> rule = new HashMap<>();

        // 根据表达式类型创建变形规则
        if (expression.contains("*")) {
            // 乘法形式: A = B * C
            String[] factors = expression.split("\\*");
            if (factors.length == 2) {
                rule.put(var1, expression);
                rule.put(var2, var1 + "/" + var3);
                rule.put(var3, var1 + "/" + var2);
            }
        } else if (expression.contains("/")) {
            // 除法形式: A = B / C
            String[] parts = expression.split("/");
            if (parts.length == 2) {
                rule.put(var1, expression);
                rule.put(var2, var1 + "*" + var3);
                rule.put(var3, var2 + "/" + var1);
            }
        }

        rules.put(result + "=" + expression, rule);
    }

    /**
     * 标准化公式格式
     */
    private String normalizeFormula(String formula) {
        return formula
                .replaceAll("\\s+", "")    // 移除空格
                .replace("*", "*")         // 标准化乘法符号
                .replace("pi", "π")        // 标准化π
                .replace("Pi", "π")        // 标准化π
                .replace("sqrt", "sqrt");  // 标准化函数名
    }
}