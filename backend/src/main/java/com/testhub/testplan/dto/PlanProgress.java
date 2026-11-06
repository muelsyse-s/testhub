package com.testhub.testplan.dto;

import lombok.Data;

/** 计划执行进度统计。 */
@Data
public class PlanProgress {

    private long total;
    private long executed;
    private long pass;
    private long fail;
    private long block;
    private long skip;
    private long notRun;

    public double getPassRate() {
        long effective = pass + fail;
        return effective == 0 ? 0d : Math.round(pass * 10000.0 / effective) / 100.0;
    }

    public double getProgress() {
        return total == 0 ? 0d : Math.round(executed * 10000.0 / total) / 100.0;
    }
}
