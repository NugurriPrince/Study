// 파일 이름: NoDiscountStrategy.java
public class NoDiscountStrategy implements DiscountStrategy {
    @Override public double applyDiscount(double originalFee) { return originalFee; }
    @Override public String getStrategyName() { return "일반 (할인 없음)"; }
}