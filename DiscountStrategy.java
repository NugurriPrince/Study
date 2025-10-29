// 파일 이름: DiscountStrategy.java
public interface DiscountStrategy {
    double applyDiscount(double originalFee);
    String getStrategyName();
}