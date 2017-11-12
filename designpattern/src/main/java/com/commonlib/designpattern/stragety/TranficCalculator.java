package com.commonlib.designpattern.stragety;

/**
 * Created by zhangweilong on 2017/11/12.
 */

public class TranficCalculator {

    private CalculateStrategy calculateStrategy;

    public TranficCalculator() {

    }

    public void setCalculateStrategy(CalculateStrategy calculateStrategy) {
        this.calculateStrategy = calculateStrategy;
    }


    public void calculatePrice(int km) {
        if (calculateStrategy != null) {
            calculateStrategy.calculatePrice(km);
        }
    }

}
